package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LatLngBoundsExtTest {

    @Test
    fun fromEdges_usesMapLibreNorthEastSouthWestOrder() {
        val bounds = LatLngBoundsExt.fromEdges(
            south = 51.50,
            west = -0.11,
            north = 51.51,
            east = -0.10,
        )
        assertTrue(bounds.latitudeNorth > bounds.latitudeSouth)
        assertTrue(bounds.longitudeEast > bounds.longitudeWest)
        assertEquals(51.51, bounds.latitudeNorth, 1e-9)
        assertEquals(51.50, bounds.latitudeSouth, 1e-9)
        assertEquals(-0.10, bounds.longitudeEast, 1e-9)
        assertEquals(-0.11, bounds.longitudeWest, 1e-9)
    }

    @Test
    fun fromEdges_expandsDegenerateLatitudeSpan() {
        val bounds = LatLngBoundsExt.fromEdges(
            south = 51.50,
            west = -0.10,
            north = 51.50,
            east = -0.09,
        )
        assertTrue(bounds.latitudeNorth > bounds.latitudeSouth)
    }
}
