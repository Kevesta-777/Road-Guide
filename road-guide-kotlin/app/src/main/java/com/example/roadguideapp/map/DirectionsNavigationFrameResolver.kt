package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style

/**
 * Keeps the navigation marker and remaining-route line on the same polyline point.
 */
internal object DirectionsNavigationFrameResolver {

    fun resolveDisplayFrame(
        engineFrame: DirectionsNavFrame,
        route: List<LatLng>,
        smoother: DirectionsNavigationDisplaySmoother,
    ): DirectionsNavFrame {
        if (route.size < 2) return engineFrame
        return smoother.smooth(engineFrame, route)
    }

    fun syncNavigationVisuals(
        style: Style,
        route: List<LatLng>,
        frame: DirectionsNavFrame,
        origin: MapPlaceDetail,
        stops: List<MapPlaceDetail>,
        valhallaRoute: DirectionsRouteResult?,
        travelMode: DirectionsTravelMode,
    ) {
        val anchor = LatLng(frame.lat, frame.lng)
        DirectionsNavigationVehicleLayer.sync(
            style = style,
            lat = anchor.latitude,
            lng = anchor.longitude,
            bearingDegrees = frame.bearingDegrees,
        )
        DirectionsRouteOverlay.sync(
            style = style,
            origin = origin,
            stops = stops,
            valhallaRoute = valhallaRoute,
            showRemainingRoute = true,
            navigationGeometry = route,
            routeStartPosition = anchor,
            routeStartDistanceM = frame.routeSliceDistanceM,
            routeTrimAheadM = DirectionsNavConfig.ROUTE_TRIM_BEHIND_MARKER_M,
            travelMode = travelMode,
        )
    }
}
