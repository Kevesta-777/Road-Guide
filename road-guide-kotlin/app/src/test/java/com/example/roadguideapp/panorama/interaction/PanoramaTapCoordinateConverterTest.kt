package com.example.roadguideapp.panorama.interaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PanoramaTapCoordinateConverterTest {
    private val viewState = PanoramaViewState(
        yaw = 120f,
        pitch = 10f,
        fieldOfView = 90f,
        viewportAspect = 0.5625f,
        viewportWidth = 1080,
        viewportHeight = 1920,
    )

    @Test
    fun screenCenter_mapsToCurrentViewAngles() {
        val angles = PanoramaTapCoordinateConverter.screenToAngles(
            screenX = 540f,
            screenY = 960f,
            viewState = viewState,
        )
        assertEquals(120f, angles.bearing, 0.01f)
        assertEquals(10f, angles.pitch, 0.01f)
    }

    @Test
    fun screenRight_decreasesBearingLikeDrag() {
        val center = PanoramaTapCoordinateConverter.screenToAngles(540f, 960f, viewState)
        val right = PanoramaTapCoordinateConverter.screenToAngles(1080f, 960f, viewState)
        assertTrue(right.bearing < center.bearing)
    }

    @Test
    fun screenTop_increasesPitchLikeDrag() {
        val center = PanoramaTapCoordinateConverter.screenToAngles(540f, 960f, viewState)
        val top = PanoramaTapCoordinateConverter.screenToAngles(540f, 0f, viewState)
        assertTrue(top.pitch > center.pitch)
    }

    @Test
    fun normalizeBearing_wrapsNegativeAngles() {
        assertEquals(90f, PanoramaTapCoordinateConverter.normalizeBearing(-270f), 0.01f)
    }
}
