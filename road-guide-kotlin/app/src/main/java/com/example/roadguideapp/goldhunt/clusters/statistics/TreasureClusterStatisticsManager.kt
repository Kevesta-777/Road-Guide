package com.example.roadguideapp.goldhunt.clusters.statistics

import android.content.Context
import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository

/**
 * Persists lifetime treasure cluster statistics on the explorer profile singleton.
 */
internal class TreasureClusterStatisticsManager private constructor(context: Context) {
    private val repository = ExplorerProfileRepository.get(context.applicationContext)

    suspend fun load(): TreasureClusterStatistics =
        TreasureClusterStatisticsMapper.fromProfile(repository.loadProfile())

    suspend fun recordDiscovery(
        clusterType: ClusterType,
        timestampMs: Long = System.currentTimeMillis(),
    ): TreasureClusterStatistics {
        val updated = repository.updateProfile(timestampMs) { profile ->
            val stats = TreasureClusterStatisticsMapper.fromProfile(profile).withDiscovery(clusterType)
            TreasureClusterStatisticsMapper.applyToProfile(profile, stats)
        }
        return TreasureClusterStatisticsMapper.fromProfile(updated)
    }

    suspend fun recordCompletion(
        clusterType: ClusterType,
        creditsEarned: Int,
        timestampMs: Long = System.currentTimeMillis(),
    ): TreasureClusterStatistics {
        val updated = repository.updateProfile(timestampMs) { profile ->
            val stats = TreasureClusterStatisticsMapper
                .fromProfile(profile)
                .withCompletion(clusterType, creditsEarned)
            TreasureClusterStatisticsMapper.applyToProfile(profile, stats)
        }
        return TreasureClusterStatisticsMapper.fromProfile(updated)
    }

    companion object {
        @Volatile
        private var instance: TreasureClusterStatisticsManager? = null

        fun get(context: Context): TreasureClusterStatisticsManager =
            instance ?: synchronized(this) {
                instance ?: TreasureClusterStatisticsManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
