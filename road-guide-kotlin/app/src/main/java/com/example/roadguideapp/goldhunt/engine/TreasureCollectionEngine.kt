package com.example.roadguideapp.goldhunt.engine

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator
import com.example.roadguideapp.goldhunt.treasure.TreasureHoardGenerator
import com.example.roadguideapp.goldhunt.treasure.events.EventTreasureGenerator
import com.example.roadguideapp.goldhunt.treasure.TreasureTapCollector
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import kotlin.math.cos

internal class TreasureCollectionEngine(
    private val repository: GoldHuntRepository,
) {
    private var lastCollectMs = 0L
    private var lastLat: Double? = null
    private var lastLng: Double? = null

    suspend fun tryCollectNear(lat: Double, lng: Double, speedMps: Double?) {
        val now = System.currentTimeMillis()
        if (now - lastCollectMs < GoldHuntConfig.TREASURE_COLLECT_COOLDOWN_MS) return
        val prevLat = lastLat
        val prevLng = lastLng
        if (prevLat != null && prevLng != null) {
            val moved = haversineMeters(prevLat, prevLng, lat, lng)
            if (moved < GoldHuntConfig.TREASURE_MIN_MOVEMENT_M) return
        }
        val speed = speedMps ?: 0.0
        if (speed > GoldHuntConfig.TREASURE_MAX_SPEED_MPS) return
        val searchRadius = if (speed >= GoldHuntConfig.TREASURE_DRIVE_SPEED_MPS) {
            GoldHuntConfig.TREASURE_COLLECT_RADIUS_DRIVE_M
        } else {
            GoldHuntConfig.TREASURE_COLLECT_RADIUS_WALK_M
        }
        val region = repository.playRegion()
        val tier = repository.treasureGenerationTier()
        val isCollected: (String) -> Boolean = { repository.isTreasureCollectedCached(it) }
        val isInPlayRegion: (Double, Double) -> Boolean = { tLat, tLng ->
            repository.isTreasurePlacementExploredCached(tLat, tLng)
        }
        val candidates = TreasureGenerator.nearLatLng(
            lat = lat,
            lng = lng,
            radiusM = searchRadius,
            region = region,
            isCollected = isCollected,
            tier = tier,
            isInPlayRegion = isInPlayRegion,
        )
        val metersPerDegLat = 111_320.0
        val metersPerDegLng = 111_320.0 * cos(Math.toRadians(lat)).coerceAtLeast(0.2)
        val dLat = searchRadius / metersPerDegLat
        val dLng = searchRadius / metersPerDegLng
        val searchBounds = LatLngBounds.Builder()
            .include(LatLng(lat - dLat, lng - dLng))
            .include(LatLng(lat + dLat, lng + dLng))
            .build()
        val hoardCandidates = repository.activeTreasureHoard()?.let { hoard ->
            TreasureHoardGenerator.treasuresForViewport(
                hoard = hoard,
                bounds = searchBounds,
                region = region,
                tier = tier,
                isCollected = isCollected,
            )
        } ?: emptyList()
        val eventCandidates = EventTreasureGenerator.nearLatLng(
            lat = lat,
            lng = lng,
            radiusM = searchRadius,
            region = region,
            isCollected = isCollected,
            tier = tier,
            isInPlayRegion = isInPlayRegion,
        )
        val allCandidates = candidates + hoardCandidates + eventCandidates
        for (spec in allCandidates) {
            val dist = haversineMeters(lat, lng, spec.lat, spec.lng)
            if (dist > searchRadius) continue
            val outcome = TreasureTapCollector.collectOnTap(repository, spec, now)
            if (outcome is com.example.roadguideapp.goldhunt.treasure.TreasureCollectOutcome.Collected) {
                lastCollectMs = now
                lastLat = lat
                lastLng = lng
                return
            }
        }
        lastLat = lat
        lastLng = lng
    }

    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) *
            kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
        return 6_371_000.0 * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }
}
