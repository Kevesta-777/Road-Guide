package com.example.roadguideapp.goldhunt.achievements.titles

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementRarityTier
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatisticsCalculator

internal enum class AchievementTitleRarity(val id: String) {
    COMMON("common"),
    RARE("rare"),
    LEGENDARY("legendary"),
    ;

    companion object {
        fun fromId(id: String): AchievementTitleRarity? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }

        fun fromAchievement(achievement: Achievement): AchievementTitleRarity =
            when (AchievementStatisticsCalculator.rarityTier(achievement)) {
                AchievementRarityTier.LEGENDARY -> LEGENDARY
                AchievementRarityTier.RARE -> RARE
                AchievementRarityTier.COMMON -> COMMON
            }
    }
}
