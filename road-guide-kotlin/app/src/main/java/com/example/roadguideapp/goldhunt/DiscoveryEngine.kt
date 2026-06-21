package com.example.roadguideapp.goldhunt

import com.example.roadguideapp.goldhunt.events.DiscoveryEvent
import com.example.roadguideapp.goldhunt.events.DiscoveryEventBus
import com.example.roadguideapp.goldhunt.storage.DiscoveryStore
import org.maplibre.android.geometry.LatLng

internal class DiscoveryEngine(
    private val store: DiscoveryStore,
    private val eventBus: DiscoveryEventBus,
) {

    private var lastStamp: LatLng? = null
    private var lastStampTimeMs: Long = 0L

    fun resetMovementGate() {
        lastStamp = null
        lastStampTimeMs = 0L
    }

    /**
     * @return true if at least one new cell was revealed.
     */
    fun tryStamp(
        lat: Double,
        lng: Double,
        accuracyMeters: Float?,
        fromNavigation: Boolean,
    ): Boolean {
        val region = RegionCatalog.regionForLatLng(lat, lng) ?: return false
        if (!fromNavigation) {
            val accuracy = accuracyMeters ?: return false
            if (accuracy > DiscoveryConfig.MAX_ACCURACY_METERS) return false
        }
        val now = System.currentTimeMillis()
        val candidate = LatLng(lat, lng)
        val previous = lastStamp
        if (previous != null) {
            val dist = DiscoveryGrid.haversineMeters(
                previous.latitude,
                previous.longitude,
                lat,
                lng,
            )
            val elapsed = now - lastStampTimeMs
            if (dist < DiscoveryConfig.MIN_STAMP_DISTANCE_METERS &&
                elapsed < DiscoveryConfig.MAX_STAMP_INTERVAL_MS
            ) {
                return false
            }
        }
        lastStamp = candidate
        lastStampTimeMs = now

        val cells = DiscoveryGrid.disk(lat, lng, DiscoveryConfig.STAMP_RING)
        val added = store.markDiscovered(region.id, cells)
        if (added.isEmpty()) return false

        val ts = now
        for (cellId in added) {
            eventBus.publish(
                DiscoveryEvent(
                    cellId = cellId,
                    lat = lat,
                    lng = lng,
                    regionId = region.id,
                    timestampMs = ts,
                ),
            )
        }
        return true
    }
}
