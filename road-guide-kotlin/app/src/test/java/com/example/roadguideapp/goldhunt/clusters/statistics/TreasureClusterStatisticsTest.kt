package com.example.roadguideapp.goldhunt.clusters.statistics

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import org.junit.Assert.assertEquals
import org.junit.Test

class TreasureClusterStatisticsTest {
    @Test
    fun withDiscovery_incrementsTotalsAndTypeFound() {
        val next = TreasureClusterStatistics.EMPTY.withDiscovery(ClusterType.ANCIENT_VAULT)
        assertEquals(1, next.clustersFound)
        assertEquals(1, next.ancientVaultsFound)
        assertEquals(0, next.clustersCompleted)
    }

    @Test
    fun withCompletion_incrementsTotalsTypeCompletedAndRewards() {
        val next = TreasureClusterStatistics.EMPTY
            .withCompletion(ClusterType.LEGENDARY_HOARD, creditsEarned = 250)
        assertEquals(1, next.clustersCompleted)
        assertEquals(1, next.legendaryHoardsCompleted)
        assertEquals(250, next.totalRewardsEarned)
    }

    @Test
    fun completedEntriesOrdered_coversAllClusterFamilies() {
        val stats = TreasureClusterStatistics(
            roadsideCachesCompleted = 2,
            explorerNestsCompleted = 1,
            treasureGardensCompleted = 3,
            ancientVaultsCompleted = 0,
            legendaryHoardsCompleted = 1,
        )
        assertEquals(ClusterType.entries.size, stats.completedEntriesOrdered().size)
        assertEquals(2, stats.completedCountFor(ClusterType.ROADSIDE_CACHE))
    }
}
