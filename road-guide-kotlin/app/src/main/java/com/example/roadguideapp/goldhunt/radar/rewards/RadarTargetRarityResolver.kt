package com.example.roadguideapp.goldhunt.radar.rewards

import com.example.roadguideapp.goldhunt.radar.RadarSignal
import com.example.roadguideapp.goldhunt.radar.RadarTargetCategory
import com.example.roadguideapp.goldhunt.treasure.TreasureHash

/**
 * Deterministic target rarity rolls for radar detections (offline, no DB lookup).
 */
internal object RadarTargetRarityResolver {
    fun resolve(signal: RadarSignal): RadarTargetRarity {
        val roll = TreasureHash.unitFraction(seed(signal))
        val band = rarityBand(signal.targetCategory)
        val tierSpan = band.last - band.first + 1
        val tier = band.first + (roll * tierSpan).toInt().coerceIn(0, tierSpan - 1)
        return RadarTargetRarity.fromTier(tier)
    }

    fun seed(signal: RadarSignal): String =
        listOf(
            "radarTargetRarity",
            signal.targetCategory.id,
            signal.targetId,
            "v${RadarRewardSchema.VERSION}",
        ).joinToString("|")

    private fun rarityBand(category: RadarTargetCategory): IntRange = when (category) {
        RadarTargetCategory.TREASURE -> 1..3
        RadarTargetCategory.CLUSTER -> 2..4
        RadarTargetCategory.SECRET_PLACE -> 3..4
        RadarTargetCategory.STORY_FRAGMENT -> 4..5
        RadarTargetCategory.LEGENDARY_RELIC -> 5..6
    }
}
