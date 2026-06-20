package com.example.roadguideapp.goldhunt.clusters

import com.example.roadguideapp.goldhunt.engine.PlayRegion

/**
 * Inputs for a single deterministic cluster roll at a grid tile.
 */
internal data class ClusterGenerationRequest(
    val tile: ClusterTileCoordinate,
    val worldSeed: String,
    val regionSeed: String,
    val playRegionId: String,
    val region: PlayRegion,
)
