package com.example.roadguideapp.goldhunt.radar.direction

import org.junit.Assert.assertEquals
import org.junit.Test

class RadarBearingCalculatorTest {
    @Test
    fun bearingDegrees_pointsNorth() {
        val bearing = RadarBearingCalculator.bearingDegrees(
            fromLat = 51.5,
            fromLng = -0.2,
            toLat = 52.0,
            toLng = -0.2,
        )
        assertEquals(0.0, bearing, 1.0)
    }

    @Test
    fun bearingDegrees_pointsEast() {
        val bearing = RadarBearingCalculator.bearingDegrees(
            fromLat = 51.5,
            fromLng = -0.2,
            toLat = 51.5,
            toLng = 0.2,
        )
        assertEquals(90.0, bearing, 1.5)
    }

    @Test
    fun bearingDegrees_pointsSouth() {
        val bearing = RadarBearingCalculator.bearingDegrees(
            fromLat = 51.5,
            fromLng = -0.2,
            toLat = 51.0,
            toLng = -0.2,
        )
        assertEquals(180.0, bearing, 1.0)
    }

    @Test
    fun normalizeDegrees_wrapsNegativeAndOverflow() {
        assertEquals(270.0, RadarBearingCalculator.normalizeDegrees(-90.0), 0.0001)
        assertEquals(45.0, RadarBearingCalculator.normalizeDegrees(405.0), 0.0001)
    }
}
