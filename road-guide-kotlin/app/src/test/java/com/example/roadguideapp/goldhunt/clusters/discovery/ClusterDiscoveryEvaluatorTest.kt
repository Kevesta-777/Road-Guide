package com.example.roadguideapp.goldhunt.clusters.discovery

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.clusters.distribution.ClusterGeoMath
import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import com.example.roadguideapp.map.LatLngBoundsExt

class ClusterDiscoveryEvaluatorTest {
    private val region = PlayRegion(
        west = -0.5,
        south = 51.4,
        east = 0.1,
        north = 51.6,
        totalCellsL0 = 1_000_000L,
    )

    @Test
    fun evaluate_distanceTrigger_whenPlayerNearCenter() {
        val cluster = sampleCluster(lat = 51.5, lng = -0.2, radiusM = 120.0)
        val trigger = ClusterDiscoveryEvaluator.evaluate(
            cluster = cluster,
            playerLat = 51.5,
            playerLng = -0.2,
            region = region,
            isExplored = { false },
        )
        assertEquals(ClusterDiscoveryTrigger.DISTANCE, trigger)
    }

    @Test
    fun evaluate_explorationTrigger_whenEnoughCellsExplored() {
        val cluster = sampleCluster(lat = 51.5, lng = -0.2, radiusM = 180.0)
        val explored = exploredL0CellIdsInClusterRadius(cluster).toSet()
        val trigger = ClusterDiscoveryEvaluator.evaluate(
            cluster = cluster,
            playerLat = 51.52,
            playerLng = -0.18,
            region = region,
            isExplored = { explored.contains(it) },
        )
        assertEquals(ClusterDiscoveryTrigger.EXPLORATION, trigger)
    }

    @Test
    fun evaluate_returnsNull_whenNeitherThresholdMet() {
        val cluster = sampleCluster(lat = 51.5, lng = -0.2, radiusM = 60.0)
        val trigger = ClusterDiscoveryEvaluator.evaluate(
            cluster = cluster,
            playerLat = 51.52,
            playerLng = -0.18,
            region = region,
            isExplored = { false },
        )
        assertNull(trigger)
    }

    @Test
    fun discoveryDistanceThreshold_usesSmallerOfFixedOrRadius() {
        val small = sampleCluster(radiusM = 40.0)
        val large = sampleCluster(radiusM = 200.0)
        assertEquals(40.0, ClusterDiscoveryEvaluator.discoveryDistanceThresholdM(small), 0.001)
        assertEquals(
            ClusterDiscoveryConfig.DISTANCE_THRESHOLD_M,
            ClusterDiscoveryEvaluator.discoveryDistanceThresholdM(large),
            0.001,
        )
    }

    private fun exploredL0CellIdsInClusterRadius(cluster: TreasureCluster): List<String> {
        val radiusM = cluster.radiusMeters
        val latDelta = radiusM / 111_320.0
        val lngDelta = radiusM / ClusterGeoMath.metersPerDegreeLng(cluster.centerLatitude)
        val bounds = LatLngBoundsExt.fromEdges(
            south = cluster.centerLatitude - latDelta,
            west = cluster.centerLongitude - lngDelta,
            north = cluster.centerLatitude + latDelta,
            east = cluster.centerLongitude + lngDelta,
        )
        return GridIndex.cellsInBounds(bounds, level = 0, region = region, maxCells = 96)
            .filter { cell ->
                ClusterGeoMath.isWithinRadius(
                    cluster.centerLatitude,
                    cluster.centerLongitude,
                    cell.center.latitude,
                    cell.center.longitude,
                    radiusM,
                )
            }
            .take(ClusterDiscoveryConfig.MIN_EXPLORED_L0_CELLS)
            .map { it.id }
    }

    private fun sampleCluster(
        lat: Double = 51.5,
        lng: Double = -0.2,
        radiusM: Double = 100.0,
    ): TreasureCluster = TreasureCluster(
        clusterId = "cluster:test",
        clusterType = ClusterType.EXPLORER_NEST,
        centerLatitude = lat,
        centerLongitude = lng,
        radiusMeters = radiusM,
        treasureCount = 5,
        clusterSeed = 42L,
    )
}
