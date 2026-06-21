package com.example.roadguideapp.goldhunt.achievements.badges

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementBadgeRarityTest {
    @Test
    fun fromAchievement_mapsMasterExplorerToLegendary() {
        val achievement = Achievement(
            achievementId = AchievementSchema.AchievementKeys.firstUnlock(
                AchievementCategory.MASTER_EXPLORER,
            ),
            title = "Master Explorer — first unlock",
            description = "Test",
            category = AchievementCategory.MASTER_EXPLORER,
            targetValue = 1,
            currentValue = 1,
            completed = true,
            completionDate = 1_000L,
            rewardCredits = 25,
            rewardXp = 60L,
        )
        assertEquals(
            AchievementBadgeRarity.LEGENDARY,
            AchievementBadgeRarity.fromAchievement(achievement),
        )
    }

    @Test
    fun fromId_parsesStoredRarity() {
        assertEquals(AchievementBadgeRarity.RARE, AchievementBadgeRarity.fromId("rare"))
    }
}
