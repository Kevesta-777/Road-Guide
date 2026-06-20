package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.radar.direction.RadarCompassDirection
import com.example.roadguideapp.goldhunt.radar.direction.RadarDirectionalMapper
import com.example.roadguideapp.goldhunt.radar.direction.RadarDirectionalScanResult

/**
 * Output of one offline radar pulse at a player coordinate.
 */
internal data class RadarScanResult(
    val latitude: Double,
    val longitude: Double,
    val scannedAtMs: Long,
    val radarType: RadarType,
    val detectionRangeMeters: Int,
    val signals: List<RadarSignal>,
) {
    val hasDetections: Boolean
        get() = signals.isNotEmpty()

    val strongestSignal: RadarSignal?
        get() = signals.maxByOrNull { it.signalStrength }

    /** Coordinate-free view for UI — omits player and target positions. */
    fun toDirectionalView(): RadarDirectionalScanResult =
        RadarDirectionalMapper.fromScanResult(this)

    fun signalsByCompassDirection(): Map<RadarCompassDirection, List<RadarSignal>> =
        signals
            .filter { it.compassDirection != null }
            .groupBy { it.compassDirection!! }
}
