package com.example.roadguideapp.goldhunt.achievements.chain

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookEntity
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementChainResolverTest {
    private val treasure100 = AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 100)
    private val treasure500 = AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.TREASURE, 500)
    private val panoramaUnlock = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.PANORAMA)

    @Test
    fun isProgressAllowed_blocksMilestoneUntilParentCompleted() {
        val context = AchievementChainResolver.buildContext(
            achievementsByKey = mapOf(
                treasure100 to achievement(treasure100, completed = false),
                treasure500 to achievement(treasure500, completed = false),
            ),
        )

        assertFalse(AchievementChainResolver.isProgressAllowed(treasure500, context))
    }

    @Test
    fun isProgressAllowed_allowsMilestoneAfterParentCompleted() {
        val context = AchievementChainResolver.buildContext(
            achievementsByKey = mapOf(
                treasure100 to achievement(treasure100, completed = true),
                treasure500 to achievement(treasure500, completed = false),
            ),
        )

        assertTrue(AchievementChainResolver.isProgressAllowed(treasure500, context))
    }

    @Test
    fun isProgressAllowed_requiresChainUnlockForCrossCategoryTargets() {
        val context = AchievementChainResolver.buildContext(
            achievementsByKey = mapOf(
                panoramaUnlock to achievement(panoramaUnlock, category = AchievementCategory.PANORAMA),
            ),
        )

        assertFalse(AchievementChainResolver.isProgressAllowed(panoramaUnlock, context))
    }

    @Test
    fun isProgressAllowed_derivesCrossCategoryUnlockFromCompletedSource() {
        val secretPlaceKey = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.SECRET_PLACE)
        val context = AchievementChainResolver.buildContext(
            achievementsByKey = mapOf(
                secretPlaceKey to achievement(secretPlaceKey, category = AchievementCategory.SECRET_PLACE, completed = true),
                panoramaUnlock to achievement(panoramaUnlock, category = AchievementCategory.PANORAMA),
            ),
        )

        assertTrue(AchievementChainResolver.isProgressAllowed(panoramaUnlock, context))
    }

    @Test
    fun isProgressAllowed_allowsCrossCategoryTargetAfterChainHook() {
        val context = AchievementChainResolver.buildContext(
            achievementsByKey = mapOf(
                panoramaUnlock to achievement(panoramaUnlock, category = AchievementCategory.PANORAMA),
            ),
            chainHooks = listOf(
                AchievementRewardHookEntity(
                    eventId = "chain:panorama",
                    achievementId = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.SECRET_PLACE),
                    hookType = AchievementRewardSchema.HookTypes.CHAIN_UNLOCK,
                    hookKey = "${AchievementChainSchema.HookSubtypes.ACHIEVEMENT}:$panoramaUnlock",
                    grantedAtMs = 1_000L,
                ),
            ),
        )

        assertTrue(AchievementChainResolver.isProgressAllowed(panoramaUnlock, context))
    }

    @Test
    fun missingDependencyKeys_listsUnmetParents() {
        val context = AchievementChainResolver.buildContext(
            achievementsByKey = mapOf(
                treasure100 to achievement(treasure100, completed = false),
            ),
        )

        assertEquals(
            listOf(treasure100),
            AchievementChainResolver.missingDependencyKeys(treasure500, context),
        )
    }

    private fun achievement(
        key: String,
        category: AchievementCategory = AchievementCategory.TREASURE,
        completed: Boolean = false,
    ) = Achievement(
        achievementId = key,
        title = key,
        description = "Test",
        category = category,
        targetValue = 100,
        currentValue = if (completed) 100 else 0,
        completed = completed,
        completionDate = if (completed) 1_000L else null,
        rewardCredits = 25,
        rewardXp = 60L,
    )
}
