package com.example.roadguideapp.map

import com.google.gson.JsonObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceClaimEligibilityTest {

    @Test
    fun hasBusinessPoiPresentation_requiresBothSpriteAndLabel() {
        assertTrue(PlaceClaimEligibility.hasBusinessPoiPresentation(hasSpriteIcon = true, hasLabelText = true))
        assertFalse(PlaceClaimEligibility.hasBusinessPoiPresentation(hasSpriteIcon = true, hasLabelText = false))
        assertFalse(PlaceClaimEligibility.hasBusinessPoiPresentation(hasSpriteIcon = false, hasLabelText = true))
        assertFalse(PlaceClaimEligibility.hasBusinessPoiPresentation(hasSpriteIcon = false, hasLabelText = false))
    }

    @Test
    fun forMapSymbolPresentation_rejectsGeographicEvenWithBothPresentationChannels() {
        val properties = JsonObject().apply {
            addProperty("class", "city")
            addProperty("name", "London")
        }
        assertFalse(
            PlaceClaimEligibility.forMapSymbolPresentation(
                hasSpriteIcon = true,
                hasLabelText = true,
                properties = properties,
                category = "city",
            ),
        )
    }

    @Test
    fun forMapSymbolPresentation_allowsRestaurantWithBothPresentationChannels() {
        val properties = JsonObject().apply {
            addProperty("class", "restaurant")
            addProperty("name", "Road Guide Cafe")
        }
        assertTrue(
            PlaceClaimEligibility.forMapSymbolPresentation(
                hasSpriteIcon = true,
                hasLabelText = true,
                properties = properties,
                category = "restaurant",
            ),
        )
    }
}
