package com.example.roadguideapp.goldhunt.events.progress

import android.content.Context
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementManager
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookRepository
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardGrantResult
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardOutcome
import com.example.roadguideapp.goldhunt.events.statistics.SeasonalEventStatistics
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository

/**
 * Persists per-event and lifetime explorer profile seasonal progress.
 */
internal class SeasonalEventProgressManager private constructor(context: Context) {
    private val progressRepository = SeasonalEventProgressRepository.get(context.applicationContext)
    private val explorerProfileRepository = ExplorerProfileRepository.get(context.applicationContext)
    private val achievementManager = SeasonalEventAchievementManager.get(context.applicationContext)
    private val collectionBookRepository = SeasonalEventCollectionBookRepository.get(context.applicationContext)

    suspend fun load(eventId: String): SeasonalEventProgress? =
        progressRepository.load(eventId)

    suspend fun loadAll(): List<SeasonalEventProgress> =
        progressRepository.loadAll()

    suspend fun loadLifetimeStatistics(): SeasonalEventStatistics =
        explorerProfileRepository.loadProfile().seasonalEventStatistics

    suspend fun recordTreasureGrant(
        outcome: SeasonalEventRewardOutcome,
        grantResult: SeasonalEventRewardGrantResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventProgress {
        val event = outcome.event
        val existing = progressRepository.load(event.eventId)
        val fragmentDelta = if (grantResult.hooks.storyFragmentRecorded) 1 else 0
        val nextTreasures = (existing?.treasuresCollected ?: 0) + 1
        val nextXp = (existing?.xpEarned ?: 0L) + grantResult.xpGranted.coerceAtLeast(0L)
        val nextCredits = (existing?.creditsEarned ?: 0) + grantResult.creditsGranted.coerceAtLeast(0)
        val nextFragments = (existing?.fragmentsEarned ?: 0) + fragmentDelta
        val completionPercent = SeasonalEventProgressCalculator.completionPercent(
            eventType = event.eventType,
            treasuresCollected = nextTreasures,
            fragmentsEarned = nextFragments,
        )
        val wasCompleted = existing?.isCompleted == true
        val isCompleted = SeasonalEventProgressCalculator.isCompleted(completionPercent)
        val progress = SeasonalEventProgress(
            eventId = event.eventId,
            eventType = event.eventType,
            cycleYear = event.cycleYear,
            treasuresCollected = nextTreasures,
            xpEarned = nextXp,
            creditsEarned = nextCredits,
            fragmentsEarned = nextFragments,
            completionPercent = completionPercent,
            isCompleted = isCompleted,
            updatedAtMs = timestampMs,
        )
        val saved = progressRepository.save(progress, timestampMs)
        val isFirstJoin = existing == null || existing.treasuresCollected <= 0
        val completedCycleDelta = if (!wasCompleted && isCompleted) 1 else 0
        val allProgress = progressRepository.loadAll()
        syncExplorerProfile(
            isFirstJoin = isFirstJoin,
            grantResult = grantResult,
            completedCycleDelta = completedCycleDelta,
            allProgress = allProgress,
            timestampMs = timestampMs,
        )
        collectionBookRepository.syncFromTreasureGrant(
            event = event,
            progress = saved,
            grantResult = grantResult,
            isFirstJoin = isFirstJoin,
            timestampMs = timestampMs,
        )
        if (!wasCompleted && isCompleted) {
            val completionResult = achievementManager.tryGrantCompletionRewards(event, timestampMs)
            collectionBookRepository.syncFromCompletion(
                event = event,
                progress = saved,
                completionResult = completionResult,
                timestampMs = timestampMs,
            )
        }
        return saved
    }

    private suspend fun syncExplorerProfile(
        isFirstJoin: Boolean,
        grantResult: SeasonalEventRewardGrantResult,
        completedCycleDelta: Int,
        allProgress: List<SeasonalEventProgress>,
        timestampMs: Long,
    ) {
        explorerProfileRepository.updateProfile(timestampMs) { profile ->
            val nextStats = profile.seasonalEventStatistics.withTreasureGrant(
                isFirstJoin = isFirstJoin,
                grantResult = grantResult,
                completedCycleDelta = completedCycleDelta,
                allProgress = allProgress,
            )
            profile.copy(seasonalEventStatistics = nextStats)
        }
    }

    companion object {
        @Volatile
        private var instance: SeasonalEventProgressManager? = null

        fun get(context: Context): SeasonalEventProgressManager =
            instance ?: synchronized(this) {
                instance ?: SeasonalEventProgressManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
