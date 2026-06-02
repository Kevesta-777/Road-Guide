package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLngBounds

/**
 * MapLibre [LatLngBounds.from] expects `(latNorth, lonEast, latSouth, lonWest)`.
 * Use this helper when you have min/max south-west and north-east edges.
 */
internal object LatLngBoundsExt {

    private const val MIN_LAT_SPAN = 1e-5

    fun fromEdges(
        south: Double,
        west: Double,
        north: Double,
        east: Double,
    ): LatLngBounds {
        var latNorth = maxOf(south, north)
        var latSouth = minOf(south, north)
        val lonEast = maxOf(west, east)
        val lonWest = minOf(west, east)

        if (latNorth - latSouth < MIN_LAT_SPAN) {
            val mid = (latNorth + latSouth) / 2.0
            latNorth = mid + MIN_LAT_SPAN / 2.0
            latSouth = mid - MIN_LAT_SPAN / 2.0
        }

        return LatLngBounds.from(latNorth, lonEast, latSouth, lonWest)
    }
}
