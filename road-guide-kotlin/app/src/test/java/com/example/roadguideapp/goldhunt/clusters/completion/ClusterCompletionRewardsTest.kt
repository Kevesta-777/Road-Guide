package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import org.junit.Assert.assertTrue
import org.junit.Test

class ClusterCompletionRewardsTest {
    @Test
    fun rewards_scaleWithClusterTypeTierAndTreasureCount() {
        val roadside = sampleCluster(ClusterType.ROADSIDE_CACHE, treasureCount = 2)
        val hoard = sampleCluster(ClusterType.LEGENDARY_HOARD, treasureCount = 10)
        assertTrue(ClusterCompletionRewards.creditsFor(hoard) > ClusterCompletionRewards.creditsFor(roadside))
        assertTrue(ClusterCompletionRewards.xpFor(hoard) > ClusterCompletionRewards.xpFor(roadside))
    }

    private fun sampleCluster(type: ClusterType, treasureCount: Int): TreasureCluster =
        TreasureCluster(
            clusterId = "tc:v1:test:L1:1:1:seed1",
            clusterType = type,
            centerLatitude = 51.5,
            centerLongitude = -0.2,
            radiusMeters = 100.0,
            treasureCount = treasureCount,
            clusterSeed = 1L,
        )
}
