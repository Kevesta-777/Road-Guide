package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.TreasureClusterId
import com.example.roadguideapp.goldhunt.treasure.TreasureId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ClusterCompletionResolverTest {
    @Test
    fun parseClusterIdFromTreasureId_roundTripsClusterId() {
        val clusterId = TreasureClusterId.forL1Anchor(
            playRegionId = "r:-0.5000:51.4000",
            regionL1Id = "L1:3:7",
            clusterSeed = 42L,
        )
        val treasureId = TreasureId.forClusterSlot(clusterId, index = 2)
        assertEquals(clusterId, ClusterCompletionResolver.parseClusterIdFromTreasureId(treasureId))
    }

    @Test
    fun treasureClusterId_parse_extractsAnchorParts() {
        val clusterId = TreasureClusterId.forL1Anchor(
            playRegionId = "r:-0.5000:51.4000",
            regionL1Id = "L1:3:7",
            clusterSeed = 42L,
        )
        val parsed = TreasureClusterId.parse(clusterId)
        assertNotNull(parsed)
        requireNotNull(parsed)
        assertEquals("r:-0.5000:51.4000", parsed.playRegionId)
        assertEquals(1, parsed.level)
        assertEquals(3, parsed.tileX)
        assertEquals(7, parsed.tileY)
        assertEquals(42L, parsed.clusterSeed)
    }
}
