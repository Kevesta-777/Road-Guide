package com.example.roadguideapp.goldhunt.treasure.events

import com.example.roadguideapp.goldhunt.treasure.TreasureGenerationTier
import com.example.roadguideapp.goldhunt.treasure.TreasureHash
import com.example.roadguideapp.goldhunt.treasure.TreasureType

/** Event-biased treasure type picker, normalized per unlock tier. */
internal object EventTreasureTypePicker {
    fun pick(
        seed: String,
        profile: EventTreasureProfile,
        tier: TreasureGenerationTier,
    ): TreasureType {
        val weights = normalizedWeights(profile, tier)
        val roll = TreasureHash.unitFraction("$seed:type")
        var cumulative = 0.0
        for ((type, weight) in weights) {
            cumulative += weight
            if (roll < cumulative) return type
        }
        return weights.last().first
    }

    internal fun normalizedWeights(
        profile: EventTreasureProfile,
        tier: TreasureGenerationTier,
    ): List<Pair<TreasureType, Double>> {
        val allowedTypes = allowedTypesForTier(tier)
        val filtered = profile.typeWeights
            .filterKeys { it in allowedTypes }
            .ifEmpty {
                mapOf(TreasureType.STAR to 1)
            }
        val total = filtered.values.sum()
        return filtered.map { (type, weight) -> type to weight.toDouble() / total }
    }

    private fun allowedTypesForTier(tier: TreasureGenerationTier): Set<TreasureType> =
        when (tier) {
            TreasureGenerationTier.STAR_ONLY ->
                setOf(TreasureType.STAR)
            TreasureGenerationTier.STAR_AND_FLOWER ->
                setOf(TreasureType.STAR, TreasureType.FLOWER)
            TreasureGenerationTier.STAR_FLOWER_AND_CRYSTAL ->
                setOf(TreasureType.STAR, TreasureType.FLOWER, TreasureType.CRYSTAL)
            TreasureGenerationTier.STAR_FLOWER_CRYSTAL_AND_GIFT ->
                setOf(TreasureType.STAR, TreasureType.FLOWER, TreasureType.CRYSTAL, TreasureType.GIFT)
        }
}
