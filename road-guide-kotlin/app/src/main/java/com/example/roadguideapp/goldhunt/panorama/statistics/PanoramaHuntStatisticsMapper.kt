package com.example.roadguideapp.goldhunt.panorama.statistics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfile
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity

internal object PanoramaHuntStatisticsMapper {
    fun fromProfile(profile: ExplorerProfile): PanoramaHuntStatistics =
        profile.panoramaHuntStatistics

    fun fromEntity(entity: ExplorerProfileEntity): PanoramaHuntStatistics = PanoramaHuntStatistics(
        totalHunts = entity.panoramaHuntStatsTotalHunts,
        huntsCompleted = entity.totalPanoramaHuntsCompleted,
        hiddenSymbolsFound = entity.panoramaHuntStatsHiddenSymbolsFound,
        objectsFound = entity.panoramaHuntStatsObjectsFound,
        codesSolved = entity.panoramaHuntStatsCodesSolved,
        panoramaPuzzlesSolved = entity.panoramaHuntStatsPanoramaPuzzlesSolved,
        relicHuntsCompleted = entity.panoramaHuntStatsRelicHuntsCompleted,
        targetsFound = entity.panoramaHuntStatsTargetsFound,
        rewardsEarned = entity.panoramaHuntStatsRewardsEarned,
        xpEarned = entity.panoramaHuntStatsXpEarned,
    )

    fun applyToProfile(
        profile: ExplorerProfile,
        stats: PanoramaHuntStatistics,
    ): ExplorerProfile = profile.copy(
        totalPanoramaHuntsCompleted = stats.huntsCompleted,
        panoramaHuntStatistics = stats,
    )

    fun applyToEntity(
        entity: ExplorerProfileEntity,
        stats: PanoramaHuntStatistics,
    ): ExplorerProfileEntity = entity.copy(
        panoramaHuntStatsTotalHunts = stats.totalHunts,
        totalPanoramaHuntsCompleted = stats.huntsCompleted,
        panoramaHuntStatsHiddenSymbolsFound = stats.hiddenSymbolsFound,
        panoramaHuntStatsObjectsFound = stats.objectsFound,
        panoramaHuntStatsCodesSolved = stats.codesSolved,
        panoramaHuntStatsPanoramaPuzzlesSolved = stats.panoramaPuzzlesSolved,
        panoramaHuntStatsRelicHuntsCompleted = stats.relicHuntsCompleted,
        panoramaHuntStatsTargetsFound = stats.targetsFound,
        panoramaHuntStatsRewardsEarned = stats.rewardsEarned,
        panoramaHuntStatsXpEarned = stats.xpEarned,
    )
}
