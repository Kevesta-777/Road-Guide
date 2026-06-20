package com.example.roadguideapp.goldhunt.achievements.progress

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementProgressUpdaterTest {
    private val base = Achievement(
        achievementId = "achievement:treasure:count_100",
        title = "Treasures collected — 100",
        description = "Collect treasures scattered across the Gold Hunt world.",
        category = AchievementCategory.TREASURE,
        targetValue = 100,
        currentValue = 10,
        completed = false,
        completionDate = null,
        rewardCredits = 25,
        rewardXp = 60L,
    )

    @Test
    fun applyValue_updatesProgressWithoutCompleting() {
        val result = AchievementProgressUpdater.applyValue(base, proposedValue = 42, timestampMs = 1_000L)
        assertTrue(result.progressChanged)
        assertFalse(result.isNewCompletion)
        assertEquals(42, result.achievement!!.currentValue)
        assertFalse(result.achievement.completed)
    }

    @Test
    fun applyValue_marksCompletionOnce() {
        val result = AchievementProgressUpdater.applyValue(base, proposedValue = 100, timestampMs = 2_000L)
        assertTrue(result.progressChanged)
        assertTrue(result.isNewCompletion)
        assertEquals(100, result.achievement!!.currentValue)
        assertTrue(result.achievement.completed)
        assertEquals(2_000L, result.achievement.completionDate)
    }

    @Test
    fun applyValue_skipsDuplicateCompletion() {
        val completed = base.withProgress(currentValue = 100, completed = true, completionDate = 3_000L)
        val result = AchievementProgressUpdater.applyValue(completed, proposedValue = 150, timestampMs = 4_000L)
        assertFalse(result.progressChanged)
        assertFalse(result.isNewCompletion)
        assertEquals(3_000L, result.achievement!!.completionDate)
    }

    @Test
    fun applyIncrement_accumulatesUntilTarget() {
        val first = AchievementProgressUpdater.applyIncrement(base, increment = 40, timestampMs = 1_000L)
        assertEquals(50, first.achievement!!.currentValue)
        val second = AchievementProgressUpdater.applyIncrement(first.achievement!!, increment = 60, timestampMs = 2_000L)
        assertTrue(second.isNewCompletion)
        assertEquals(100, second.achievement!!.currentValue)
    }
}
