package com.example.roadguideapp.goldhunt.clusters.distribution

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal object ClusterGeoMath {
    private const val METERS_PER_DEG_LAT = 111_320.0

    fun metersPerDegreeLng(latitude: Double): Double =
        METERS_PER_DEG_LAT * cos(Math.toRadians(latitude)).coerceAtLeast(0.2)

    fun offsetMeters(
        centerLat: Double,
        centerLng: Double,
        bearingRad: Double,
        distanceM: Double,
    ): Pair<Double, Double> {
        val dLat = distanceM * sin(bearingRad) / METERS_PER_DEG_LAT
        val dLng = distanceM * cos(bearingRad) / metersPerDegreeLng(centerLat)
        return centerLat + dLat to centerLng + dLng
    }

    fun distanceMeters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double,
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        return 6_371_000.0 * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    fun isWithinRadius(
        centerLat: Double,
        centerLng: Double,
        lat: Double,
        lng: Double,
        radiusM: Double,
    ): Boolean = distanceMeters(centerLat, centerLng, lat, lng) <= radiusM
}
