package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarDetectionScopeTest {
    @Test
    fun categoriesDetectableBy_basicRadarOnlyTreasures() {
        val categories = RadarDetectionScope.categoriesDetectableBy(RadarType.BASIC_RADAR)
        assertEquals(setOf(RadarTargetCategory.TREASURE), categories)
    }

    @Test
    fun categoriesDetectableBy_legendaryRadar_includesRelicStoryAndCluster() {
        val categories = RadarDetectionScope.categoriesDetectableBy(RadarType.LEGENDARY_RADAR)
        assertTrue(RadarTargetCategory.LEGENDARY_RELIC in categories)
        assertTrue(RadarTargetCategory.STORY_FRAGMENT in categories)
        assertTrue(RadarTargetCategory.CLUSTER in categories)
        assertFalse(RadarTargetCategory.TREASURE in categories)
    }

    @Test
    fun priorityWeight_prefersPrimaryCategory() {
        val primary = RadarDetectionScope.priorityWeight(
            RadarTargetCategory.CLUSTER,
            RadarType.CLUSTER_RADAR,
        )
        val secondary = RadarDetectionScope.priorityWeight(
            RadarTargetCategory.TREASURE,
            RadarType.CLUSTER_RADAR,
        )
        assertTrue(primary > secondary)
    }
}
