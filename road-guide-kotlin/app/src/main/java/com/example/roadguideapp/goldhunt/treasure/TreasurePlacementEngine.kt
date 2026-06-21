package com.example.roadguideapp.goldhunt.treasure

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.engine.GridCell
import com.example.roadguideapp.goldhunt.engine.PlayRegion

internal object TreasurePlacementEngine {
    fun placeInL1Cell(
        region: PlayRegion,
        l1: GridCell,
        slot: Int,
        treasureId: String,
    ): Pair<Double, Double> {
        val cellDeg = GoldHuntConfig.L1_CELL_DEG
        val west = region.west + l1.ix * cellDeg
        val south = region.south + l1.iy * cellDeg
        val fracX = TreasureHash.unitFraction("$treasureId:jx")
        val fracY = TreasureHash.unitFraction("$treasureId:jy")
        val margin = 0.12
        val lng = west + (margin + fracX * (1.0 - 2 * margin)) * cellDeg
        val lat = south + (margin + fracY * (1.0 - 2 * margin)) * cellDeg
        return lat to lng
    }
}
