package com.example.roadguideapp.goldhunt.clusters

import com.example.roadguideapp.goldhunt.engine.GridCell

/**
 * Grid tile identity for deterministic cluster rolls.
 * Aligns with L1 procedural anchors used by [TreasureClusterId].
 */
internal data class ClusterTileCoordinate(
    val level: Int,
    val tileX: Int,
    val tileY: Int,
    val slot: Int = ClusterGenerationConfig.CLUSTER_SLOT_PER_L1,
) {
    init {
        require(level >= 0) { "level must be non-negative" }
        require(slot >= 0) { "slot must be non-negative" }
    }

    fun encode(): String = "L$level:$tileX:$tileY:s$slot"

    val regionL1Id: String
        get() = "L$level:$tileX:$tileY"

    companion object {
        fun fromL1Cell(l1: GridCell, slot: Int = ClusterGenerationConfig.CLUSTER_SLOT_PER_L1): ClusterTileCoordinate =
            ClusterTileCoordinate(
                level = l1.level,
                tileX = l1.ix,
                tileY = l1.iy,
                slot = slot,
            )
    }
}
