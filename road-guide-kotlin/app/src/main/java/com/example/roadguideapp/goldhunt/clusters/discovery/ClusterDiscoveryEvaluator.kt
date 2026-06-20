package com.example.roadguideapp.goldhunt.clusters.discovery

import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.clusters.distribution.ClusterGeoMath
import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.map.LatLngBoundsExt

internal object ClusterDiscoveryEvaluator {
    fun discoveryDistanceThresholdM(cluster: TreasureCluster): Double =
        minOf(ClusterDiscoveryConfig.DISTANCE_THRESHOLD_M, cluster.radiusMeters)

    fun evaluateDistance(
        cluster: TreasureCluster,
        playerLat: Double,
        playerLng: Double,
    ): Boolean {
        val distanceM = ClusterGeoMath.distanceMeters(
            playerLat,
            playerLng,
            cluster.centerLatitude,
            cluster.centerLongitude,
        )
        return distanceM <= discoveryDistanceThresholdM(cluster)
    }

    fun countExploredL0CellsInRadius(
        cluster: TreasureCluster,
        region: PlayRegion,
        isExplored: (String) -> Boolean,
    ): Int {
        val radiusM = cluster.radiusMeters
        val latDelta = radiusM / 111_320.0
        val lngDelta = radiusM / ClusterGeoMath.metersPerDegreeLng(cluster.centerLatitude)
        val bounds = LatLngBoundsExt.fromEdges(
            south = cluster.centerLatitude - latDelta,
            west = cluster.centerLongitude - lngDelta,
            north = cluster.centerLatitude + latDelta,
            east = cluster.centerLongitude + lngDelta,
        )
        val cells = GridIndex.cellsInBounds(
            bounds = bounds,
            level = 0,
            region = region,
            maxCells = 96,
        )
        return cells.count { cell ->
            ClusterGeoMath.isWithinRadius(
                centerLat = cluster.centerLatitude,
                centerLng = cluster.centerLongitude,
                lat = cell.center.latitude,
                lng = cell.center.longitude,
                radiusM = radiusM,
            ) && isExplored(cell.id)
        }
    }

    fun evaluate(
        cluster: TreasureCluster,
        playerLat: Double,
        playerLng: Double,
        region: PlayRegion,
        isExplored: (String) -> Boolean,
    ): ClusterDiscoveryTrigger? {
        if (evaluateDistance(cluster, playerLat, playerLng)) {
            return ClusterDiscoveryTrigger.DISTANCE
        }
        if (countExploredL0CellsInRadius(cluster, region, isExplored) >=
            ClusterDiscoveryConfig.MIN_EXPLORED_L0_CELLS
        ) {
            return ClusterDiscoveryTrigger.EXPLORATION
        }
        return null
    }
}
