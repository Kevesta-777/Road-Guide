package com.example.roadguideapp.goldhunt.radar.statistics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class RadarStatisticsMapperTest {
    @Test
    fun entityRoundTrip_preservesRadarStatistics() {
        val stats = RadarStatistics(
            totalScans = 12,
            successfulScans = 8,
            treasuresFound = 5,
            clustersFound = 3,
            secretPlacesFound = 2,
            storyDiscoveries = 4,
            legendaryDiscoveries = 1,
        )
        val entity = ExplorerProfileEntity(
            radarStatsTotalScans = stats.totalScans,
            radarStatsSuccessfulScans = stats.successfulScans,
            radarStatsTreasuresFound = stats.treasuresFound,
            radarStatsClustersFound = stats.clustersFound,
            radarStatsSecretPlacesFound = stats.secretPlacesFound,
            radarStatsStoryDiscoveries = stats.storyDiscoveries,
            radarStatsLegendaryDiscoveries = stats.legendaryDiscoveries,
        )

        val mapped = RadarStatisticsMapper.fromEntity(entity)
        val roundTrip = RadarStatisticsMapper.fromEntity(
            RadarStatisticsMapper.applyToEntity(entity, mapped),
        )

        assertEquals(stats, mapped)
        assertEquals(stats, roundTrip)
    }
}
