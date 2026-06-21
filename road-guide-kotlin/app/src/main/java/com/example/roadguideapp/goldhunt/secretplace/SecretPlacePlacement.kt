package com.example.roadguideapp.goldhunt.secretplace

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.engine.GridCell
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureHash

internal object SecretPlacePlacement {
    fun placeInL2Cell(
        region: PlayRegion,
        l2: GridCell,
        slot: Int,
        secretPlaceId: String,
    ): Pair<Double, Double> {
        val cellDeg = GoldHuntConfig.L2_CELL_DEG
        val west = region.west + l2.ix * cellDeg
        val south = region.south + l2.iy * cellDeg
        val fracX = TreasureHash.unitFraction("$secretPlaceId:jx")
        val fracY = TreasureHash.unitFraction("$secretPlaceId:jy")
        val margin = 0.18
        val lng = west + (margin + fracX * (1.0 - 2 * margin)) * cellDeg
        val lat = south + (margin + fracY * (1.0 - 2 * margin)) * cellDeg
        return lat to lng
    }
}
