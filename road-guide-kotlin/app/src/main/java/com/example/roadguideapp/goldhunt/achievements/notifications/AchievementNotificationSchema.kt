package com.example.roadguideapp.goldhunt.achievements.notifications

internal object AchievementNotificationSchema {
    const val VERSION = 1
    const val AUTO_DISMISS_MS = 3_500L
    const val MAX_QUEUE_SIZE = 12

    object ExtensionKeys {
        const val COSMETIC_KEY = "cosmeticKey"
        const val COSMETIC_TYPE = "cosmeticType"
    }
}
