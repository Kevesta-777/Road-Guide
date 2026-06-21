package com.example.roadguideapp.goldhunt.achievements.progress

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantManager
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatisticsManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Central achievement progress engine.
 *
 * Seeds the unified achievement table from [AchievementRegistry], reconciles live
 * gameplay counters, and mirrors legacy panorama achievement increments without
 * duplicate completion grants.
 *
 * Hidden and secret achievements are tracked identically to visible ones. Visibility
 * tiers only affect journal display and reveal after completion.
 */
internal class AchievementProgressEngine private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val repository = AchievementProgressRepository.get(appContext)
    private val counterSource = AchievementProgressCounterSource.get(appContext)
    private val grantManager = AchievementRewardGrantManager.get(appContext)
    private val statisticsManager = AchievementStatisticsManager.get(appContext)
    private val reconcileMutex = Mutex()

    suspend fun ensureSeeded(timestampMs: Long = System.currentTimeMillis()) {
        repository.ensureSeeded(timestampMs)
        grantManager.reconcilePendingGrants(timestampMs)
        statisticsManager.reconcile(timestampMs)
    }

    suspend fun handle(event: AchievementProgressEvent): AchievementProgressBatchResult =
        reconcileMutex.withLock {
            val batch = when (event) {
                is AchievementProgressEvent.LegacyIncrement -> {
                    val incrementResult = repository.applyIncrement(
                        achievementId = event.achievementKey,
                        increment = event.increment,
                        timestampMs = event.timestampMs,
                    )
                    val categories = AchievementProgressEvaluator.categoriesForEvent(event)
                    val reconcileResult = reconcileCategoriesUnlocked(categories, event.timestampMs)
                    AchievementProgressBatchResult(
                        results = listOf(incrementResult) + reconcileResult.results,
                    )
                }
                is AchievementProgressEvent.FullReconcile ->
                    reconcileCategoriesUnlocked(AchievementCategory.ALL_ORDERED.toSet(), event.timestampMs)
                else -> {
                    val categories = AchievementProgressEvaluator.categoriesForEvent(event)
                    reconcileCategoriesUnlocked(categories, event.timestampMs)
                }
            }
            dispatchRewardsFor(batch.newCompletions, eventTimestampMs(event))
            batch
        }

    suspend fun recordLegacyIncrement(
        achievementKey: String,
        increment: Int = 1,
        timestampMs: Long = System.currentTimeMillis(),
    ): AchievementProgressResult =
        handle(
            AchievementProgressEvent.LegacyIncrement(
                achievementKey = achievementKey,
                increment = increment,
                timestampMs = timestampMs,
            ),
        ).results.firstOrNull() ?: AchievementProgressResult.skipped

    suspend fun onTreasureCollected(timestampMs: Long = System.currentTimeMillis()): AchievementProgressBatchResult =
        handle(AchievementProgressEvent.TreasureCollected(timestampMs))

    suspend fun onCellExplored(timestampMs: Long = System.currentTimeMillis()): AchievementProgressBatchResult =
        handle(AchievementProgressEvent.CellExplored(timestampMs))

    suspend fun onSecretPlaceDiscovered(timestampMs: Long = System.currentTimeMillis()): AchievementProgressBatchResult =
        handle(AchievementProgressEvent.SecretPlaceDiscovered(timestampMs))

    suspend fun onProfileSynced(timestampMs: Long = System.currentTimeMillis()): AchievementProgressBatchResult =
        handle(AchievementProgressEvent.ProfileSynced(timestampMs))

    suspend fun reconcileAll(timestampMs: Long = System.currentTimeMillis()): AchievementProgressBatchResult =
        handle(AchievementProgressEvent.FullReconcile(timestampMs))

    private suspend fun dispatchRewardsFor(
        completions: List<Achievement>,
        timestampMs: Long,
    ) {
        for (achievement in completions) {
            grantManager.tryGrantRewards(achievement, timestampMs)
        }
    }

    private fun eventTimestampMs(event: AchievementProgressEvent): Long = event.timestampMs

    private suspend fun reconcileCategoriesUnlocked(
        categories: Set<AchievementCategory>,
        timestampMs: Long,
    ): AchievementProgressBatchResult {
        if (categories.isEmpty()) return AchievementProgressBatchResult(emptyList())
        val counters = counterSource.load()
        val definitions = categories.flatMap { category ->
            AchievementRegistry.definitionsFor(category)
        }.distinctBy { it.key }
        return repository.reconcileDefinitions(
            definitions = definitions,
            counters = counters,
            timestampMs = timestampMs,
        )
    }

    companion object {
        @Volatile
        private var instance: AchievementProgressEngine? = null

        fun get(context: Context): AchievementProgressEngine =
            instance ?: synchronized(this) {
                instance ?: AchievementProgressEngine(context.applicationContext)
                    .also { instance = it }
            }
    }
}
