package com.example.roadguideapp.goldhunt.panorama.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpAwarder
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository

/**
 * Applies panorama hunt completion rewards: credits, XP, achievement progress, and reward hooks.
 */
internal class PanoramaHuntRewardGrantManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val explorerProfileRepository = ExplorerProfileRepository.get(appContext)
    private val goldHuntRepository = GoldHuntRepository.get(appContext)
    private val gameplayXp = GameplayXpAwarder.get(appContext)
    private val achievementProgressRepository = PanoramaHuntAchievementProgressRepository.get(appContext)
    private val rewardHookRepository = PanoramaHuntRewardHookRepository.get(appContext)

    suspend fun resolveCompletionOutcome(hunt: PanoramaHunt): PanoramaHuntRewardOutcome {
        val explorerLevel = explorerProfileRepository.loadProfile().explorerLevel
        return PanoramaHuntRewardDispatcher.dispatchCompletion(hunt, explorerLevel)
    }

    suspend fun applyCompletionRewards(
        outcome: PanoramaHuntRewardOutcome,
        timestampMs: Long = System.currentTimeMillis(),
    ): PanoramaHuntRewardGrantResult {
        goldHuntRepository.ensureInitialized()
        val creditsGranted = goldHuntRepository.applyCreditGrant(outcome.creditGrant, timestampMs)
        val xpOutcome = gameplayXp.tryAwardPanoramaHuntCompletion(
            hunt = outcome.hunt,
            xpAwarded = outcome.xp,
            timestampMs = timestampMs,
        )
        val achievementProgress = outcome.bundle.achievementKey?.let { key ->
            achievementProgressRepository.increment(
                achievementKey = key,
                increment = outcome.bundle.achievementProgressIncrement,
                timestampMs = timestampMs,
            )
        } ?: PanoramaHuntAchievementProgressResult.skipped
        val hooks = rewardHookRepository.recordCompletionHooks(outcome, timestampMs)
        return PanoramaHuntRewardGrantResult(
            creditsGranted = creditsGranted,
            xpGranted = xpOutcome.xpAwarded,
            rewardOutcome = outcome,
            achievementProgress = achievementProgress.progress,
            hooks = hooks,
        )
    }

    companion object {
        @Volatile
        private var instance: PanoramaHuntRewardGrantManager? = null

        fun get(context: Context): PanoramaHuntRewardGrantManager =
            instance ?: synchronized(this) {
                instance ?: PanoramaHuntRewardGrantManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}

internal data class PanoramaHuntRewardGrantResult(
    val creditsGranted: Int,
    val xpGranted: Long,
    val rewardOutcome: PanoramaHuntRewardOutcome,
    val achievementProgress: PanoramaHuntAchievementProgress? = null,
    val hooks: PanoramaHuntRewardHookRecordResult = PanoramaHuntRewardHookRecordResult(),
)
