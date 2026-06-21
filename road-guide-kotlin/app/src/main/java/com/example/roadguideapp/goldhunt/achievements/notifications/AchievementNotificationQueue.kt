package com.example.roadguideapp.goldhunt.achievements.notifications

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf

/**
 * In-memory FIFO queue observed by Compose notification hosts.
 */
@Stable
internal class AchievementNotificationQueue {
    private val items = mutableStateListOf<AchievementNotification>()

    val head: AchievementNotification?
        get() = items.firstOrNull()

    val pendingCount: Int
        get() = items.size

    val trailingCount: Int
        get() = (items.size - 1).coerceAtLeast(0)

    fun enqueue(notification: AchievementNotification): Boolean {
        if (items.size >= AchievementNotificationSchema.MAX_QUEUE_SIZE) {
            return false
        }
        items.add(notification)
        return true
    }

    fun dismissHead() {
        if (items.isNotEmpty()) {
            items.removeAt(0)
        }
    }

    fun clear() {
        items.clear()
    }
}
