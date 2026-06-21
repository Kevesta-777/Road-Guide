package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RadarSignalFactoryTest {
    @Test
    fun factory_buildsEachTargetCategory() {
        val treasure = RadarSignalFactory.treasure("t-1", 50.0, 800)
        val cluster = RadarSignalFactory.cluster("c-1", 50.0, 1_500)
        val secretPlace = RadarSignalFactory.secretPlace("sp-1", 50.0, 1_200)
        val storyFragment = RadarSignalFactory.storyFragment(
            "story_fragment_historic_landmark",
            50.0,
            2_000,
        )
        val relic = RadarSignalFactory.legendaryRelic(
            "legendary_relic_panorama_hunt",
            50.0,
            3_000,
        )

        assertNotNull(treasure)
        assertNotNull(cluster)
        assertNotNull(secretPlace)
        assertNotNull(storyFragment)
        assertNotNull(relic)

        assertEquals(RadarTargetCategory.TREASURE, treasure!!.targetCategory)
        assertEquals(RadarTargetCategory.CLUSTER, cluster!!.targetCategory)
        assertEquals(RadarTargetCategory.SECRET_PLACE, secretPlace!!.targetCategory)
        assertEquals(RadarTargetCategory.STORY_FRAGMENT, storyFragment!!.targetCategory)
        assertEquals(RadarTargetCategory.LEGENDARY_RELIC, relic!!.targetCategory)
    }

    @Test
    fun fromProfile_usesEquippedRadarRangeAndFilter() {
        val profile = RadarProfile(
            currentRadarType = RadarType.STORY_RADAR,
            unlockLevel = 10,
            lastScanTime = null,
            totalScans = 0,
            successfulScans = 0,
        )
        val fragment = RadarSignalFactory.fromProfile(
            profile = profile,
            targetCategory = RadarTargetCategory.STORY_FRAGMENT,
            targetId = "story_fragment_secret_code",
            distanceMeters = 250.0,
            detectedTime = 1_700_000_000_000L,
        )
        val relic = RadarSignalFactory.fromProfile(
            profile = profile,
            targetCategory = RadarTargetCategory.LEGENDARY_RELIC,
            targetId = "legendary_relic_scope",
            distanceMeters = 250.0,
            detectedTime = 1_700_000_000_000L,
        )

        assertNotNull(fragment)
        assertEquals(2_000, profile.detectionProfile().detectionRangeMeters)
        assertEquals(0.575, fragment!!.signalStrength, 0.0001)
        assertEquals(RadarSignalType.NEARBY, fragment.signalType)
        assertEquals(null, relic)
    }
}
