package com.example.roadguideapp.goldhunt.clusters.completion

import android.content.Context
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaRepository
import com.example.roadguideapp.goldhunt.clusters.statistics.TreasureClusterStatisticsManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

internal class TreasureClusterCompletionRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val dao = GoldHuntDatabase.get(appContext).treasureClusterCompletionDao()
    private val clusterStatisticsManager = TreasureClusterStatisticsManager.get(appContext)
    private val clusterEncyclopediaRepository = ClusterEncyclopediaRepository.get(appContext)
    private val writeMutex = Mutex()
    private val completionCache = ConcurrentHashMap<String, Boolean>()
    private var cacheLoaded = false

    suspend fun ensureCacheLoaded() {
        if (cacheLoaded) return
        writeMutex.withLock {
            if (cacheLoaded) return@withLock
            for (row in dao.allOrdered()) {
                completionCache[row.clusterId] = true
            }
            cacheLoaded = true
        }
    }

    fun isCompletedCached(clusterId: String): Boolean = completionCache[clusterId] == true

    suspend fun recordCompletion(
        cluster: TreasureCluster,
        creditsGranted: Int,
        xpGranted: Long,
        completedAtMs: Long,
    ): ClusterCompletionRecordResult = writeMutex.withLock {
        ensureCacheLoadedUnlocked()
        if (isCompletedCached(cluster.clusterId)) {
            return@withLock ClusterCompletionRecordResult.skipped
        }
        val entity = TreasureClusterCompletionMapper.toEntity(
            cluster = cluster,
            creditsGranted = creditsGranted,
            xpGranted = xpGranted,
            completedAtMs = completedAtMs,
        )
        val inserted = dao.insert(entity)
        if (inserted < 0L) {
            completionCache[cluster.clusterId] = true
            return@withLock ClusterCompletionRecordResult.skipped
        }
        completionCache[cluster.clusterId] = true
        clusterStatisticsManager.recordCompletion(
            clusterType = cluster.clusterType,
            creditsEarned = creditsGranted,
            timestampMs = completedAtMs,
        )
        clusterEncyclopediaRepository.recordCompletion(
            cluster = cluster,
            creditsEarned = creditsGranted,
            completedAtMs = completedAtMs,
        )
        val completion = TreasureClusterCompletionMapper.toDomain(entity)
            ?: return@withLock ClusterCompletionRecordResult.skipped
        ClusterCompletionRecordResult(
            completion = completion,
            isNew = true,
            creditsGranted = creditsGranted,
            xpGranted = xpGranted,
        )
    }

    private suspend fun ensureCacheLoadedUnlocked() {
        if (cacheLoaded) return
        for (row in dao.allOrdered()) {
            completionCache[row.clusterId] = true
        }
        cacheLoaded = true
    }

    companion object {
        @Volatile
        private var instance: TreasureClusterCompletionRepository? = null

        fun get(context: Context): TreasureClusterCompletionRepository =
            instance ?: synchronized(this) {
                instance ?: TreasureClusterCompletionRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
