package com.example.roadguideapp.goldhunt.achievements.notifications

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementNotificationQueueTest {
    private fun notification(id: String) = AchievementNotification(
        notificationId = "notification:$id",
        achievementId = "achievement:test:$id",
        title = "Achievement $id",
        category = AchievementCategory.TREASURE,
        rewards = listOf(AchievementNotificationReward.Credits(10)),
    )

    @Test
    fun enqueue_andDismissHead_processesFifoQueue() {
        val queue = AchievementNotificationQueue()
        assertNull(queue.head)

        assertTrue(queue.enqueue(notification("one")))
        assertTrue(queue.enqueue(notification("two")))
        assertEquals("Achievement one", queue.head?.title)
        assertEquals(1, queue.trailingCount)

        queue.dismissHead()
        assertEquals("Achievement two", queue.head?.title)
        assertEquals(0, queue.trailingCount)

        queue.dismissHead()
        assertNull(queue.head)
    }

    @Test
    fun enqueue_respectsMaxQueueSize() {
        val queue = AchievementNotificationQueue()
        repeat(AchievementNotificationSchema.MAX_QUEUE_SIZE) { index ->
            assertTrue(queue.enqueue(notification(index.toString())))
        }
        assertFalse(queue.enqueue(notification("overflow")))
        assertEquals(AchievementNotificationSchema.MAX_QUEUE_SIZE, queue.pendingCount)
    }
}
