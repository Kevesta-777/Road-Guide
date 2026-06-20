package com.example.roadguideapp.goldhunt.clusters.completion

import android.content.Context
import com.example.roadguideapp.goldhunt.clusters.statistics.TreasureClusterStatistics
import com.example.roadguideapp.goldhunt.clusters.statistics.TreasureClusterStatisticsManager
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpAwarder
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec

internal class ClusterCompletionManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val completionRepository = TreasureClusterCompletionRepository.get(appContext)
    private val clusterStatisticsManager = TreasureClusterStatisticsManager.get(appContext)
    private val gameplayXp = GameplayXpAwarder.get(appContext)

    suspend fun tryCompleteAfterTreasureCollection(
        spec: TreasureSpec,
        region: PlayRegion,
        isTreasureCollected: (String) -> Boolean,
        applyGrant: suspend (CreditGrant) -> Int,
        timestampMs: Long,
    ): ClusterCompletionOutcome {
        val clusterId = ClusterCompletionResolver.parseClusterIdFromTreasureId(spec.treasureId)
            ?: return ClusterCompletionOutcome.none
        completionRepository.ensureCacheLoaded()
        if (completionRepository.isCompletedCached(clusterId)) {
            return ClusterCompletionOutcome.none
        }
        val cluster = ClusterCompletionResolver.resolveCluster(clusterId, region)
            ?: return ClusterCompletionOutcome.none
        if (!ClusterCompletionEvaluator.isComplete(cluster, isTreasureCollected)) {
            return ClusterCompletionOutcome.none
        }

        val credits = ClusterCompletionRewards.creditsFor(cluster)
        val xp = ClusterCompletionRewards.xpFor(cluster)
        val record = completionRepository.recordCompletion(
            cluster = cluster,
            creditsGranted = credits,
            xpGranted = xp,
            completedAtMs = timestampMs,
        )
        if (!record.isNew) {
            return ClusterCompletionOutcome.none
        }

        applyGrant(ClusterCompletionRewardDispatcher.grantFor(cluster))
        val xpOutcome = gameplayXp.tryAwardClusterCompletion(cluster, timestampMs)
        return ClusterCompletionOutcome(
            completed = true,
            creditsGranted = credits,
            xpGranted = xpOutcome.xpAwarded,
            clusterId = cluster.clusterId,
        )
    }

    suspend fun ensureCacheLoaded() {
        completionRepository.ensureCacheLoaded()
    }

    suspend fun loadStatistics(): TreasureClusterStatistics =
        clusterStatisticsManager.load()

    fun isCompletedCached(clusterId: String): Boolean =
        completionRepository.isCompletedCached(clusterId)

    companion object {
        @Volatile
        private var instance: ClusterCompletionManager? = null

        fun get(context: Context): ClusterCompletionManager =
            instance ?: synchronized(this) {
                instance ?: ClusterCompletionManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
