package com.example.roadguideapp.panorama.interaction

import kotlin.math.atan
import kotlin.math.tan

/**
 * Converts panorama viewer screen taps to sphere bearing/pitch angles.
 *
 * Uses the same angular offsets as drag look ([com.example.roadguideapp.panorama.gl.PanoramaRenderer.addLookFromDrag]):
 * screen center maps to the current view yaw/pitch; edge taps offset by half the active FOV.
 */
object PanoramaTapCoordinateConverter {
    private const val MIN_PITCH = -85f
    private const val MAX_PITCH = 85f

    fun screenToAngles(
        screenX: Float,
        screenY: Float,
        viewState: PanoramaViewState,
    ): PanoramaTapAngles = screenToAngles(
        screenX = screenX,
        screenY = screenY,
        viewWidth = viewState.viewportWidth,
        viewHeight = viewState.viewportHeight,
        viewYaw = viewState.yaw,
        viewPitch = viewState.pitch,
        fieldOfView = viewState.fieldOfView,
        viewportAspect = viewState.viewportAspect,
    )

    fun screenToAngles(
        screenX: Float,
        screenY: Float,
        viewWidth: Int,
        viewHeight: Int,
        viewYaw: Float,
        viewPitch: Float,
        fieldOfView: Float,
        viewportAspect: Float,
    ): PanoramaTapAngles {
        if (viewWidth <= 0 || viewHeight <= 0) {
            return PanoramaTapAngles(
                bearing = normalizeBearing(viewYaw),
                pitch = viewPitch.coerceIn(MIN_PITCH, MAX_PITCH),
            )
        }

        val normalizedX = (screenX / viewWidth - 0.5f) * 2f
        val normalizedY = (0.5f - screenY / viewHeight) * 2f

        val halfFovY = fieldOfView * 0.5f
        val halfFovX = Math.toDegrees(
            atan(tan(Math.toRadians(halfFovY.toDouble())) * viewportAspect),
        ).toFloat()

        // Match drag sign: finger right decreases yaw; tap above center increases pitch.
        val yawOffset = -normalizedX * halfFovX
        val pitchOffset = normalizedY * halfFovY

        return PanoramaTapAngles(
            bearing = normalizeBearing(viewYaw + yawOffset),
            pitch = (viewPitch + pitchOffset).coerceIn(MIN_PITCH, MAX_PITCH),
        )
    }

    fun normalizeBearing(degrees: Float): Float {
        var value = degrees % 360f
        if (value < 0f) value += 360f
        return value
    }
}
