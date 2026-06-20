package com.example.roadguideapp.goldhunt.events.achievements

import android.content.Context
import com.example.roadguideapp.goldhunt.events.SeasonalEvent
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardHookRepository
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardOutcome
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpAwarder
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository

internal class SeasonalEventAchievementGrantManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val goldHuntRepository by lazy { GoldHuntRepository.get(appContext) }
    private val gameplayXp = GameplayXpAwarder.get(appContext)
    private val achievementRepository = SeasonalEventAchievementRepository.get(appContext)
    private val completionRepository = SeasonalEventAchievementCompletionRepository.get(appContext)
    private val rewardHookRepository = SeasonalEventRewardHookRepository.get(appContext)

    suspend fun recordTreasureAchievements(
        outcome: SeasonalEventRewardOutcome,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventTreasureAchievementResult {
        val definitions = SeasonalEventAchievementCatalog.eventAchievements(outcome.event.eventType)
        val participationDefinition = definitions.first {
            it.tier == SeasonalEventAchievementTier.FIRST_PARTICIPATION
        }
        val masteryDefinition = definitions.first {
            it.tier == SeasonalEventAchievementTier.MASTERY
        }
        return SeasonalEventTreasureAchievementResult(
            participationProgress = achievementRepository.increment(
                definition = participationDefinition,
                timestampMs = timestampMs,
            ),
            masteryProgress = achievementRepository.increment(
                definition = masteryDefinition,
                timestampMs = timestampMs,
            ),
        )
    }

    suspend fun tryGrantCompletionRewards(
        event: SeasonalEvent,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventCompletionAchievementResult {
        if (completionRepository.isCompletionGranted(event.eventId)) {
            return SeasonalEventCompletionAchievementResult.skipped
        }
        val outcome = SeasonalEventCompletionRewardDispatcher.dispatch(event)
        val completionDefinition = SeasonalEventAchievementCatalog.eventAchievements(event.eventType)
            .first { it.tier == SeasonalEventAchievementTier.COMPLETION }
        val record = completionRepository.recordCompletion(
            SeasonalEventAchievementCompletionEntity(
                eventId = event.eventId,
                eventType = event.eventType.id,
                cycleYear = event.cycleYear,
                achievementKey = outcome.achievementKey,
                creditsGranted = outcome.creditGrant.amount,
                xpGranted = outcome.xp,
                category = SeasonalEventCategory.forType(event.eventType).name,
                completedAtMs = timestampMs,
            ),
        )
        if (!record.isNew) {
            return SeasonalEventCompletionAchievementResult.skipped
        }

        goldHuntRepository.ensureInitialized()
        val creditsGranted = goldHuntRepository.applyCreditGrant(outcome.creditGrant, timestampMs)
        val xpOutcome = gameplayXp.tryAwardSeasonalEventCompletion(
            eventId = event.eventId,
            eventType = event.eventType,
            xpAwarded = outcome.xp,
            timestampMs = timestampMs,
        )
        val completionProgress = achievementRepository.increment(
            definition = completionDefinition,
            timestampMs = timestampMs,
        )
        rewardHookRepository.recordCompletionHook(
            event = event,
            achievementKey = outcome.achievementKey,
            timestampMs = timestampMs,
        )
        val category = SeasonalEventCategory.forType(event.eventType)
        val categoryProgress = SeasonalEventCategoryAchievementEvaluator.evaluateCategoryCollectors(
            category = category,
            repository = achievementRepository,
            timestampMs = timestampMs,
        )
        return SeasonalEventCompletionAchievementResult(
            eventId = event.eventId,
            completionProgress = completionProgress,
            categoryProgress = categoryProgress,
            creditsGranted = creditsGranted,
            xpGranted = xpOutcome.xpAwarded,
            isNewGrant = true,
        )
    }

    companion object {
        @Volatile
        private var instance: SeasonalEventAchievementGrantManager? = null

        fun get(context: Context): SeasonalEventAchievementGrantManager =
            instance ?: synchronized(this) {
                instance ?: SeasonalEventAchievementGrantManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
