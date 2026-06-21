package com.example.roadguideapp.goldhunt.clusters

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureHash

internal object ClusterPlacementEngine {
    fun placeInTile(
        region: PlayRegion,
        tile: ClusterTileCoordinate,
        clusterId: String,
    ): Pair<Double, Double> {
        val cellDeg = when (tile.level) {
            1 -> GoldHuntConfig.L1_CELL_DEG
            2 -> GoldHuntConfig.L2_CELL_DEG
            else -> GoldHuntConfig.L0_CELL_DEG
        }
        val west = region.west + tile.tileX * cellDeg
        val south = region.south + tile.tileY * cellDeg
        val fracX = TreasureHash.unitFraction("$clusterId:cx")
        val fracY = TreasureHash.unitFraction("$clusterId:cy")
        val margin = ClusterGenerationConfig.CENTER_MARGIN
        val lng = west + (margin + fracX * (1.0 - 2 * margin)) * cellDeg
        val lat = south + (margin + fracY * (1.0 - 2 * margin)) * cellDeg
        return lat to lng
    }
}
