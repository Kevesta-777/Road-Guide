package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

internal object MapViewportBounds {

    fun center(map: MapLibreMap): LatLng? = map.cameraPosition.target

    /**
     * Geographic bounds of the current map view (all four screen corners projected to lat/lng).
     */
    fun visibleBounds(map: MapLibreMap, mapView: MapView): LatLngBounds? {
        val width = mapView.width
        val height = mapView.height
        if (width <= 0 || height <= 0) return null

        val corners = listOf(
            map.projection.fromScreenLocation(android.graphics.PointF(0f, 0f)),
            map.projection.fromScreenLocation(android.graphics.PointF(width.toFloat(), 0f)),
            map.projection.fromScreenLocation(android.graphics.PointF(0f, height.toFloat())),
            map.projection.fromScreenLocation(android.graphics.PointF(width.toFloat(), height.toFloat())),
        )
        return runCatching {
            val builder = LatLngBounds.Builder()
            corners.forEach { builder.include(it) }
            builder.build()
        }.getOrNull()
    }

    /** Expands [bounds] symmetrically by [factor] (>1 widens the box). */
    fun expand(bounds: LatLngBounds, factor: Double): LatLngBounds? {
        if (factor <= 1.0) return bounds
        return runCatching {
            val latSpan = (bounds.latitudeNorth - bounds.latitudeSouth) * (factor - 1.0) / 2.0
            val lonSpan = (bounds.longitudeEast - bounds.longitudeWest) * (factor - 1.0) / 2.0
            val south = (bounds.latitudeSouth - latSpan).coerceIn(-85.0, 85.0)
            val north = (bounds.latitudeNorth + latSpan).coerceIn(-85.0, 85.0)
            val west = (bounds.longitudeWest - lonSpan).coerceIn(-180.0, 180.0)
            val east = (bounds.longitudeEast + lonSpan).coerceIn(-180.0, 180.0)
            if (south >= north) return@runCatching null
            LatLngBoundsExt.fromEdges(south = south, west = west, north = north, east = east)
        }.getOrNull()
    }
}
