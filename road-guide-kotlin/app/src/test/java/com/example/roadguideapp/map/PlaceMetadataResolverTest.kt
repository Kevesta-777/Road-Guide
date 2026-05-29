package com.example.roadguideapp.map

import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceMetadataResolverTest {

    @Test
    fun extractLocalityFromLabel_stripsNamePrefix() {
        val locality = PlaceMetadataResolver.extractLocalityFromLabel(
            "McDonald's, Chiyoda, Tokyo",
            "McDonald's",
        )
        assertEquals("Chiyoda", locality)
    }

    @Test
    fun estimateOpenNow_24_7() {
        assertTrue(PlaceMetadataResolver.estimateOpenNow("24/7"))
    }

    @Test
    fun formatHoursSummary_24_7() {
        assertEquals("Open 24 hours", PlaceMetadataResolver.formatHoursSummary("24/7"))
    }

    @Test
    fun fromJsonObject_readsOpeningHoursAndCity() {
        val props = JsonObject().apply {
            addProperty("opening_hours", "24/7")
            addProperty("addr:city", "Chiyoda")
        }
        val meta = PlaceMetadataResolver.fromJsonObject(props)
        assertEquals("Chiyoda", meta.locality)
        assertTrue(meta.isOpenNow)
        assertEquals("Open 24 hours", meta.hoursSummary)
    }

    @Test
    fun fromJsonObject_emptyHoursNotOpen() {
        val meta = PlaceMetadataResolver.fromJsonObject(JsonObject())
        assertFalse(meta.isOpenNow)
        assertEquals("", meta.hoursSummary)
    }
}
