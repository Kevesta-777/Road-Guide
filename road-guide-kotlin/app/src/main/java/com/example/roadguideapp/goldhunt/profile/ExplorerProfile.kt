package com.example.roadguideapp.goldhunt.profile

import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatistics
import com.example.roadguideapp.goldhunt.relics.statistics.HiddenRelicDiscoveryStatistics
import com.example.roadguideapp.goldhunt.relics.statistics.LegendaryRelicStatistics
import com.example.roadguideapp.goldhunt.clusters.statistics.TreasureClusterStatistics
import com.example.roadguideapp.goldhunt.panorama.statistics.PanoramaHuntStatistics
import com.example.roadguideapp.goldhunt.events.statistics.SeasonalEventStatistics
import com.example.roadguideapp.goldhunt.radar.statistics.RadarStatistics
import com.example.roadguideapp.goldhunt.secretplaces.statistics.SecretPlaceCategoryStatistics
import com.example.roadguideapp.goldhunt.treasure.stats.TreasureRarityStatistics

/**
 * Domain model for the Gold Hunt explorer profile (offline, persisted in Room).
 * XP/level progression rules and rewards are applied elsewhere; this type is storage-only.
 */
internal data class ExplorerProfile(
    val explorerLevel: Int,
    val currentXp: Long,
    val totalCredits: Int,
    val totalRoadsDiscovered: Int,
    val totalAreasDiscovered: Int,
    val totalTreasuresCollected: Int,
    val totalSecretPlacesFound: Int,
    val totalClustersDiscovered: Int = 0,
    val totalClustersCompleted: Int = 0,
    val totalPanoramaHuntsCompleted: Int = 0,
    val totalDistanceExploredM: Double,
    val treasureRarityStats: TreasureRarityStatistics = TreasureRarityStatistics.EMPTY,
    val clusterStatistics: TreasureClusterStatistics = TreasureClusterStatistics.EMPTY,
    val secretPlaceCategoryStatistics: SecretPlaceCategoryStatistics =
        SecretPlaceCategoryStatistics.EMPTY,
    val panoramaHuntStatistics: PanoramaHuntStatistics = PanoramaHuntStatistics.EMPTY,
    val radarStatistics: RadarStatistics = RadarStatistics.EMPTY,
    val seasonalEventStatistics: SeasonalEventStatistics = SeasonalEventStatistics.EMPTY,
    val achievementStatistics: AchievementStatistics = AchievementStatistics.EMPTY,
    val legendaryRelicStatistics: LegendaryRelicStatistics = LegendaryRelicStatistics.EMPTY,
    val hiddenRelicDiscoveryStatistics: HiddenRelicDiscoveryStatistics =
        HiddenRelicDiscoveryStatistics.EMPTY,
    val activeAchievementTitleKey: String? = null,
    val schemaVersion: Int = ExplorerProfileSchema.VERSION,
    val extensionJson: String = ExplorerProfileSchema.EMPTY_EXTENSIONS_JSON,
    val createdAtMs: Long = 0L,
    val updatedAtMs: Long = 0L,
) {
    companion object {
        fun default(nowMs: Long = System.currentTimeMillis()): ExplorerProfile = ExplorerProfile(
            explorerLevel = ExplorerProfileSchema.DEFAULT_EXPLORER_LEVEL,
            currentXp = 0L,
            totalCredits = 0,
            totalRoadsDiscovered = 0,
            totalAreasDiscovered = 0,
            totalTreasuresCollected = 0,
            totalSecretPlacesFound = 0,
            totalClustersDiscovered = 0,
            totalClustersCompleted = 0,
            totalPanoramaHuntsCompleted = 0,
            totalDistanceExploredM = 0.0,
            panoramaHuntStatistics = PanoramaHuntStatistics.EMPTY,
            radarStatistics = RadarStatistics.EMPTY,
            seasonalEventStatistics = SeasonalEventStatistics.EMPTY,
            achievementStatistics = AchievementStatistics.EMPTY,
            legendaryRelicStatistics = LegendaryRelicStatistics.EMPTY,
            hiddenRelicDiscoveryStatistics = HiddenRelicDiscoveryStatistics.EMPTY,
            clusterStatistics = TreasureClusterStatistics.EMPTY,
            secretPlaceCategoryStatistics = SecretPlaceCategoryStatistics.EMPTY,
            schemaVersion = ExplorerProfileSchema.VERSION,
            extensionJson = ExplorerProfileSchema.EMPTY_EXTENSIONS_JSON,
            createdAtMs = nowMs,
            updatedAtMs = nowMs,
        )
    }
}
