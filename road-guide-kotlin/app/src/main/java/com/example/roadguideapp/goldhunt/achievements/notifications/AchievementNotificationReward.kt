package com.example.roadguideapp.goldhunt.achievements.notifications

/**
 * Reward lines rendered inside an achievement completion popup.
 */
internal sealed class AchievementNotificationReward {
    data class Credits(val amount: Int) : AchievementNotificationReward()
    data class Xp(val amount: Long) : AchievementNotificationReward()
    data class Title(val titleKey: String) : AchievementNotificationReward()
    data class RelicProgress(val relicKey: String) : AchievementNotificationReward()
    data class Cosmetic(
        val cosmeticKey: String,
        val cosmeticType: String,
    ) : AchievementNotificationReward()
}
