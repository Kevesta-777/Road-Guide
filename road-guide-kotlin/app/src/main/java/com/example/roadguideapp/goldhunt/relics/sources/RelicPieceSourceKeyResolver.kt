package com.example.roadguideapp.goldhunt.relics.sources

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType
import com.example.roadguideapp.goldhunt.relics.RelicSchema

/**
 * Maps gameplay source events to relic piece lookup keys.
 */
internal object RelicPieceSourceKeyResolver {
    fun resolve(event: RelicPieceSourceEvent): List<RelicPieceSourceKeyMatch> =
        when (event) {
            is RelicPieceSourceEvent.SecretPlaceDiscovered ->
                resolveSecretPlace(event)
            is RelicPieceSourceEvent.TreasureClusterCompleted ->
                listOf(
                    match(
                        sourceType = RelicPieceSourceType.TREASURE_CLUSTER,
                        sourceKey = RelicSchema.PieceSourceKeys.TreasureCluster.forType(event.clusterType),
                        domainId = event.clusterId,
                    ),
                )
            is RelicPieceSourceEvent.StoryFragmentUnlocked ->
                resolveStoryFragment(event)
            is RelicPieceSourceEvent.PanoramaHuntCompleted ->
                resolvePanoramaHunt(event)
            is RelicPieceSourceEvent.SeasonalEventGranted ->
                listOf(
                    match(
                        sourceType = RelicPieceSourceType.SEASONAL_EVENT,
                        sourceKey = RelicSchema.PieceSourceKeys.SeasonalEvent.forType(event.eventType),
                        domainId = "${event.eventType.id.lowercase()}:${event.grantKey}",
                    ),
                )
            is RelicPieceSourceEvent.AchievementCompleted ->
                resolveAchievement(event)
            is RelicPieceSourceEvent.RadarDiscovery ->
                listOf(
                    match(
                        sourceType = RelicPieceSourceType.RADAR_DISCOVERY,
                        sourceKey = RelicSchema.PieceSourceKeys.RadarDiscovery.forType(event.radarType),
                        domainId = event.targetId,
                    ),
                )
        }

    private fun resolveSecretPlace(
        event: RelicPieceSourceEvent.SecretPlaceDiscovered,
    ): List<RelicPieceSourceKeyMatch> {
        val matches = linkedSetOf(
            match(
                sourceType = RelicPieceSourceType.SECRET_PLACE,
                sourceKey = RelicSchema.PieceSourceKeys.SecretPlace.forCategory(event.category),
                domainId = event.secretPlaceId,
            ),
        )
        relicCategoriesLinkedToSecretPlace(event.category).forEach { relicCategory ->
            RelicSchema.SecretPlaceKeys.forCategory(relicCategory)?.let { legacyKey ->
                matches += match(
                    sourceType = RelicPieceSourceType.SECRET_PLACE,
                    sourceKey = legacyKey,
                    domainId = event.secretPlaceId,
                )
            }
        }
        return matches.toList()
    }

    private fun resolveStoryFragment(
        event: RelicPieceSourceEvent.StoryFragmentUnlocked,
    ): List<RelicPieceSourceKeyMatch> {
        val matches = linkedSetOf(
            match(
                sourceType = RelicPieceSourceType.STORY_FRAGMENT,
                sourceKey = RelicSchema.PieceSourceKeys.StoryFragment.forKey(event.storyFragmentKey),
                domainId = event.storyFragmentKey,
            ),
            match(
                sourceType = RelicPieceSourceType.STORY_FRAGMENT,
                sourceKey = event.storyFragmentKey,
                domainId = event.storyFragmentKey,
            ),
        )
        RelicCategory.ALL_ORDERED.forEach { category ->
            val legacyKey = RelicSchema.StoryFragmentKeys.forCategory(category)
            if (legacyKey == event.storyFragmentKey) {
                matches += match(
                    sourceType = RelicPieceSourceType.STORY_FRAGMENT,
                    sourceKey = legacyKey,
                    domainId = event.storyFragmentKey,
                )
            }
        }
        return matches.toList()
    }

    private fun resolvePanoramaHunt(
        event: RelicPieceSourceEvent.PanoramaHuntCompleted,
    ): List<RelicPieceSourceKeyMatch> {
        val matches = linkedSetOf(
            match(
                sourceType = RelicPieceSourceType.PANORAMA_HUNT,
                sourceKey = RelicSchema.PieceSourceKeys.PanoramaHunt.forType(event.huntType),
                domainId = event.huntId,
            ),
        )
        RelicCategory.ALL_ORDERED.forEach { category ->
            PanoramaHuntType.ALL_ORDERED.indices.forEach { index ->
                val pieceNumber = index + 1
                val legacyKey = RelicSchema.PanoramaHuntKeys.forCategory(category, pieceNumber)
                if (legacyKey.endsWith(":${event.huntType.id.lowercase()}")) {
                    matches += match(
                        sourceType = RelicPieceSourceType.PANORAMA_HUNT,
                        sourceKey = legacyKey,
                        domainId = event.huntId,
                    )
                }
            }
        }
        return matches.toList()
    }

    private fun resolveAchievement(
        event: RelicPieceSourceEvent.AchievementCompleted,
    ): List<RelicPieceSourceKeyMatch> {
        val matches = linkedSetOf(
            match(
                sourceType = RelicPieceSourceType.ACHIEVEMENT,
                sourceKey = RelicSchema.PieceSourceKeys.Achievement.forKey(event.achievementKey),
                domainId = event.achievementKey,
            ),
            match(
                sourceType = RelicPieceSourceType.ACHIEVEMENT,
                sourceKey = event.achievementKey,
                domainId = event.achievementKey,
            ),
        )
        RelicCategory.ALL_ORDERED.forEach { category ->
            val legacyKey = RelicSchema.AchievementKeys.firstDiscovery(category)
            if (legacyKey == event.achievementKey) {
                matches += match(
                    sourceType = RelicPieceSourceType.ACHIEVEMENT,
                    sourceKey = legacyKey,
                    domainId = event.achievementKey,
                )
            }
        }
        return matches.toList()
    }

    private fun relicCategoriesLinkedToSecretPlace(
        category: com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory,
    ): List<RelicCategory> =
        RelicCategory.ALL_ORDERED.filter { relicCategory ->
            RelicSchema.SecretPlaceKeys.linkedSecretPlaceCategory(relicCategory) == category
        }

    private fun match(
        sourceType: RelicPieceSourceType,
        sourceKey: String,
        domainId: String,
    ): RelicPieceSourceKeyMatch = RelicPieceSourceKeyMatch(
        sourceType = sourceType,
        sourceKey = sourceKey,
        domainId = domainId,
    )
}

internal data class RelicPieceSourceKeyMatch(
    val sourceType: RelicPieceSourceType,
    val sourceKey: String,
    val domainId: String,
)
