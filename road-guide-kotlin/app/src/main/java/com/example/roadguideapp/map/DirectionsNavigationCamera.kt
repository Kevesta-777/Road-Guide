package com.example.roadguideapp.map

import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.constants.MapLibreConstants
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

/**
 * Chase camera behind the navigation vehicle.
 * User zoom (pinch or +/-) is preserved while position and bearing keep following the route.
 */
internal class DirectionsNavigationCamera(
    private val map: MapLibreMap,
    private val viewportHeightPx: () -> Int,
) {
    private var smoothedLat = 0.0
    private var smoothedLng = 0.0
    private var smoothedBearing = 0.0
    private var smoothedZoom =
        DirectionsNavConfig.chaseViewport(51.5, DirectionsNavConfig.DEFAULT_TILT_DEG).zoom
    private var lastBottomPaddingFraction = DirectionsNavConfig.CHASE_BOTTOM_PADDING_FRACTION
    private var initialized = false

    /** Added on top of the auto chase zoom from tilt/height. */
    private var userZoomDelta: Double = 0.0
    private var userZoomLocked: Boolean = false

    var eyeHeightM: Double = DirectionsNavConfig.DEFAULT_CAMERA_HEIGHT_M
        private set
    var tiltDegrees: Double = DirectionsNavConfig.DEFAULT_TILT_DEG
        private set

    fun reset() {
        initialized = false
        eyeHeightM = DirectionsNavConfig.DEFAULT_CAMERA_HEIGHT_M
        tiltDegrees = DirectionsNavConfig.DEFAULT_TILT_DEG
        userZoomDelta = 0.0
        userZoomLocked = false
    }

    fun adjustEyeHeight(deltaM: Double) {
        eyeHeightM = (eyeHeightM + deltaM).coerceIn(
            DirectionsNavConfig.MIN_CAMERA_HEIGHT_M,
            DirectionsNavConfig.MAX_CAMERA_HEIGHT_M,
        )
    }

    fun adjustTilt(deltaDeg: Double) {
        tiltDegrees = (tiltDegrees + deltaDeg).coerceIn(
            DirectionsNavConfig.MIN_TILT_DEG,
            DirectionsNavConfig.MAX_TILT_DEG,
        )
        map.setMaxPitchPreference(DirectionsNavConfig.MAPLIBRE_MAX_PITCH_DEG)
    }

    fun adjustZoom(delta: Double) {
        userZoomLocked = true
        userZoomDelta = (userZoomDelta + delta).coerceIn(
            -DirectionsNavConfig.MAX_USER_ZOOM_DELTA,
            DirectionsNavConfig.MAX_USER_ZOOM_DELTA,
        )
    }

    /** Call after pinch/double-tap zoom so follow mode keeps the user's zoom level. */
    fun syncUserZoomFromMap(frame: DirectionsNavFrame, currentMapZoom: Double) {
        val baseZoom = DirectionsNavConfig.chaseViewport(frame.lat, tiltDegrees, eyeHeightM).zoom
        userZoomDelta = (DirectionsNavConfig.clampNavZoom(currentMapZoom) - baseZoom).coerceIn(
            -DirectionsNavConfig.MAX_USER_ZOOM_DELTA,
            DirectionsNavConfig.MAX_USER_ZOOM_DELTA,
        )
        userZoomLocked = true
        smoothedZoom = DirectionsNavConfig.clampNavZoom(baseZoom + userZoomDelta)
    }

    fun enter(frame: DirectionsNavFrame) {
        map.cancelTransitions()
        applyNavPitchLimit()
        map.setMinZoomPreference(DirectionsNavConfig.NAV_CAMERA_MIN_ZOOM)
        map.setMaxZoomPreference(DirectionsNavConfig.NAV_CAMERA_MAX_ZOOM + 1.0)
        smoothedLat = frame.lat
        smoothedLng = frame.lng
        smoothedBearing = frame.bearingDegrees
        userZoomDelta = 0.0
        userZoomLocked = false
        val viewport = DirectionsNavConfig.chaseViewport(frame.lat, tiltDegrees, eyeHeightM)
        smoothedZoom = viewport.zoom
        lastBottomPaddingFraction = viewport.bottomPaddingFraction
        initialized = true
        applyChaseCamera(frame, forceSnap = true)
    }

    fun exit() {
        initialized = false
        userZoomDelta = 0.0
        userZoomLocked = false
        map.setMaxPitchPreference(MapLibreConstants.MAXIMUM_TILT.toDouble())
        map.setMinZoomPreference(MapConstants.MIN_ZOOM)
        map.setMaxZoomPreference(MapConstants.MAX_ZOOM)
        clearPadding()
    }

    fun follow(frame: DirectionsNavFrame) {
        if (!initialized) {
            enter(frame)
            return
        }
        applyChaseCamera(frame, forceSnap = false)
    }

    fun refreshCameraNow(frame: DirectionsNavFrame) {
        if (!initialized) return
        applyChaseCamera(frame, forceSnap = true)
    }

    private fun applyNavPitchLimit() {
        map.setMaxPitchPreference(DirectionsNavConfig.MAPLIBRE_MAX_PITCH_DEG)
    }

    private fun applyChaseCamera(frame: DirectionsNavFrame, forceSnap: Boolean) {
        val viewport = DirectionsNavConfig.chaseViewport(frame.lat, tiltDegrees, eyeHeightM)

        if (forceSnap) {
            smoothedLat = frame.lat
            smoothedLng = frame.lng
        } else {
            smoothedLat = frame.lat + (smoothedLat - frame.lat) * (1.0 - DirectionsNavConfig.POSITION_SMOOTHING)
            smoothedLng = frame.lng + (smoothedLng - frame.lng) * (1.0 - DirectionsNavConfig.POSITION_SMOOTHING)
        }

        val bearingT = if (forceSnap) 1.0 else DirectionsNavConfig.BEARING_SMOOTHING
        smoothedBearing = if (bearingT >= 1.0) {
            frame.bearingDegrees
        } else {
            lerpAngleDegreesNav(smoothedBearing, frame.bearingDegrees, bearingT)
        }

        val autoZoom = viewport.zoom
        val targetZoom = DirectionsNavConfig.clampNavZoom(autoZoom + userZoomDelta)
        smoothedZoom = when {
            forceSnap -> targetZoom
            userZoomLocked -> targetZoom
            else -> smoothedZoom + (autoZoom - smoothedZoom) * 0.22
        }
        lastBottomPaddingFraction = if (forceSnap) {
            viewport.bottomPaddingFraction
        } else {
            lastBottomPaddingFraction +
                (viewport.bottomPaddingFraction - lastBottomPaddingFraction) * 0.22
        }

        val (targetLat, targetLng) = if (DirectionsNavConfig.TARGET_AHEAD_OF_VEHICLE_M <= 0.0) {
            smoothedLat to smoothedLng
        } else {
            destinationPointNav(
                frame.lat,
                frame.lng,
                smoothedBearing,
                DirectionsNavConfig.TARGET_AHEAD_OF_VEHICLE_M,
            )
        }

        val position = CameraPosition.Builder()
            .target(LatLng(targetLat, targetLng))
            .zoom(smoothedZoom)
            .tilt(viewport.mapPitchDeg)
            .bearing(smoothedBearing)
            .padding(chasePadding(lastBottomPaddingFraction))
            .build()
        map.moveCamera(CameraUpdateFactory.newCameraPosition(position))
    }

    private fun chasePadding(bottomFraction: Double): DoubleArray {
        val h = viewportHeightPx().coerceAtLeast(1)
        return doubleArrayOf(
            0.0,
            h * DirectionsNavConfig.CHASE_TOP_PADDING_FRACTION,
            0.0,
            h * bottomFraction,
        )
    }

    private fun clearPadding() {
        val current = map.cameraPosition
        val target = current.target ?: return
        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(target)
                    .zoom(current.zoom)
                    .tilt(current.tilt)
                    .bearing(current.bearing)
                    .padding(doubleArrayOf(0.0, 0.0, 0.0, 0.0))
                    .build(),
            ),
        )
    }
}
