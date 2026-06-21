package com.example.roadguideapp.goldhunt.treasure.stats

import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarityGenerator
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureTileCoordinate
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureWorldSeed
import org.junit.Assert.assertEquals
import org.junit.Test

class TreasureRarityResolverTest {
    @Test
    fun resolve_l1TreasureId_usesDeterministicRoll() {
        val treasureId = "tr:v1:region-abc:L1:12:34:s2"
        val tile = TreasureTileCoordinate(level = 1, tileX = 12, tileY = 34, slot = 2)
        val worldSeed = "${TreasureWorldSeed.DEFAULT}:region-abc"
        val expected = TreasureRarityGenerator.generate(tile, treasureId, worldSeed)
        val spec = TreasureSpec(
            treasureId = treasureId,
            type = TreasureType.STAR,
            lat = 0.0,
            lng = 0.0,
            creditAmount = 1,
            placementKind = "l1",
            regionL1Id = "L1:12:34",
        )
        assertEquals(expected, TreasureRarityResolver.resolve(spec))
    }

    @Test
    fun resolve_hoardTreasureId_fallsBackToTypeRarity() {
        val spec = TreasureSpec(
            treasureId = "tr:hoard:v1:region:hoard:seed:i0",
            type = TreasureType.GIFT,
            lat = 0.0,
            lng = 0.0,
            creditAmount = 5,
            placementKind = "hoard",
            regionL1Id = "",
        )
        assertEquals(TreasureRarity.EPIC, TreasureRarityResolver.resolve(spec))
    }
}
