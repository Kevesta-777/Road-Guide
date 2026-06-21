package com.example.roadguideapp.goldhunt.panorama

import com.example.roadguideapp.goldhunt.treasure.TreasureHash

/** Picks a [PanoramaHuntType] using configured spawn weights. */
internal object PanoramaHuntTypePicker {
    private val weightedTypes: List<Pair<PanoramaHuntType, Double>> by lazy {
        val raw = listOf(
            PanoramaHuntType.HIDDEN_SYMBOL to PanoramaHuntGenerationConfig.WEIGHT_HIDDEN_SYMBOL,
            PanoramaHuntType.OBJECT_HUNT to PanoramaHuntGenerationConfig.WEIGHT_OBJECT_HUNT,
            PanoramaHuntType.PANORAMA_PUZZLE to PanoramaHuntGenerationConfig.WEIGHT_PANORAMA_PUZZLE,
            PanoramaHuntType.SECRET_CODE to PanoramaHuntGenerationConfig.WEIGHT_SECRET_CODE,
            PanoramaHuntType.RELIC_HUNT to PanoramaHuntGenerationConfig.WEIGHT_RELIC_HUNT,
        )
        val total = raw.sumOf { it.second }
        if (total <= 0.0) {
            PanoramaHuntType.ALL_ORDERED.map { it to 1.0 / PanoramaHuntType.ALL_ORDERED.size }
        } else {
            raw.map { (type, weight) -> type to weight / total }
        }
    }

    fun pick(seed: String): PanoramaHuntType {
        val roll = TreasureHash.unitFraction("$seed:type")
        var cumulative = 0.0
        for ((type, weight) in weightedTypes) {
            cumulative += weight
            if (roll < cumulative) return type
        }
        return weightedTypes.last().first
    }
}
