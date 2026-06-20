package com.example.roadguideapp.goldhunt.achievements.chain

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRepository
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookRepository
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRepository

/**
 * Idempotent dispatcher for achievement chain completion unlocks.
 */
internal class AchievementChainUnlockManager private constructor(context: Context) {
    private val hookRepository = AchievementRewardHookRepository.get(context.applicationContext)
    private val badgeRepository = AchievementBadgeRepository.get(context.applicationContext)
    private val titleRepository = AchievementTitleRepository.get(context.applicationContext)

    suspend fun dispatchChainUnlocks(
        sourceAchievement: Achievement,
        timestampMs: Long = System.currentTimeMillis(),
    ): AchievementChainUnlockResult {
        if (!sourceAchievement.completed) {
            return AchievementChainUnlockResult(sourceAchievementId = sourceAchievement.achievementId)
        }

        val unlocks = AchievementChainCatalog.rewardsFor(sourceAchievement.achievementId)
        if (unlocks.isEmpty()) {
            return AchievementChainUnlockResult(sourceAchievementId = sourceAchievement.achievementId)
        }

        val operations = AchievementChainUnlockDispatcher.dispatch(
            sourceAchievement = sourceAchievement,
            unlocks = unlocks,
            timestampMs = timestampMs,
        )

        var achievementUnlocks = 0
        var badgeUnlocks = 0
        var titleUnlocks = 0
        var storyFragments = 0
        var legendaryRelics = 0

        for (operation in operations) {
            val recorded = hookRepository.recordChainHook(operation.hook)
            if (!recorded) continue
            when (operation) {
                is AchievementChainUnlockOperation.AchievementUnlock ->
                    achievementUnlocks++
                is AchievementChainUnlockOperation.BadgeUnlock -> {
                    badgeUnlocks += if (
                        badgeRepository.recordUnlock(
                            badgeKey = operation.badgeKey,
                            achievementId = sourceAchievement.achievementId,
                            unlockedAtMs = timestampMs,
                        ) != null
                    ) {
                        1
                    } else {
                        0
                    }
                }
                is AchievementChainUnlockOperation.TitleUnlock -> {
                    titleUnlocks += if (
                        titleRepository.recordUnlock(
                            titleKey = operation.titleKey,
                            achievementId = sourceAchievement.achievementId,
                            unlockedAtMs = timestampMs,
                        ) != null
                    ) {
                        1
                    } else {
                        0
                    }
                }
                is AchievementChainUnlockOperation.StoryFragmentUnlock -> storyFragments++
                is AchievementChainUnlockOperation.LegendaryRelicUnlock -> legendaryRelics++
            }
        }

        return AchievementChainUnlockResult(
            sourceAchievementId = sourceAchievement.achievementId,
            achievementUnlocksRecorded = achievementUnlocks,
            badgeUnlocksRecorded = badgeUnlocks,
            titleUnlocksRecorded = titleUnlocks,
            storyFragmentsRecorded = storyFragments,
            legendaryRelicsRecorded = legendaryRelics,
        )
    }

    companion object {
        @Volatile
        private var instance: AchievementChainUnlockManager? = null

        fun get(context: Context): AchievementChainUnlockManager =
            instance ?: synchronized(this) {
                instance ?: AchievementChainUnlockManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
