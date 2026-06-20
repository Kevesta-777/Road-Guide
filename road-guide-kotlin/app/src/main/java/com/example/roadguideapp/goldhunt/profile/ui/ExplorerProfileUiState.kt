package com.example.roadguideapp.goldhunt.profile.ui

import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatistics
import com.example.roadguideapp.goldhunt.relics.statistics.HiddenRelicDiscoveryStatistics
import com.example.roadguideapp.goldhunt.relics.statistics.LegendaryRelicStatistics
import com.example.roadguideapp.goldhunt.clusters.statistics.TreasureClusterStatistics
import com.example.roadguideapp.goldhunt.panorama.statistics.PanoramaHuntStatistics
import com.example.roadguideapp.goldhunt.events.statistics.SeasonalEventStatistics
import com.example.roadguideapp.goldhunt.radar.statistics.RadarStatistics
import com.example.roadguideapp.goldhunt.secretplaces.statistics.SecretPlaceCategoryStatistics
import com.example.roadguideapp.goldhunt.treasure.stats.TreasureRarityStatistics

internal data class ExplorerProfileUiState(
    val level: Int,
    val rankTitle: String,
    val displayTitle: String = rankTitle,
    val usesAchievementTitle: Boolean = false,
    val lifetimeXp: Long,
    val xpIntoLevel: Long,
    val xpToNextLevel: Long,
    val progressFraction: Float,
    val totalCredits: Int,
    val treasuresCollected: Int,
    val secretPlacesFound: Int,
    val clusterStatistics: TreasureClusterStatistics = TreasureClusterStatistics.EMPTY,
    val panoramaHuntStatistics: PanoramaHuntStatistics = PanoramaHuntStatistics.EMPTY,
    val radarStatistics: RadarStatistics = RadarStatistics.EMPTY,
    val seasonalEventStatistics: SeasonalEventStatistics = SeasonalEventStatistics.EMPTY,
    val achievementStatistics: AchievementStatistics = AchievementStatistics.EMPTY,
    val legendaryRelicStatistics: LegendaryRelicStatistics = LegendaryRelicStatistics.EMPTY,
    val hiddenRelicDiscoveryStatistics: HiddenRelicDiscoveryStatistics =
        HiddenRelicDiscoveryStatistics.EMPTY,
    val secretPlaceCategoryStatistics: SecretPlaceCategoryStatistics =
        SecretPlaceCategoryStatistics.EMPTY,
    val distanceExploredM: Double,
    val treasureRarityStats: TreasureRarityStatistics = TreasureRarityStatistics.EMPTY,
    val encyclopediaProgressLabel: String = "",
    val encyclopediaProgressFraction: Float = 0f,
    val clusterEncyclopediaProgressLabel: String = "",
    val clusterEncyclopediaProgressFraction: Float = 0f,
    val collectionBookProgressLabel: String = "",
    val collectionBookProgressFraction: Float = 0f,
    val panoramaHuntJournalProgressLabel: String = "",
    val panoramaHuntJournalDiscoveredProgressLabel: String = "",
    val panoramaHuntJournalMissingProgressLabel: String = "",
    val panoramaHuntJournalProgressFraction: Float = 0f,
    val seasonalEventCollectionBookProgressLabel: String = "",
    val seasonalEventCollectionBookProgressFraction: Float = 0f,
    val achievementJournalProgressLabel: String = "",
    val achievementJournalCompletionPercentageLabel: String = "",
    val achievementJournalProgressFraction: Float = 0f,
    val achievementBadgeProgressLabel: String = "",
    val achievementBadgeCompletionPercentageLabel: String = "",
    val achievementBadgeProgressFraction: Float = 0f,
    val achievementTitleProgressLabel: String = "",
    val achievementTitleCompletionPercentageLabel: String = "",
    val achievementTitleProgressFraction: Float = 0f,
    val activeAchievementTitleLabel: String? = null,
    val achievementCollectionBookProgressLabel: String = "",
    val achievementCollectionBookCompletionPercentageLabel: String = "",
    val achievementCollectionBookRareLegendaryLabel: String = "",
    val achievementCollectionBookProgressFraction: Float = 0f,
    val legendaryRelicProgressLabel: String = "",
    val legendaryRelicCompletionPercentageLabel: String = "",
    val legendaryRelicPieceProgressLabel: String = "",
    val legendaryRelicPieceCompletionPercentageLabel: String = "",
    val legendaryRelicProgressFraction: Float = 0f,
    val legendaryRelicPieceProgressFraction: Float = 0f,
    val legendaryRelicHiddenDiscoveredLabel: String = "",
    val legendaryRelicHiddenDiscoveryPercentageLabel: String = "",
    val hiddenRelicRevealProgressLabel: String = "",
    val hiddenRelicRevealPercentageLabel: String = "",
    val hiddenRelicUndiscoveredLabel: String = "",
    val hiddenRelicRevealProgressFraction: Float = 0f,
    val legendaryRelicJournalCompletedProgressLabel: String = "",
    val legendaryRelicJournalPiecesFoundProgressLabel: String = "",
    val legendaryRelicJournalPiecesMissingProgressLabel: String = "",
    val legendaryRelicJournalProgressFraction: Float = 0f,
    val legendaryRelicShowcaseCompletedProgressLabel: String = "",
    val legendaryRelicShowcaseBadgesEarnedLabel: String = "",
    val legendaryRelicShowcaseTitlesEarnedLabel: String = "",
    val legendaryRelicShowcaseProgressFraction: Float = 0f,
) {
    companion object {
        val Empty = ExplorerProfileUiState(
            level = 1,
            rankTitle = "",
            lifetimeXp = 0L,
            xpIntoLevel = 0L,
            xpToNextLevel = 0L,
            progressFraction = 0f,
            totalCredits = 0,
            treasuresCollected = 0,
            secretPlacesFound = 0,
            distanceExploredM = 0.0,
        )
    }
}
