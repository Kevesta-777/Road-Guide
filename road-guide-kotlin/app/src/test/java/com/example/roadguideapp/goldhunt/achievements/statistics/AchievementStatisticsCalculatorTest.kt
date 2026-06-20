package com.example.roadguideapp.goldhunt.achievements.statistics

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementStatisticsCalculatorTest {
    private fun completed(
        achievementId: String,
        category: AchievementCategory,
        legendaryRelicKey: String? = null,
    ) = Achievement(
        achievementId = achievementId,
        title = achievementId,
        description = "Test achievement",
        category = category,
        targetValue = 1,
        currentValue = 1,
        completed = true,
        completionDate = 1_000L,
        rewardCredits = 25,
        rewardXp = 60L,
        legendaryRelicKey = legendaryRelicKey,
    )

    @Test
    fun rarityTier_classifiesLegendaryCategories() {
        val achievement = completed(
            achievementId = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.MASTER_EXPLORER),
            category = AchievementCategory.MASTER_EXPLORER,
        )
        assertEquals(AchievementRarityTier.LEGENDARY, AchievementStatisticsCalculator.rarityTier(achievement))
    }

    @Test
    fun rarityTier_classifiesMilestoneTiers() {
        val rare = completed(
            achievementId = "achievement:treasure:count_500",
            category = AchievementCategory.TREASURE,
        )
        val legendary = completed(
            achievementId = "achievement:treasure:count_1000",
            category = AchievementCategory.TREASURE,
        )
        assertEquals(AchievementRarityTier.RARE, AchievementStatisticsCalculator.rarityTier(rare))
        assertEquals(AchievementRarityTier.LEGENDARY, AchievementStatisticsCalculator.rarityTier(legendary))
    }

    @Test
    fun rarityTier_classifiesRareCategories() {
        val achievement = completed(
            achievementId = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.PANORAMA),
            category = AchievementCategory.PANORAMA,
        )
        assertEquals(AchievementRarityTier.RARE, AchievementStatisticsCalculator.rarityTier(achievement))
    }

    @Test
    fun compute_aggregatesCompletionAndRewardTotals() {
        val achievements = listOf(
            completed("achievement:treasure:count_100", AchievementCategory.TREASURE),
            completed("achievement:treasure:count_500", AchievementCategory.TREASURE),
            completed(
                AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.MASTER_EXPLORER),
                AchievementCategory.MASTER_EXPLORER,
            ),
        )
        val grants = listOf(
            AchievementRewardGrantEntity(
                achievementId = "achievement:treasure:count_100",
                creditsGranted = 25,
                xpGranted = 60L,
                storyFragmentKey = null,
                storyFragmentRecorded = false,
                legendaryRelicKey = null,
                legendaryRelicRecorded = false,
                titleKey = null,
                titleRecorded = false,
                badgeKey = null,
                badgeRecorded = false,
                grantedAtMs = 1_000L,
            ),
            AchievementRewardGrantEntity(
                achievementId = "achievement:treasure:count_500",
                creditsGranted = 25,
                xpGranted = 60L,
                storyFragmentKey = null,
                storyFragmentRecorded = false,
                legendaryRelicKey = null,
                legendaryRelicRecorded = false,
                titleKey = null,
                titleRecorded = false,
                badgeKey = null,
                badgeRecorded = false,
                grantedAtMs = 2_000L,
            ),
        )

        val stats = AchievementStatisticsCalculator.compute(
            achievements = achievements,
            grants = grants,
            catalogSize = 120,
        )

        assertEquals(120, stats.totalCount)
        assertEquals(3, stats.completedCount)
        assertEquals(1, stats.rareCompletedCount)
        assertEquals(1, stats.legendaryCompletedCount)
        assertEquals(50, stats.creditsEarned)
        assertEquals(120L, stats.xpEarned)
        assertEquals(0.025f, stats.completionPercentage, 0.0001f)
    }
}
