package com.example.roadguideapp.goldhunt

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.roadguideapp.goldhunt.clusters.completion.ClusterCompletionManager
import com.example.roadguideapp.goldhunt.clusters.discovery.TreasureClusterDiscoveryEngine
import com.example.roadguideapp.goldhunt.clusters.discovery.TreasureClusterDiscoveryRepository
import com.example.roadguideapp.goldhunt.discovery.GoldHuntFogVisibility
import com.example.roadguideapp.goldhunt.engine.FogDiscoveryEngine
import com.example.roadguideapp.goldhunt.engine.LocationSampler
import com.example.roadguideapp.goldhunt.engine.TreasureCollectionEngine
import com.example.roadguideapp.goldhunt.overlays.GoldHuntTreasureMapStyle
import com.example.roadguideapp.goldhunt.overlays.SecretPlaceOverlay
import com.example.roadguideapp.goldhunt.overlays.TreasureOverlay
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceGenerator
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceGeoBuilder
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceRevealPolicy
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator
import com.example.roadguideapp.goldhunt.treasure.TreasureGeoBuilder
import com.example.roadguideapp.goldhunt.treasure.TreasureHoardGenerator
import com.example.roadguideapp.goldhunt.treasure.events.EventTreasureGenerator
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportLogBuilder
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportNumberLogBuilder
import com.example.roadguideapp.map.MapScreenController
import com.example.roadguideapp.map.MapViewportBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap

@Composable
internal fun GoldHuntLocationEffect(
    context: Context,
    controller: MapScreenController,
    goldHunt: GoldHuntController,
    hasLocationPermission: Boolean,
) {
    val engines = remember { GoldHuntMapEffectEngines() }
    LaunchedEffect(goldHunt.isActive, goldHunt.initialized) {
        if (goldHunt.isActive && goldHunt.initialized) {
            engines.ensureReady(context, goldHunt)
        } else if (!goldHunt.isActive) {
            engines.reset()
        }
    }
    LaunchedEffect(
        goldHunt.isActive,
        hasLocationPermission,
        goldHunt.initialized,
        controller.mapOverlayCameraTick,
    ) {
        if (!goldHunt.isActive || !hasLocationPermission || !goldHunt.initialized) return@LaunchedEffect
        if (!engines.ready) return@LaunchedEffect
        val fogEngine = engines.fogDiscoveryEngine ?: return@LaunchedEffect
        val treasureEngine = engines.treasureEngine ?: return@LaunchedEffect
        val clusterDiscoveryEngine = engines.clusterDiscoveryEngine
        LocationSampler.samples(context).collect { sample ->
            fogEngine.tryStamp(
                lat = sample.lat,
                lng = sample.lng,
                accuracyMeters = sample.accuracyM,
                timestampMs = sample.timestampMs,
            )
            val clusterDiscoveries = clusterDiscoveryEngine?.tryDiscoverNear(
                lat = sample.lat,
                lng = sample.lng,
                accuracyMeters = sample.accuracyM,
                timestampMs = sample.timestampMs,
            ).orEmpty()
            if (clusterDiscoveries.isNotEmpty()) {
                goldHunt.onClustersDiscovered(clusterDiscoveries.size)
            }
            val map = controller.mapLibreMap
            val zoom = map?.cameraPosition?.zoom?.toDouble()
            if (zoom != null && GoldHuntMapZoom.treasuresVisibleAt(zoom)) {
                treasureEngine.tryCollectNear(sample.lat, sample.lng, sample.speedMps)
            }
        }
    }
}

@Composable
internal fun GoldHuntTreasureEffect(
    context: Context,
    controller: MapScreenController,
    goldHunt: GoldHuntController,
    map3dEnabled: Boolean,
) {
    LaunchedEffect(
        goldHunt.isActive,
        goldHunt.initialized,
        goldHunt.treasureRevision,
        goldHunt.hiddenTreasureRevision,
        goldHunt.profile,
        controller.mapRuntime,
        controller.mapOverlayCameraTick,
        map3dEnabled,
    ) {
        if (!goldHunt.isActive || !goldHunt.initialized) {
            val runtime = controller.mapRuntime
            if (runtime != null) {
                withContext(Dispatchers.Main.immediate) {
                    TreasureOverlay.remove(runtime.second)
                }
            }
            return@LaunchedEffect
        }
        val (map, style) = controller.mapRuntime ?: return@LaunchedEffect
        val mapView = controller.mapView ?: return@LaunchedEffect
        val zoom = map.cameraPosition.zoom.toDouble()
        val display3d = GoldHuntTreasureMapStyle.isDisplay3d(
            map3dEnabled = map3dEnabled,
            cameraTiltDeg = map.cameraPosition.tilt,
        )
        val density = context.applicationContext.resources.displayMetrics.density
        if (!GoldHuntMapZoom.treasuresVisibleAt(zoom)) {
            if (goldHunt.isTreasureDisappearing) {
                withContext(Dispatchers.Main.immediate) {
                    TreasureOverlay.applyDisappearVisuals(
                        style,
                        goldHunt.treasureDisappearProgress,
                        display3d,
                        density,
                    )
                }
                return@LaunchedEffect
            }
            val valueLog = TreasureViewportLogBuilder.fromTreasures(emptyList(), zoom)
            val numberLog = TreasureViewportNumberLogBuilder.fromTreasures(emptyList(), zoom)
            withContext(Dispatchers.Main.immediate) {
                TreasureOverlay.remove(style)
                goldHunt.updateTreasureValueLog(valueLog)
                goldHunt.updateTreasureNumberLog(numberLog)
            }
            return@LaunchedEffect
        }
        val bounds = MapViewportBounds.visibleBounds(map, mapView) ?: return@LaunchedEffect
        val repo = goldHunt.repository()
        val treasurePayload = withContext(Dispatchers.IO) {
            repo.ensureInitialized()
            val region = repo.playRegion()
            val tier = repo.treasureGenerationTier()
            val isCollected: (String) -> Boolean = { repo.isTreasureCollectedCached(it) }
            val isInPlayRegion: (Double, Double) -> Boolean = { lat, lng ->
                repo.isTreasurePlacementExploredCached(lat, lng)
            }
            val grid = TreasureGenerator.treasuresInBounds(
                bounds = bounds,
                region = region,
                isCollected = isCollected,
                tier = tier,
                isInPlayRegion = isInPlayRegion,
            )
            val clusterCompletionManager = ClusterCompletionManager.get(context.applicationContext)
            clusterCompletionManager.ensureCacheLoaded()
            val clusterTreasures = TreasureGenerator.clusterTreasuresInBounds(
                bounds = bounds,
                region = region,
                tier = tier,
                isCollected = isCollected,
                isInPlayRegion = isInPlayRegion,
                isClusterCompleted = clusterCompletionManager::isCompletedCached,
            )
            val hoard = GoldHuntPreferences.getActiveHoard(context.applicationContext)
            val hoardTreasures = hoard?.let {
                TreasureHoardGenerator.treasuresForViewport(
                    hoard = it,
                    bounds = bounds,
                    region = region,
                    tier = tier,
                    isCollected = isCollected,
                )
            } ?: emptyList()
            val eventTreasures = EventTreasureGenerator.treasuresInBounds(
                bounds = bounds,
                region = region,
                isCollected = isCollected,
                tier = tier,
                isInPlayRegion = isInPlayRegion,
            )
            val merged = LinkedHashMap<String, TreasureSpec>(
                grid.size + clusterTreasures.size + hoardTreasures.size + eventTreasures.size,
            )
            for (spec in grid) merged[spec.treasureId] = spec
            for (spec in clusterTreasures) merged[spec.treasureId] = spec
            for (spec in hoardTreasures) merged[spec.treasureId] = spec
            for (spec in eventTreasures) merged[spec.treasureId] = spec
            val treasures = merged.values.filter { spec ->
                !goldHunt.isTreasureHiddenFromOverlay(spec.treasureId)
            }
            val collection = TreasureGeoBuilder.build(treasures)
            val valueLog = TreasureViewportLogBuilder.fromTreasures(treasures, zoom)
            val numberLog = TreasureViewportNumberLogBuilder.fromTreasures(treasures, zoom)
            Triple(collection, valueLog, numberLog)
        }
        withContext(Dispatchers.Main.immediate) {
            TreasureOverlay.sync(
                style,
                context.applicationContext,
                treasurePayload.first,
                display3d = display3d,
            )
            if (goldHunt.treasureAppearProgress < 1f) {
                TreasureOverlay.applyAppearVisuals(
                    style,
                    goldHunt.treasureAppearProgress,
                    display3d,
                    density,
                )
            }
            goldHunt.updateTreasureValueLog(treasurePayload.second)
            goldHunt.updateTreasureNumberLog(treasurePayload.third)
        }
    }
}

@Composable
internal fun GoldHuntSecretPlaceEffect(
    context: Context,
    controller: MapScreenController,
    goldHunt: GoldHuntController,
    map3dEnabled: Boolean,
) {
    LaunchedEffect(
        goldHunt.isActive,
        goldHunt.initialized,
        goldHunt.secretRevision,
        goldHunt.profile,
        controller.mapRuntime,
        controller.mapOverlayCameraTick,
        map3dEnabled,
    ) {
        if (!goldHunt.isActive || !goldHunt.initialized) {
            val runtime = controller.mapRuntime
            if (runtime != null) {
                withContext(Dispatchers.Main.immediate) {
                    SecretPlaceOverlay.remove(runtime.second)
                }
            }
            return@LaunchedEffect
        }
        val (map, style) = controller.mapRuntime ?: return@LaunchedEffect
        val mapView = controller.mapView ?: return@LaunchedEffect
        val zoom = map.cameraPosition.zoom.toDouble()
        if (!GoldHuntMapZoom.secretsVisibleAt(zoom)) {
            withContext(Dispatchers.Main.immediate) {
                SecretPlaceOverlay.remove(style)
            }
            return@LaunchedEffect
        }
        val bounds = MapViewportBounds.visibleBounds(map, mapView) ?: return@LaunchedEffect
        val collection = withContext(Dispatchers.IO) {
            val repo = goldHunt.repository()
            repo.ensureInitialized()
            val region = repo.playRegion()
            val secretsUnlocked = repo.areSecretPlacesUnlocked()
            val isDiscovered: (String) -> Boolean = { repo.isSecretPlaceDiscoveredCached(it) }
            val generated = SecretPlaceGenerator.secretsInBounds(
                bounds = bounds,
                region = region,
                isDiscovered = isDiscovered,
                additionalSecretGeneration = secretsUnlocked,
            )
            val visible = ArrayList<SecretPlaceSpec>()
            for (spec in generated) {
                if (
                    secretsUnlocked &&
                    SecretPlaceRevealPolicy.isVisible(spec, repo) &&
                    GoldHuntFogVisibility.isInPlayRegion(spec.lat, spec.lng, region)
                ) {
                    visible += spec
                }
            }
            SecretPlaceGeoBuilder.build(visible)
        }
        val display3d = GoldHuntTreasureMapStyle.isDisplay3d(
            map3dEnabled = map3dEnabled,
            cameraTiltDeg = map.cameraPosition.tilt,
        )
        withContext(Dispatchers.Main.immediate) {
            SecretPlaceOverlay.sync(
                style,
                context.applicationContext,
                collection,
                display3d = display3d,
            )
        }
    }
}

internal class GoldHuntMapEffectEngines {
    var treasureEngine: TreasureCollectionEngine? = null
        private set
    var fogDiscoveryEngine: FogDiscoveryEngine? = null
        private set
    var clusterDiscoveryEngine: TreasureClusterDiscoveryEngine? = null
        private set

    val ready: Boolean get() =
        treasureEngine != null && fogDiscoveryEngine != null && clusterDiscoveryEngine != null

    fun ensureReady(context: Context, goldHunt: GoldHuntController) {
        val repository = goldHunt.repository()
        val discoveryRepository = TreasureClusterDiscoveryRepository.get(context)
        treasureEngine = TreasureCollectionEngine(repository)
        fogDiscoveryEngine = FogDiscoveryEngine(repository)
        clusterDiscoveryEngine = TreasureClusterDiscoveryEngine(repository, discoveryRepository)
    }

    fun reset() {
        fogDiscoveryEngine?.resetMovementGate()
    }
}
