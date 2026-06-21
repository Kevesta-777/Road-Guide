package com.example.roadguideapp.goldhunt.achievements.progress

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaSchema
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventCategory
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementCatalog
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntTypeSchema
import com.example.roadguideapp.goldhunt.radar.RadarSchema
import com.example.roadguideapp.goldhunt.radar.RadarType
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategorySchema
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import com.example.roadguideapp.goldhunt.treasure.stats.TreasureRarityStatisticsSchema

internal object AchievementProgressEvaluator {
    fun counterValue(
        definition: AchievementDefinition,
        counters: AchievementProgressCounters,
    ): Int {
        val legacy = counters.legacyCount(definition.key)
        val derived = derivedCounter(definition, counters)
        return maxOf(legacy, derived)
    }

    fun categoriesForEvent(event: AchievementProgressEvent): Set<AchievementCategory> = when (event) {
        is AchievementProgressEvent.LegacyIncrement -> setOfNotNull(
            definitionCategoryForKey(event.achievementKey),
        )
        is AchievementProgressEvent.TreasureCollected ->
            setOf(
                AchievementCategory.TREASURE,
                AchievementCategory.RARITY,
                AchievementCategory.TREASURE_CLUSTER,
                AchievementCategory.MASTER_EXPLORER,
            )
        is AchievementProgressEvent.CellExplored ->
            setOf(AchievementCategory.EXPLORATION)
        is AchievementProgressEvent.SecretPlaceDiscovered ->
            setOf(AchievementCategory.SECRET_PLACE, AchievementCategory.STORY)
        is AchievementProgressEvent.ProfileSynced ->
            setOf(
                AchievementCategory.MASTER_EXPLORER,
                AchievementCategory.EXPLORATION,
                AchievementCategory.TREASURE,
                AchievementCategory.RARITY,
                AchievementCategory.SECRET_PLACE,
                AchievementCategory.TREASURE_CLUSTER,
                AchievementCategory.PANORAMA,
                AchievementCategory.RADAR,
                AchievementCategory.SEASONAL_EVENT,
                AchievementCategory.STORY,
                AchievementCategory.LEGENDARY_RELIC,
            )
        is AchievementProgressEvent.CategoryReconcile -> event.categories
        is AchievementProgressEvent.FullReconcile ->
            AchievementCategory.ALL_ORDERED.toSet()
    }

    private fun definitionCategoryForKey(key: String): AchievementCategory? {
        val categoryId = key.substringAfter("achievement:", "")
            .substringBefore(":")
            .uppercase()
        return AchievementCategory.fromId(categoryId)
            ?: when {
                key.startsWith("seasonal_event") -> AchievementCategory.SEASONAL_EVENT
                key.startsWith("panorama_hunt_type") -> AchievementCategory.PANORAMA
                key.startsWith("secret_place_category") -> AchievementCategory.SECRET_PLACE
                key.startsWith("cluster_encyclopedia") -> AchievementCategory.TREASURE_CLUSTER
                key.startsWith("radar_type") -> AchievementCategory.RADAR
                key.startsWith("treasure_rarity") -> AchievementCategory.RARITY
                else -> null
            }
    }

    private fun derivedCounter(
        definition: AchievementDefinition,
        counters: AchievementProgressCounters,
    ): Int {
        val key = definition.key
        milestoneCount(key)?.let { milestone ->
            return counterForMilestone(definition.category, milestone, counters)
        }
        return when (definition.category) {
            AchievementCategory.EXPLORATION -> counters.exploredCells
            AchievementCategory.TREASURE -> counters.treasuresCollected
            AchievementCategory.RARITY -> rarityCounter(key, counters)
            AchievementCategory.SECRET_PLACE -> secretPlaceCounter(key, counters)
            AchievementCategory.TREASURE_CLUSTER -> clusterCounter(key, counters)
            AchievementCategory.STORY -> counters.storyFragmentsEarned
            AchievementCategory.RADAR -> radarCounter(key, counters)
            AchievementCategory.PANORAMA -> panoramaCounter(key, counters)
            AchievementCategory.SEASONAL_EVENT -> seasonalCounter(key, counters)
            AchievementCategory.LEGENDARY_RELIC -> counters.legendaryRelicsEarned
            AchievementCategory.STREAK -> 0
            AchievementCategory.MASTER_EXPLORER -> masterExplorerCounter(key, counters)
        }
    }

    private fun milestoneCount(key: String): Int? {
        if (!key.startsWith("${AchievementSchema.AchievementKeys.PREFIX}:")) return null
        val suffix = key.substringAfterLast(":count_", "")
        return suffix.toIntOrNull()
    }

    private fun counterForMilestone(
        category: AchievementCategory,
        milestone: Int,
        counters: AchievementProgressCounters,
    ): Int = when (category) {
        AchievementCategory.EXPLORATION -> counters.exploredCells
        AchievementCategory.TREASURE -> counters.treasuresCollected
        AchievementCategory.STORY -> counters.storyFragmentsEarned
        AchievementCategory.LEGENDARY_RELIC -> counters.legendaryRelicsEarned
        else -> 0
    }

    private fun rarityCounter(key: String, counters: AchievementProgressCounters): Int {
        TreasureRarity.ALL_ORDERED.forEach { rarity ->
            val rarityId = rarity.id.lowercase()
            if (key == TreasureRarityStatisticsSchema.AchievementKeys.firstOf(rarityId)) {
                return counters.rarityCount(rarity).coerceAtMost(1)
            }
            val milestone = key.substringAfter("$rarityId:count_", "").toIntOrNull()
            if (milestone != null && key.startsWith("treasure_rarity:$rarityId:count_")) {
                return counters.rarityCount(rarity)
            }
        }
        return 0
    }

    private fun secretPlaceCounter(key: String, counters: AchievementProgressCounters): Int {
        if (key == SecretPlaceCategorySchema.AchievementKeys.completeAllCategories()) {
            return counters.secretPlaceCategoryCompleted.values.count { it > 0 }
        }
        SecretPlaceCategory.ALL_ORDERED.forEach { category ->
            if (key == SecretPlaceCategorySchema.AchievementKeys.firstDiscovery(category)) {
                return counters.secretPlaceFound(category).coerceAtMost(1)
            }
            if (key == SecretPlaceCategorySchema.AchievementKeys.firstVisit(category)) {
                return counters.secretPlaceCompleted(category).coerceAtMost(1)
            }
        }
        return 0
    }

    private fun clusterCounter(key: String, counters: AchievementProgressCounters): Int {
        if (key == ClusterEncyclopediaSchema.AchievementKeys.completeAllFamilies()) {
            return counters.clusterTypeCompleted.values.count { it > 0 }
        }
        ClusterType.ALL_ORDERED.forEach { type ->
            if (key == ClusterEncyclopediaSchema.AchievementKeys.firstDiscovery(type)) {
                return counters.clusterDiscovered(type).coerceAtMost(1)
            }
            if (key == ClusterEncyclopediaSchema.AchievementKeys.firstCompletion(type)) {
                return counters.clusterCompleted(type).coerceAtMost(1)
            }
        }
        return 0
    }

    private fun radarCounter(key: String, counters: AchievementProgressCounters): Int {
        if (key == RadarSchema.AchievementKeys.unlockAll()) {
            return RadarType.ALL_ORDERED.count { counters.radarTotalScans > 0 }
        }
        RadarType.ALL_ORDERED.forEach { type ->
            if (key == RadarSchema.AchievementKeys.firstUnlock(type)) {
                return if (counters.radarTotalScans > 0) 1 else 0
            }
        }
        return counters.radarSuccessfulScans
    }

    private fun panoramaCounter(key: String, counters: AchievementProgressCounters): Int {
        if (key == PanoramaHuntTypeSchema.AchievementKeys.completeAllTypes()) {
            return counters.panoramaTypeCompleted.values.count { it > 0 }
        }
        PanoramaHuntType.ALL_ORDERED.forEach { type ->
            if (key == PanoramaHuntTypeSchema.AchievementKeys.firstCompletion(type)) {
                return counters.panoramaCompleted(type).coerceAtMost(1)
            }
        }
        return counters.panoramaHuntsCompleted
    }

    private fun seasonalCounter(key: String, counters: AchievementProgressCounters): Int {
        SeasonalEventType.ALL_ORDERED.forEach { type ->
            SeasonalEventAchievementCatalog.eventAchievements(type).forEach { definition ->
                if (definition.key == key) {
                    return when {
                        definition.targetCount > 1 -> counters.legacyCount(key)
                        else -> counters.legacyCount(key).coerceAtMost(1)
                    }
                }
            }
        }
        SeasonalEventCategory.ALL_ORDERED.forEach { category ->
            SeasonalEventAchievementCatalog.categoryAchievements(category).forEach { definition ->
                if (definition.key == key) {
                    return counters.legacyCount(key)
                }
            }
        }
        if (key == AchievementSchema.AchievementKeys.collector(AchievementCategory.SEASONAL_EVENT)) {
            return counters.seasonalCompletedCycles
        }
        return counters.seasonalTreasuresCollected
    }

    private fun masterExplorerCounter(key: String, counters: AchievementProgressCounters): Int {
        val levelTarget = key.substringAfter(":level_", "").toIntOrNull()
        if (levelTarget != null) {
            return if (counters.explorerLevel >= levelTarget) levelTarget else counters.explorerLevel
        }
        return if (counters.explorerLevel >= definitionMinLevel(key)) 1 else 0
    }

    private fun definitionMinLevel(key: String): Int =
        when (key) {
            AchievementSchema.AchievementKeys.mastery(AchievementCategory.MASTER_EXPLORER) ->
                AchievementSchema.CategoryGates.minExplorerLevel(AchievementCategory.MASTER_EXPLORER)
            else -> 1
        }
}
