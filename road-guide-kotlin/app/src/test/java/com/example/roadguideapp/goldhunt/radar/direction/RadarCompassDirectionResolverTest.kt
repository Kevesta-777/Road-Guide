package com.example.roadguideapp.goldhunt.radar.direction

import org.junit.Assert.assertEquals
import org.junit.Test

class RadarCompassDirectionResolverTest {
    @Test
    fun fromBearingDegrees_mapsEightSectors() {
        assertEquals(RadarCompassDirection.NORTH, RadarCompassDirectionResolver.fromBearingDegrees(0.0))
        assertEquals(RadarCompassDirection.NORTH, RadarCompassDirectionResolver.fromBearingDegrees(350.0))
        assertEquals(RadarCompassDirection.NORTH_EAST, RadarCompassDirectionResolver.fromBearingDegrees(45.0))
        assertEquals(RadarCompassDirection.EAST, RadarCompassDirectionResolver.fromBearingDegrees(90.0))
        assertEquals(RadarCompassDirection.SOUTH_EAST, RadarCompassDirectionResolver.fromBearingDegrees(135.0))
        assertEquals(RadarCompassDirection.SOUTH, RadarCompassDirectionResolver.fromBearingDegrees(180.0))
        assertEquals(RadarCompassDirection.SOUTH_WEST, RadarCompassDirectionResolver.fromBearingDegrees(225.0))
        assertEquals(RadarCompassDirection.WEST, RadarCompassDirectionResolver.fromBearingDegrees(270.0))
        assertEquals(RadarCompassDirection.NORTH_WEST, RadarCompassDirectionResolver.fromBearingDegrees(315.0))
    }

    @Test
    fun sectorCenterBearing_alignsWithCompassRose() {
        assertEquals(0.0, RadarCompassDirectionResolver.sectorCenterBearing(RadarCompassDirection.NORTH), 0.0001)
        assertEquals(90.0, RadarCompassDirectionResolver.sectorCenterBearing(RadarCompassDirection.EAST), 0.0001)
        assertEquals(180.0, RadarCompassDirectionResolver.sectorCenterBearing(RadarCompassDirection.SOUTH), 0.0001)
        assertEquals(270.0, RadarCompassDirectionResolver.sectorCenterBearing(RadarCompassDirection.WEST), 0.0001)
    }

    @Test
    fun allOrdered_containsEightDirections() {
        assertEquals(8, RadarCompassDirection.ALL_ORDERED.size)
        assertEquals(
            listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW"),
            RadarCompassDirection.ALL_ORDERED.map { it.shortLabel },
        )
    }
}
