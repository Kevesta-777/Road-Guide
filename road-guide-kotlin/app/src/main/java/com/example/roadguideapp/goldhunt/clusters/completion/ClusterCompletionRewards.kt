package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.TreasureCluster

internal object ClusterCompletionRewards {
    fun creditsFor(cluster: TreasureCluster): Int =
        (ClusterCompletionConfig.BASE_COMPLETION_CREDITS *
            cluster.clusterType.rewardMultiplier *
            cluster.treasureCount)
            .toInt()
            .coerceAtLeast(1)

    fun xpFor(cluster: TreasureCluster): Long =
        (ClusterCompletionConfig.BASE_COMPLETION_XP *
            cluster.clusterType.tier *
            cluster.clusterType.rewardMultiplier)
            .toLong()
            .coerceAtLeast(1L)
}
