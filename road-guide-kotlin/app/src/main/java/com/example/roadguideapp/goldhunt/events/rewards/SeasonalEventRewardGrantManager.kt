package com.example.roadguideapp.goldhunt.events.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementManager
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementProgress
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgress
import com.example.roadguideapp.goldhunt.events.progress.SeasonalEventProgressManager
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntAchievementProgress
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntAchievementProgressRepository
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntAchievementProgressResult
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpAwarder
import com.example.roadguideapp.goldhunt.profile.xp.XpAwardOutcome
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.events.EventTreasureDetector

/**
 * Grants seasonal event rewards for event treasure collections.
 */
internal class SeasonalEventRewardGrantManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val goldHuntRepository by lazy { GoldHuntRepository.get(appContext) }
    private val explorerProfileRepository = ExplorerProfileRepository.get(appContext)
    private val gameplayXp = GameplayXpAwarder.get(appContext)
    private val achievementManager = SeasonalEventAchievementManager.get(appContext)
    private val achievementProgressRepository = PanoramaHuntAchievementProgressRepository.get(appContext)
    private val rewardHookRepository = SeasonalEventRewardHookRepository.get(appContext)
    private val progressManager = SeasonalEventProgressManager.get(appContext)

    suspend fun resolveTreasureOutcome(spec: TreasureSpec): SeasonalEventRewardOutcome? {
        if (!EventTreasureDetector.isEventTreasure(spec)) return null
        val explorerLevel = explorerProfileRepository.loadProfile().explorerLevel
        return SeasonalEventRewardDispatcher.dispatchTreasureCollection(
            spec = spec,
            explorerLevel = explorerLevel,
        )
    }

    suspend fun applyTreasureRewards(
        outcome: SeasonalEventRewardOutcome,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventRewardGrantResult {
        goldHuntRepository.ensureInitialized()
        val creditsGranted = goldHuntRepository.applyCreditGrant(outcome.creditGrant, timestampMs)
        val xpOutcome = gameplayXp.tryAwardSeasonalEventTreasure(
            treasureId = outcome.spec.treasureId,
            eventId = outcome.event.eventId,
            eventType = outcome.event.eventType,
            xpAwarded = outcome.xp,
            timestampMs = timestampMs,
        )
        val treasureAchievements = achievementManager.recordTreasureAchievements(
            outcome = outcome,
            timestampMs = timestampMs,
        )
        val relicProgress = if (outcome.bundle.legendaryRelicProgressIncrement > 0) {
            achievementProgressRepository.increment(
                achievementKey = outcome.bundle.legendaryRelicKey,
                increment = outcome.bundle.legendaryRelicProgressIncrement,
                timestampMs = timestampMs,
            )
        } else {
            PanoramaHuntAchievementProgressResult.skipped
        }
        val hooks = rewardHookRepository.recordTreasureHooks(outcome, timestampMs)
        val grantResult = SeasonalEventRewardGrantResult(
            creditsGranted = creditsGranted,
            xpGranted = xpOutcome.xpAwarded,
            xpOutcome = xpOutcome,
            rewardOutcome = outcome,
            participationProgress = treasureAchievements.participationProgress?.toPanoramaProgress(),
            masteryProgress = treasureAchievements.masteryProgress?.toPanoramaProgress(),
            relicProgress = relicProgress.progress,
            hooks = hooks,
        )
        val eventProgress = progressManager.recordTreasureGrant(
            outcome = outcome,
            grantResult = grantResult,
            timestampMs = timestampMs,
        )
        return grantResult.copy(eventProgress = eventProgress)
    }

    suspend fun grantForTreasureCollection(
        spec: TreasureSpec,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventRewardGrantResult? {
        val outcome = resolveTreasureOutcome(spec) ?: return null
        return applyTreasureRewards(outcome, timestampMs)
    }

    companion object {
        @Volatile
        private var instance: SeasonalEventRewardGrantManager? = null

        fun get(context: Context): SeasonalEventRewardGrantManager =
            instance ?: synchronized(this) {
                instance ?: SeasonalEventRewardGrantManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}

private fun SeasonalEventAchievementProgress.toPanoramaProgress(): PanoramaHuntAchievementProgress =
    PanoramaHuntAchievementProgress(
        achievementKey = definition.key,
        completionCount = currentCount,
        updatedAtMs = updatedAtMs,
    )

internal data class SeasonalEventRewardGrantResult(
    val creditsGranted: Int,
    val xpGranted: Long,
    val xpOutcome: XpAwardOutcome = XpAwardOutcome.Duplicate,
    val rewardOutcome: SeasonalEventRewardOutcome,
    val participationProgress: PanoramaHuntAchievementProgress? = null,
    val masteryProgress: PanoramaHuntAchievementProgress? = null,
    val relicProgress: PanoramaHuntAchievementProgress? = null,
    val hooks: SeasonalEventRewardHookRecordResult = SeasonalEventRewardHookRecordResult(),
    val eventProgress: SeasonalEventProgress? = null,
)
