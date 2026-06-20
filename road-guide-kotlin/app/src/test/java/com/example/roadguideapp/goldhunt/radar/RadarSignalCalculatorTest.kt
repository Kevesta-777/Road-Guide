package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RadarSignalCalculatorTest {
    @Test
    fun strength_atRangeEdge_isZero() {
        assertEquals(0.0, RadarSignalCalculator.strength(800.0, 800), 0.0001)
    }

    @Test
    fun strength_atOrigin_isOne() {
        assertEquals(1.0, RadarSignalCalculator.strength(0.0, 800), 0.0001)
    }

    @Test
    fun strength_matchesCanonicalDistanceCurve() {
        assertEquals(0.8, RadarSignalCalculator.strength(100.0, 3_000), 0.0001)
        assertEquals(0.5, RadarSignalCalculator.strength(300.0, 3_000), 0.0001)
        assertEquals(0.2, RadarSignalCalculator.strength(600.0, 3_000), 0.0001)
    }

    @Test
    fun build_returnsNullWhenOutOfRange() {
        val signal = RadarSignalCalculator.build(
            targetCategory = RadarTargetCategory.TREASURE,
            targetId = "treasure-1",
            distanceMeters = 900.0,
            maxRangeMeters = 800,
        )
        assertNull(signal)
    }

    @Test
    fun build_assignsSignalTypeFromStrength() {
        val signal = RadarSignalCalculator.build(
            targetCategory = RadarTargetCategory.CLUSTER,
            targetId = "cluster-abc",
            distanceMeters = 100.0,
            maxRangeMeters = 1_500,
            detectedTime = 1_700_000_000_000L,
        )
        assertNotNull(signal)
        assertEquals(RadarTargetCategory.CLUSTER, signal!!.targetCategory)
        assertEquals("cluster-abc", signal.targetId)
        assertEquals(1_700_000_000_000L, signal.detectedTime)
        assertEquals(0.8, signal.signalStrength, 0.0001)
        assertEquals(RadarSignalType.IMMEDIATE, signal.signalType)
        assertEquals(null, signal.bearingDegrees)
        assertEquals(null, signal.compassDirection)
    }

    @Test
    fun buildWithBearing_assignsCompassDirection() {
        val signal = RadarSignalCalculator.buildWithBearing(
            targetCategory = RadarTargetCategory.SECRET_PLACE,
            targetId = "secret-direction",
            distanceMeters = 200.0,
            maxRangeMeters = 1_200,
            playerLat = 51.5,
            playerLng = -0.2,
            targetLat = 51.4,
            targetLng = -0.3,
            detectedTime = 1_700_000_000_000L,
        )
        assertNotNull(signal)
        assertNotNull(signal!!.bearingDegrees)
        assertNotNull(signal.compassDirection)
    }

    @Test
    fun build_respectsRadarTypeFilter() {
        val blocked = RadarSignalCalculator.build(
            targetCategory = RadarTargetCategory.LEGENDARY_RELIC,
            targetId = "legendary_relic_scope",
            distanceMeters = 100.0,
            maxRangeMeters = 3_000,
            radarType = RadarType.BASIC_RADAR,
        )
        assertNull(blocked)

        val allowed = RadarSignalCalculator.build(
            targetCategory = RadarTargetCategory.TREASURE,
            targetId = "treasure-1",
            distanceMeters = 100.0,
            maxRangeMeters = 500,
            radarType = RadarType.BASIC_RADAR,
        )
        assertNotNull(allowed)
    }
}
