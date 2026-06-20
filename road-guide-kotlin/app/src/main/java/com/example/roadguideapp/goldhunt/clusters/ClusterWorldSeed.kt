package com.example.roadguideapp.goldhunt.clusters

import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator

/**
 * Stable seeds for offline cluster rolls.
 * Same play region + world version yields identical clusters across app and device restarts.
 */
internal object ClusterWorldSeed {
    const val DEFAULT: String =
        "goldhunt:cluster:world:v${ClusterGenerationConfig.GENERATOR_VERSION}"

    fun fromPlayRegion(region: PlayRegion): String =
        "${DEFAULT}:${TreasureGenerator.playRegionId(region)}"
}

internal object ClusterRegionSeed {
    fun fromPlayRegionId(playRegionId: String): String =
        "goldhunt:cluster:region:v${ClusterGenerationConfig.GENERATOR_VERSION}:$playRegionId"
}
