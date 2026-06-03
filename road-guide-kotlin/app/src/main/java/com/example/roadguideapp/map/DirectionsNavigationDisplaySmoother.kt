package com.example.roadguideapp.map



import android.os.SystemClock

import org.maplibre.android.geometry.LatLng



/**

 * Smooths navigation display along route arc length (not raw lat/lng) to avoid high-zoom jitter.

 */

internal class DirectionsNavigationDisplaySmoother {

    private var displayDistanceM = 0.0

    private var displayBearing = 0.0

    private var routeLengthM = 0.0

    private var initialized = false

    private var lastSmoothElapsedMs: Long = 0L



    fun reset(frame: DirectionsNavFrame, route: List<LatLng>) {

        routeLengthM = DirectionsRouteGeometry.totalArcLengthM(route)

        displayDistanceM = frame.cumulativeDistanceM.coerceIn(0.0, routeLengthM)

        displayBearing = DirectionsRouteGeometry.bearingAlongRouteM(

            route,

            displayDistanceM,

            DirectionsNavConfig.MARKER_BEARING_LOOK_AHEAD_M,

        )

        initialized = route.size >= 2

        lastSmoothElapsedMs = SystemClock.elapsedRealtime()

    }



    fun clear() {

        initialized = false

        routeLengthM = 0.0

        lastSmoothElapsedMs = 0L

    }



    fun smooth(engineFrame: DirectionsNavFrame, route: List<LatLng>): DirectionsNavFrame {

        if (route.size < 2) return engineFrame

        if (!initialized) {

            reset(engineFrame, route)

        }



        val nowMs = SystemClock.elapsedRealtime()

        val dtSeconds = if (lastSmoothElapsedMs == 0L) {

            1.0 / 60.0

        } else {

            ((nowMs - lastSmoothElapsedMs) / 1000.0).coerceIn(1.0 / 120.0, 0.05)

        }

        lastSmoothElapsedMs = nowMs



        val engineDist = engineFrame.cumulativeDistanceM.coerceIn(0.0, routeLengthM)

        val posAlpha = DirectionsNavConfig.smoothingAlpha(

            dtSeconds,

            DirectionsNavConfig.MARKER_POSITION_TIME_CONSTANT_S,

        )

        var targetDist = displayDistanceM + (engineDist - displayDistanceM) * posAlpha

        val maxLag = DirectionsNavConfig.MARKER_MAX_LAG_M

        if (engineDist - targetDist > maxLag) {

            targetDist = engineDist - maxLag

        }

        displayDistanceM = targetDist.coerceIn(0.0, engineDist.coerceAtMost(routeLengthM))



        val targetBearing = DirectionsRouteGeometry.bearingAlongRouteM(

            route,

            displayDistanceM,

            DirectionsNavConfig.MARKER_BEARING_LOOK_AHEAD_M,

        )

        val bearingAlpha = DirectionsNavConfig.smoothingAlpha(

            dtSeconds,

            DirectionsNavConfig.MARKER_BEARING_TIME_CONSTANT_S,

        )

        displayBearing = lerpAngleDegreesNav(displayBearing, targetBearing, bearingAlpha)



        val sampled = DirectionsRouteGeometry.sampleAtDistanceM(route, displayDistanceM)

        return DirectionsNavFrame(

            lat = sampled.lat,

            lng = sampled.lng,

            bearingDegrees = displayBearing,

            cumulativeDistanceM = engineDist,

            routeSliceDistanceM = displayDistanceM,

        )

    }

}


