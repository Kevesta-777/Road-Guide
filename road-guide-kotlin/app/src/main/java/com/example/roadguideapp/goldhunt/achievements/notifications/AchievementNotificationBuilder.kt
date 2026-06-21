package com.example.roadguideapp.goldhunt.achievements.notifications

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantResult
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardOutcome

internal object AchievementNotificationBuilder {
    fun fromGrant(
        achievement: Achievement,
        outcome: AchievementRewardOutcome,
        grantResult: AchievementRewardGrantResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): AchievementNotification? {
        if (!grantResult.isNewGrant) return null
        val rewards = buildRewardLines(achievement, outcome, grantResult)
        return AchievementNotification(
            notificationId = "achievement_notification:${achievement.achievementId}:$timestampMs",
            achievementId = achievement.achievementId,
            title = achievement.title,
            category = achievement.category,
            rewards = rewards,
            queuedAtMs = timestampMs,
        )
    }

    private fun buildRewardLines(
        achievement: Achievement,
        outcome: AchievementRewardOutcome,
        grantResult: AchievementRewardGrantResult,
    ): List<AchievementNotificationReward> = buildList {
        if (grantResult.creditsGranted > 0) {
            add(AchievementNotificationReward.Credits(grantResult.creditsGranted))
        }
        if (grantResult.xpGranted > 0L) {
            add(AchievementNotificationReward.Xp(grantResult.xpGranted))
        }
        if (grantResult.titleRecorded && !outcome.titleKey.isNullOrBlank()) {
            add(AchievementNotificationReward.Title(outcome.titleKey))
        }
        if (grantResult.legendaryRelicRecorded && !outcome.legendaryRelicKey.isNullOrBlank()) {
            add(AchievementNotificationReward.RelicProgress(outcome.legendaryRelicKey))
        }
        if (grantResult.badgeRecorded && !outcome.badgeKey.isNullOrBlank()) {
            add(
                AchievementNotificationReward.Cosmetic(
                    cosmeticKey = outcome.badgeKey,
                    cosmeticType = "badge",
                ),
            )
        }
        if (grantResult.storyFragmentRecorded && !outcome.storyFragmentKey.isNullOrBlank()) {
            add(
                AchievementNotificationReward.Cosmetic(
                    cosmeticKey = outcome.storyFragmentKey,
                    cosmeticType = "storyFragment",
                ),
            )
        }
        if (isEmpty()) {
            add(AchievementNotificationReward.Credits(achievement.rewardCredits.coerceAtLeast(0)))
            if (achievement.rewardXp > 0L) {
                add(AchievementNotificationReward.Xp(achievement.rewardXp))
            }
        }
    }
}
