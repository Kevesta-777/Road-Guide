package com.example.roadguideapp.goldhunt.treasure

import com.example.roadguideapp.goldhunt.GoldHuntConfig

/** Picks a treasure type using configured spawn ratios, normalized per unlock tier. */
internal object TreasureTypePicker {
    fun pick(seed: String, tier: TreasureGenerationTier): TreasureType {
        val roll = TreasureHash.unitFraction("$seed:type")
        var cumulative = 0.0
        for ((type, weight) in normalizedWeights(tier)) {
            cumulative += weight
            if (roll < cumulative) return type
        }
        return normalizedWeights(tier).last().first
    }

    private fun normalizedWeights(tier: TreasureGenerationTier): List<Pair<TreasureType, Double>> {
        val allowed = when (tier) {
            TreasureGenerationTier.STAR_ONLY ->
                listOf(TreasureType.STAR to GoldHuntConfig.SPAWN_RATIO_STAR)
            TreasureGenerationTier.STAR_AND_FLOWER -> listOf(
                TreasureType.STAR to GoldHuntConfig.SPAWN_RATIO_STAR,
                TreasureType.FLOWER to GoldHuntConfig.SPAWN_RATIO_FLOWER,
            )
            TreasureGenerationTier.STAR_FLOWER_AND_CRYSTAL -> listOf(
                TreasureType.STAR to GoldHuntConfig.SPAWN_RATIO_STAR,
                TreasureType.FLOWER to GoldHuntConfig.SPAWN_RATIO_FLOWER,
                TreasureType.CRYSTAL to GoldHuntConfig.SPAWN_RATIO_CRYSTAL,
            )
            TreasureGenerationTier.STAR_FLOWER_CRYSTAL_AND_GIFT -> listOf(
                TreasureType.STAR to GoldHuntConfig.SPAWN_RATIO_STAR,
                TreasureType.FLOWER to GoldHuntConfig.SPAWN_RATIO_FLOWER,
                TreasureType.CRYSTAL to GoldHuntConfig.SPAWN_RATIO_CRYSTAL,
                TreasureType.GIFT to GoldHuntConfig.SPAWN_RATIO_GIFT,
            )
        }
        val total = allowed.sumOf { it.second }
        if (total <= 0.0) return listOf(TreasureType.STAR to 1.0)
        return allowed.map { (type, weight) -> type to weight / total }
    }
}
