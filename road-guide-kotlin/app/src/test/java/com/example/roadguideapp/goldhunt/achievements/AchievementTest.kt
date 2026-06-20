package com.example.roadguideapp.goldhunt.achievements

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementTest {
    private val incomplete = Achievement(
        achievementId = "achievement:treasure:first_unlock",
        title = "Treasure — first unlock",
        description = "Collect treasures scattered across the Gold Hunt world.",
        category = AchievementCategory.TREASURE,
        targetValue = 1,
        currentValue = 0,
        completed = false,
        completionDate = null,
        rewardCredits = 25,
        rewardXp = 60L,
    )

    @Test
    fun incompleteAchievement_reportsZeroProgress() {
        assertFalse(incomplete.completed)
        assertNull(incomplete.completionDate)
        assertEquals(0f, incomplete.progressFraction, 0.001f)
        assertFalse(incomplete.isComplete)
    }

    @Test
    fun withProgress_marksCompleteAndSetsCompletionDate() {
        val completed = incomplete.withProgress(currentValue = 1, completionDate = 2_000L)
        assertTrue(completed.completed)
        assertEquals(2_000L, completed.completionDate)
        assertEquals(1f, completed.progressFraction, 0.001f)
        assertTrue(completed.isComplete)
    }

    @Test
    fun foundationMasterExplorer_reservesFutureRewards() {
        val achievement = AchievementCatalog.achievementFromDefinition(
            AchievementCatalog.foundationDefinition(AchievementCategory.MASTER_EXPLORER),
        )
        assertNotNull(achievement.legendaryRelicKey)
        assertNotNull(achievement.titleKey)
        assertNotNull(achievement.badgeKey)
        assertTrue(achievement.hasRelicReward)
        assertTrue(achievement.hasTitleReward)
        assertTrue(achievement.hasBadgeReward)
    }

    @Test
    fun foundationTreasure_hasNoFutureCosmeticRewards() {
        val achievement = AchievementCatalog.achievementFromDefinition(
            AchievementCatalog.foundationDefinition(AchievementCategory.TREASURE),
        )
        assertNull(achievement.legendaryRelicKey)
        assertNull(achievement.titleKey)
        assertNull(achievement.badgeKey)
        assertFalse(achievement.hasRelicReward)
        assertFalse(achievement.hasTitleReward)
        assertFalse(achievement.hasBadgeReward)
    }
}
