package com.example.roadguideapp.goldhunt.treasure.rarity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TreasureRarityGeneratorTest {
    private val tile = TreasureTileCoordinate(level = 1, tileX = 12, tileY = 34, slot = 2)
    private val treasureId = "tr:v1:r:0.0000:0.0000:L1:12:34:s2"
    private val worldSeed = "goldhunt:world:v1:r:0.0000:0.0000"

    @Test
    fun generate_sameInputs_isStable() {
        val first = TreasureRarityGenerator.generate(tile, treasureId, worldSeed)
        val second = TreasureRarityGenerator.generate(tile, treasureId, worldSeed)
        assertEquals(first, second)
    }

    @Test
    fun generate_differentTreasureId_canDiffer() {
        val a = TreasureRarityGenerator.generate(tile, treasureId, worldSeed)
        val b = TreasureRarityGenerator.generate(tile, "$treasureId:alt", worldSeed)
        // Not asserting inequality (could collide); only that call succeeds.
        assertTrue(a in TreasureRarity.entries)
        assertTrue(b in TreasureRarity.entries)
    }

    @Test
    fun generate_differentTile_changesRollSeed() {
        val otherTile = tile.copy(tileX = 99)
        val seedA = TreasureRarityGenerator.rollSeed(tile, treasureId, worldSeed)
        val seedB = TreasureRarityGenerator.rollSeed(otherTile, treasureId, worldSeed)
        assertTrue(seedA != seedB)
    }

    @Test
    fun generate_allRarityTiers_areReachable() {
        val seen = mutableSetOf<TreasureRarity>()
        for (slot in 0 until 8_000) {
            val candidateTile = tile.copy(slot = slot)
            val id = "$treasureId:probe:$slot"
            seen += TreasureRarityGenerator.generate(candidateTile, id, worldSeed)
            if (seen.size == TreasureRarity.entries.size) break
        }
        assertEquals(TreasureRarity.entries.toSet(), seen)
    }
}
