package com.example.roadguideapp.goldhunt.achievements.statistics

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory

/**
 * Schema metadata and rarity classification rules for achievement statistics.
 */
internal object AchievementStatisticsSchema {
    const val VERSION = 1

    /** Category difficulty multipliers at or above this tier count as rare completions. */
    const val RARE_DIFFICULTY_THRESHOLD = 1.2

    /** Category difficulty multipliers at or above this tier count as legendary completions. */
    const val LEGENDARY_DIFFICULTY_THRESHOLD = 1.4

    val LEGENDARY_CATEGORIES: Set<AchievementCategory> = setOf(
        AchievementCategory.LEGENDARY_RELIC,
        AchievementCategory.MASTER_EXPLORER,
    )

    val RARE_CATEGORIES: Set<AchievementCategory> = setOf(
        AchievementCategory.PANORAMA,
        AchievementCategory.SEASONAL_EVENT,
        AchievementCategory.STREAK,
        AchievementCategory.TREASURE_CLUSTER,
        AchievementCategory.RARITY,
        AchievementCategory.SECRET_PLACE,
    )

    object MilestoneSuffixes {
        const val RARE = "count_500"
        const val LEGENDARY = "count_1000"
    }
}
