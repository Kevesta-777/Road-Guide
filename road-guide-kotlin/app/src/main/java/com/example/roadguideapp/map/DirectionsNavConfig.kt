package com.example.roadguideapp.map

import kotlin.math.cos
import kotlin.math.ln

internal object DirectionsNavConfig {
    const val DRIVE_SPEED_MPS = 10.0
    const val WALK_SPEED_MPS = 1.4
    const val BICYCLE_SPEED_MPS = 4.2
    const val MIN_SPEED_MPS = 5.0
    const val MAX_SPEED_MPS = 50.0
    const val SPEED_STEP_MPS = 1.0

    const val DEFAULT_CAMERA_HEIGHT_M = 1.5
    const val MIN_CAMERA_HEIGHT_M = 0.5
    const val MAX_CAMERA_HEIGHT_M = 10.0
    const val CAMERA_HEIGHT_STEP_M = 0.5

    const val DISTANCE_BEHIND_M = 10.0
    /** 0 = camera bearing/position locked to the vehicle marker (no look-ahead pivot). */
    const val TARGET_AHEAD_OF_VEHICLE_M = 0.0

    const val DEFAULT_TILT_DEG = 58.0
    const val MIN_TILT_DEG = 30.0
    const val MAX_TILT_DEG = 80.0
    const val TILT_STEP_DEG = 2.0
    const val ZOOM_STEP = 0.5
    const val MAX_USER_ZOOM_DELTA = 5.0

    const val MAPLIBRE_MAX_PITCH_DEG = 60.0
    private const val EXTRA_TILT_ZOOM_PER_DEG = 0.085

    const val CHASE_BOTTOM_PADDING_FRACTION = 0.22
    const val CHASE_TOP_PADDING_FRACTION = 0.06
    /** Chase camera position lerp per frame (~0.2 = smooth at max zoom). */
    const val POSITION_SMOOTHING = 0.2
    const val BEARING_SMOOTHING = 0.24
    /**
     * Time constant (seconds) for marker arc-length smoothing; lower = snappier, higher = calmer.
     * ~0.42s settles ~63% toward the engine each second at 60 fps.
     */
    const val MARKER_POSITION_TIME_CONSTANT_S = 0.42
    const val MARKER_BEARING_TIME_CONSTANT_S = 0.38
    /** Bearing from this far ahead on the route reduces vertex wobble at high zoom. */
    const val MARKER_BEARING_LOOK_AHEAD_M = 6.0
    /** Soft catch-up cap (m); avoids rubber-banding that causes visible vibration. */
    const val MARKER_MAX_LAG_M = 8.0
    /** Route line begins at the puck (0 = no gap ahead of the marker). */
    const val ROUTE_TRIM_BEHIND_MARKER_M = 0.0
    /** Max spacing when densifying long segments along the road centerline (m). */
    const val ROUTE_DENSIFY_SEGMENT_M = 0.85
    /** Drop redundant router vertices closer than this (m), except at corners. */
    const val ROUTE_DEDUPE_MIN_M = 0.12
    /** Keep a vertex when the turn exceeds this bearing change (degrees). */
    const val ROUTE_CORNER_MIN_BEARING_DEG = 7.0
    const val NAV_CAMERA_MIN_ZOOM = 14.5
    const val NAV_CAMERA_MAX_ZOOM = 21.0
    const val FOV_DRIVING_DEG = 55.0
    const val DEFAULT_FOV_DEG = 36.86989764584402

    data class ChaseViewport(
        val mapPitchDeg: Double,
        val zoom: Double,
        val bottomPaddingFraction: Double,
    )

    fun speedMps(mode: DirectionsTravelMode): Double = when (mode) {
        DirectionsTravelMode.Walk -> WALK_SPEED_MPS
        DirectionsTravelMode.Bicycle -> BICYCLE_SPEED_MPS
        DirectionsTravelMode.Drive -> DRIVE_SPEED_MPS
    }

    fun chaseViewport(
        lat: Double,
        requestedTiltDeg: Double,
        eyeHeightM: Double = DEFAULT_CAMERA_HEIGHT_M,
    ): ChaseViewport {
        val tilt = requestedTiltDeg.coerceIn(MIN_TILT_DEG, MAX_TILT_DEG)
        val height = eyeHeightM.coerceIn(MIN_CAMERA_HEIGHT_M, MAX_CAMERA_HEIGHT_M)
        val mapPitch = tilt.coerceAtMost(MAPLIBRE_MAX_PITCH_DEG)
        val extraTilt = (tilt - MAPLIBRE_MAX_PITCH_DEG).coerceAtLeast(0.0)
        val latRad = Math.toRadians(lat)
        var zoom = 19.0 + ln(DEFAULT_CAMERA_HEIGHT_M / height) / ln(2.0) * 1.25
        zoom -= ln(DISTANCE_BEHIND_M / 5.0) / ln(2.0) * 0.35
        val tiltNorm = tilt / MAX_TILT_DEG
        zoom -= (1.0 - tiltNorm) * 0.55
        zoom -= extraTilt * EXTRA_TILT_ZOOM_PER_DEG
        zoom += ln(1.0 / cos(latRad).coerceIn(0.65, 1.0)) / ln(2.0) * 0.03
        val ratio = kotlin.math.tan(Math.toRadians(FOV_DRIVING_DEG / 2.0)) /
            kotlin.math.tan(Math.toRadians(DEFAULT_FOV_DEG / 2.0))
        zoom += -ln(ratio) / ln(2.0)
        zoom = zoom.coerceIn(NAV_CAMERA_MIN_ZOOM, NAV_CAMERA_MAX_ZOOM)
        val heightNorm = (height - MIN_CAMERA_HEIGHT_M) / (MAX_CAMERA_HEIGHT_M - MIN_CAMERA_HEIGHT_M)
        val bottomPad = CHASE_BOTTOM_PADDING_FRACTION * (0.72 + heightNorm * 0.95)
        return ChaseViewport(mapPitch, zoom, bottomPad)
    }

    fun clampNavZoom(zoom: Double): Double = zoom.coerceIn(NAV_CAMERA_MIN_ZOOM, NAV_CAMERA_MAX_ZOOM)

    /** Frame-rate independent exponential smoothing factor in `[0, 1]`. */
    fun smoothingAlpha(dtSeconds: Double, timeConstantSeconds: Double): Double {
        if (timeConstantSeconds <= 0.0) return 1.0
        val dt = dtSeconds.coerceIn(1.0 / 120.0, 0.05)
        return (1.0 - kotlin.math.exp(-dt / timeConstantSeconds)).coerceIn(0.0, 1.0)
    }
}
