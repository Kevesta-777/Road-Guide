package com.example.roadguideapp.goldhunt.treasure.stats

import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

/**
 * Lifetime counts of collected treasures by rolled rarity tier.
 */
internal data class TreasureRarityStatistics(
    val commonFound: Int = 0,
    val uncommonFound: Int = 0,
    val rareFound: Int = 0,
    val epicFound: Int = 0,
    val legendaryFound: Int = 0,
    val mythicFound: Int = 0,
) {
    fun countFor(rarity: TreasureRarity): Int = when (rarity) {
        TreasureRarity.COMMON -> commonFound
        TreasureRarity.UNCOMMON -> uncommonFound
        TreasureRarity.RARE -> rareFound
        TreasureRarity.EPIC -> epicFound
        TreasureRarity.LEGENDARY -> legendaryFound
        TreasureRarity.MYTHIC -> mythicFound
    }

    fun totalFound(): Int =
        commonFound + uncommonFound + rareFound + epicFound + legendaryFound + mythicFound

    fun withIncrement(rarity: TreasureRarity, delta: Int = 1): TreasureRarityStatistics {
        require(delta >= 0) { "delta must be non-negative" }
        if (delta == 0) return this
        return when (rarity) {
            TreasureRarity.COMMON -> copy(commonFound = commonFound + delta)
            TreasureRarity.UNCOMMON -> copy(uncommonFound = uncommonFound + delta)
            TreasureRarity.RARE -> copy(rareFound = rareFound + delta)
            TreasureRarity.EPIC -> copy(epicFound = epicFound + delta)
            TreasureRarity.LEGENDARY -> copy(legendaryFound = legendaryFound + delta)
            TreasureRarity.MYTHIC -> copy(mythicFound = mythicFound + delta)
        }
    }

    /** Ordered pairs for UI and achievement evaluation. */
    fun entriesOrdered(): List<Pair<TreasureRarity, Int>> =
        TreasureRarity.ALL_ORDERED.map { it to countFor(it) }

    companion object {
        val EMPTY = TreasureRarityStatistics()
    }
}
