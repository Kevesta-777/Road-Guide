package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.treasure.TreasureId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClusterCompletionEvaluatorTest {
    @Test
    fun isComplete_whenAllSlotsCollected() {
        val cluster = sampleCluster(treasureCount = 3)
        val collected = (0 until cluster.treasureCount).map { index ->
            TreasureId.forClusterSlot(cluster.clusterId, index)
        }.toSet()
        assertTrue(
            ClusterCompletionEvaluator.isComplete(cluster) { collected.contains(it) },
        )
        assertEquals(cluster.treasureCount, ClusterCompletionEvaluator.collectedCount(cluster) {
            collected.contains(it)
        })
    }

    @Test
    fun isComplete_falseWhenOneTreasureMissing() {
        val cluster = sampleCluster(treasureCount = 3)
        val collected = setOf(
            TreasureId.forClusterSlot(cluster.clusterId, 0),
            TreasureId.forClusterSlot(cluster.clusterId, 1),
        )
        assertFalse(
            ClusterCompletionEvaluator.isComplete(cluster) { collected.contains(it) },
        )
    }

    private fun sampleCluster(treasureCount: Int = 4): TreasureCluster = TreasureCluster(
        clusterId = "tc:v1:r:-0.5000:51.4000:L1:3:7:seed42",
        clusterType = ClusterType.TREASURE_GARDEN,
        centerLatitude = 51.5,
        centerLongitude = -0.2,
        radiusMeters = 100.0,
        treasureCount = treasureCount,
        clusterSeed = 42L,
    )
}
