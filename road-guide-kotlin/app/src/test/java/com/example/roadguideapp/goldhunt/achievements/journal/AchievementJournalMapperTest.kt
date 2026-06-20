package com.example.roadguideapp.goldhunt.achievements.journal

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.achievements.visibility.AchievementVisibilitySchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementJournalMapperTest {
    private val definition = AchievementDefinition(
        key = "achievement:panorama:first_unlock",
        category = AchievementCategory.PANORAMA,
        displayName = "Panorama — first unlock",
        targetCount = 1,
        minExplorerLevel = 7,
        storyFragmentKey = "story_fragment_achievement_panorama",
        legendaryRelicKey = "legendary_relic_achievement_panorama",
    )

    private val achievement = Achievement(
        achievementId = definition.key,
        title = definition.displayName,
        description = "Complete panorama hunt families and hidden symbol challenges.",
        category = definition.category,
        targetValue = 1,
        currentValue = 0,
        completed = false,
        completionDate = null,
        rewardCredits = 32,
        rewardXp = 78L,
        legendaryRelicKey = definition.legendaryRelicKey,
        titleKey = "title_achievement_panorama",
        badgeKey = "badge_achievement_panorama",
    )

    @Test
    fun toEntry_buildsRewardPreviewForVisibleAchievements() {
        val secretPlaceKey = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.SECRET_PLACE)
        val entry = AchievementJournalMapper.toEntry(
            definition = definition,
            achievement = achievement,
            explorerLevel = 10,
            achievementsByKey = mapOf(
                definition.key to achievement,
                secretPlaceKey to Achievement(
                    achievementId = secretPlaceKey,
                    title = "Secret place",
                    description = "Discover a secret place.",
                    category = AchievementCategory.SECRET_PLACE,
                    targetValue = 1,
                    currentValue = 1,
                    completed = true,
                    completionDate = 1_000L,
                    rewardCredits = 25,
                    rewardXp = 60L,
                ),
            ),
        )

        assertEquals(AchievementJournalStatus.INCOMPLETE, entry.status)
        assertEquals(32, entry.rewardPreview.credits)
        assertEquals(78L, entry.rewardPreview.xp)
        assertTrue(entry.rewardPreview.hasTitle)
        assertTrue(entry.rewardPreview.hasRelic)
        assertTrue(entry.rewardPreview.hasStoryFragment)
    }

    @Test
    fun buildJournal_aggregatesStatusCounts() {
        val completed = achievement.copy(
            achievementId = "achievement:treasure:count_100",
            title = "Treasures collected — 100",
            category = AchievementCategory.TREASURE,
            targetValue = 100,
            currentValue = 100,
            completed = true,
            completionDate = 1_000L,
        )
        val definitions = listOf(
            definition,
            AchievementDefinition(
                key = "achievement:treasure:count_100",
                category = AchievementCategory.TREASURE,
                displayName = "Treasures collected — 100",
                targetCount = 100,
            ),
        )
        val secretPlaceKey = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.SECRET_PLACE)
        val journal = AchievementJournalMapper.buildJournal(
            definitions = definitions,
            achievementsByKey = mapOf(
                definition.key to achievement,
                completed.achievementId to completed,
                secretPlaceKey to Achievement(
                    achievementId = secretPlaceKey,
                    title = "Secret place",
                    description = "Discover a secret place.",
                    category = AchievementCategory.SECRET_PLACE,
                    targetValue = 1,
                    currentValue = 1,
                    completed = true,
                    completionDate = 1_000L,
                    rewardCredits = 25,
                    rewardXp = 60L,
                ),
            ),
            explorerLevel = 10,
        )

        assertEquals(2, journal.progress.trackableCount)
        assertEquals(1, journal.progress.completedCount)
        assertEquals(1, journal.progress.incompleteCount)
        assertEquals(0.5f, journal.progress.completionFraction, 0.0001f)
    }

    @Test
    fun buildJournal_masksHiddenAchievementsUntilCompleted() {
        val hiddenDefinition = AchievementDefinition(
            key = "achievement:treasure:count_500",
            category = AchievementCategory.TREASURE,
            displayName = "Treasures collected — 500",
            targetCount = 500,
        )
        val hiddenAchievement = Achievement(
            achievementId = hiddenDefinition.key,
            title = hiddenDefinition.displayName,
            description = "Collect 500 treasures.",
            category = hiddenDefinition.category,
            targetValue = 500,
            currentValue = 120,
            completed = false,
            completionDate = null,
            rewardCredits = 50,
            rewardXp = 120L,
        )
        val entry = AchievementJournalMapper.toEntry(
            definition = hiddenDefinition,
            achievement = hiddenAchievement,
            explorerLevel = 10,
        )

        assertEquals(AchievementJournalStatus.HIDDEN, entry.status)
        assertEquals(AchievementVisibilitySchema.Placeholders.HIDDEN_TITLE, entry.title)
        assertEquals(0, entry.rewardPreview.credits)
    }

    @Test
    fun buildJournal_excludesSecretAchievementsUntilCompleted() {
        val secretDefinition = AchievementDefinition(
            key = "achievement:treasure:count_1000",
            category = AchievementCategory.TREASURE,
            displayName = "Treasures collected — 1000",
            targetCount = 1000,
        )
        val secretAchievement = Achievement(
            achievementId = secretDefinition.key,
            title = secretDefinition.displayName,
            description = "Collect 1000 treasures.",
            category = secretDefinition.category,
            targetValue = 1000,
            currentValue = 900,
            completed = false,
            completionDate = null,
            rewardCredits = 100,
            rewardXp = 250L,
        )
        val journal = AchievementJournalMapper.buildJournal(
            definitions = listOf(secretDefinition),
            achievementsByKey = mapOf(secretDefinition.key to secretAchievement),
            explorerLevel = 10,
        )

        assertTrue(journal.entries.isEmpty())
        assertEquals(1, journal.progress.undiscoveredSecretCount)
        assertEquals(1, journal.progress.catalogCount)
    }
}
