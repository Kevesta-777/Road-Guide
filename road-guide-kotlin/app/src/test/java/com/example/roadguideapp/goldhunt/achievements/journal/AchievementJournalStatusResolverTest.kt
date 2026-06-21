package com.example.roadguideapp.goldhunt.achievements.journal

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.achievements.chain.AchievementChainResolver
import com.example.roadguideapp.goldhunt.achievements.visibility.AchievementVisibility
import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementJournalStatusResolverTest {
    private fun definition(
        key: String,
        category: AchievementCategory = AchievementCategory.TREASURE,
        minLevel: Int = 1,
        targetCount: Int = 100,
    ) = AchievementDefinition(
        key = key,
        category = category,
        displayName = key,
        targetCount = targetCount,
        minExplorerLevel = minLevel,
    )

    private fun achievement(
        key: String,
        category: AchievementCategory = AchievementCategory.TREASURE,
        currentValue: Int = 0,
        targetValue: Int = 100,
        completed: Boolean = false,
    ) = Achievement(
        achievementId = key,
        title = key,
        description = "Test",
        category = category,
        targetValue = targetValue,
        currentValue = currentValue,
        completed = completed,
        completionDate = if (completed) 1_000L else null,
        rewardCredits = 25,
        rewardXp = 60L,
    )

    @Test
    fun resolve_marksCompletedAchievements() {
        val key = "achievement:treasure:count_100"
        val result = AchievementJournalStatusResolver.resolve(
            definition = definition(key),
            achievement = achievement(key, currentValue = 100, completed = true),
            explorerLevel = 5,
        )
        assertEquals(AchievementJournalStatus.COMPLETED, result)
    }

    @Test
    fun resolve_marksLockedWhenExplorerLevelTooLow() {
        val key = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.MASTER_EXPLORER)
        val result = AchievementJournalStatusResolver.resolve(
            definition = definition(
                key = key,
                category = AchievementCategory.MASTER_EXPLORER,
                minLevel = 20,
            ),
            achievement = achievement(key, category = AchievementCategory.MASTER_EXPLORER),
            explorerLevel = 10,
            visibility = AchievementVisibility.HIDDEN,
        )
        assertEquals(AchievementJournalStatus.LOCKED, result)
    }

    @Test
    fun resolve_hidesMilestone500UntilCompleted() {
        val count500 = "achievement:treasure:count_500"
        val result = AchievementJournalStatusResolver.resolve(
            definition = definition(count500, targetCount = 500),
            achievement = achievement(count500, targetValue = 500, currentValue = 120),
            explorerLevel = 10,
            visibility = AchievementVisibility.HIDDEN,
        )
        assertEquals(AchievementJournalStatus.HIDDEN, result)
    }

    @Test
    fun resolve_revealsMilestone500OnCompletion() {
        val count500 = "achievement:treasure:count_500"
        val result = AchievementJournalStatusResolver.resolve(
            definition = definition(count500, targetCount = 500),
            achievement = achievement(
                count500,
                targetValue = 500,
                currentValue = 500,
                completed = true,
            ),
            explorerLevel = 10,
            visibility = AchievementVisibility.HIDDEN,
        )
        assertEquals(AchievementJournalStatus.COMPLETED, result)
    }

    @Test
    fun resolve_marksChainLockedWhenParentIncomplete() {
        val parentKey = "achievement:treasure:count_100"
        val childKey = "achievement:treasure:count_500"
        val chainContext = AchievementChainResolver.buildContext(
            achievementsByKey = mapOf(
                parentKey to achievement(parentKey, currentValue = 40),
                childKey to achievement(childKey, targetValue = 500),
            ),
        )
        val result = AchievementJournalStatusResolver.resolve(
            definition = definition(childKey, targetCount = 500),
            achievement = achievement(childKey, targetValue = 500),
            explorerLevel = 10,
            visibility = AchievementVisibility.VISIBLE,
            chainContext = chainContext,
        )
        assertEquals(AchievementJournalStatus.LOCKED, result)
    }

    @Test
    fun resolve_marksVisibleIncompleteAchievements() {
        val key = "achievement:treasure:count_100"
        val result = AchievementJournalStatusResolver.resolve(
            definition = definition(key),
            achievement = achievement(key, currentValue = 12),
            explorerLevel = 10,
            visibility = AchievementVisibility.VISIBLE,
        )
        assertEquals(AchievementJournalStatus.INCOMPLETE, result)
    }
}
