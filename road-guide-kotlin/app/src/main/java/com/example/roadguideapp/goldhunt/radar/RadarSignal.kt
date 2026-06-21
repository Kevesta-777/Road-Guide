package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.radar.direction.RadarCompassDirection
import com.example.roadguideapp.goldhunt.radar.direction.RadarCompassDirectionResolver

/**
 * One offline radar detection returned by a pulse.
 * Reports bearing and compass sector only — never target coordinates.
 */
internal data class RadarSignal(
    val signalType: RadarSignalType,
    val signalStrength: Double,
    val distanceMeters: Double,
    val targetCategory: RadarTargetCategory,
    val targetId: String,
    val detectedTime: Long,
    val bearingDegrees: Double? = null,
    val compassDirection: RadarCompassDirection? = null,
    val schemaVersion: Int = RadarSchema.VERSION,
) {
    val hasDirectionalHint: Boolean
        get() = bearingDegrees != null && compassDirection != null

    init {
        require(targetId.isNotBlank()) { "targetId must not be blank" }
        require(distanceMeters >= 0.0) { "distanceMeters must be non-negative" }
        require(
            signalStrength in RadarSchema.MIN_SIGNAL_STRENGTH..RadarSchema.MAX_SIGNAL_STRENGTH,
        ) {
            "signalStrength must be between ${RadarSchema.MIN_SIGNAL_STRENGTH} and ${RadarSchema.MAX_SIGNAL_STRENGTH}"
        }
        require(detectedTime > 0L) { "detectedTime must be positive" }
        val hasBearing = bearingDegrees != null
        val hasDirection = compassDirection != null
        require(hasBearing == hasDirection) {
            "bearingDegrees and compassDirection must both be set or both be null"
        }
        if (bearingDegrees != null && compassDirection != null) {
            require(bearingDegrees in 0.0..360.0) { "bearingDegrees must be in 0..360" }
            require(
                compassDirection == RadarCompassDirectionResolver.fromBearingDegrees(bearingDegrees),
            ) {
                "compassDirection does not match bearingDegrees"
            }
        }
    }

    fun detectionEventId(): String =
        RadarSchema.detectionEventId(targetCategory, targetId, detectedTime)
}
