package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.radar.direction.RadarBearingCalculator
import com.example.roadguideapp.goldhunt.radar.direction.RadarCompassDirection
import com.example.roadguideapp.goldhunt.radar.direction.RadarCompassDirectionResolver

/**
 * Pure strength and signal-type rules for radar detections.
 */
internal object RadarSignalCalculator {
    fun strength(
        distanceMeters: Double,
        maxRangeMeters: Int,
    ): Double = RadarSignalStrengthAlgorithm.strength(distanceMeters, maxRangeMeters)

    fun strengthForRadarType(
        distanceMeters: Double,
        radarType: RadarType,
    ): Double = RadarSignalStrengthAlgorithm.strengthForRadarType(distanceMeters, radarType)

    fun isWithinRange(distanceMeters: Double, maxRangeMeters: Int): Boolean =
        RadarSignalStrengthAlgorithm.isWithinRange(distanceMeters, maxRangeMeters)

    fun build(
        targetCategory: RadarTargetCategory,
        targetId: String,
        distanceMeters: Double,
        maxRangeMeters: Int,
        detectedTime: Long = System.currentTimeMillis(),
        radarType: RadarType? = null,
        bearingDegrees: Double? = null,
        compassDirection: RadarCompassDirection? = null,
    ): RadarSignal? {
        val normalizedId = targetId.trim()
        if (normalizedId.isEmpty()) return null
        if (!isWithinRange(distanceMeters, maxRangeMeters)) return null
        if (radarType != null && !RadarDetectionScope.isDetectable(targetCategory, radarType)) {
            return null
        }
        val computedStrength = strength(distanceMeters, maxRangeMeters)
        return RadarSignal(
            signalType = RadarSignalType.fromStrength(computedStrength),
            signalStrength = computedStrength,
            distanceMeters = distanceMeters,
            targetCategory = targetCategory,
            targetId = normalizedId,
            detectedTime = detectedTime,
            bearingDegrees = bearingDegrees,
            compassDirection = compassDirection,
        )
    }

    fun buildWithBearing(
        targetCategory: RadarTargetCategory,
        targetId: String,
        distanceMeters: Double,
        maxRangeMeters: Int,
        playerLat: Double,
        playerLng: Double,
        targetLat: Double,
        targetLng: Double,
        detectedTime: Long = System.currentTimeMillis(),
        radarType: RadarType? = null,
    ): RadarSignal? {
        val bearing = RadarBearingCalculator.bearingDegrees(
            fromLat = playerLat,
            fromLng = playerLng,
            toLat = targetLat,
            toLng = targetLng,
        )
        val direction = RadarCompassDirectionResolver.fromBearingDegrees(bearing)
        return build(
            targetCategory = targetCategory,
            targetId = targetId,
            distanceMeters = distanceMeters,
            maxRangeMeters = maxRangeMeters,
            detectedTime = detectedTime,
            radarType = radarType,
            bearingDegrees = bearing,
            compassDirection = direction,
        )
    }
}
