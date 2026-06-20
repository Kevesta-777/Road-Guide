package com.example.roadguideapp.goldhunt.achievements.rewards

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType

internal object AchievementRewardDispatcher {
    fun dispatch(achievement: Achievement): AchievementRewardOutcome {
        val definition = AchievementRegistry.findByKey(achievement.achievementId)
        return AchievementRewardOutcome(
            achievement = achievement,
            creditGrant = CreditGrant(
                eventId = AchievementRewardSchema.creditsEventId(achievement.achievementId),
                ruleType = RewardRuleType.ACHIEVEMENT_COMPLETION,
                amount = achievement.rewardCredits,
                label = achievement.achievementId,
            ),
            xp = achievement.rewardXp,
            storyFragmentKey = definition?.storyFragmentKey,
            legendaryRelicKey = achievement.legendaryRelicKey,
            titleKey = achievement.titleKey,
            badgeKey = achievement.badgeKey,
        )
    }
}
