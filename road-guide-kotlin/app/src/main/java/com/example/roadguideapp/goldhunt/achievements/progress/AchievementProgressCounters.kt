package com.example.roadguideapp.goldhunt.achievements.progress

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

internal data class AchievementProgressCounters(
    val exploredCells: Int = 0,
    val treasuresCollected: Int = 0,
    val explorerLevel: Int = 1,
    val secretPlacesFound: Int = 0,
    val clustersDiscovered: Int = 0,
    val clustersCompleted: Int = 0,
    val panoramaHuntsCompleted: Int = 0,
    val radarTotalScans: Int = 0,
    val radarSuccessfulScans: Int = 0,
    val seasonalTreasuresCollected: Int = 0,
    val seasonalCompletedCycles: Int = 0,
    val storyFragmentsEarned: Int = 0,
    val legendaryRelicsEarned: Int = 0,
    val rarityCounts: Map<TreasureRarity, Int> = emptyMap(),
    val secretPlaceCategoryFound: Map<SecretPlaceCategory, Int> = emptyMap(),
    val secretPlaceCategoryCompleted: Map<SecretPlaceCategory, Int> = emptyMap(),
    val clusterTypeDiscovered: Map<ClusterType, Int> = emptyMap(),
    val clusterTypeCompleted: Map<ClusterType, Int> = emptyMap(),
    val panoramaTypeCompleted: Map<PanoramaHuntType, Int> = emptyMap(),
    val legacyCompletionCounts: Map<String, Int> = emptyMap(),
) {
    fun rarityCount(rarity: TreasureRarity): Int = rarityCounts[rarity] ?: 0

    fun secretPlaceFound(category: SecretPlaceCategory): Int =
        secretPlaceCategoryFound[category] ?: 0

    fun secretPlaceCompleted(category: SecretPlaceCategory): Int =
        secretPlaceCategoryCompleted[category] ?: 0

    fun clusterDiscovered(type: ClusterType): Int = clusterTypeDiscovered[type] ?: 0

    fun clusterCompleted(type: ClusterType): Int = clusterTypeCompleted[type] ?: 0

    fun panoramaCompleted(type: PanoramaHuntType): Int = panoramaTypeCompleted[type] ?: 0

    fun legacyCount(key: String): Int = legacyCompletionCounts[key] ?: 0

    companion object {
        fun fromProfile(
            profile: ExplorerProfileEntity,
            exploredCells: Int,
            storyFragmentsEarned: Int,
            legendaryRelicsEarned: Int,
            legacyCompletionCounts: Map<String, Int>,
        ): AchievementProgressCounters = AchievementProgressCounters(
            exploredCells = exploredCells,
            treasuresCollected = profile.totalTreasuresCollected,
            explorerLevel = profile.explorerLevel,
            secretPlacesFound = profile.totalSecretPlacesFound,
            clustersDiscovered = profile.totalClustersDiscovered,
            clustersCompleted = profile.totalClustersCompleted,
            panoramaHuntsCompleted = profile.totalPanoramaHuntsCompleted,
            radarTotalScans = profile.radarStatsTotalScans,
            radarSuccessfulScans = profile.radarStatsSuccessfulScans,
            seasonalTreasuresCollected = profile.seasonalEventStatsTreasuresCollected,
            seasonalCompletedCycles = profile.seasonalEventStatsCompletedCycles,
            storyFragmentsEarned = storyFragmentsEarned,
            legendaryRelicsEarned = legendaryRelicsEarned,
            rarityCounts = mapOf(
                TreasureRarity.COMMON to profile.commonTreasuresFound,
                TreasureRarity.UNCOMMON to profile.uncommonTreasuresFound,
                TreasureRarity.RARE to profile.rareTreasuresFound,
                TreasureRarity.EPIC to profile.epicTreasuresFound,
                TreasureRarity.LEGENDARY to profile.legendaryTreasuresFound,
                TreasureRarity.MYTHIC to profile.mythicTreasuresFound,
            ),
            secretPlaceCategoryFound = mapOf(
                SecretPlaceCategory.NATURE_SANCTUARY to profile.secretPlaceCatStatsNatureSanctuaryFound,
                SecretPlaceCategory.SCENIC_VIEWPOINT to profile.secretPlaceCatStatsScenicViewpointFound,
                SecretPlaceCategory.HISTORIC_LANDMARK to profile.secretPlaceCatStatsHistoricLandmarkFound,
                SecretPlaceCategory.MYSTERY_ZONE to profile.secretPlaceCatStatsMysteryZoneFound,
                SecretPlaceCategory.ANCIENT_RELIC_SITE to profile.secretPlaceCatStatsAncientRelicSiteFound,
                SecretPlaceCategory.EXPLORER_HIDEOUT to profile.secretPlaceCatStatsExplorerHideoutFound,
                SecretPlaceCategory.MYTHICAL_PLACE to profile.secretPlaceCatStatsMythicalPlaceFound,
                SecretPlaceCategory.LEGENDARY_SITE to profile.secretPlaceCatStatsLegendarySiteFound,
            ),
            secretPlaceCategoryCompleted = mapOf(
                SecretPlaceCategory.NATURE_SANCTUARY to profile.secretPlaceCatStatsNatureSanctuaryCompleted,
                SecretPlaceCategory.SCENIC_VIEWPOINT to profile.secretPlaceCatStatsScenicViewpointCompleted,
                SecretPlaceCategory.HISTORIC_LANDMARK to profile.secretPlaceCatStatsHistoricLandmarkCompleted,
                SecretPlaceCategory.MYSTERY_ZONE to profile.secretPlaceCatStatsMysteryZoneCompleted,
                SecretPlaceCategory.ANCIENT_RELIC_SITE to profile.secretPlaceCatStatsAncientRelicSiteCompleted,
                SecretPlaceCategory.EXPLORER_HIDEOUT to profile.secretPlaceCatStatsExplorerHideoutCompleted,
                SecretPlaceCategory.MYTHICAL_PLACE to profile.secretPlaceCatStatsMythicalPlaceCompleted,
                SecretPlaceCategory.LEGENDARY_SITE to profile.secretPlaceCatStatsLegendarySiteCompleted,
            ),
            clusterTypeDiscovered = mapOf(
                ClusterType.ROADSIDE_CACHE to profile.clusterStatsRoadsideCachesFound,
                ClusterType.EXPLORER_NEST to profile.clusterStatsExplorerNestsFound,
                ClusterType.TREASURE_GARDEN to profile.clusterStatsTreasureGardensFound,
                ClusterType.ANCIENT_VAULT to profile.clusterStatsAncientVaultsFound,
                ClusterType.LEGENDARY_HOARD to profile.clusterStatsLegendaryHoardsFound,
            ),
            clusterTypeCompleted = mapOf(
                ClusterType.ROADSIDE_CACHE to profile.clusterStatsRoadsideCachesCompleted,
                ClusterType.EXPLORER_NEST to profile.clusterStatsExplorerNestsCompleted,
                ClusterType.TREASURE_GARDEN to profile.clusterStatsTreasureGardensCompleted,
                ClusterType.ANCIENT_VAULT to profile.clusterStatsAncientVaultsCompleted,
                ClusterType.LEGENDARY_HOARD to profile.clusterStatsLegendaryHoardsCompleted,
            ),
            panoramaTypeCompleted = mapOf(
                PanoramaHuntType.HIDDEN_SYMBOL to profile.panoramaHuntStatsHiddenSymbolsFound,
                PanoramaHuntType.OBJECT_HUNT to profile.panoramaHuntStatsObjectsFound,
                PanoramaHuntType.PANORAMA_PUZZLE to profile.panoramaHuntStatsPanoramaPuzzlesSolved,
                PanoramaHuntType.SECRET_CODE to profile.panoramaHuntStatsCodesSolved,
                PanoramaHuntType.RELIC_HUNT to profile.panoramaHuntStatsRelicHuntsCompleted,
            ),
            legacyCompletionCounts = legacyCompletionCounts,
        )
    }
}
