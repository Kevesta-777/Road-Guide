package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.treasure.TreasureId

internal object ClusterCompletionEvaluator {
    fun collectedCount(
        cluster: TreasureCluster,
        isCollected: (String) -> Boolean,
    ): Int {
        var count = 0
        for (index in 0 until cluster.treasureCount) {
            if (isCollected(TreasureId.forClusterSlot(cluster.clusterId, index))) {
                count++
            }
        }
        return count
    }

    fun isComplete(
        cluster: TreasureCluster,
        isCollected: (String) -> Boolean,
    ): Boolean = collectedCount(cluster, isCollected) >= cluster.treasureCount
}
