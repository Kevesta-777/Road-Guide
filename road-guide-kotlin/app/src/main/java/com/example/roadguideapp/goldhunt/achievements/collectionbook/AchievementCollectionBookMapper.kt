package com.example.roadguideapp.goldhunt.achievements.collectionbook

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeCollection
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournal
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalEntry
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookEntity
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementRarityTier
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatistics
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatisticsCalculator
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleCollection

internal object AchievementCollectionBookMapper {
    fun build(
        journal: AchievementJournal,
        badges: AchievementBadgeCollection,
        titles: AchievementTitleCollection,
        statistics: AchievementStatistics,
        explorerLevel: Int,
        achievementsByKey: Map<String, Achievement>,
        rewardHooks: List<AchievementRewardHookEntity>,
    ): AchievementCollectionBook {
        val hookFlagsByAchievement = buildHookFlagsByAchievement(rewardHooks)
        val badgeByAchievement = badges.entries.associateBy { it.achievementId }
        val titleByAchievement = titles.entries.associateBy { it.achievementId }
        val achievementEntries = journal.entries.map { entry ->
            toAchievementEntry(
                entry = entry,
                achievement = achievementsByKey[entry.achievementId],
                hookFlags = hookFlagsByAchievement[entry.achievementId] ?: AchievementHookEarnedFlags.EMPTY,
                badgeUnlocked = badgeByAchievement[entry.achievementId]?.isUnlocked == true,
                titleUnlocked = titleByAchievement[entry.achievementId]?.isUnlocked == true,
            )
        }
        return AchievementCollectionBook(
            achievementEntries = achievementEntries,
            badgeEntries = badges.entries,
            titleEntries = titles.entries,
            progress = buildProgress(
                journal = journal,
                badges = badges,
                titles = titles,
                statistics = statistics,
                explorerLevel = explorerLevel,
                achievementEntries = achievementEntries,
            ),
        )
    }

    fun buildHookFlagsByAchievement(
        hooks: List<AchievementRewardHookEntity>,
    ): Map<String, AchievementHookEarnedFlags> {
        val grouped = hooks.groupBy { it.achievementId }
        return grouped.mapValues { (_, achievementHooks) ->
            AchievementHookEarnedFlags(
                storyFragmentEarned = achievementHooks.any {
                    it.hookType == AchievementRewardSchema.HookTypes.STORY_FRAGMENT
                },
                legendaryRelicEarned = achievementHooks.any {
                    it.hookType == AchievementRewardSchema.HookTypes.LEGENDARY_RELIC
                },
                titleEarned = achievementHooks.any {
                    it.hookType == AchievementRewardSchema.HookTypes.TITLE
                },
                badgeEarned = achievementHooks.any {
                    it.hookType == AchievementRewardSchema.HookTypes.BADGE
                },
            )
        }
    }

    private fun toAchievementEntry(
        entry: AchievementJournalEntry,
        achievement: Achievement?,
        hookFlags: AchievementHookEarnedFlags,
        badgeUnlocked: Boolean,
        titleUnlocked: Boolean,
    ): AchievementCollectionBookEntry {
        val preview = entry.rewardPreview
        val rarityTier = achievement?.let { AchievementStatisticsCalculator.rarityTier(it) }
            ?: AchievementRarityTier.COMMON
        return AchievementCollectionBookEntry(
            achievementId = entry.achievementId,
            displayName = entry.title,
            category = entry.category,
            status = entry.status,
            rarityTier = rarityTier,
            minimumExplorerLevel = entry.minimumExplorerLevel,
            currentValue = entry.currentValue,
            targetValue = entry.targetValue,
            progressFraction = entry.progressFraction,
            completionDateMs = entry.completionDateMs,
            badgeKey = preview.badgeKey,
            badgeUnlocked = badgeUnlocked || hookFlags.badgeEarned,
            titleKey = preview.titleKey,
            titleUnlocked = titleUnlocked || hookFlags.titleEarned,
            storyFragmentKey = preview.storyFragmentKey,
            storyFragmentEarned = hookFlags.storyFragmentEarned,
            legendaryRelicKey = preview.legendaryRelicKey,
            legendaryRelicEarned = hookFlags.legendaryRelicEarned,
        )
    }

    private fun buildProgress(
        journal: AchievementJournal,
        badges: AchievementBadgeCollection,
        titles: AchievementTitleCollection,
        statistics: AchievementStatistics,
        explorerLevel: Int,
        achievementEntries: List<AchievementCollectionBookEntry>,
    ): AchievementCollectionBookProgress = AchievementCollectionBookProgress(
        explorerLevel = explorerLevel,
        catalogCount = journal.progress.catalogCount,
        completedCount = statistics.completedCount,
        rareCompletedCount = statistics.rareCompletedCount,
        legendaryCompletedCount = statistics.legendaryCompletedCount,
        lockedCount = journal.progress.lockedCount,
        hiddenCount = journal.progress.hiddenCount,
        badgesUnlockedCount = badges.progress.unlockedCount,
        badgesTotalCount = badges.progress.totalCount,
        titlesUnlockedCount = titles.progress.unlockedCount,
        titlesTotalCount = titles.progress.totalCount,
        activeTitleKey = titles.activeTitleKey,
        storyFragmentsEarned = achievementEntries.count { it.storyFragmentEarned },
        legendaryRelicsEarned = achievementEntries.count { it.legendaryRelicEarned },
        creditsEarned = statistics.creditsEarned,
        xpEarned = statistics.xpEarned,
    )
}
