package com.example.roadguideapp.goldhunt.clusters.discovery

import android.content.Context
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaRepository
import com.example.roadguideapp.goldhunt.clusters.statistics.TreasureClusterStatisticsManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

internal class TreasureClusterDiscoveryRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val dao = GoldHuntDatabase.get(appContext).treasureClusterDiscoveryDao()
    private val clusterStatisticsManager = TreasureClusterStatisticsManager.get(appContext)
    private val clusterEncyclopediaRepository = ClusterEncyclopediaRepository.get(appContext)
    private val writeMutex = Mutex()
    private val discoveryCache = ConcurrentHashMap<String, Boolean>()
    private var cacheLoaded = false

    suspend fun ensureCacheLoaded() {
        if (cacheLoaded) return
        writeMutex.withLock {
            if (cacheLoaded) return@withLock
            for (row in dao.allOrdered()) {
                discoveryCache[row.clusterId] = true
            }
            cacheLoaded = true
        }
    }

    fun isDiscoveredCached(clusterId: String): Boolean = discoveryCache[clusterId] == true

    suspend fun countDiscovered(): Int {
        ensureCacheLoaded()
        return dao.count()
    }

    suspend fun recordDiscovery(
        cluster: TreasureCluster,
        trigger: ClusterDiscoveryTrigger,
        discoveredAtMs: Long,
    ): ClusterDiscoveryRecordResult = writeMutex.withLock {
        ensureCacheLoadedUnlocked()
        if (isDiscoveredCached(cluster.clusterId)) {
            return@withLock ClusterDiscoveryRecordResult.skipped
        }
        val entity = TreasureClusterDiscoveryMapper.toEntity(
            cluster = cluster,
            trigger = trigger,
            discoveredAtMs = discoveredAtMs,
        )
        val inserted = dao.insert(entity)
        if (inserted < 0L) {
            discoveryCache[cluster.clusterId] = true
            return@withLock ClusterDiscoveryRecordResult.skipped
        }
        discoveryCache[cluster.clusterId] = true
        clusterStatisticsManager.recordDiscovery(cluster.clusterType, discoveredAtMs)
        clusterEncyclopediaRepository.recordDiscovery(cluster, discoveredAtMs)
        val discovery = TreasureClusterDiscoveryMapper.toDomain(entity)
            ?: return@withLock ClusterDiscoveryRecordResult.skipped
        ClusterDiscoveryRecordResult(discovery = discovery, isNew = true)
    }

    private suspend fun ensureCacheLoadedUnlocked() {
        if (cacheLoaded) return
        for (row in dao.allOrdered()) {
            discoveryCache[row.clusterId] = true
        }
        cacheLoaded = true
    }

    companion object {
        @Volatile
        private var instance: TreasureClusterDiscoveryRepository? = null

        fun get(context: Context): TreasureClusterDiscoveryRepository =
            instance ?: synchronized(this) {
                instance ?: TreasureClusterDiscoveryRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
