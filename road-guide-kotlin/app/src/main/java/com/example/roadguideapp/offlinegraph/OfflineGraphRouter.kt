package com.example.roadguideapp.offlinegraph

import android.util.Log
import com.example.roadguideapp.map.DirectionsPathOptimizer
import com.example.roadguideapp.map.DirectionsRouteLeg
import com.example.roadguideapp.map.DirectionsRouteResult
import com.example.roadguideapp.map.DirectionsTravelMode
import com.graphhopper.GHRequest
import com.graphhopper.GHResponse
import com.graphhopper.GraphHopper
import com.graphhopper.ResponsePath
import com.graphhopper.config.CHProfile
import com.graphhopper.config.Profile
import com.graphhopper.routing.ev.VehicleAccess
import com.graphhopper.routing.util.AccessFilter
import com.graphhopper.routing.util.EdgeFilter
import com.graphhopper.storage.index.Snap
import com.graphhopper.util.Parameters
import com.graphhopper.util.PointList
import org.maplibre.android.geometry.LatLng
import java.io.File
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Loads a GraphHopper graph-cache and routes offline (car / bike / foot).
 * All GraphHopper calls run on a dedicated single thread (same as OfflineGraphNavigation).
 */
internal object OfflineGraphRouter {

    private const val TAG = "OfflineGraphRouter"
    /** Warn when snap is farther than this; routing still uses the snapped point (donor behaviour). */
    private const val MAX_SNAP_WARN_METERS = 500.0
    private const val MIN_ROUTE_SEPARATION_METERS = 20.0
    private const val BOUNDS_MARGIN_DEGREES = 0.02

    @Volatile
    private var hopper: GraphHopper? = null

    fun isReady(): Boolean = hopper != null

    /**
     * Blocking load on the graph thread. Caller must enforce timeout via [OfflineGraphThreadRunner].
     */
    fun loadGraphBlocking(
        graphFolderPath: String,
        onProgress: (OfflineGraphProgress) -> Unit = {},
    ): Result<Unit> {
        return try {
            val graphDir = File(graphFolderPath)
            if (!graphDir.isDirectory) {
                return Result.failure(
                    IllegalStateException("Graph folder missing: $graphFolderPath"),
                )
            }
            if (!GraphBundleImporter.isValidGraphCache(graphDir)) {
                return Result.failure(
                    IllegalStateException(
                        "Not a valid GraphHopper graph-cache. Use graph-cache from prepare_graph.ps1.",
                    ),
                )
            }
            GraphBundleImporter.logGraphCacheSummary(graphDir)

            val loadStart = System.currentTimeMillis()
            onProgress(
                OfflineGraphProgress(
                    phase = OfflineGraphEngine.ImportPhase.LoadingGraphHopper,
                    detail = "Opening graph files…",
                    percent = null,
                    elapsedMs = 0L,
                ),
            )

            shutdownBlocking()
            val instance = createHopper(graphFolderPath)
            val loaded = try {
                instance.load()
            } catch (e: Exception) {
                Log.e(TAG, "GraphHopper.load() threw", e)
                runCatching { instance.close() }
                return Result.failure(e)
            }
            if (!loaded) {
                runCatching { instance.close() }
                val listing = graphDir.list()?.take(8)?.joinToString(", ").orEmpty()
                return Result.failure(
                    IllegalStateException(
                        "GraphHopper could not load this graph. " +
                            "Rebuild with prepare_graph.ps1 (car, bike, foot), use GraphHopper 7.0, " +
                            "and try a smaller region if the device runs out of memory. " +
                            if (listing.isNotBlank()) "Found: $listing" else "Graph folder is empty.",
                    ),
                )
            }
            hopper = instance
            val elapsed = System.currentTimeMillis() - loadStart
            Log.i(
                TAG,
                "Loaded graph from $graphFolderPath in ${elapsed}ms " +
                    "(nodes=${instance.baseGraph.nodes}, edges=${instance.baseGraph.edges})",
            )
            logPostLoadSmokeRoute(instance)
            onProgress(
                OfflineGraphProgress(
                    phase = OfflineGraphEngine.ImportPhase.LoadingGraphHopper,
                    detail = "Graph ready",
                    percent = 100,
                    elapsedMs = elapsed,
                ),
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load graph", e)
            Result.failure(e)
        }
    }

    fun shutdownBlocking() {
        runCatching { hopper?.close() }
        hopper = null
    }

    fun shutdown() {
        OfflineGraphThreadRunner.runBlocking("shutdown") {
            shutdownBlocking()
        }
    }

    /** Road-network distance for stop-order optimization (CH routing). */
    fun roadDistanceBlocking(
        from: LatLng,
        to: LatLng,
        mode: DirectionsTravelMode,
    ): Double? {
        val engine = hopper ?: return null
        for (profileId in profileCandidates(mode)) {
            val path = routeBetween(engine, from, to, profileId) ?: continue
            return path.distance
        }
        return null
    }

    fun routeBlocking(
        waypoints: List<LatLng>,
        mode: DirectionsTravelMode,
    ): DirectionsRouteResult? {
        val engine = hopper ?: run {
            Log.w(TAG, "routeBlocking: GraphHopper not loaded")
            return null
        }
        if (waypoints.size < 2) return null

        return try {
            val profiles = profileCandidates(mode)
            val combined = ArrayList<LatLng>()
            val legs = ArrayList<DirectionsRouteLeg>()
            var totalDistance = 0.0
            var totalTime = 0.0

            for (i in 0 until waypoints.lastIndex) {
                val from = waypoints[i]
                val to = waypoints[i + 1]

                var path: ResponsePath? = null
                var usedProfile: String? = null
                for (profileId in profiles) {
                    path = routeBetween(engine, from, to, profileId)
                    if (path != null) {
                        usedProfile = profileId
                        break
                    }
                }
                if (path == null) {
                    Log.w(
                        TAG,
                        "No GraphHopper path for leg $i (${from.latitude},${from.longitude} -> " +
                            "${to.latitude},${to.longitude}); tried $profiles",
                    )
                    return null
                }

                val fromSnap = snapToNetwork(engine, from, usedProfile!!)
                val toSnap = snapToNetwork(engine, to, usedProfile)
                val legEndpoints = if (fromSnap.valid && toSnap.valid) {
                    fromSnap.point to toSnap.point
                } else {
                    from to to
                }

                if (haversineMeters(legEndpoints.first, legEndpoints.second) <
                    MIN_ROUTE_SEPARATION_METERS
                ) {
                    if (combined.isEmpty()) combined.add(legEndpoints.first)
                    if (combined.last() != legEndpoints.second) {
                        combined.add(legEndpoints.second)
                    }
                    legs.add(
                        DirectionsRouteLeg(
                            durationSeconds = 1.0,
                            midPoint = DirectionsPathOptimizer.greatCircleMidpoint(
                                legEndpoints.first,
                                legEndpoints.second,
                            ),
                        ),
                    )
                    totalDistance += haversineMeters(legEndpoints.first, legEndpoints.second)
                    totalTime += 1_000.0
                    continue
                }

                val points = path.points.toLatLngList()
                if (points.size < 2) {
                    Log.w(TAG, "Leg $i path has < 2 points")
                    return null
                }

                val legDistance = path.distance
                val legTimeMs = path.time.toDouble()
                legs.add(
                    DirectionsRouteLeg(
                        durationSeconds = legTimeMs / 1000.0,
                        midPoint = points[points.size / 2],
                    ),
                )
                if (combined.isEmpty()) {
                    combined.addAll(points)
                } else {
                    combined.addAll(points.drop(1))
                }
                totalDistance += legDistance
                totalTime += legTimeMs
            }

            if (combined.size < 2) {
                Log.w(TAG, "GraphHopper produced no geometry (combined=${combined.size})")
                return null
            }

            val result = DirectionsRouteResult(
                geometry = combined,
                legs = legs,
                totalDurationSeconds = totalTime / 1000.0,
                totalLengthKm = totalDistance / 1000.0,
            )
            Log.i(
                TAG,
                "Offline route OK: ${result.geometry.size} pts, " +
                    "${"%.2f".format(result.totalLengthKm)} km, ${legs.size} legs",
            )
            result
        } catch (e: Exception) {
            Log.w(TAG, "Routing failed", e)
            null
        }
    }

    private fun logPostLoadSmokeRoute(engine: GraphHopper) {
        val bounds = engine.baseGraph.bounds ?: return
        val from = LatLng(
            (bounds.minLat + bounds.maxLat) / 2.0,
            (bounds.minLon + bounds.maxLon) / 2.0,
        )
        val to = LatLng(from.latitude + 0.002, from.longitude + 0.002)
        val ok = routeBetween(engine, from, to, "car") != null
        Log.i(TAG, "Post-load smoke route (car): ${if (ok) "OK" else "FAILED"}")
    }

    /**
     * Same setup as OfflineGraphNavigation [GraphHopperOfflineEngine.createHopper]:
     * no [GraphHopper.init] — load() opens an existing PC-built graph-cache as-is.
     */
    private fun createHopper(graphFolderPath: String): GraphHopper {
        val profiles = listOf("car", "bike", "foot").map { id ->
            Profile(id).setVehicle(id).setWeighting("shortest")
        }
        return GraphHopper().apply {
            setGraphHopperLocation(graphFolderPath)
            setProfiles(*profiles.toTypedArray())
            chPreparationHandler.setCHProfiles(
                *profiles.map { CHProfile(it.name) }.toTypedArray(),
            )
        }
    }

    private data class RoadSnap(val point: LatLng, val valid: Boolean)

    private fun profileCandidates(mode: DirectionsTravelMode): List<String> = when (mode) {
        DirectionsTravelMode.Drive -> listOf("car", "bike", "foot")
        DirectionsTravelMode.Bicycle -> listOf("bike", "car", "foot")
        DirectionsTravelMode.Walk -> listOf("foot", "bike", "car")
    }

    private fun routeBetween(
        engine: GraphHopper,
        from: LatLng,
        to: LatLng,
        profileId: String,
    ): ResponsePath? {
        val fromSnap = snapToNetwork(engine, from, profileId)
        val toSnap = snapToNetwork(engine, to, profileId)
        if (!fromSnap.valid || !toSnap.valid) {
            Log.w(
                TAG,
                "Snap failed ($profileId): fromValid=${fromSnap.valid} toValid=${toSnap.valid} " +
                    "from=(${from.latitude},${from.longitude}) to=(${to.latitude},${to.longitude})",
            )
            return null
        }
        return routeOptimizedPath(engine, fromSnap.point, toSnap.point, profileId)
    }

    /** Tries CH routing first, then flexible routing (same as OfflineGraphNavigation). */
    private fun routeOptimizedPath(
        engine: GraphHopper,
        from: LatLng,
        to: LatLng,
        profileId: String,
    ): ResponsePath? {
        for (useCh in booleanArrayOf(true, false)) {
            val response = routeSegment(engine, from, to, profileId, useCh = useCh)
            val path = response.best?.takeIf { !response.hasErrors() && it.points.size() >= 2 }
            if (path != null) {
                if (!useCh) {
                    Log.i(TAG, "Routed leg without CH ($profileId)")
                }
                return path
            }
            if (response.hasErrors()) {
                Log.w(
                    TAG,
                    "Route failed (ch=$useCh, $profileId): ${formatRouteErrors(response)}",
                )
            }
        }
        return null
    }

    private fun routeSegment(
        engine: GraphHopper,
        from: LatLng,
        to: LatLng,
        profileId: String,
        useCh: Boolean,
    ): GHResponse {
        val request = GHRequest(from.latitude, from.longitude, to.latitude, to.longitude)
            .setProfile(profileId)
        if (!useCh) {
            request.putHint(Parameters.CH.DISABLE, true)
            request.putHint(Parameters.Landmark.DISABLE, true)
        }
        return engine.route(request)
    }

    private fun formatRouteErrors(response: GHResponse): String =
        response.errors.joinToString { it.toString() }

    private fun snapToNetwork(
        engine: GraphHopper,
        point: LatLng,
        profileId: String,
    ): RoadSnap {
        if (!isWithinGraphBounds(engine, point)) {
            Log.w(TAG, "Tap outside graph bounds (${point.latitude}, ${point.longitude})")
            return RoadSnap(point, valid = false)
        }

        val profileSnap = findSnap(engine, point, profileId)
        if (profileSnap.valid) return profileSnap

        val anyRoadSnap = findSnap(engine, point, vehicleId = null)
        if (anyRoadSnap.valid) {
            Log.w(TAG, "Using generic road snap ($profileId profile snap failed)")
            return anyRoadSnap
        }

        Log.w(TAG, "No road snap near ${point.latitude},${point.longitude}")
        return RoadSnap(point, valid = false)
    }

    private fun isWithinGraphBounds(engine: GraphHopper, point: LatLng): Boolean {
        val bounds = engine.baseGraph.bounds ?: return true
        val margin = BOUNDS_MARGIN_DEGREES
        return point.latitude in (bounds.minLat - margin)..(bounds.maxLat + margin) &&
            point.longitude in (bounds.minLon - margin)..(bounds.maxLon + margin)
    }

    private fun findSnap(
        engine: GraphHopper,
        point: LatLng,
        vehicleId: String?,
    ): RoadSnap {
        val snap = try {
            if (vehicleId != null) {
                val accessEnc = engine.encodingManager.getBooleanEncodedValue(
                    VehicleAccess.key(vehicleId),
                )
                engine.locationIndex.findClosest(
                    point.latitude,
                    point.longitude,
                    AccessFilter.allEdges(accessEnc),
                )
            } else {
                engine.locationIndex.findClosest(
                    point.latitude,
                    point.longitude,
                    EdgeFilter.ALL_EDGES,
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "findClosest failed", e)
            return RoadSnap(point, valid = false)
        }
        return snap.toRoadSnap(point)
    }

    private fun Snap.toRoadSnap(fallback: LatLng): RoadSnap {
        if (!isValid) return RoadSnap(fallback, valid = false)
        val snapped = snappedPoint
        if (!snapped.lat.isFinite() || !snapped.lon.isFinite()) {
            return RoadSnap(fallback, valid = false)
        }
        if (queryDistance > MAX_SNAP_WARN_METERS) {
            Log.w(TAG, "Snap distance ${"%.0f".format(queryDistance)}m is far from tap")
        }
        return RoadSnap(LatLng(snapped.lat, snapped.lon), valid = true)
    }

    private fun PointList.toLatLngList(): List<LatLng> {
        val total = size()
        if (total == 0) return emptyList()
        val maxPoints = 800
        val step = maxOf(1, total / maxPoints)
        val list = ArrayList<LatLng>(minOf(total, maxPoints) + 1)
        var i = 0
        while (i < total) {
            val lat = getLat(i)
            val lon = getLon(i)
            if (lat.isFinite() && lon.isFinite()) {
                list.add(LatLng(lat, lon))
            }
            i += step
        }
        val lastIdx = total - 1
        if (lastIdx >= 0) {
            val lat = getLat(lastIdx)
            val lon = getLon(lastIdx)
            if (lat.isFinite() && lon.isFinite()) {
                val end = LatLng(lat, lon)
                if (list.isEmpty() || list.last() != end) list.add(end)
            }
        }
        return list
    }

    private fun haversineMeters(a: LatLng, b: LatLng): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(h))
    }
}
