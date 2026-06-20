package com.example.roadguideapp.goldhunt.radar.direction

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Computes true bearing from a player position toward a radar target.
 * Coordinates are used only during calculation and are not exposed on [com.example.roadguideapp.goldhunt.radar.RadarSignal].
 */
internal object RadarBearingCalculator {
    fun bearingDegrees(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
    ): Double {
        val phi1 = Math.toRadians(fromLat)
        val phi2 = Math.toRadians(toLat)
        val deltaLng = Math.toRadians(toLng - fromLng)
        val y = sin(deltaLng) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLng)
        return normalizeDegrees(Math.toDegrees(atan2(y, x)))
    }

    fun normalizeDegrees(degrees: Double): Double =
        ((degrees % 360.0) + 360.0) % 360.0
}
