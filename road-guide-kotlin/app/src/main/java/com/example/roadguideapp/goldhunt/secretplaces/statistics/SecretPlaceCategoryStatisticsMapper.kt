package com.example.roadguideapp.goldhunt.secretplaces.statistics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfile
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

internal object SecretPlaceCategoryStatisticsMapper {
    fun fromProfile(profile: ExplorerProfile): SecretPlaceCategoryStatistics =
        profile.secretPlaceCategoryStatistics

    fun fromEntity(entity: ExplorerProfileEntity): SecretPlaceCategoryStatistics =
        SecretPlaceCategoryStatistics(
            discoveredCount = entity.secretPlaceCatStatsDiscovered,
            completedCount = entity.secretPlaceCatStatsCompleted,
            creditsEarned = entity.secretPlaceCatStatsCreditsEarned,
            xpEarned = entity.secretPlaceCatStatsXpEarned,
            bestCategory = entity.secretPlaceCatStatsBestCategoryId
                ?.let(SecretPlaceCategory::fromId),
            rarestCategory = entity.secretPlaceCatStatsRarestCategoryId
                ?.let(SecretPlaceCategory::fromId),
            natureSanctuariesFound = entity.secretPlaceCatStatsNatureSanctuaryFound,
            scenicViewpointsFound = entity.secretPlaceCatStatsScenicViewpointFound,
            historicLandmarksFound = entity.secretPlaceCatStatsHistoricLandmarkFound,
            mysteryZonesFound = entity.secretPlaceCatStatsMysteryZoneFound,
            ancientRelicSitesFound = entity.secretPlaceCatStatsAncientRelicSiteFound,
            explorerHideoutsFound = entity.secretPlaceCatStatsExplorerHideoutFound,
            mythicalPlacesFound = entity.secretPlaceCatStatsMythicalPlaceFound,
            legendarySitesFound = entity.secretPlaceCatStatsLegendarySiteFound,
            natureSanctuariesCompleted = entity.secretPlaceCatStatsNatureSanctuaryCompleted,
            scenicViewpointsCompleted = entity.secretPlaceCatStatsScenicViewpointCompleted,
            historicLandmarksCompleted = entity.secretPlaceCatStatsHistoricLandmarkCompleted,
            mysteryZonesCompleted = entity.secretPlaceCatStatsMysteryZoneCompleted,
            ancientRelicSitesCompleted = entity.secretPlaceCatStatsAncientRelicSiteCompleted,
            explorerHideoutsCompleted = entity.secretPlaceCatStatsExplorerHideoutCompleted,
            mythicalPlacesCompleted = entity.secretPlaceCatStatsMythicalPlaceCompleted,
            legendarySitesCompleted = entity.secretPlaceCatStatsLegendarySiteCompleted,
        )

    fun applyToProfile(
        profile: ExplorerProfile,
        stats: SecretPlaceCategoryStatistics,
    ): ExplorerProfile = profile.copy(
        secretPlaceCategoryStatistics = stats,
    )

    fun applyToEntity(
        entity: ExplorerProfileEntity,
        stats: SecretPlaceCategoryStatistics,
    ): ExplorerProfileEntity = entity.copy(
        secretPlaceCatStatsDiscovered = stats.discoveredCount,
        secretPlaceCatStatsCompleted = stats.completedCount,
        secretPlaceCatStatsCreditsEarned = stats.creditsEarned,
        secretPlaceCatStatsXpEarned = stats.xpEarned,
        secretPlaceCatStatsBestCategoryId = stats.bestCategory?.id,
        secretPlaceCatStatsRarestCategoryId = stats.rarestCategory?.id,
        secretPlaceCatStatsNatureSanctuaryFound = stats.natureSanctuariesFound,
        secretPlaceCatStatsScenicViewpointFound = stats.scenicViewpointsFound,
        secretPlaceCatStatsHistoricLandmarkFound = stats.historicLandmarksFound,
        secretPlaceCatStatsMysteryZoneFound = stats.mysteryZonesFound,
        secretPlaceCatStatsAncientRelicSiteFound = stats.ancientRelicSitesFound,
        secretPlaceCatStatsExplorerHideoutFound = stats.explorerHideoutsFound,
        secretPlaceCatStatsMythicalPlaceFound = stats.mythicalPlacesFound,
        secretPlaceCatStatsLegendarySiteFound = stats.legendarySitesFound,
        secretPlaceCatStatsNatureSanctuaryCompleted = stats.natureSanctuariesCompleted,
        secretPlaceCatStatsScenicViewpointCompleted = stats.scenicViewpointsCompleted,
        secretPlaceCatStatsHistoricLandmarkCompleted = stats.historicLandmarksCompleted,
        secretPlaceCatStatsMysteryZoneCompleted = stats.mysteryZonesCompleted,
        secretPlaceCatStatsAncientRelicSiteCompleted = stats.ancientRelicSitesCompleted,
        secretPlaceCatStatsExplorerHideoutCompleted = stats.explorerHideoutsCompleted,
        secretPlaceCatStatsMythicalPlaceCompleted = stats.mythicalPlacesCompleted,
        secretPlaceCatStatsLegendarySiteCompleted = stats.legendarySitesCompleted,
    )
}
