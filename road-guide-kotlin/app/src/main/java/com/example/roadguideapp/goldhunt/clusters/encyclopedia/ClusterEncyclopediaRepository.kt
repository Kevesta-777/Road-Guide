package com.example.roadguideapp.goldhunt.clusters.encyclopedia

import android.content.Context
import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class ClusterEncyclopediaRepository private constructor(context: Context) {
    private val database = GoldHuntDatabase.get(context.applicationContext)
    private val encyclopediaDao = database.clusterEncyclopediaDao()
    private val discoveryDao = database.treasureClusterDiscoveryDao()
    private val completionDao = database.treasureClusterCompletionDao()
    private val writeMutex = Mutex()

    suspend fun load(): ClusterEncyclopedia = writeMutex.withLock {
        ensureSeededUnlocked()
        reconcileFromClusterTablesUnlocked()
        buildEncyclopedia()
    }

    suspend fun recordDiscovery(
        cluster: TreasureCluster,
        discoveredAtMs: Long,
    ): ClusterEncyclopediaEntry? = writeMutex.withLock {
        ensureSeededUnlocked()
        recordDiscoveryUnlocked(cluster.clusterType, discoveredAtMs)
    }

    suspend fun recordCompletion(
        cluster: TreasureCluster,
        creditsEarned: Int,
        completedAtMs: Long,
    ): ClusterEncyclopediaEntry? = writeMutex.withLock {
        ensureSeededUnlocked()
        recordCompletionUnlocked(cluster, creditsEarned, completedAtMs)
    }

    private suspend fun recordDiscoveryUnlocked(
        clusterType: ClusterType,
        discoveredAtMs: Long,
    ): ClusterEncyclopediaEntry? {
        val template = ClusterEncyclopediaCatalog.templateForType(clusterType) ?: return null
        val existing = encyclopediaDao.get(clusterType.id)
        val discoveredCount = (existing?.discoveredCount ?: 0) + 1
        val completedCount = existing?.completedCount ?: 0
        val firstDiscoveredAtMs = existing?.firstDiscoveredAtMs?.takeIf { it > 0L } ?: discoveredAtMs
        val entity = ClusterEncyclopediaMapper.toEntity(
            clusterType = clusterType,
            status = ClusterEncyclopediaMapper.resolveStatus(discoveredCount, completedCount),
            firstDiscoveredAtMs = firstDiscoveredAtMs,
            completedAtMs = existing?.completedAtMs,
            totalRewardsEarned = existing?.totalRewardsEarned ?: 0,
            discoveredCount = discoveredCount,
            completedCount = completedCount,
            achievementKey = existing?.achievementKey ?: template.achievementKey,
            storyFragmentId = existing?.storyFragmentId ?: template.storyFragmentKey,
            timestampMs = discoveredAtMs,
        )
        encyclopediaDao.upsert(entity)
        return ClusterEncyclopediaMapper.toDomain(template, entity)
    }

    private suspend fun recordCompletionUnlocked(
        cluster: TreasureCluster,
        creditsEarned: Int,
        completedAtMs: Long,
    ): ClusterEncyclopediaEntry? {
        val template = ClusterEncyclopediaCatalog.templateForType(cluster.clusterType) ?: return null
        val existing = encyclopediaDao.get(cluster.clusterType.id)
        val discoveredCount = maxOf(existing?.discoveredCount ?: 0, 1)
        val completedCount = (existing?.completedCount ?: 0) + 1
        val firstDiscoveredAtMs = existing?.firstDiscoveredAtMs?.takeIf { it > 0L } ?: completedAtMs
        val completedAt = existing?.completedAtMs?.takeIf { it > 0L } ?: completedAtMs
        val achievementKey = cluster.resolvedAchievementKey ?: template.achievementKey
        val storyFragmentId = cluster.resolvedStoryFragmentId ?: template.storyFragmentKey
        val entity = ClusterEncyclopediaMapper.toEntity(
            clusterType = cluster.clusterType,
            status = ClusterEncyclopediaMapper.resolveStatus(discoveredCount, completedCount),
            firstDiscoveredAtMs = firstDiscoveredAtMs,
            completedAtMs = completedAt,
            totalRewardsEarned = (existing?.totalRewardsEarned ?: 0) + creditsEarned.coerceAtLeast(0),
            discoveredCount = discoveredCount,
            completedCount = completedCount,
            achievementKey = achievementKey,
            storyFragmentId = storyFragmentId,
            timestampMs = completedAtMs,
        )
        encyclopediaDao.upsert(entity)
        return ClusterEncyclopediaMapper.toDomain(template, entity)
    }

    private suspend fun ensureSeededUnlocked() {
        val existingKeys = encyclopediaDao.all().map { it.catalogKey }.toSet()
        val now = System.currentTimeMillis()
        val seeds = ClusterEncyclopediaCatalog.displayTemplates
            .filter { it.clusterType.id !in existingKeys }
            .map { template ->
                ClusterEncyclopediaMapper.toEntity(
                    clusterType = template.clusterType,
                    status = ClusterEncyclopediaCatalog.initialStatus(),
                    firstDiscoveredAtMs = null,
                    completedAtMs = null,
                    totalRewardsEarned = 0,
                    discoveredCount = 0,
                    completedCount = 0,
                    achievementKey = template.achievementKey,
                    storyFragmentId = template.storyFragmentKey,
                    timestampMs = now,
                )
            }
        if (seeds.isNotEmpty()) {
            encyclopediaDao.upsertAll(seeds)
        }
    }

    private suspend fun reconcileFromClusterTablesUnlocked() {
        val discoveries = discoveryDao.allOrdered()
        val completions = completionDao.allOrdered()
        if (discoveries.isEmpty() && completions.isEmpty()) return
        val now = System.currentTimeMillis()
        val discoveryByType = discoveries.groupBy { it.clusterType }
        val completionByType = completions.groupBy { it.clusterType }
        for (template in ClusterEncyclopediaCatalog.displayTemplates) {
            val typeId = template.clusterType.id
            val typeDiscoveries = discoveryByType[typeId].orEmpty()
            val typeCompletions = completionByType[typeId].orEmpty()
            if (typeDiscoveries.isEmpty() && typeCompletions.isEmpty()) continue
            val discoveredCount = typeDiscoveries.size
            val completedCount = typeCompletions.size
            val firstDiscoveredAtMs = typeDiscoveries.minOfOrNull { it.discoveredAtMs }
            val completedAtMs = typeCompletions.minOfOrNull { it.completedAtMs }
            val totalRewards = typeCompletions.sumOf { it.creditsGranted.coerceAtLeast(0) }
            val latestCompletion = typeCompletions.maxByOrNull { it.completedAtMs }
            encyclopediaDao.upsert(
                ClusterEncyclopediaMapper.toEntity(
                    clusterType = template.clusterType,
                    status = ClusterEncyclopediaMapper.resolveStatus(discoveredCount, completedCount),
                    firstDiscoveredAtMs = firstDiscoveredAtMs,
                    completedAtMs = completedAtMs,
                    totalRewardsEarned = totalRewards,
                    discoveredCount = discoveredCount,
                    completedCount = completedCount,
                    achievementKey = latestCompletion?.achievementKey ?: template.achievementKey,
                    storyFragmentId = latestCompletion?.storyFragmentId ?: template.storyFragmentKey,
                    timestampMs = now,
                ),
            )
        }
    }

    private suspend fun buildEncyclopedia(): ClusterEncyclopedia {
        val entities = encyclopediaDao.all().associateBy { it.catalogKey }
        val entries = ClusterEncyclopediaCatalog.displayTemplates.map { template ->
            ClusterEncyclopediaMapper.toDomain(template, entities[template.clusterType.id])
        }
        return ClusterEncyclopedia(
            entries = entries,
            progress = ClusterEncyclopediaMapper.buildProgress(entries),
        )
    }

    companion object {
        @Volatile
        private var instance: ClusterEncyclopediaRepository? = null

        fun get(context: Context): ClusterEncyclopediaRepository =
            instance ?: synchronized(this) {
                instance ?: ClusterEncyclopediaRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
