package com.example.roadguideapp.map

import android.content.Context
import android.util.Log
import com.example.roadguideapp.offlinegraph.OfflineGraphEngine
import com.example.roadguideapp.offlinegraph.OfflineGraphProgress
import com.example.roadguideapp.offlinegraph.OfflineGraphRouter
import com.example.roadguideapp.offlinegraph.OfflineGraphThreadRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng

internal enum class DirectionsRouteSource {
    Preview,
    Valhalla,
    OfflineGraph,
    Unavailable,
}

internal data class DirectionsRoutingOutcome(
    val result: DirectionsRouteResult?,
    val source: DirectionsRouteSource,
)

internal data class DirectionsPlanOutcome(
    val optimizedStops: List<MapPlaceDetail>,
    val result: DirectionsRouteResult?,
    val source: DirectionsRouteSource,
)

/**
 * Offline-first directions: load graph if imported, route on roads, optional Valhalla fallback.
 */
internal object DirectionsRoutingService {

    private const val TAG = "DirectionsRoutingService"

    suspend fun planDirectionsRoute(
        context: Context,
        origin: MapPlaceDetail,
        stops: List<MapPlaceDetail>,
        mode: DirectionsTravelMode,
        tripWaypoints: List<MapPlaceDetail> = emptyList(),
        onLoadProgress: (OfflineGraphProgress) -> Unit = {},
    ): DirectionsPlanOutcome {
        val useFullTrip = tripWaypoints.size >= 2
        if (stops.isEmpty() && !useFullTrip) {
            return DirectionsPlanOutcome(stops, null, DirectionsRouteSource.Unavailable)
        }

        if (hasSavedGraph(context) && !OfflineGraphRouter.isReady()) {
            val loaded = awaitOfflineGraphReady(context, onLoadProgress)
            if (!loaded) {
                Log.w(TAG, "Saved graph could not be loaded; trying online/preview fallback")
            }
        }

        val graphReady = OfflineGraphRouter.isReady()
        Log.d(
            TAG,
            "planDirectionsRoute graphReady=$graphReady hasSaved=${hasSavedGraph(context)} " +
                "tripLegs=${if (useFullTrip) tripWaypoints.size - 1 else stops.size}",
        )

        val optimizedStops = if (useFullTrip) {
            tripWaypoints.drop(1)
        } else if (graphReady && stops.size > 1) {
            DirectionsStopOptimizer.optimize(origin.latLng, stops, mode)
        } else {
            stops
        }
        val waypoints = if (useFullTrip) {
            tripWaypoints.map { it.latLng }
        } else {
            listOf(origin.latLng) + optimizedStops.map { it.latLng }
        }

        if (graphReady) {
            val route = routeOffline(waypoints, mode)
            if (route != null && route.geometry.size >= 2) {
                Log.i(TAG, "Using offline GraphHopper route (${route.geometry.size} points)")
                return DirectionsPlanOutcome(
                    optimizedStops = if (useFullTrip) stops else optimizedStops,
                    result = route.withMapDisplayGeometry(),
                    source = DirectionsRouteSource.OfflineGraph,
                )
            }
            Log.w(TAG, "Offline GraphHopper route failed for ${waypoints.size} waypoints; trying fallbacks")
        }

        if (ValhallaReachability.probeIfNeeded()) {
            ValhallaRouteClient.fetchRoute(waypoints, mode)?.let { route ->
                if (route.geometry.size >= 2) {
                    return DirectionsPlanOutcome(
                        optimizedStops = if (useFullTrip) stops else optimizedStops,
                        result = route.withMapDisplayGeometry(),
                        source = DirectionsRouteSource.Valhalla,
                    )
                }
            }
        }

        if (hasSavedGraph(context)) {
            Log.e(
                TAG,
                "Offline graph is saved but routing failed (graphReady=$graphReady, " +
                    "waypoints=${waypoints.size})",
            )
            return DirectionsPlanOutcome(
                optimizedStops = if (useFullTrip) stops else optimizedStops,
                result = null,
                source = DirectionsRouteSource.Unavailable,
            )
        }

        if (OfflineGraphEngine.isImportInProgress() || OfflineGraphEngine.isLoadInProgress()) {
            Log.i(TAG, "Graph import/load in progress; skipping preview route line")
            return DirectionsPlanOutcome(
                optimizedStops = if (useFullTrip) stops else optimizedStops,
                result = null,
                source = DirectionsRouteSource.Unavailable,
            )
        }

        if (!hasSavedGraph(context)) {
            Log.i(TAG, "No offline graph imported; skipping straight-line preview route")
            return DirectionsPlanOutcome(
                optimizedStops = if (useFullTrip) stops else optimizedStops,
                result = null,
                source = DirectionsRouteSource.Unavailable,
            )
        }

        val preview = buildPreviewRoute(waypoints, mode).withMapDisplayGeometry()
        return DirectionsPlanOutcome(
            optimizedStops = if (useFullTrip) stops else optimizedStops,
            result = preview,
            source = if (preview.geometry.size >= 2) {
                DirectionsRouteSource.Preview
            } else {
                DirectionsRouteSource.Unavailable
            },
        )
    }

    suspend fun planRoute(
        context: Context,
        waypoints: List<LatLng>,
        mode: DirectionsTravelMode,
        onLoadProgress: (OfflineGraphProgress) -> Unit = {},
    ): DirectionsRoutingOutcome {
        if (waypoints.size < 2) {
            return DirectionsRoutingOutcome(null, DirectionsRouteSource.Unavailable)
        }

        if (hasSavedGraph(context) && !OfflineGraphRouter.isReady()) {
            awaitOfflineGraphReady(context, onLoadProgress)
        }

        if (OfflineGraphRouter.isReady()) {
            routeOffline(waypoints, mode)?.let { route ->
                return DirectionsRoutingOutcome(route.withMapDisplayGeometry(), DirectionsRouteSource.OfflineGraph)
            }
        }

        if (!hasSavedGraph(context) && ValhallaReachability.probeIfNeeded()) {
            ValhallaRouteClient.fetchRoute(waypoints, mode)?.let { route ->
                return DirectionsRoutingOutcome(route.withMapDisplayGeometry(), DirectionsRouteSource.Valhalla)
            }
        }

        return DirectionsRoutingOutcome(null, DirectionsRouteSource.Unavailable)
    }

    fun hasSavedGraph(context: Context): Boolean = OfflineGraphEngine.hasSavedGraph(context)

    /** True when an imported offline graph exists or is already loaded in memory. */
    fun isOfflineRoutingConfigured(context: Context): Boolean =
        hasSavedGraph(context) || OfflineGraphEngine.isLoaded()

    suspend fun canRoute(context: Context): Boolean {
        if (OfflineGraphEngine.isLoaded() || hasSavedGraph(context)) return true
        return ValhallaReachability.probeIfNeeded() && ValhallaReachability.isReachable()
    }

    suspend fun awaitOfflineGraphReady(
        context: Context,
        onProgress: (OfflineGraphProgress) -> Unit = {},
    ): Boolean {
        if (OfflineGraphEngine.isLoaded()) return true
        if (!hasSavedGraph(context)) return false
        while (OfflineGraphEngine.isLoadInProgress()) {
            delay(250)
        }
        return OfflineGraphEngine.ensureReady(context, onProgress)
    }

    fun hasOfflineGraph(): Boolean = OfflineGraphEngine.isLoaded()

    private suspend fun routeOffline(
        waypoints: List<LatLng>,
        mode: DirectionsTravelMode,
    ): DirectionsRouteResult? = withContext(Dispatchers.IO) {
        val threadResult = OfflineGraphThreadRunner.runBlocking("route") {
            OfflineGraphRouter.routeBlocking(waypoints, mode)
        }
        when {
            threadResult.isFailure -> {
                Log.e(TAG, "Offline route thread failed", threadResult.exceptionOrNull())
                null
            }
            else -> threadResult.getOrNull()
        }
    }

    private fun buildPreviewRoute(
        waypoints: List<LatLng>,
        mode: DirectionsTravelMode,
    ): DirectionsRouteResult {
        val geometry = DirectionsPathOptimizer.buildPolyline(waypoints, segmentsPerLeg = 32)
        val speedMps = assumedSpeedMps(mode)
        val legs = ArrayList<DirectionsRouteLeg>(waypoints.size)
        var totalSeconds = 0.0
        var totalKm = 0.0
        for (i in 0 until waypoints.lastIndex) {
            val a = waypoints[i]
            val b = waypoints[i + 1]
            val meters = DirectionsPathOptimizer.haversineMeters(a, b)
            val legSeconds = (meters / speedMps).coerceAtLeast(1.0)
            totalSeconds += legSeconds
            totalKm += meters / 1000.0
            legs.add(
                DirectionsRouteLeg(
                    durationSeconds = legSeconds,
                    midPoint = DirectionsPathOptimizer.greatCircleMidpoint(a, b),
                ),
            )
        }
        return DirectionsRouteResult(
            geometry = geometry,
            legs = legs,
            totalDurationSeconds = totalSeconds,
            totalLengthKm = totalKm,
        )
    }

    private fun assumedSpeedMps(mode: DirectionsTravelMode): Double = when (mode) {
        DirectionsTravelMode.Walk -> 1.39
        DirectionsTravelMode.Bicycle -> 4.17
        DirectionsTravelMode.Drive -> 22.22
    }
}
