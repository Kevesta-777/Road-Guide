package com.example.roadguideapp.goldhunt.panorama

import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator

/**
 * Stable world seeds for offline panorama hunt rolls.
 * Same play region + generator version yields identical hunts across app restarts.
 */
internal object PanoramaHuntWorldSeed {
    const val DEFAULT: String =
        "goldhunt:panorama:world:v${PanoramaHuntGenerationConfig.GENERATOR_VERSION}"

    fun fromPlayRegion(region: PlayRegion): String =
        "${DEFAULT}:${TreasureGenerator.playRegionId(region)}"
}
