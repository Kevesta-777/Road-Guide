package com.example.roadguideapp.goldhunt.achievements.notifications

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory

internal data class AchievementNotification(
    val notificationId: String,
    val achievementId: String,
    val title: String,
    val category: AchievementCategory,
    val rewards: List<AchievementNotificationReward>,
    val queuedAtMs: Long = System.currentTimeMillis(),
) {
    init {
        require(notificationId.isNotBlank()) { "notificationId must not be blank" }
        require(achievementId.isNotBlank()) { "achievementId must not be blank" }
        require(title.isNotBlank()) { "title must not be blank" }
    }
}
