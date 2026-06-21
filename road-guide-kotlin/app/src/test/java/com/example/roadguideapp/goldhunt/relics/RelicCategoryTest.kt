package com.example.roadguideapp.goldhunt.relics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicCategoryTest {
    @Test
    fun allOrdered_containsEightCategoriesInProgressionOrder() {
        assertEquals(8, RelicCategory.ALL_ORDERED.size)
        assertEquals(RelicCategory.EXPLORER, RelicCategory.ALL_ORDERED.first())
        assertEquals(RelicCategory.HIDDEN, RelicCategory.ALL_ORDERED.last())
    }

    @Test
    fun rarityMultipliers_anchorEntryAndCapstone() {
        assertEquals(1.2, RelicCategory.EXPLORER.rarityMultiplier, 0.001)
        assertEquals(3.0, RelicCategory.HIDDEN.rarityMultiplier, 0.001)
        assertTrue(RelicCategory.EXPLORER.rarityMultiplier < RelicCategory.HIDDEN.rarityMultiplier)
        assertTrue(
            RelicCategory.ALL_ORDERED.all {
                it.rarityMultiplier <= RelicCategory.HIDDEN.rarityMultiplier
            },
        )
    }

    @Test
    fun minimumExplorerLevels_increaseTowardCapstone() {
        assertTrue(RelicCategory.EXPLORER.minimumExplorerLevel < RelicCategory.HIDDEN.minimumExplorerLevel)
        assertTrue(
            RelicCategory.ALL_ORDERED.zipWithNext().all { (left, right) ->
                left.minimumExplorerLevel <= right.minimumExplorerLevel
            },
        )
    }

    @Test
    fun fromId_resolvesCaseInsensitive() {
        assertEquals(RelicCategory.ANCIENT_CIVILIZATION, RelicCategory.fromId("ancient_civilization"))
        assertNull(RelicCategory.fromId("unknown_relic_category"))
    }

    @Test
    fun hidden_hasCapstoneMetadata() {
        val category = RelicCategory.HIDDEN
        assertEquals("Hidden", category.displayName)
        assertEquals("relic_hidden", category.iconKey)
        assertEquals(3.0, category.rarityMultiplier, 0.001)
        assertEquals(30, category.minimumExplorerLevel)
    }
}
