package com.example.roadguideapp.goldhunt.panorama.completion

import android.content.Context
import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.interaction.PanoramaHuntTapResult
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntRewardGrantManager
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetSet

/**
 * Orchestrates persistent panorama hunt progress, completion, and idempotent rewards.
 */
internal class PanoramaHuntCompletionManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val repository = PanoramaHuntCompletionRepository.get(appContext)
    private val rewardGrantManager = PanoramaHuntRewardGrantManager.get(appContext)

    suspend fun loadProgress(
        hunt: PanoramaHunt,
        targetSet: PanoramaHiddenTargetSet,
    ): PanoramaHuntProgress {
        repository.ensureCacheLoaded()
        return repository.loadProgress(hunt.huntId, targetSet)
    }

    suspend fun processTapResult(
        hunt: PanoramaHunt,
        targetSet: PanoramaHiddenTargetSet,
        result: PanoramaHuntTapResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): PanoramaHuntCompletionOutcome {
        repository.ensureCacheLoaded()
        return when (result) {
            is PanoramaHuntTapResult.Hit -> {
                val targetResult = repository.recordTargetFinding(result.target, timestampMs)
                val progress = repository.loadProgress(hunt.huntId, targetSet)
                PanoramaHuntCompletionOutcome(
                    targetRecorded = targetResult.isNew,
                    progress = progress,
                )
            }
            is PanoramaHuntTapResult.HuntComplete -> {
                val targetResult = repository.recordTargetFinding(result.lastTarget, timestampMs)
                completeHuntIfNeeded(
                    hunt = hunt,
                    targetSet = targetSet,
                    timestampMs = timestampMs,
                    targetRecorded = targetResult.isNew,
                )
            }
            else -> PanoramaHuntCompletionOutcome(
                progress = repository.loadProgress(hunt.huntId, targetSet),
            )
        }
    }

    private suspend fun completeHuntIfNeeded(
        hunt: PanoramaHunt,
        targetSet: PanoramaHiddenTargetSet,
        timestampMs: Long,
        targetRecorded: Boolean,
    ): PanoramaHuntCompletionOutcome {
        if (repository.isHuntCompletedCached(hunt.huntId)) {
            return PanoramaHuntCompletionOutcome(
                targetRecorded = targetRecorded,
                progress = repository.loadProgress(hunt.huntId, targetSet),
            )
        }
        val rewardOutcome = rewardGrantManager.resolveCompletionOutcome(hunt)
        val record = repository.recordHuntCompletion(
            hunt = hunt,
            targetSet = targetSet,
            creditsGranted = rewardOutcome.credits,
            xpGranted = rewardOutcome.xp,
            completedAtMs = timestampMs,
        )
        if (!record.isNew) {
            return PanoramaHuntCompletionOutcome(
                targetRecorded = targetRecorded,
                progress = repository.loadProgress(hunt.huntId, targetSet),
            )
        }

        val grantResult = rewardGrantManager.applyCompletionRewards(rewardOutcome, timestampMs)
        return PanoramaHuntCompletionOutcome(
            targetRecorded = targetRecorded,
            huntCompleted = true,
            creditsGranted = grantResult.creditsGranted,
            xpGranted = grantResult.xpGranted,
            storyFragmentGranted = grantResult.hooks.storyFragmentRecorded,
            achievementProgressKey = grantResult.achievementProgress?.achievementKey,
            achievementCompletionCount = grantResult.achievementProgress?.completionCount,
            legendaryRelicHookRecorded = grantResult.hooks.legendaryRelicRecorded,
            progress = repository.loadProgress(hunt.huntId, targetSet),
        )
    }

    companion object {
        @Volatile
        private var instance: PanoramaHuntCompletionManager? = null

        fun get(context: Context): PanoramaHuntCompletionManager =
            instance ?: synchronized(this) {
                instance ?: PanoramaHuntCompletionManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
