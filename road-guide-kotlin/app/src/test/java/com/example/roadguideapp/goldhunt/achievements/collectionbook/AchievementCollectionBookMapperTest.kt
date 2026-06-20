package com.example.roadguideapp.goldhunt.achievements.collectionbook

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeCollection
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeEntry
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeProgress
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRarity
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournal
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalEntry
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalProgress
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalRewardPreview
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalStatus
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookEntity
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementRarityTier
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatistics
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleCollection
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleEntry
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleProgress
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRarity
import com.example.roadguideapp.goldhunt.achievements.visibility.AchievementVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCollectionBookMapperTest {
    private val achievementId = AchievementSchema.AchievementKeys.firstUnlock(
        AchievementCategory.MASTER_EXPLORER,
    )

    private val journalEntry = AchievementJournalEntry(
        achievementId = achievementId,
        title = "Master Explorer — first unlock",
        description = "Reach master explorer milestones.",
        category = AchievementCategory.MASTER_EXPLORER,
        status = AchievementJournalStatus.COMPLETED,
        visibility = AchievementVisibility.HIDDEN,
        currentValue = 1,
        targetValue = 1,
        progressFraction = 1f,
        completionDateMs = 2_000L,
        minimumExplorerLevel = 20,
        rewardPreview = AchievementJournalRewardPreview(
            credits = 50,
            xp = 120L,
            titleKey = "title_achievement_master_explorer",
            legendaryRelicKey = "legendary_relic_achievement_master_explorer",
            badgeKey = "badge_achievement_master_explorer",
            storyFragmentKey = "story_fragment_achievement_master_explorer",
        ),
    )

    private val achievement = Achievement(
        achievementId = achievementId,
        title = journalEntry.title,
        description = journalEntry.description,
        category = journalEntry.category,
        targetValue = 1,
        currentValue = 1,
        completed = true,
        completionDate = 2_000L,
        rewardCredits = 50,
        rewardXp = 120L,
        titleKey = "title_achievement_master_explorer",
        badgeKey = "badge_achievement_master_explorer",
        legendaryRelicKey = "legendary_relic_achievement_master_explorer",
    )

    @Test
    fun build_marksStoryFragmentAndRelicEarnedFromHooks() {
        val book = AchievementCollectionBookMapper.build(
            journal = AchievementJournal(
                entries = listOf(journalEntry),
                progress = AchievementJournalProgress(
                    trackableCount = 1,
                    catalogCount = 1,
                    completedCount = 1,
                    incompleteCount = 0,
                    lockedCount = 0,
                    hiddenCount = 0,
                ),
            ),
            badges = emptyBadgeCollection(),
            titles = emptyTitleCollection(),
            statistics = AchievementStatistics(
                totalCount = 1,
                completedCount = 1,
                rareCompletedCount = 0,
                legendaryCompletedCount = 1,
                creditsEarned = 50,
                xpEarned = 120L,
            ),
            explorerLevel = 22,
            achievementsByKey = mapOf(achievementId to achievement),
            rewardHooks = listOf(
                hook(AchievementRewardSchema.HookTypes.STORY_FRAGMENT, "story_fragment_achievement_master_explorer"),
                hook(AchievementRewardSchema.HookTypes.LEGENDARY_RELIC, "legendary_relic_achievement_master_explorer"),
                hook(AchievementRewardSchema.HookTypes.TITLE, "title_achievement_master_explorer"),
                hook(AchievementRewardSchema.HookTypes.BADGE, "badge_achievement_master_explorer"),
            ),
        )

        val entry = book.achievementEntries.single()
        assertTrue(entry.storyFragmentEarned)
        assertTrue(entry.legendaryRelicEarned)
        assertTrue(entry.titleUnlocked)
        assertTrue(entry.badgeUnlocked)
        assertEquals(AchievementRarityTier.LEGENDARY, entry.rarityTier)
        assertEquals(1, book.progress.storyFragmentsEarned)
        assertEquals(1, book.progress.legendaryRelicsEarned)
        assertEquals(22, book.progress.explorerLevel)
    }

    @Test
    fun buildHookFlagsByAchievement_groupsHookTypes() {
        val flags = AchievementCollectionBookMapper.buildHookFlagsByAchievement(
            listOf(
                hook(AchievementRewardSchema.HookTypes.STORY_FRAGMENT, "story_a"),
                hook(AchievementRewardSchema.HookTypes.BADGE, "badge_a"),
            ),
        ).getValue(achievementId)

        assertTrue(flags.storyFragmentEarned)
        assertTrue(flags.badgeEarned)
    }

    private fun hook(type: String, key: String) = AchievementRewardHookEntity(
        eventId = "event:$type:$achievementId",
        achievementId = achievementId,
        hookType = type,
        hookKey = key,
        grantedAtMs = 1_000L,
    )

    private fun emptyBadgeCollection() = AchievementBadgeCollection(
        entries = listOf(
            AchievementBadgeEntry(
                badgeKey = "badge_achievement_master_explorer",
                achievementId = achievementId,
                title = achievement.title,
                iconKey = "achievement_master_explorer",
                rarity = AchievementBadgeRarity.LEGENDARY,
                unlockDateMs = 1_000L,
            ),
        ),
        progress = AchievementBadgeProgress(
            totalCount = 1,
            unlockedCount = 1,
            lockedCount = 0,
            legendaryUnlockedCount = 1,
        ),
    )

    private fun emptyTitleCollection() = AchievementTitleCollection(
        entries = listOf(
            AchievementTitleEntry(
                titleKey = "title_achievement_master_explorer",
                achievementId = achievementId,
                displayTitle = achievement.title,
                rarity = AchievementTitleRarity.LEGENDARY,
                unlockDateMs = 1_000L,
                isActive = true,
            ),
        ),
        progress = AchievementTitleProgress(
            totalCount = 1,
            unlockedCount = 1,
            lockedCount = 0,
            activeCount = 1,
        ),
        activeTitleKey = "title_achievement_master_explorer",
    )
}
