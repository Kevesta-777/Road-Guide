package com.example.roadguideapp.goldhunt.clusters.encyclopedia

import com.example.roadguideapp.goldhunt.clusters.ClusterType

internal data class ClusterEncyclopediaEntry(
    val clusterType: ClusterType,
    val displayName: String,
    val status: ClusterEncyclopediaStatus,
    val firstDiscoveredAtMs: Long?,
    val completedAtMs: Long?,
    val totalRewardsEarned: Int,
    val discoveredCount: Int,
    val completedCount: Int,
    val achievementKey: String?,
    val storyFragmentKey: String?,
    val schemaVersion: Int = ClusterEncyclopediaSchema.VERSION,
    val extensionJson: String = ClusterEncyclopediaSchema.EMPTY_EXTENSIONS_JSON,
) {
    val isMissing: Boolean get() = status == ClusterEncyclopediaStatus.MISSING
    val isDiscovered: Boolean get() = status == ClusterEncyclopediaStatus.DISCOVERED
    val isCompleted: Boolean get() = status == ClusterEncyclopediaStatus.COMPLETED
}

internal data class ClusterEncyclopediaProgress(
    val discoveredCount: Int,
    val completedCount: Int,
    val trackableCount: Int,
    val totalRewardsEarned: Int,
) {
    val discoveredFraction: Float
        get() = if (trackableCount <= 0) 0f else discoveredCount.toFloat() / trackableCount.toFloat()

    val completionFraction: Float
        get() = if (trackableCount <= 0) 0f else completedCount.toFloat() / trackableCount.toFloat()
}

internal data class ClusterEncyclopedia(
    val entries: List<ClusterEncyclopediaEntry>,
    val progress: ClusterEncyclopediaProgress,
    val schemaVersion: Int = ClusterEncyclopediaSchema.VERSION,
)
