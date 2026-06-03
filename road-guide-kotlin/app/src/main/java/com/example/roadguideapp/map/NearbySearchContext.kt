package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng

/**
 * Where nearby category search is anchored: map center, a place, or a route corridor.
 */
internal sealed class NearbySearchContext {
    abstract fun distanceMeters(target: LatLng, mapCenter: LatLng): Double

    /** Whether [target] should be included in results for this context. */
    open fun includes(target: LatLng, mapCenter: LatLng): Boolean = true

    data object MapCenter : NearbySearchContext() {
        override fun distanceMeters(target: LatLng, mapCenter: LatLng): Double =
            DirectionsPathOptimizer.haversineMeters(mapCenter, target)
    }

    data class NearPlace(
        val location: LatLng,
        val label: String?,
    ) : NearbySearchContext() {
        override fun distanceMeters(target: LatLng, mapCenter: LatLng): Double =
            DirectionsPathOptimizer.haversineMeters(location, target)
    }

    data class AlongRoute(
        val polyline: List<LatLng>,
        val radiusMeters: Double = DEFAULT_CORRIDOR_RADIUS_METERS,
    ) : NearbySearchContext() {
        override fun distanceMeters(target: LatLng, mapCenter: LatLng): Double =
            PolylineDistance.distanceToPolylineMeters(target, polyline)

        override fun includes(target: LatLng, mapCenter: LatLng): Boolean =
            distanceMeters(target, mapCenter) <= radiusMeters
    }

    companion object {
        const val DEFAULT_CORRIDOR_RADIUS_METERS = 500.0
        const val NEAR_PLACE_BOUNDS_PADDING_METERS = 2_500.0

        fun resolveDefault(
            mapCenter: LatLng,
            selectedPlace: MapPlaceDetail?,
            routeGeometry: List<LatLng>?,
            hasActiveDirections: Boolean,
        ): NearbySearchContext {
            if (hasActiveDirections && routeGeometry != null && routeGeometry.size >= 2) {
                return AlongRoute(routeGeometry)
            }
            if (selectedPlace != null) {
                return NearPlace(selectedPlace.latLng, selectedPlace.name)
            }
            return MapCenter
        }
    }
}
