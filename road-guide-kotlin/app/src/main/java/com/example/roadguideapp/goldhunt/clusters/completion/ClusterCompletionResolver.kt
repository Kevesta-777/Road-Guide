package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.ClusterRegionSeed
import com.example.roadguideapp.goldhunt.clusters.ClusterSchema
import com.example.roadguideapp.goldhunt.clusters.ClusterTileCoordinate
import com.example.roadguideapp.goldhunt.clusters.ClusterWorldSeed
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.clusters.TreasureClusterGenerator
import com.example.roadguideapp.goldhunt.clusters.TreasureClusterId
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator

internal object ClusterCompletionResolver {
    fun parseClusterIdFromTreasureId(treasureId: String): String? {
        val prefix = "tr:cluster:v${ClusterSchema.VERSION}:"
        if (!treasureId.startsWith(prefix)) return null
        val remainder = treasureId.removePrefix(prefix)
        val slotMarker = ":i"
        val slotIndex = remainder.lastIndexOf(slotMarker)
        if (slotIndex <= 0) return null
        return remainder.substring(0, slotIndex)
    }

    fun resolveCluster(clusterId: String, region: PlayRegion): TreasureCluster? {
        val anchor = TreasureClusterId.parse(clusterId) ?: return null
        val tile = ClusterTileCoordinate(
            level = anchor.level,
            tileX = anchor.tileX,
            tileY = anchor.tileY,
        )
        val cluster = TreasureClusterGenerator.generateAtTile(
            tile = tile,
            worldSeed = ClusterWorldSeed.fromPlayRegion(region),
            regionSeed = ClusterRegionSeed.fromPlayRegionId(TreasureGenerator.playRegionId(region)),
            playRegionId = TreasureGenerator.playRegionId(region),
            region = region,
        ) ?: return null
        return cluster.takeIf { it.clusterId == clusterId }
    }
}
