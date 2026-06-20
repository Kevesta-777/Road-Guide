package com.example.roadguideapp.goldhunt.clusters

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClusterTypeTest {
    @Test
    fun allOrdered_containsFiveFamiliesInTierOrder() {
        assertEquals(5, ClusterType.ALL_ORDERED.size)
        assertEquals(ClusterType.ROADSIDE_CACHE, ClusterType.ALL_ORDERED.first())
        assertEquals(ClusterType.LEGENDARY_HOARD, ClusterType.ALL_ORDERED.last())
        assertTrue(ClusterType.ALL_ORDERED.zipWithNext().all { (a, b) -> a.tier < b.tier })
    }

    @Test
    fun fromId_resolvesCaseInsensitive() {
        assertEquals(ClusterType.TREASURE_GARDEN, ClusterType.fromId("treasure_garden"))
        assertNull(ClusterType.fromId("UNKNOWN_CLUSTER"))
    }

    @Test
    fun roadsideCache_hasBaselineTuning() {
        val type = ClusterType.ROADSIDE_CACHE
        assertEquals("Roadside Cache", type.displayName)
        assertEquals(420, type.rarityWeight)
        assertEquals(1, type.minimumTreasureCount)
        assertEquals(2, type.maximumTreasureCount)
        assertEquals(1.0, type.rewardMultiplier, 0.001)
        assertNull(type.achievementKey)
    }

    @Test
    fun legendaryHoard_reservesFutureHooks() {
        val type = ClusterType.LEGENDARY_HOARD
        assertNotNull(type.achievementKey)
        assertNotNull(type.storyFragmentKey)
        assertNotNull(type.radarHighlightKey)
        assertEquals(6, type.minimumTreasureCount)
        assertEquals(12, type.maximumTreasureCount)
    }

    @Test
    fun catalog_totalRarityWeight_matchesEnumSum() {
        val expected = ClusterType.entries.sumOf { it.rarityWeight }
        assertEquals(expected, ClusterCatalog.totalRarityWeight())
    }
}
