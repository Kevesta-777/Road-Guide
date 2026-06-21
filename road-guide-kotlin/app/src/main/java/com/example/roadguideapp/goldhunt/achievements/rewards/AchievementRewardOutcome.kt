package com.example.roadguideapp.goldhunt.achievements.rewards

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.rewards.CreditGrant

internal data class AchievementRewardOutcome(
    val achievement: Achievement,
    val creditGrant: CreditGrant,
    val xp: Long,
    val storyFragmentKey: String?,
    val legendaryRelicKey: String?,
    val titleKey: String?,
    val badgeKey: String?,
) {
    fun storyFragmentEventId(): String? =
        storyFragmentKey?.let { AchievementRewardSchema.storyFragmentEventId(achievement.achievementId) }

    fun legendaryRelicEventId(): String? =
        legendaryRelicKey?.let { AchievementRewardSchema.legendaryRelicEventId(achievement.achievementId) }

    fun titleEventId(): String? =
        titleKey?.let { AchievementRewardSchema.titleEventId(achievement.achievementId) }

    fun badgeEventId(): String? =
        badgeKey?.let { AchievementRewardSchema.badgeEventId(achievement.achievementId) }
}
