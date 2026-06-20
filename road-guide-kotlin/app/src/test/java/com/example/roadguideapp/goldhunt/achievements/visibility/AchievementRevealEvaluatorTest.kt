package com.example.roadguideapp.goldhunt.achievements.visibility

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementRevealEvaluatorTest {
    private fun achievement(completed: Boolean = false, currentValue: Int = 0) = Achievement(
        achievementId = "achievement:treasure:count_500",
        title = "Treasures collected — 500",
        description = "Collect 500 treasures.",
        category = AchievementCategory.TREASURE,
        targetValue = 500,
        currentValue = currentValue,
        completed = completed,
        completionDate = if (completed) 1_000L else null,
        rewardCredits = 50,
        rewardXp = 120L,
    )

    @Test
    fun isRevealed_visibleAchievementsAlwaysRevealed() {
        assertTrue(
            AchievementRevealEvaluator.isRevealed(
                AchievementVisibility.VISIBLE,
                achievement(currentValue = 42),
            ),
        )
    }

    @Test
    fun isRevealed_hiddenStaysMaskedUntilCompleted() {
        assertFalse(
            AchievementRevealEvaluator.isRevealed(
                AchievementVisibility.HIDDEN,
                achievement(currentValue = 42),
            ),
        )
        assertTrue(
            AchievementRevealEvaluator.isRevealed(
                AchievementVisibility.HIDDEN,
                achievement(completed = true, currentValue = 500),
            ),
        )
    }

    @Test
    fun shouldIncludeInJournal_secretExcludedUntilCompleted() {
        assertFalse(
            AchievementRevealEvaluator.shouldIncludeInJournal(
                AchievementVisibility.SECRET,
                achievement(currentValue = 900),
            ),
        )
        assertTrue(
            AchievementRevealEvaluator.shouldIncludeInJournal(
                AchievementVisibility.SECRET,
                achievement(completed = true, currentValue = 1000),
            ),
        )
    }

    @Test
    fun shouldIncludeInJournal_hiddenAlwaysIncluded() {
        assertTrue(
            AchievementRevealEvaluator.shouldIncludeInJournal(
                AchievementVisibility.HIDDEN,
                achievement(currentValue = 12),
            ),
        )
    }
}
