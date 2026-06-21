package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.ClusterType

internal data class TreasureClusterCompletion(
    val clusterId: String,
    val clusterType: ClusterType,
    val treasureCount: Int,
    val creditsGranted: Int,
    val xpGranted: Long,
    val completedAtMs: Long,
    val achievementKey: String?,
    val storyFragmentId: String?,
    val legendaryRelicKey: String?,
)

internal data class ClusterCompletionRecordResult(
    val completion: TreasureClusterCompletion?,
    val isNew: Boolean,
    val creditsGranted: Int = 0,
    val xpGranted: Long = 0L,
) {
    companion object {
        val skipped = ClusterCompletionRecordResult(
            completion = null,
            isNew = false,
        )
    }
}

internal data class ClusterCompletionOutcome(
    val completed: Boolean,
    val creditsGranted: Int = 0,
    val xpGranted: Long = 0L,
    val clusterId: String? = null,
) {
    companion object {
        val none = ClusterCompletionOutcome(completed = false)
    }
}
