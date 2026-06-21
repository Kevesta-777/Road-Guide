package com.example.roadguideapp.goldhunt.achievements.notifications

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantResult
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardOutcome

/**
 * App-wide achievement notification queue for Compose hosts on the map screen.
 */
internal class AchievementNotificationCenter private constructor() {
    val queue = AchievementNotificationQueue()

    fun enqueue(notification: AchievementNotification): Boolean =
        queue.enqueue(notification)

    fun enqueueFromGrant(
        achievement: Achievement,
        outcome: AchievementRewardOutcome,
        grantResult: AchievementRewardGrantResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): Boolean {
        val notification = AchievementNotificationBuilder.fromGrant(
            achievement = achievement,
            outcome = outcome,
            grantResult = grantResult,
            timestampMs = timestampMs,
        ) ?: return false
        return enqueue(notification)
    }

    fun dismissCurrent() {
        queue.dismissHead()
    }

    companion object {
        @Volatile
        private var instance: AchievementNotificationCenter? = null

        fun get(): AchievementNotificationCenter =
            instance ?: synchronized(this) {
                instance ?: AchievementNotificationCenter().also { instance = it }
            }
    }
}
