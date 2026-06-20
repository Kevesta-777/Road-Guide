package com.example.roadguideapp.goldhunt.achievements.rewards

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AchievementRewardDispatcherTest {
    @Test
    fun dispatch_buildsCreditXpAndHookKeysFromAchievement() {
        val achievement = Achievement(
            achievementId = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.PANORAMA),
            title = "Panorama — first unlock",
            description = "Complete panorama hunt families and hidden symbol challenges.",
            category = AchievementCategory.PANORAMA,
            targetValue = 1,
            currentValue = 1,
            completed = true,
            completionDate = 1_000L,
            rewardCredits = 175,
            rewardXp = 420L,
            legendaryRelicKey = AchievementSchema.LegendaryRelicKeys.forCategory(AchievementCategory.PANORAMA),
            titleKey = null,
            badgeKey = AchievementSchema.BadgeKeys.forCategory(AchievementCategory.PANORAMA),
        )

        val outcome = AchievementRewardDispatcher.dispatch(achievement)

        assertEquals(achievement, outcome.achievement)
        assertEquals(175, outcome.creditGrant.amount)
        assertEquals(RewardRuleType.ACHIEVEMENT_COMPLETION, outcome.creditGrant.ruleType)
        assertEquals(
            AchievementRewardSchema.creditsEventId(achievement.achievementId),
            outcome.creditGrant.eventId,
        )
        assertEquals(420L, outcome.xp)
        assertEquals(
            AchievementSchema.StoryFragmentKeys.forCategory(AchievementCategory.PANORAMA),
            outcome.storyFragmentKey,
        )
        assertNotNull(outcome.legendaryRelicKey)
        assertNull(outcome.titleKey)
        assertNotNull(outcome.badgeKey)
    }

    @Test
    fun dispatch_omitsStoryFragmentWhenCategoryUnsupported() {
        val achievement = Achievement(
            achievementId = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.TREASURE),
            title = "Treasure — first unlock",
            description = "Collect treasures scattered across the Gold Hunt world.",
            category = AchievementCategory.TREASURE,
            targetValue = 1,
            currentValue = 1,
            completed = true,
            completionDate = 1_000L,
            rewardCredits = 25,
            rewardXp = 60L,
        )

        val outcome = AchievementRewardDispatcher.dispatch(achievement)

        assertNull(outcome.storyFragmentKey)
        assertNull(outcome.legendaryRelicKey)
        assertNull(outcome.titleKey)
        assertNull(outcome.badgeKey)
    }
}
