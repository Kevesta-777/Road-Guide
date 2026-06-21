package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarTypeTest {
    @Test
    fun allOrdered_containsSixFamiliesInUnlockOrder() {
        assertEquals(6, RadarType.ALL_ORDERED.size)
        assertEquals(RadarType.BASIC_RADAR, RadarType.ALL_ORDERED.first())
        assertEquals(RadarType.LEGENDARY_RADAR, RadarType.ALL_ORDERED.last())
        assertTrue(RadarType.ALL_ORDERED.zipWithNext().all { (a, b) -> a.unlockLevel <= b.unlockLevel })
    }

    @Test
    fun fromId_resolvesCaseInsensitive() {
        assertEquals(RadarType.TREASURE_RADAR, RadarType.fromId("treasure_radar"))
        assertNull(RadarType.fromId("UNKNOWN_RADAR"))
    }

    @Test
    fun basicRadar_hasIntroductoryTuning() {
        val type = RadarType.BASIC_RADAR
        assertEquals("Basic Radar", type.displayName)
        assertEquals(1, type.unlockLevel)
        assertEquals(500, type.detectionRangeMeters)
        assertEquals(60, type.cooldownSeconds)
        val template = RadarTemplate.from(type)
        assertNotNull(template.achievementKey)
        assertNull(template.seasonalEventKey)
        assertNull(template.legendaryRelicKey)
    }

    @Test
    fun legendaryRadar_reservesFutureHooks() {
        val type = RadarType.LEGENDARY_RADAR
        assertEquals(15, type.unlockLevel)
        assertEquals(3_000, type.detectionRangeMeters)
        assertEquals(300, type.cooldownSeconds)
        val template = RadarTemplate.from(type)
        assertNotNull(template.achievementKey)
        assertNotNull(template.seasonalEventKey)
        assertNotNull(template.legendaryRelicKey)
    }

    @Test
    fun storyRadar_reservesSeasonalAndRelicHooks() {
        val template = RadarTemplate.from(RadarType.STORY_RADAR)
        assertNotNull(template.seasonalEventKey)
        assertNotNull(template.legendaryRelicKey)
    }
}
