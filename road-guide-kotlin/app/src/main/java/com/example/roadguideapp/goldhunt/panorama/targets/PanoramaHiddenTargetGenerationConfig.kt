package com.example.roadguideapp.goldhunt.panorama.targets

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType

/**
 * Offline tuning for [PanoramaHiddenTargetGenerator].
 */
internal object PanoramaHiddenTargetGenerationConfig {
    const val GENERATOR_VERSION = 1

    /** Comfortable vertical placement band (inside viewer pitch clamp). */
    const val PLACEMENT_MIN_PITCH = -55f
    const val PLACEMENT_MAX_PITCH = 55f

    /** Angular radius bounds scaled by hunt difficulty (1 = easy/large, 5 = hard/small). */
    const val RADIUS_AT_DIFFICULTY_1_MIN = 14f
    const val RADIUS_AT_DIFFICULTY_1_MAX = 20f
    const val RADIUS_AT_DIFFICULTY_5_MIN = 4f
    const val RADIUS_AT_DIFFICULTY_5_MAX = 9f

    /** Rare legendary upgrade on relic hunts (future viewer VFX). */
    const val LEGENDARY_ROLL_THRESHOLD = 0.08

    /** Animated target roll for eligible hunt families. */
    const val ANIMATED_ROLL_THRESHOLD = 0.18

    fun targetCountFor(huntType: PanoramaHuntType): Int = when (huntType) {
        PanoramaHuntType.HIDDEN_SYMBOL -> 1
        PanoramaHuntType.OBJECT_HUNT -> 2
        PanoramaHuntType.PANORAMA_PUZZLE -> 2
        PanoramaHuntType.SECRET_CODE -> 3
        PanoramaHuntType.RELIC_HUNT -> 1
    }
}
