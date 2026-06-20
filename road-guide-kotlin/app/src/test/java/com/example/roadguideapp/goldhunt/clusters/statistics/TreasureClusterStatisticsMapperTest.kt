package com.example.roadguideapp.goldhunt.clusters.statistics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class TreasureClusterStatisticsMapperTest {
    @Test
    fun entityRoundTrip_preservesClusterStatistics() {
        val entity = ExplorerProfileEntity(
            totalClustersDiscovered = 5,
            totalClustersCompleted = 3,
            totalClusterRewardsEarned = 420,
            clusterStatsRoadsideCachesFound = 2,
            clusterStatsExplorerNestsFound = 1,
            clusterStatsTreasureGardensFound = 1,
            clusterStatsAncientVaultsFound = 1,
            clusterStatsLegendaryHoardsFound = 0,
            clusterStatsRoadsideCachesCompleted = 1,
            clusterStatsExplorerNestsCompleted = 1,
            clusterStatsTreasureGardensCompleted = 1,
            clusterStatsAncientVaultsCompleted = 0,
            clusterStatsLegendaryHoardsCompleted = 0,
        )
        val stats = TreasureClusterStatisticsMapper.fromEntity(entity)
        assertEquals(5, stats.clustersFound)
        assertEquals(3, stats.clustersCompleted)
        assertEquals(420, stats.totalRewardsEarned)
        assertEquals(2, stats.roadsideCachesFound)
        assertEquals(1, stats.explorerNestsCompleted)
        val roundTrip = TreasureClusterStatisticsMapper.applyToEntity(entity, stats)
        assertEquals(entity, roundTrip)
    }
}
