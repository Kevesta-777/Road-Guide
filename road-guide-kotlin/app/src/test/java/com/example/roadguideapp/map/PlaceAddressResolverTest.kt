package com.example.roadguideapp.map

import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.maplibre.android.geometry.LatLng

class PlaceAddressResolverTest {

    @Test
    fun needsReverseGeocode_detectsCoordinateStrings() {
        assertTrue(PlaceAddressResolver.needsReverseGeocode("51.5074, -0.1278"))
        assertTrue(PlaceAddressResolver.needsReverseGeocode(""))
        assertFalse(PlaceAddressResolver.needsReverseGeocode("10 Downing Street, London"))
    }

    @Test
    fun formatFromOsmProperties_prefersStructuredTags() {
        val props = JsonObject().apply {
            addProperty("addr:housenumber", "10")
            addProperty("addr:street", "Downing Street")
            addProperty("addr:city", "London")
            addProperty("addr:country", "United Kingdom")
        }
        val address = PlaceAddressResolver.formatFromOsmProperties(props, LatLng(51.5034, -0.1276))
        assertEquals("10 Downing Street, London, United Kingdom", address)
    }

    @Test
    fun formatFromOsmProperties_fallsBackToCoordinatesWhenMissingTags() {
        val address = PlaceAddressResolver.formatFromOsmProperties(JsonObject(), LatLng(51.5, -0.12))
        assertEquals("51.5, -0.12", address)
    }
}
