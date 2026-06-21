package com.example.roadguideapp.goldhunt.engine

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.repository.DiscoveryTickResult
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Stamps explored grid cells from GPS during Gold Hunt (fog + road/area discovery XP).
 */
internal class FogDiscoveryEngine(
    private val repository: GoldHuntRepository,
) {
    private var lastStampLat: Double? = null
    private var lastStampLng: Double? = null
    private var lastStampTimeMs: Long = 0L

    fun resetMovementGate() {
        lastStampLat = null
        lastStampLng = null
        lastStampTimeMs = 0L
    }

    suspend fun tryStamp(
        lat: Double,
        lng: Double,
        accuracyMeters: Float?,
        timestampMs: Long = System.currentTimeMillis(),
    ): DiscoveryTickResult {
        if (accuracyMeters != null && accuracyMeters > GoldHuntConfig.MAX_LOCATION_ACCURACY_M) {
            return DiscoveryTickResult.skipped()
        }
        val previousLat = lastStampLat
        val previousLng = lastStampLng
        if (previousLat != null && previousLng != null) {
            val dist = haversineMeters(previousLat, previousLng, lat, lng)
            val elapsed = timestampMs - lastStampTimeMs
            if (dist < GoldHuntConfig.MIN_LOCATION_DISTANCE_M &&
                elapsed < GoldHuntConfig.MIN_LOCATION_INTERVAL_MS
            ) {
                return DiscoveryTickResult.skipped()
            }
        }
        lastStampLat = lat
        lastStampLng = lng
        lastStampTimeMs = timestampMs

        repository.ensureInitialized()
        val region = repository.playRegion()
        val cell = GridIndex.encode(lat, lng, level = 0, region = region)
            ?: return DiscoveryTickResult.skipped()
        return repository.discoverCell(cell, timestampMs)
    }

    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        return 6_371_000.0 * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
