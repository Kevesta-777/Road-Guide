package com.example.roadguideapp.goldhunt.achievements.notifications

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantResult
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardOutcome
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementNotificationBuilderTest {
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
        legendaryRelicKey = "legendary_relic_achievement_panorama",
        titleKey = "title_achievement_panorama",
        badgeKey = "badge_achievement_panorama",
    )

    private val outcome = AchievementRewardOutcome(
        achievement = achievement,
        creditGrant = CreditGrant(
            eventId = "achievement_reward:credits:achievement:panorama:first_unlock",
            ruleType = RewardRuleType.ACHIEVEMENT_COMPLETION,
            amount = 32,
        ),
        xp = 78L,
        storyFragmentKey = "story_fragment_achievement_panorama",
        legendaryRelicKey = "legendary_relic_achievement_panorama",
        titleKey = "title_achievement_panorama",
        badgeKey = "badge_achievement_panorama",
    )

    @Test
    fun fromGrant_buildsCreditsXpTitleAndRelicLines() {
        val grantResult = AchievementRewardGrantResult(
            achievementId = achievement.achievementId,
            creditsGranted = 32,
            xpGranted = 78L,
            storyFragmentRecorded = true,
            legendaryRelicRecorded = true,
            titleRecorded = true,
            badgeRecorded = true,
            isNewGrant = true,
        )

        val notification = AchievementNotificationBuilder.fromGrant(
            achievement = achievement,
            outcome = outcome,
            grantResult = grantResult,
            timestampMs = 5_000L,
        )

        assertNotNull(notification)
        assertEquals(achievement.title, notification!!.title)
        assertTrue(notification.rewards.any { it is AchievementNotificationReward.Credits })
        assertTrue(notification.rewards.any { it is AchievementNotificationReward.Xp })
        assertTrue(notification.rewards.any { it is AchievementNotificationReward.Title })
        assertTrue(notification.rewards.any { it is AchievementNotificationReward.RelicProgress })
    }

    @Test
    fun fromGrant_returnsNullForDuplicateGrants() {
        val grantResult = AchievementRewardGrantResult.skipped
        assertNull(
            AchievementNotificationBuilder.fromGrant(
                achievement = achievement,
                outcome = outcome,
                grantResult = grantResult,
            ),
        )
    }
}
