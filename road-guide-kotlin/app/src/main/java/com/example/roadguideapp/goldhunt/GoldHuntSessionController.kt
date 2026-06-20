package com.example.roadguideapp.goldhunt

import android.content.Context
import com.example.roadguideapp.goldhunt.events.DiscoveryEventBus
import com.example.roadguideapp.goldhunt.storage.DiscoveryStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Orchestrates Gold Hunt mode: persistence, discovery stamping, and location sampling.
 */
internal class GoldHuntSessionController(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val store = DiscoveryStore(appContext)
    val eventBus = DiscoveryEventBus()
    val engine = DiscoveryEngine(store, eventBus)
    val region: DiscoveryRegion = RegionCatalog.defaultRegion

    private val locationSampler = LocationSampler(appContext) { lat, lng, accuracy ->
        onPosition(lat, lng, accuracy, fromNavigation = false)
    }

    @Volatile
    var isActive: Boolean = false
        private set

    @Volatile
    var loadComplete: Boolean = false
        private set

    private var flushJob: Job? = null

    fun setActive(active: Boolean, onReady: (() -> Unit)? = null) {
        if (active == isActive && (loadComplete || !active)) {
            onReady?.invoke()
            return
        }
        isActive = active
        if (!active) {
            flushJob?.cancel()
            flushJob = null
            loadComplete = false
            locationSampler.stop()
            store.flushNow(region.id)
            onReady?.invoke()
            return
        }
        scope.launch(Dispatchers.IO) {
            store.ensureLoaded(region)
            loadComplete = true
            engine.resetMovementGate()
            withContext(Dispatchers.Main.immediate) {
                locationSampler.start()
            }
            flushJob?.cancel()
            flushJob = scope.launch {
                while (isActive) {
                    delay(DiscoveryConfig.FLUSH_INTERVAL_MS)
                    store.flushNow(region.id)
                }
            }
            withContext(Dispatchers.Main.immediate) {
                onReady?.invoke()
            }
        }
    }

    fun onPosition(
        lat: Double,
        lng: Double,
        accuracyMeters: Float?,
        fromNavigation: Boolean,
    ): Boolean {
        if (!isActive || !loadComplete) return false
        return engine.tryStamp(lat, lng, accuracyMeters, fromNavigation)
    }

    fun regionPercent(): Float = ProgressCalculator.regionPercent(store, region)

    fun dispose() {
        locationSampler.stop()
        if (isActive) {
            store.flushNow(region.id)
        }
    }
}
