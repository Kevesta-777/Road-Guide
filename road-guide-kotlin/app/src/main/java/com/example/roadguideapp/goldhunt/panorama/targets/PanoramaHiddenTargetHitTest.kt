package com.example.roadguideapp.goldhunt.panorama.targets

import com.example.roadguideapp.panorama.gl.PanoramaRenderer
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Angular hit testing between panorama view/tap angles and hidden targets.
 * Viewer integration can call this without duplicating bearing/pitch math.
 */
internal object PanoramaHiddenTargetHitTest {
    fun isTapHit(
        target: PanoramaHiddenTarget,
        tapBearing: Float,
        tapPitch: Float,
    ): Boolean {
        val bearingDelta = angularDistanceDegrees(tapBearing, target.bearing)
        val pitchDelta = abs(tapPitch - target.pitch)
        return bearingDelta <= target.radius && pitchDelta <= target.radius
    }

    fun findTapHits(
        targets: List<PanoramaHiddenTarget>,
        tapBearing: Float,
        tapPitch: Float,
    ): List<PanoramaHiddenTarget> = targets.filter { target ->
        isTapHit(target, tapBearing, tapPitch)
    }

    fun tapDistance(
        target: PanoramaHiddenTarget,
        tapBearing: Float,
        tapPitch: Float,
    ): Float {
        val bearingDelta = angularDistanceDegrees(tapBearing, target.bearing)
        val pitchDelta = abs(tapPitch - target.pitch)
        return sqrt(bearingDelta * bearingDelta + pitchDelta * pitchDelta)
    }
    fun isFocused(
        target: PanoramaHiddenTarget,
        viewYaw: Float,
        viewPitch: Float,
        fieldOfView: Float = 90f,
    ): Boolean {
        val bearingDelta = angularDistanceDegrees(viewYaw, target.bearing)
        val pitchDelta = abs(viewPitch - target.pitch)
        val tolerance = target.radius + fieldOfView * 0.15f
        return bearingDelta <= tolerance && pitchDelta <= tolerance
    }

    fun angularDistanceDegrees(a: Float, b: Float): Float {
        val left = normalizeBearing(a)
        val right = normalizeBearing(b)
        val diff = abs(left - right)
        return min(diff, 360f - diff)
    }

    fun normalizeBearing(degrees: Float): Float {
        var value = degrees % 360f
        if (value < 0f) value += 360f
        return value
    }
}
