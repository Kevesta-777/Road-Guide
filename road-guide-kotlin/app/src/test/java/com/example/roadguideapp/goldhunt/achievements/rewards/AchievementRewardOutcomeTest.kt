package com.example.roadguideapp.goldhunt.achievements.rewards

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AchievementRewardOutcomeTest {
    private val achievement = Achievement(
        achievementId = "achievement:story:first_unlock",
        title = "Story — first unlock",
        description = "Unlock story fragments tied to your exploration journey.",
        category = AchievementCategory.STORY,
        targetValue = 1,
        currentValue = 1,
        completed = true,
        completionDate = 1_000L,
        rewardCredits = 25,
        rewardXp = 60L,
        legendaryRelicKey = "legendary_relic_achievement_story",
        titleKey = null,
        badgeKey = null,
    )

    @Test
    fun eventIdHelpers_returnNullWhenHookKeyMissing() {
        val outcome = AchievementRewardOutcome(
            achievement = achievement,
            creditGrant = CreditGrant(
                eventId = "achievement_reward:credits:achievement:story:first_unlock",
                ruleType = RewardRuleType.ACHIEVEMENT_COMPLETION,
                amount = 25,
            ),
            xp = 60L,
            storyFragmentKey = null,
            legendaryRelicKey = null,
            titleKey = null,
            badgeKey = null,
        )

        assertNull(outcome.storyFragmentEventId())
        assertNull(outcome.legendaryRelicEventId())
        assertNull(outcome.titleEventId())
        assertNull(outcome.badgeEventId())
    }

    @Test
    fun eventIdHelpers_buildStableIdsWhenHookKeysPresent() {
        val outcome = AchievementRewardOutcome(
            achievement = achievement,
            creditGrant = CreditGrant(
                eventId = "achievement_reward:credits:achievement:story:first_unlock",
                ruleType = RewardRuleType.ACHIEVEMENT_COMPLETION,
                amount = 25,
            ),
            xp = 60L,
            storyFragmentKey = "story_fragment_achievement_story",
            legendaryRelicKey = "legendary_relic_achievement_story",
            titleKey = "title_achievement_story",
            badgeKey = "badge_achievement_story",
        )

        assertEquals(
            AchievementRewardSchema.storyFragmentEventId(achievement.achievementId),
            outcome.storyFragmentEventId(),
        )
        assertEquals(
            AchievementRewardSchema.legendaryRelicEventId(achievement.achievementId),
            outcome.legendaryRelicEventId(),
        )
        assertEquals(
            AchievementRewardSchema.titleEventId(achievement.achievementId),
            outcome.titleEventId(),
        )
        assertEquals(
            AchievementRewardSchema.badgeEventId(achievement.achievementId),
            outcome.badgeEventId(),
        )
    }
}
