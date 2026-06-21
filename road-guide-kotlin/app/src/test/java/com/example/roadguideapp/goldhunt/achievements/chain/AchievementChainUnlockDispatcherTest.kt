package com.example.roadguideapp.goldhunt.achievements.chain

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementChainUnlockDispatcherTest {
    @Test
    fun dispatch_buildsIdempotentHookOperationsForAllUnlockTypes() {
        val sourceKey = AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 1_000)
        val source = Achievement(
            achievementId = sourceKey,
            title = "Treasure 1000",
            description = "Collect 1000 treasures",
            category = AchievementCategory.TREASURE,
            targetValue = 1_000,
            currentValue = 1_000,
            completed = true,
            completionDate = 2_000L,
            rewardCredits = 250,
            rewardXp = 600L,
        )

        val operations = AchievementChainUnlockDispatcher.dispatch(
            sourceAchievement = source,
            unlocks = AchievementChainCatalog.rewardsFor(sourceKey),
            timestampMs = 2_000L,
        )

        assertEquals(2, operations.size)
        assertTrue(operations.any { it is AchievementChainUnlockOperation.TitleUnlock })
        assertTrue(operations.any { it is AchievementChainUnlockOperation.LegendaryRelicUnlock })
        operations.forEach { operation ->
            assertEquals(sourceKey, operation.hook.achievementId)
            assertEquals(AchievementRewardSchema.HookTypes.CHAIN_UNLOCK, operation.hook.hookType)
            assertEquals(2_000L, operation.hook.grantedAtMs)
        }
    }
}
