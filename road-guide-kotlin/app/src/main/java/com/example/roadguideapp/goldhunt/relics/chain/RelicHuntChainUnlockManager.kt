package com.example.roadguideapp.goldhunt.relics.chain

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRepository
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookRepository
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRepository
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardHookRepository

/**
 * Idempotent dispatcher for relic hunt chain completion unlocks.
 */
internal class RelicHuntChainUnlockManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val relicHookRepository = LegendaryRelicRewardHookRepository.get(appContext)
    private val achievementHookRepository = AchievementRewardHookRepository.get(appContext)
    private val badgeRepository = AchievementBadgeRepository.get(appContext)
    private val titleRepository = AchievementTitleRepository.get(appContext)
    suspend fun dispatchChainUnlocks(
        sourceRelic: LegendaryRelic,
        timestampMs: Long = System.currentTimeMillis(),
    ): RelicHuntChainUnlockResult {
        if (!sourceRelic.completed) {
            return RelicHuntChainUnlockResult(sourceRelicId = sourceRelic.relicId)
        }

        val unlocks = RelicHuntChainCatalog.rewardsFor(sourceRelic.relicId)
        if (unlocks.isEmpty()) {
            return RelicHuntChainUnlockResult(sourceRelicId = sourceRelic.relicId)
        }

        val operations = RelicHuntChainUnlockDispatcher.dispatch(
            sourceRelic = sourceRelic,
            unlocks = unlocks,
            timestampMs = timestampMs,
        )

        var achievementUnlocks = 0
        var badgeUnlocks = 0
        var titleUnlocks = 0
        var storyFragments = 0
        var legendaryRelics = 0

        for (operation in operations) {
            when (operation) {
                is RelicHuntChainUnlockOperation.AchievementUnlock -> {
                    val relicRecorded = relicHookRepository.recordChainHook(operation.relicHook)
                    val achievementRecorded =
                        achievementHookRepository.recordChainHook(operation.achievementHook)
                    if (relicRecorded || achievementRecorded) {
                        achievementUnlocks++
                    }
                }
                is RelicHuntChainUnlockOperation.BadgeUnlock -> {
                    if (!relicHookRepository.recordChainHook(operation.hook)) continue
                    badgeUnlocks += if (
                        badgeRepository.recordUnlock(
                            badgeKey = operation.badgeKey,
                            achievementId = sourceRelic.relicId,
                            unlockedAtMs = timestampMs,
                        ) != null
                    ) {
                        1
                    } else {
                        0
                    }
                }
                is RelicHuntChainUnlockOperation.TitleUnlock -> {
                    if (!relicHookRepository.recordChainHook(operation.hook)) continue
                    titleUnlocks += if (
                        titleRepository.recordUnlock(
                            titleKey = operation.titleKey,
                            achievementId = sourceRelic.relicId,
                            unlockedAtMs = timestampMs,
                        ) != null
                    ) {
                        1
                    } else {
                        0
                    }
                }
                is RelicHuntChainUnlockOperation.StoryFragmentUnlock -> {
                    if (relicHookRepository.recordChainHook(operation.hook)) {
                        storyFragments++
                    }
                }
                is RelicHuntChainUnlockOperation.LegendaryRelicUnlock -> {
                    if (relicHookRepository.recordChainHook(operation.hook)) {
                        legendaryRelics++
                    }
                }
            }
        }

        return RelicHuntChainUnlockResult(
            sourceRelicId = sourceRelic.relicId,
            achievementUnlocksRecorded = achievementUnlocks,
            badgeUnlocksRecorded = badgeUnlocks,
            titleUnlocksRecorded = titleUnlocks,
            storyFragmentsRecorded = storyFragments,
            legendaryRelicsRecorded = legendaryRelics,
        )
    }

    companion object {
        @Volatile
        private var instance: RelicHuntChainUnlockManager? = null

        fun get(context: Context): RelicHuntChainUnlockManager =
            instance ?: synchronized(this) {
                instance ?: RelicHuntChainUnlockManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
