package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster

internal object TreasureClusterCompletionMapper {
    fun toEntity(
        cluster: TreasureCluster,
        creditsGranted: Int,
        xpGranted: Long,
        completedAtMs: Long,
    ): TreasureClusterCompletionEntity = TreasureClusterCompletionEntity(
        clusterId = cluster.clusterId,
        clusterType = cluster.clusterType.id,
        treasureCount = cluster.treasureCount,
        creditsGranted = creditsGranted,
        xpGranted = xpGranted,
        completedAtMs = completedAtMs,
        achievementKey = cluster.resolvedAchievementKey,
        storyFragmentId = cluster.resolvedStoryFragmentId,
        legendaryRelicKey = cluster.legendaryRelicKey,
    )

    fun toDomain(entity: TreasureClusterCompletionEntity): TreasureClusterCompletion? {
        val type = ClusterType.fromId(entity.clusterType) ?: return null
        return TreasureClusterCompletion(
            clusterId = entity.clusterId,
            clusterType = type,
            treasureCount = entity.treasureCount,
            creditsGranted = entity.creditsGranted,
            xpGranted = entity.xpGranted,
            completedAtMs = entity.completedAtMs,
            achievementKey = entity.achievementKey,
            storyFragmentId = entity.storyFragmentId,
            legendaryRelicKey = entity.legendaryRelicKey,
        )
    }

    fun statisticsFromRows(rows: List<ClusterTypeCountRow>): ClusterCompletionStatistics {
        if (rows.isEmpty()) return ClusterCompletionStatistics.EMPTY
        val counts = linkedMapOf<ClusterType, Int>()
        for (row in rows) {
            val type = ClusterType.fromId(row.clusterType) ?: continue
            counts[type] = row.completedCount
        }
        return ClusterCompletionStatistics(countsByType = counts)
    }
}
