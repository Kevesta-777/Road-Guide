package com.example.roadguideapp.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.android.geometry.LatLng

class PolylineDistanceTest {

    @Test
    fun sampleAlongPolyline_includesEndpoints() {
        val polyline = listOf(
            LatLng(51.50, -0.10),
            LatLng(51.55, -0.10),
        )
        val samples = PolylineDistance.sampleAlongPolyline(polyline, spacingMeters = 500.0)
        assertEquals(polyline.first(), samples.first())
        assertEquals(polyline.last(), samples.last())
        assertTrue(samples.size >= 2)
    }

    @Test
    fun boundsWithBuffer_expandsPolyline() {
        val polyline = listOf(
            LatLng(51.50, -0.10),
            LatLng(51.51, -0.11),
        )
        val bounds = PolylineDistance.boundsWithBuffer(polyline, bufferMeters = 500.0)
        assertNotNull(bounds)
        assertTrue(bounds!!.latitudeNorth > 51.51)
        assertTrue(bounds.latitudeSouth < 51.50)
    }
}
