package com.example.roadguideapp.goldhunt.treasure

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.clusters.ClusterSchema
import com.example.roadguideapp.goldhunt.engine.GridCell

internal object TreasureId {
    fun forL1Slot(
        playRegionId: String,
        l1: GridCell,
        slot: Int,
    ): String =
        "tr:v${GoldHuntConfig.TREASURE_GENERATOR_VERSION}:$playRegionId:L1:${l1.ix}:${l1.iy}:s$slot"

    fun forHoard(
        playRegionId: String,
        hoard: TreasureHoardRegion,
        index: Int,
    ): String =
        "tr:hoard:v${GoldHuntConfig.TREASURE_GENERATOR_VERSION}:$playRegionId:" +
            "${hoard.regionName}:${hoard.seed}:i$index"

    fun forClusterSlot(
        clusterId: String,
        index: Int,
    ): String = "tr:cluster:v${ClusterSchema.VERSION}:$clusterId:i$index"
}
