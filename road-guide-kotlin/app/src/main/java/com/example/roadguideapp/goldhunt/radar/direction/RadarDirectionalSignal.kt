package com.example.roadguideapp.goldhunt.radar.direction

import com.example.roadguideapp.goldhunt.radar.RadarSignal
import com.example.roadguideapp.goldhunt.radar.RadarSignalType
import com.example.roadguideapp.goldhunt.radar.RadarTargetCategory

/**
 * Coordinate-free radar blip for UI and logs.
 */
internal data class RadarDirectionalSignal(
    val signalType: RadarSignalType,
    val signalStrength: Double,
    val distanceMeters: Double,
    val targetCategory: RadarTargetCategory,
    val targetId: String,
    val bearingDegrees: Double,
    val compassDirection: RadarCompassDirection,
    val detectedTime: Long,
) {
    val directionLabel: String
        get() = compassDirection.displayName
}

internal data class RadarDirectionalScanResult(
    val scannedAtMs: Long,
    val radarType: com.example.roadguideapp.goldhunt.radar.RadarType,
    val detectionRangeMeters: Int,
    val signals: List<RadarDirectionalSignal>,
) {
    val hasDetections: Boolean
        get() = signals.isNotEmpty()

    fun signalsGroupedByDirection(): Map<RadarCompassDirection, List<RadarDirectionalSignal>> =
        signals.groupBy { it.compassDirection }

    fun strongestByDirection(): Map<RadarCompassDirection, RadarDirectionalSignal?> =
        RadarCompassDirection.ALL_ORDERED.associateWith { direction ->
            signals
                .filter { it.compassDirection == direction }
                .maxByOrNull { it.signalStrength }
        }
}

internal object RadarDirectionalMapper {
    fun fromSignal(signal: RadarSignal): RadarDirectionalSignal? {
        val bearing = signal.bearingDegrees ?: return null
        val direction = signal.compassDirection ?: return null
        return RadarDirectionalSignal(
            signalType = signal.signalType,
            signalStrength = signal.signalStrength,
            distanceMeters = signal.distanceMeters,
            targetCategory = signal.targetCategory,
            targetId = signal.targetId,
            bearingDegrees = bearing,
            compassDirection = direction,
            detectedTime = signal.detectedTime,
        )
    }

    fun fromScanResult(
        scanResult: com.example.roadguideapp.goldhunt.radar.RadarScanResult,
    ): RadarDirectionalScanResult = RadarDirectionalScanResult(
        scannedAtMs = scanResult.scannedAtMs,
        radarType = scanResult.radarType,
        detectionRangeMeters = scanResult.detectionRangeMeters,
        signals = scanResult.signals.mapNotNull(::fromSignal),
    )
}
