package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarCatalogTest {
    @Test
    fun templateFor_matchesEnumTuning() {
        val template = RadarCatalog.templateFor(RadarType.CLUSTER_RADAR)
        assertEquals("Cluster Radar", template.displayName)
        assertEquals(7, template.unlockLevel)
        assertEquals(1_500, template.detectionRangeMeters)
        assertEquals(120, template.cooldownSeconds)
    }

    @Test
    fun templatesUnlockedAt_filtersByExplorerLevel() {
        val levelFive = RadarCatalog.templatesUnlockedAt(5)
        assertEquals(3, levelFive.size)
        assertEquals(RadarType.BASIC_RADAR, levelFive.first().type)
        assertEquals(RadarType.SECRET_PLACE_RADAR, levelFive.last().type)

        val levelOne = RadarCatalog.templatesUnlockedAt(1)
        assertEquals(1, levelOne.size)
        assertEquals(RadarType.BASIC_RADAR, levelOne.single().type)
    }

    @Test
    fun highestUnlockedAt_returnsStrongestEligibleRadar() {
        assertNull(RadarCatalog.highestUnlockedAt(0))
        assertEquals(RadarType.BASIC_RADAR, RadarCatalog.highestUnlockedAt(1)?.type)
        assertEquals(RadarType.STORY_RADAR, RadarCatalog.highestUnlockedAt(12)?.type)
        assertEquals(RadarType.LEGENDARY_RADAR, RadarCatalog.highestUnlockedAt(20)?.type)
    }

    @Test
    fun detectionProfile_fromType_matchesTemplate() {
        val profile = RadarDetectionProfile.from(RadarType.TREASURE_RADAR)
        assertEquals(45_000L, profile.cooldownMillis)
        assertEquals(RadarSchema.AchievementKeys.firstUnlock(RadarType.TREASURE_RADAR), profile.achievementKey)
    }
}
