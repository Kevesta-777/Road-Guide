package com.example.roadguideapp.goldhunt.panorama.targets

import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.treasure.TreasureHash

/**
 * Deterministic hidden-target placement for panorama hunts.
 *
 * Rolls use [TreasureHash] (SHA-256) — fully offline, no [kotlin.random.Random].
 * Same [PanoramaHunt] + slot always yields the same bearing, pitch, radius, and type.
 *
 * Does not render targets in [com.example.roadguideapp.panorama.PanoramaViewerActivity] yet.
 */
internal object PanoramaHiddenTargetGenerator {
    fun generate(hunt: PanoramaHunt, slotIndex: Int = 0): PanoramaHiddenTarget {
        require(slotIndex >= 0) { "slotIndex must be non-negative" }
        val baseSeed = seedKey(hunt, slotIndex)
        val targetType = resolveTargetType(hunt, baseSeed)
        val isLegendary = targetType == PanoramaHiddenTargetType.LEGENDARY_RELIC
        val isAnimated = resolveAnimated(hunt.huntType, targetType, baseSeed)
        return PanoramaHiddenTarget(
            targetId = PanoramaHiddenTargetId.forHuntSlot(hunt.huntId, slotIndex),
            huntId = hunt.huntId,
            slotIndex = slotIndex,
            bearing = rollBearing(baseSeed),
            pitch = rollPitch(baseSeed),
            radius = rollRadius(baseSeed, hunt.difficulty),
            targetType = targetType,
            isAnimated = isAnimated,
            isLegendary = isLegendary,
        )
    }

    fun generateSet(hunt: PanoramaHunt): PanoramaHiddenTargetSet {
        val count = PanoramaHiddenTargetGenerationConfig.targetCountFor(hunt.huntType)
        val targets = (0 until count).map { slot -> generate(hunt, slot) }
        return PanoramaHiddenTargetSet(huntId = hunt.huntId, targets = targets)
    }

    fun targetCount(hunt: PanoramaHunt): Int =
        PanoramaHiddenTargetGenerationConfig.targetCountFor(hunt.huntType)

    /** Exposed for tests — canonical seed string passed to [TreasureHash]. */
    fun seedKey(hunt: PanoramaHunt, slotIndex: Int): String =
        listOf(
            hunt.huntId,
            "hiddenTarget",
            "slot$slotIndex",
            "v${PanoramaHiddenTargetGenerationConfig.GENERATOR_VERSION}",
        ).joinToString("|")

    private fun resolveTargetType(
        hunt: PanoramaHunt,
        baseSeed: String,
    ): PanoramaHiddenTargetType {
        if (hunt.huntType == PanoramaHuntType.RELIC_HUNT) {
            val legendaryRoll = TreasureHash.unitFraction("$baseSeed:legendary")
            if (legendaryRoll < PanoramaHiddenTargetGenerationConfig.LEGENDARY_ROLL_THRESHOLD) {
                return PanoramaHiddenTargetType.LEGENDARY_RELIC
            }
        }
        return PanoramaHiddenTargetType.defaultFor(hunt.huntType)
    }

    private fun resolveAnimated(
        huntType: PanoramaHuntType,
        targetType: PanoramaHiddenTargetType,
        baseSeed: String,
    ): Boolean {
        if (!targetType.supportsAnimation) return false
        if (huntType != PanoramaHuntType.OBJECT_HUNT &&
            huntType != PanoramaHuntType.PANORAMA_PUZZLE
        ) {
            return false
        }
        val roll = TreasureHash.unitFraction("$baseSeed:animated")
        return roll < PanoramaHiddenTargetGenerationConfig.ANIMATED_ROLL_THRESHOLD
    }

    private fun rollBearing(baseSeed: String): Float {
        val fraction = TreasureHash.unitFraction("$baseSeed:bearing")
        return (fraction * 360f).toFloat()
    }

    private fun rollPitch(baseSeed: String): Float {
        val fraction = TreasureHash.unitFraction("$baseSeed:pitch")
        val min = PanoramaHiddenTargetGenerationConfig.PLACEMENT_MIN_PITCH
        val max = PanoramaHiddenTargetGenerationConfig.PLACEMENT_MAX_PITCH
        return min + (fraction * (max - min)).toFloat()
    }

    private fun rollRadius(baseSeed: String, difficulty: Int): Float {
        val clampedDifficulty = difficulty.coerceIn(1, 5)
        val t = (clampedDifficulty - 1) / 4f
        val minAt = lerp(
            PanoramaHiddenTargetGenerationConfig.RADIUS_AT_DIFFICULTY_1_MIN,
            PanoramaHiddenTargetGenerationConfig.RADIUS_AT_DIFFICULTY_5_MIN,
            t,
        )
        val maxAt = lerp(
            PanoramaHiddenTargetGenerationConfig.RADIUS_AT_DIFFICULTY_1_MAX,
            PanoramaHiddenTargetGenerationConfig.RADIUS_AT_DIFFICULTY_5_MAX,
            t,
        )
        val fraction = TreasureHash.unitFraction("$baseSeed:radius")
        return minAt + (fraction * (maxAt - minAt)).toFloat()
    }

    private fun lerp(start: Float, end: Float, t: Float): Float =
        start + (end - start) * t.coerceIn(0f, 1f)
}
