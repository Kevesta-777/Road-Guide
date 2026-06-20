package com.example.roadguideapp.goldhunt.achievements.statistics

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantEntity

internal object AchievementStatisticsCalculator {
    fun rarityTier(achievement: Achievement): AchievementRarityTier {
        val category = achievement.category
        val achievementId = achievement.achievementId

        if (category in AchievementStatisticsSchema.LEGENDARY_CATEGORIES) {
            return AchievementRarityTier.LEGENDARY
        }
        if (achievementId.endsWith(AchievementStatisticsSchema.MilestoneSuffixes.LEGENDARY)) {
            return AchievementRarityTier.LEGENDARY
        }
        if (achievement.hasRelicReward &&
            category.difficultyMultiplier >= AchievementStatisticsSchema.LEGENDARY_DIFFICULTY_THRESHOLD
        ) {
            return AchievementRarityTier.LEGENDARY
        }
        if (category.difficultyMultiplier >= AchievementStatisticsSchema.LEGENDARY_DIFFICULTY_THRESHOLD) {
            return AchievementRarityTier.LEGENDARY
        }

        if (category in AchievementStatisticsSchema.RARE_CATEGORIES) {
            return AchievementRarityTier.RARE
        }
        if (achievementId.endsWith(AchievementStatisticsSchema.MilestoneSuffixes.RARE)) {
            return AchievementRarityTier.RARE
        }
        if (category.difficultyMultiplier >= AchievementStatisticsSchema.RARE_DIFFICULTY_THRESHOLD) {
            return AchievementRarityTier.RARE
        }

        return AchievementRarityTier.COMMON
    }

    fun compute(
        achievements: List<Achievement>,
        grants: List<AchievementRewardGrantEntity>,
        catalogSize: Int,
    ): AchievementStatistics {
        val completed = achievements.filter { it.completed }
        return AchievementStatistics(
            totalCount = catalogSize.coerceAtLeast(0),
            completedCount = completed.size,
            rareCompletedCount = completed.count {
                rarityTier(it) == AchievementRarityTier.RARE
            },
            legendaryCompletedCount = completed.count {
                rarityTier(it) == AchievementRarityTier.LEGENDARY
            },
            creditsEarned = grants.sumOf { it.creditsGranted.coerceAtLeast(0) },
            xpEarned = grants.sumOf { it.xpGranted.coerceAtLeast(0L) },
        )
    }
}
