package com.example.roadguideapp.goldhunt.achievements.statistics

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AchievementStatisticsTest {
    private val achievement = Achievement(
        achievementId = "achievement:panorama:first_unlock",
        title = "Panorama — first unlock",
        description = "Complete panorama hunt families and hidden symbol challenges.",
        category = AchievementCategory.PANORAMA,
        targetValue = 1,
        currentValue = 1,
        completed = true,
        completionDate = 1_000L,
        rewardCredits = 32,
        rewardXp = 78L,
    )

    @Test
    fun completionPercentage_derivesFromCatalogSize() {
        val stats = AchievementStatistics(totalCount = 200, completedCount = 50)
        assertEquals(0.25f, stats.completionPercentage, 0.0001f)
    }

    @Test
    fun withGrant_incrementsCountersForNewGrant() {
        val base = AchievementStatistics(totalCount = 200, completedCount = 4, rareCompletedCount = 1)
        val result = AchievementRewardGrantResult(
            achievementId = achievement.achievementId,
            creditsGranted = 32,
            xpGranted = 78L,
            storyFragmentRecorded = true,
            legendaryRelicRecorded = false,
            titleRecorded = false,
            badgeRecorded = true,
            isNewGrant = true,
        )

        val updated = base.withGrant(achievement, result)

        assertEquals(5, updated.completedCount)
        assertEquals(2, updated.rareCompletedCount)
        assertEquals(32, updated.creditsEarned)
        assertEquals(78L, updated.xpEarned)
    }

    @Test
    fun withGrant_skipsDuplicateGrants() {
        val base = AchievementStatistics(totalCount = 200, completedCount = 4)
        val result = AchievementRewardGrantResult.skipped
        val updated = base.withGrant(achievement, result)
        assertEquals(4, updated.completedCount)
        assertFalse(result.isNewGrant)
    }
}
