package com.example.roadguideapp.goldhunt.treasure.stats

import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import org.junit.Assert.assertEquals
import org.junit.Test

class TreasureRarityStatisticsTest {
    @Test
    fun withIncrement_updatesCorrectTier() {
        val stats = TreasureRarityStatistics.EMPTY
            .withIncrement(TreasureRarity.EPIC)
            .withIncrement(TreasureRarity.EPIC)
            .withIncrement(TreasureRarity.MYTHIC)
        assertEquals(2, stats.epicFound)
        assertEquals(1, stats.mythicFound)
        assertEquals(3, stats.totalFound())
    }

    @Test
    fun entriesOrdered_followsRarityTiers() {
        val stats = TreasureRarityStatistics(
            commonFound = 1,
            uncommonFound = 2,
            rareFound = 3,
            epicFound = 4,
            legendaryFound = 5,
            mythicFound = 6,
        )
        val ordered = stats.entriesOrdered().map { it.first }
        assertEquals(TreasureRarity.ALL_ORDERED, ordered)
    }
}
