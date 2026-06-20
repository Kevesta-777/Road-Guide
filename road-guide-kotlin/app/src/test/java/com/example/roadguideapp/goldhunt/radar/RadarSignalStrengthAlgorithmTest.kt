package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Test

class RadarSignalStrengthAlgorithmTest {
    private val longRangeMeters = RadarType.LEGENDARY_RADAR.detectionRangeMeters

    @Test
    fun canonicalAnchors_matchExamples() {
        assertEquals(1.0, RadarSignalStrengthAlgorithm.strength(0.0, longRangeMeters), 0.0001)
        assertEquals(100, RadarSignalStrengthAlgorithm.strengthPercent(0.0, longRangeMeters))
        assertEquals(0.8, RadarSignalStrengthAlgorithm.strength(100.0, longRangeMeters), 0.0001)
        assertEquals(80, RadarSignalStrengthAlgorithm.strengthPercent(100.0, longRangeMeters))
        assertEquals(0.5, RadarSignalStrengthAlgorithm.strength(300.0, longRangeMeters), 0.0001)
        assertEquals(50, RadarSignalStrengthAlgorithm.strengthPercent(300.0, longRangeMeters))
        assertEquals(0.2, RadarSignalStrengthAlgorithm.strength(600.0, longRangeMeters), 0.0001)
        assertEquals(20, RadarSignalStrengthAlgorithm.strengthPercent(600.0, longRangeMeters))
    }

    @Test
    fun outOfRange_returnsZero() {
        assertEquals(0.0, RadarSignalStrengthAlgorithm.strength(601.0, 600), 0.0001)
        assertEquals(0.0, RadarSignalStrengthAlgorithm.strength(3_001.0, longRangeMeters), 0.0001)
        assertEquals(0, RadarSignalStrengthAlgorithm.strengthPercent(900.0, 800))
    }

    @Test
    fun interpolatesBetweenAnchors() {
        assertEquals(0.65, RadarSignalStrengthAlgorithm.strength(200.0, longRangeMeters), 0.0001)
        assertEquals(0.35, RadarSignalStrengthAlgorithm.strength(450.0, longRangeMeters), 0.0001)
    }

    @Test
    fun decaysToZeroAtMaxRange() {
        val maxRange = longRangeMeters
        assertEquals(0.0, RadarSignalStrengthAlgorithm.strength(maxRange.toDouble(), maxRange), 0.0001)
        assertEquals(
            0.1,
            RadarSignalStrengthAlgorithm.strength(1_800.0, maxRange),
            0.0001,
        )
    }

    @Test
    fun shortRangeRadars_truncateCurveBeforeSixHundredMeterAnchor() {
        val basicRange = RadarType.BASIC_RADAR.detectionRangeMeters
        assertEquals(0.8, RadarSignalStrengthAlgorithm.strength(100.0, basicRange), 0.0001)
        assertEquals(0.5, RadarSignalStrengthAlgorithm.strength(300.0, basicRange), 0.0001)
        assertEquals(0.0, RadarSignalStrengthAlgorithm.strength(basicRange.toDouble(), basicRange), 0.0001)
        assertEquals(0.25, RadarSignalStrengthAlgorithm.strength(400.0, basicRange), 0.0001)
    }

    @Test
    fun allRadarTypes_useTypeSpecificMaxRange() {
        for (radarType in RadarType.ALL_ORDERED) {
            val range = radarType.detectionRangeMeters
            assertEquals(
                1.0,
                RadarSignalStrengthAlgorithm.strengthForRadarType(0.0, radarType),
                0.0001,
            )
            assertEquals(
                0.8,
                RadarSignalStrengthAlgorithm.strengthForRadarType(100.0, radarType),
                0.0001,
            )
            assertEquals(
                0.0,
                RadarSignalStrengthAlgorithm.strengthForRadarType(range + 1.0, radarType),
                0.0001,
            )
            assertEquals(
                RadarSignalStrengthAlgorithm.strength(250.0, range),
                RadarSignalStrengthAlgorithm.strengthForRadarType(250.0, radarType),
                0.0001,
            )
        }
    }
}
