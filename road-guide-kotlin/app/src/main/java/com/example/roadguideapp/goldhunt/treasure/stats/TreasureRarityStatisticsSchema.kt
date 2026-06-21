package com.example.roadguideapp.goldhunt.treasure.stats

/**
 * Schema metadata for [TreasureRarityStatistics] persistence on [com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity].
 */
internal object TreasureRarityStatisticsSchema {
    const val VERSION = 1

    /** Reserved achievement keys (future unlock hooks). */
    object AchievementKeys {
        const val PREFIX = "treasure_rarity"
        fun firstOf(rarityId: String): String = "$PREFIX:$rarityId:first"
        fun countMilestone(rarityId: String, count: Int): String = "$PREFIX:$rarityId:count_$count"
    }
}
