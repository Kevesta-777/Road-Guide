package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Test

class RadarSignalTypeTest {
    @Test
    fun fromStrength_mapsDistanceBands() {
        assertEquals(RadarSignalType.DISTANT, RadarSignalType.fromStrength(0.10))
        assertEquals(RadarSignalType.APPROACHING, RadarSignalType.fromStrength(0.35))
        assertEquals(RadarSignalType.NEARBY, RadarSignalType.fromStrength(0.60))
        assertEquals(RadarSignalType.IMMEDIATE, RadarSignalType.fromStrength(0.95))
    }

    @Test
    fun fromStrength_clampsOutOfRangeValues() {
        assertEquals(RadarSignalType.DISTANT, RadarSignalType.fromStrength(-1.0))
        assertEquals(RadarSignalType.IMMEDIATE, RadarSignalType.fromStrength(2.0))
    }
}
