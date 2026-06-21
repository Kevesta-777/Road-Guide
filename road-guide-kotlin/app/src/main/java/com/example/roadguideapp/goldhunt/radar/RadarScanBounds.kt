package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.clusters.distribution.ClusterGeoMath
import com.example.roadguideapp.goldhunt.radar.direction.RadarBearingCalculator
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

internal object RadarScanBounds {
    fun fromCenter(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
    ): LatLngBounds {
        val latDelta = radiusMeters / 111_320.0
        val lngDelta = radiusMeters / ClusterGeoMath.metersPerDegreeLng(latitude)
        return LatLngBounds.Builder()
            .include(LatLng(latitude - latDelta, longitude - lngDelta))
            .include(LatLng(latitude + latDelta, longitude + lngDelta))
            .build()
    }

    fun distanceMeters(
        latitude: Double,
        longitude: Double,
        targetLat: Double,
        targetLng: Double,
    ): Double = ClusterGeoMath.distanceMeters(latitude, longitude, targetLat, targetLng)

    fun bearingDegrees(
        fromLat: Double,
        fromLng: Double,
        toLat: Double,
        toLng: Double,
    ): Double = RadarBearingCalculator.bearingDegrees(fromLat, fromLng, toLat, toLng)
}
