package com.example.roadguideapp.goldhunt.treasure.rarity

import com.example.roadguideapp.goldhunt.engine.GridCell

/**
 * Grid tile identity for deterministic treasure rolls.
 * Aligns with L1 procedural slots used by [com.example.roadguideapp.goldhunt.treasure.TreasureId].
 */
internal data class TreasureTileCoordinate(
    val level: Int,
    val tileX: Int,
    val tileY: Int,
    val slot: Int = 0,
) {
    init {
        require(level >= 0) { "level must be non-negative" }
        require(slot >= 0) { "slot must be non-negative" }
    }

    fun encode(): String = "L$level:$tileX:$tileY:s$slot"

    companion object {
        fun fromL1Cell(l1: GridCell, slot: Int): TreasureTileCoordinate = TreasureTileCoordinate(
            level = l1.level,
            tileX = l1.ix,
            tileY = l1.iy,
            slot = slot,
        )
    }
}
