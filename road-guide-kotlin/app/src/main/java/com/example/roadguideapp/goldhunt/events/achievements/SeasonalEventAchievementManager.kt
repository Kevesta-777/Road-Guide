package com.example.roadguideapp.goldhunt.events.achievements

import android.content.Context
import com.example.roadguideapp.goldhunt.events.SeasonalEvent
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardOutcome

/**
 * Orchestrates seasonal event achievement progress and completion rewards.
 */
internal class SeasonalEventAchievementManager private constructor(context: Context) {
    private val achievementRepository = SeasonalEventAchievementRepository.get(context.applicationContext)
    private val grantManager = SeasonalEventAchievementGrantManager.get(context.applicationContext)

    suspend fun recordTreasureAchievements(
        outcome: SeasonalEventRewardOutcome,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventTreasureAchievementResult =
        grantManager.recordTreasureAchievements(outcome, timestampMs)

    suspend fun tryGrantCompletionRewards(
        event: SeasonalEvent,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventCompletionAchievementResult =
        grantManager.tryGrantCompletionRewards(event, timestampMs)

    suspend fun loadEventSnapshot(type: SeasonalEventType): SeasonalEventAchievementProgressSnapshot =
        achievementRepository.loadEventSnapshot(type)

    suspend fun loadCategorySnapshot(
        category: SeasonalEventCategory,
    ): List<SeasonalEventAchievementProgress> =
        achievementRepository.loadCategorySnapshot(category)

    companion object {
        @Volatile
        private var instance: SeasonalEventAchievementManager? = null

        fun get(context: Context): SeasonalEventAchievementManager =
            instance ?: synchronized(this) {
                instance ?: SeasonalEventAchievementManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
