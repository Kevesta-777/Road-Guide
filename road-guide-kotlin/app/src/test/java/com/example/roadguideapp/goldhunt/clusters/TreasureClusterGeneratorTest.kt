package com.example.roadguideapp.goldhunt.clusters

import com.example.roadguideapp.goldhunt.engine.PlayRegion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TreasureClusterGeneratorTest {
    private val region = PlayRegion(
        west = -0.5,
        south = 51.4,
        east = 0.1,
        north = 51.6,
        totalCellsL0 = 1_000_000L,
    )
    private val playRegionId = "r:-0.5000:51.4000"
    private val worldSeed = ClusterWorldSeed.DEFAULT
    private val regionSeed = ClusterRegionSeed.fromPlayRegionId(playRegionId)

    @Test
    fun generate_sameInputs_producesIdenticalCluster() {
        val tile = ClusterTileCoordinate(level = 1, tileX = 12, tileY = 34, slot = 0)
        val request = ClusterGenerationRequest(
            tile = tile,
            worldSeed = worldSeed,
            regionSeed = regionSeed,
            playRegionId = playRegionId,
            region = region,
        )
        val first = TreasureClusterGenerator.generate(request)
        val second = TreasureClusterGenerator.generate(request)
        assertEquals(first, second)
    }

    @Test
    fun generate_differentTiles_produceDifferentClusterIds() {
        val a = generateAt(ClusterTileCoordinate(level = 1, tileX = 1, tileY = 1))
        val b = generateAt(ClusterTileCoordinate(level = 1, tileX = 2, tileY = 2))
        if (a != null && b != null) {
            assertTrue(a.clusterId != b.clusterId)
        }
    }

    @Test
    fun generate_respectsClusterTypeTreasureBounds() {
        val spawned = findSpawnedCluster(ClusterTileCoordinate(level = 1, tileX = 5, tileY = 7))
        assertNotNull(spawned)
        val cluster = requireNotNull(spawned)
        assertTrue(cluster.treasureCount >= cluster.clusterType.minimumTreasureCount)
        assertTrue(cluster.treasureCount <= cluster.clusterType.maximumTreasureCount)
        assertTrue(cluster.radiusMeters >= ClusterGenerationConfig.MIN_RADIUS_M)
        assertTrue(cluster.radiusMeters <= ClusterGenerationConfig.MAX_RADIUS_M)
    }

    @Test
    fun generate_supportsAllFiveClusterFamilies() {
        val seen = mutableSetOf<ClusterType>()
        for (x in 0 until 40) {
            for (y in 0 until 40) {
                val cluster = generateAt(ClusterTileCoordinate(level = 1, tileX = x, tileY = y))
                    ?: continue
                seen += cluster.clusterType
                if (seen.size == ClusterType.entries.size) break
            }
            if (seen.size == ClusterType.entries.size) break
        }
        assertEquals(ClusterType.entries.toSet(), seen)
    }

    @Test
    fun seedKey_isStableAcrossCalls() {
        val tile = ClusterTileCoordinate(level = 1, tileX = 3, tileY = 9)
        val request = ClusterGenerationRequest(
            tile = tile,
            worldSeed = worldSeed,
            regionSeed = regionSeed,
            playRegionId = playRegionId,
            region = region,
        )
        assertEquals(
            TreasureClusterGenerator.seedKey(request),
            TreasureClusterGenerator.seedKey(request),
        )
    }

    private fun generateAt(tile: ClusterTileCoordinate): TreasureCluster? =
        TreasureClusterGenerator.generateAtTile(
            tile = tile,
            worldSeed = worldSeed,
            regionSeed = regionSeed,
            playRegionId = playRegionId,
            region = region,
        )

    private fun findSpawnedCluster(tile: ClusterTileCoordinate): TreasureCluster? {
        var current = tile
        repeat(64) { attempt ->
            val cluster = generateAt(current)
            if (cluster != null) return cluster
            current = current.copy(tileX = current.tileX + attempt + 1, tileY = current.tileY + attempt)
        }
        return null
    }
}
