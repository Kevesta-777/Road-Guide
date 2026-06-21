package com.example.roadguideapp.goldhunt.radar.statistics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfile
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity

internal object RadarStatisticsMapper {
    fun fromProfile(profile: ExplorerProfile): RadarStatistics = profile.radarStatistics

    fun fromEntity(entity: ExplorerProfileEntity): RadarStatistics = RadarStatistics(
        totalScans = entity.radarStatsTotalScans.coerceAtLeast(0),
        successfulScans = entity.radarStatsSuccessfulScans.coerceAtLeast(0)
            .coerceAtMost(entity.radarStatsTotalScans.coerceAtLeast(0)),
        treasuresFound = entity.radarStatsTreasuresFound.coerceAtLeast(0),
        clustersFound = entity.radarStatsClustersFound.coerceAtLeast(0),
        secretPlacesFound = entity.radarStatsSecretPlacesFound.coerceAtLeast(0),
        storyDiscoveries = entity.radarStatsStoryDiscoveries.coerceAtLeast(0),
        legendaryDiscoveries = entity.radarStatsLegendaryDiscoveries.coerceAtLeast(0),
    )

    fun applyToProfile(
        profile: ExplorerProfile,
        stats: RadarStatistics,
    ): ExplorerProfile = profile.copy(radarStatistics = stats)

    fun applyToEntity(
        entity: ExplorerProfileEntity,
        stats: RadarStatistics,
    ): ExplorerProfileEntity = entity.copy(
        radarStatsTotalScans = stats.totalScans.coerceAtLeast(0),
        radarStatsSuccessfulScans = stats.successfulScans.coerceAtLeast(0)
            .coerceAtMost(stats.totalScans.coerceAtLeast(0)),
        radarStatsTreasuresFound = stats.treasuresFound.coerceAtLeast(0),
        radarStatsClustersFound = stats.clustersFound.coerceAtLeast(0),
        radarStatsSecretPlacesFound = stats.secretPlacesFound.coerceAtLeast(0),
        radarStatsStoryDiscoveries = stats.storyDiscoveries.coerceAtLeast(0),
        radarStatsLegendaryDiscoveries = stats.legendaryDiscoveries.coerceAtLeast(0),
    )
}
