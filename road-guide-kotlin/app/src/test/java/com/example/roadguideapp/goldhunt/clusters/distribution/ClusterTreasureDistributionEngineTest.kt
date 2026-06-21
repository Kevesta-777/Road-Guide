package com.example.roadguideapp.goldhunt.clusters.distribution

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerationTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClusterTreasureDistributionEngineTest {
    private val region = PlayRegion(
        west = -0.5,
        south = 51.4,
        east = 0.1,
        north = 51.6,
        totalCellsL0 = 1_000_000L,
    )

    @Test
    fun distribute_sameCluster_producesIdenticalTreasures() {
        val cluster = sampleCluster(treasureCount = 4)
        val first = distribute(cluster, ClusterTreasureDistributionMode.NATURAL_SCATTER)
        val second = distribute(cluster, ClusterTreasureDistributionMode.NATURAL_SCATTER)
        assertEquals(first, second)
    }

    @Test
    fun distribute_treasuresStayInsideEffectiveRadius() {
        val cluster = sampleCluster(treasureCount = 5, radiusMeters = 120.0)
        val effectiveRadius = cluster.radiusMeters * (1.0 - ClusterDistributionConfig.EDGE_MARGIN_FRACTION)
        val treasures = distribute(cluster, ClusterTreasureDistributionMode.NATURAL_SCATTER)
        for (treasure in treasures) {
            assertTrue(
                ClusterGeoMath.isWithinRadius(
                    cluster.centerLatitude,
                    cluster.centerLongitude,
                    treasure.lat,
                    treasure.lng,
                    effectiveRadius + 0.5,
                ),
            )
        }
    }

    @Test
    fun distribute_avoidsOverlap() {
        val cluster = sampleCluster(treasureCount = 6, radiusMeters = 150.0)
        val treasures = distribute(cluster, ClusterTreasureDistributionMode.CIRCLE)
        for (i in treasures.indices) {
            for (j in i + 1 until treasures.size) {
                val distance = ClusterGeoMath.distanceMeters(
                    treasures[i].lat,
                    treasures[i].lng,
                    treasures[j].lat,
                    treasures[j].lng,
                )
                assertTrue(distance >= ClusterDistributionConfig.MIN_SEPARATION_M - 0.5)
            }
        }
    }

    @Test
    fun distribute_circleLayout_usesClusterPlacementKind() {
        val cluster = sampleCluster(treasureCount = 3)
        val treasures = distribute(cluster, ClusterTreasureDistributionMode.CIRCLE)
        assertTrue(treasures.isNotEmpty())
        assertTrue(treasures.all { it.placementKind == "CLUSTER_CIRCLE" })
    }

    @Test
    fun distribute_roadBiased_usesAnchorsWhenProvided() {
        val cluster = sampleCluster(treasureCount = 2, clusterType = ClusterType.ROADSIDE_CACHE)
        val hints = ClusterDistributionHints(
            roadAnchors = listOf(
                ClusterAnchorPoint(
                    latitude = cluster.centerLatitude + 0.0002,
                    longitude = cluster.centerLongitude + 0.0001,
                ),
                ClusterAnchorPoint(
                    latitude = cluster.centerLatitude - 0.0001,
                    longitude = cluster.centerLongitude - 0.0002,
                ),
            ),
        )
        val treasures = ClusterTreasureDistributionEngine.distribute(
            cluster = cluster,
            region = region,
            tier = TreasureGenerationTier.STAR_ONLY,
            hints = hints,
            mode = ClusterTreasureDistributionMode.ROAD_BIASED,
        )
        assertTrue(treasures.isNotEmpty())
        assertTrue(treasures.all { it.placementKind == "CLUSTER_ROAD_BIASED" })
    }

    @Test
    fun distribute_poiBiased_fallsBackWhenHintsMissing() {
        val cluster = sampleCluster(treasureCount = 3, clusterType = ClusterType.ANCIENT_VAULT)
        val treasures = ClusterTreasureDistributionEngine.distribute(
            cluster = cluster,
            region = region,
            tier = TreasureGenerationTier.STAR_ONLY,
            hints = ClusterDistributionHints.EMPTY,
            mode = ClusterTreasureDistributionMode.POI_BIASED,
        )
        assertTrue(treasures.isNotEmpty())
        assertTrue(treasures.all { it.placementKind == "CLUSTER_NATURAL_SCATTER" })
    }

    @Test
    fun modeResolver_mapsClusterFamilies() {
        assertEquals(
            ClusterTreasureDistributionMode.ROAD_BIASED,
            ClusterDistributionModeResolver.defaultMode(ClusterType.ROADSIDE_CACHE),
        )
        assertEquals(
            ClusterTreasureDistributionMode.CIRCLE,
            ClusterDistributionModeResolver.defaultMode(ClusterType.TREASURE_GARDEN),
        )
        assertEquals(
            ClusterTreasureDistributionMode.POI_BIASED,
            ClusterDistributionModeResolver.defaultMode(ClusterType.ANCIENT_VAULT),
        )
    }

    private fun distribute(
        cluster: TreasureCluster,
        mode: ClusterTreasureDistributionMode,
    ) = ClusterTreasureDistributionEngine.distribute(
        cluster = cluster,
        region = region,
        tier = TreasureGenerationTier.STAR_ONLY,
        mode = mode,
    )

    private fun sampleCluster(
        treasureCount: Int,
        radiusMeters: Double = 100.0,
        clusterType: ClusterType = ClusterType.TREASURE_GARDEN,
    ): TreasureCluster = TreasureCluster(
        clusterId = "tc:v1:test:L1:10:20:seed4242",
        clusterType = clusterType,
        centerLatitude = 51.5074,
        centerLongitude = -0.1278,
        radiusMeters = radiusMeters,
        treasureCount = treasureCount,
        clusterSeed = 4242L,
    )
}
