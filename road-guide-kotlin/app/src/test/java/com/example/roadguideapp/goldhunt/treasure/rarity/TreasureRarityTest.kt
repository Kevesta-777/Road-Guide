package com.example.roadguideapp.goldhunt.treasure.rarity

import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TreasureRarityTest {
    @Test
    fun allRarities_haveMonotonicMultipliers() {
        var previousCredit = 0.0
        var previousXp = 0.0
        for (rarity in TreasureRarity.ALL_ORDERED) {
            assertTrue(rarity.creditMultiplier >= previousCredit)
            assertTrue(rarity.xpMultiplier >= previousXp)
            assertTrue(rarity.rarityWeight > 0)
            previousCredit = rarity.creditMultiplier
            previousXp = rarity.xpMultiplier
        }
    }

    @Test
    fun totalRarityWeight_matchesSum() {
        assertEquals(
            TreasureRarity.entries.sumOf { it.rarityWeight },
            TreasureRarity.TOTAL_RARITY_WEIGHT,
        )
    }

    @Test
    fun fromId_resolvesEnumName() {
        assertEquals(TreasureRarity.LEGENDARY, TreasureRarity.fromId("legendary"))
    }

    @Test
    fun typeMapping_alignsWithTreasureTiers() {
        assertEquals(TreasureRarity.COMMON, TreasureTypeRarity.rarityFor(TreasureType.STAR))
        assertEquals(TreasureRarity.UNCOMMON, TreasureTypeRarity.rarityFor(TreasureType.FLOWER))
        assertEquals(TreasureRarity.RARE, TreasureTypeRarity.rarityFor(TreasureType.CRYSTAL))
        assertEquals(TreasureRarity.EPIC, TreasureTypeRarity.rarityFor(TreasureType.GIFT))
    }

    @Test
    fun scaledRewards_applyMultipliers() {
        assertEquals(10, TreasureRarityRewards.scaledCredits(10, TreasureRarity.COMMON))
        assertEquals(50, TreasureRarityRewards.scaledCredits(10, TreasureRarity.EPIC))
        assertEquals(15L, TreasureRarityRewards.scaledXp(3L, TreasureRarity.EPIC))
    }
}
