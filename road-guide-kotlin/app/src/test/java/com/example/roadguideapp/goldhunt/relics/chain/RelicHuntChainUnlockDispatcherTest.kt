package com.example.roadguideapp.goldhunt.relics.chain

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicHuntChainUnlockDispatcherTest {
    @Test
    fun dispatch_buildsIdempotentHookOperationsForAllUnlockTypes() {
        val sourceId = RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN)
        val source = LegendaryRelic(
            relicId = sourceId,
            name = "Hidden Relic",
            description = "Capstone relic",
            category = RelicCategory.HIDDEN,
            rarity = RelicRarity.LEGENDARY,
            pieceCount = 9,
            piecesCollected = 9,
            completed = true,
            completionDate = 2_000L,
            rewardCredits = 500,
            rewardXp = 1_000L,
        )

        val operations = RelicHuntChainUnlockDispatcher.dispatch(
            sourceRelic = source,
            unlocks = RelicHuntChainCatalog.rewardsFor(sourceId),
            timestampMs = 2_000L,
        )

        assertEquals(4, operations.size)
        assertTrue(operations.any { it is RelicHuntChainUnlockOperation.BadgeUnlock })
        assertTrue(operations.any { it is RelicHuntChainUnlockOperation.TitleUnlock })
        assertTrue(operations.any { it is RelicHuntChainUnlockOperation.StoryFragmentUnlock })
        assertTrue(operations.any { it is RelicHuntChainUnlockOperation.AchievementUnlock })
        operations.filterIsInstance<RelicHuntChainUnlockOperation.BadgeUnlock>().forEach { operation ->
            assertEquals(sourceId, operation.hook.relicId)
            assertEquals(LegendaryRelicRewardSchema.HookTypes.CHAIN_UNLOCK, operation.hook.hookType)
            assertEquals(2_000L, operation.hook.grantedAtMs)
        }
    }
}
