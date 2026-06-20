package com.example.roadguideapp.goldhunt.clusters.encyclopedia

import com.example.roadguideapp.goldhunt.clusters.ClusterType

internal object ClusterEncyclopediaMapper {
    fun toDomain(
        template: ClusterEncyclopediaTemplate,
        entity: ClusterEncyclopediaEntryEntity?,
    ): ClusterEncyclopediaEntry {
        val status = entity?.status?.let(ClusterEncyclopediaStatus::fromId)
            ?: ClusterEncyclopediaCatalog.initialStatus()
        return ClusterEncyclopediaEntry(
            clusterType = template.clusterType,
            displayName = template.displayName,
            status = status,
            firstDiscoveredAtMs = entity?.firstDiscoveredAtMs,
            completedAtMs = entity?.completedAtMs,
            totalRewardsEarned = entity?.totalRewardsEarned ?: 0,
            discoveredCount = entity?.discoveredCount ?: 0,
            completedCount = entity?.completedCount ?: 0,
            achievementKey = entity?.achievementKey ?: template.achievementKey,
            storyFragmentKey = entity?.storyFragmentId ?: template.storyFragmentKey,
            schemaVersion = entity?.schemaVersion ?: ClusterEncyclopediaSchema.VERSION,
            extensionJson = entity?.extensionJson ?: ClusterEncyclopediaSchema.EMPTY_EXTENSIONS_JSON,
        )
    }

    fun toEntity(
        clusterType: ClusterType,
        status: ClusterEncyclopediaStatus,
        firstDiscoveredAtMs: Long?,
        completedAtMs: Long?,
        totalRewardsEarned: Int,
        discoveredCount: Int,
        completedCount: Int,
        achievementKey: String?,
        storyFragmentId: String?,
        timestampMs: Long,
    ): ClusterEncyclopediaEntryEntity = ClusterEncyclopediaEntryEntity(
        catalogKey = clusterType.id,
        status = status.id,
        firstDiscoveredAtMs = firstDiscoveredAtMs,
        completedAtMs = completedAtMs,
        totalRewardsEarned = totalRewardsEarned,
        discoveredCount = discoveredCount,
        completedCount = completedCount,
        achievementKey = achievementKey,
        storyFragmentId = storyFragmentId,
        updatedAtMs = timestampMs,
    )

    fun resolveStatus(discoveredCount: Int, completedCount: Int): ClusterEncyclopediaStatus = when {
        discoveredCount <= 0 -> ClusterEncyclopediaStatus.MISSING
        completedCount > 0 -> ClusterEncyclopediaStatus.COMPLETED
        else -> ClusterEncyclopediaStatus.DISCOVERED
    }

    fun buildProgress(entries: List<ClusterEncyclopediaEntry>): ClusterEncyclopediaProgress {
        val trackable = entries.size
        val discovered = entries.count { !it.isMissing }
        val completed = entries.count { it.isCompleted }
        val rewards = entries.sumOf { it.totalRewardsEarned }
        return ClusterEncyclopediaProgress(
            discoveredCount = discovered,
            completedCount = completed,
            trackableCount = trackable,
            totalRewardsEarned = rewards,
        )
    }
}
