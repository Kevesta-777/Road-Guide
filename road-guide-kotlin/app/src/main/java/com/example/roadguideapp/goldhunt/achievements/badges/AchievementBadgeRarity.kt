package com.example.roadguideapp.goldhunt.achievements.badges

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementRarityTier
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatisticsCalculator

internal enum class AchievementBadgeRarity(val id: String) {
    COMMON("common"),
    RARE("rare"),
    LEGENDARY("legendary"),
    ;

    companion object {
        fun fromId(id: String): AchievementBadgeRarity? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }

        fun fromAchievement(achievement: Achievement): AchievementBadgeRarity =
            when (AchievementStatisticsCalculator.rarityTier(achievement)) {
                AchievementRarityTier.LEGENDARY -> LEGENDARY
                AchievementRarityTier.RARE -> RARE
                AchievementRarityTier.COMMON -> COMMON
            }
    }
}
