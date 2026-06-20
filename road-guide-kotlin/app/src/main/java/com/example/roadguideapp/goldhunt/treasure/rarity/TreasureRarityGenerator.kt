package com.example.roadguideapp.goldhunt.treasure.rarity

import com.example.roadguideapp.goldhunt.treasure.TreasureHash

/**
 * Deterministic offline rarity roller for treasure instances.
 *
 * Same [tile], [treasureId], and [worldSeed] always yield the same [TreasureRarity]
 * (SHA-256 via [TreasureHash], no randomness, no persistence).
 *
 * Does not modify [com.example.roadguideapp.goldhunt.treasure.TreasureGenerator] spawn logic.
 */
internal object TreasureRarityGenerator {
    private val weightedRarities: List<Pair<TreasureRarity, Double>> by lazy {
        val total = TreasureRarity.TOTAL_RARITY_WEIGHT.toDouble().coerceAtLeast(1.0)
        TreasureRarity.ALL_ORDERED.map { rarity ->
            rarity to rarity.rarityWeight / total
        }
    }

    /**
     * @param tile Grid tile for the treasure slot (typically L1 ix/iy + slot).
     * @param treasureId Stable treasure instance id.
     * @param worldSeed Offline world seed (use [TreasureWorldSeed.fromPlayRegion]).
     */
    fun generate(
        tile: TreasureTileCoordinate,
        treasureId: String,
        worldSeed: String,
    ): TreasureRarity {
        require(treasureId.isNotBlank()) { "treasureId must not be blank" }
        require(worldSeed.isNotBlank()) { "worldSeed must not be blank" }
        val roll = TreasureHash.unitFraction(rollSeed(tile, treasureId, worldSeed))
        return pickWeighted(roll)
    }

    /** Exposed for tests — canonical seed string passed to [TreasureHash]. */
    fun rollSeed(
        tile: TreasureTileCoordinate,
        treasureId: String,
        worldSeed: String,
    ): String = listOf(worldSeed, "rarity", tile.encode(), treasureId).joinToString("|")

    private fun pickWeighted(roll: Double): TreasureRarity {
        var cumulative = 0.0
        for ((rarity, weight) in weightedRarities) {
            cumulative += weight
            if (roll < cumulative) return rarity
        }
        return TreasureRarity.COMMON
    }
}
