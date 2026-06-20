package com.example.roadguideapp.goldhunt.relics.chain

import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicHuntChainCatalogTest {
    @Test
    fun gates_defineCategoryProgressionLadder() {
        val storyGate = RelicHuntChainCatalog.gateFor(
            RelicSchema.RelicKeys.forCategory(RelicCategory.STORY),
        )
        assertNotNull(storyGate)
        assertEquals(
            RelicSchema.RelicKeys.forCategory(RelicCategory.EXPLORER),
            (storyGate!!.dependencies.first() as RelicHuntChainDependency.RelicCompleted).relicId,
        )
    }

    @Test
    fun rewards_includeAllUnlockTypesForWarriorCompletion() {
        val unlocks = RelicHuntChainCatalog.rewardsFor(
            RelicSchema.RelicKeys.forCategory(RelicCategory.WARRIOR),
        )
        assertTrue(unlocks.any { it is RelicHuntChainUnlock.Title })
        assertTrue(unlocks.any { it is RelicHuntChainUnlock.Achievement })
    }

    @Test
    fun royalCompletion_unlocksHiddenRelicChainTarget() {
        val unlocks = RelicHuntChainCatalog.rewardsFor(
            RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL),
        )
        assertTrue(
            unlocks.filterIsInstance<RelicHuntChainUnlock.LegendaryRelic>()
                .any { it.relicKey == RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN) },
        )
        assertTrue(RelicHuntChainCatalog.requiresChainUnlock(
            RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN),
        ))
    }
}
