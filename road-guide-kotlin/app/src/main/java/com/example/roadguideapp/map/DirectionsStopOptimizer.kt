package com.example.roadguideapp.map

import com.example.roadguideapp.offlinegraph.OfflineGraphRouter
import com.example.roadguideapp.offlinegraph.OfflineGraphThreadRunner
import org.maplibre.android.geometry.LatLng

/** Orders intermediate stops from a fixed origin using road or haversine distances. */
internal object DirectionsStopOptimizer {

    private const val MAX_TWO_OPT_ITERATIONS = 12
    private const val MAX_GRAPH_OPTIMIZE_STOPS = 8

    suspend fun optimize(
        origin: LatLng,
        stops: List<MapPlaceDetail>,
        mode: DirectionsTravelMode,
    ): List<MapPlaceDetail> {
        if (stops.size <= 1) return stops

        val useGraph = OfflineGraphRouter.isReady() && stops.size <= MAX_GRAPH_OPTIMIZE_STOPS
        val distanceFn: suspend (LatLng, LatLng) -> Double = { a, b ->
            if (useGraph) {
                val meters = OfflineGraphThreadRunner.runBlocking("stop-opt-distance") {
                    OfflineGraphRouter.roadDistanceBlocking(a, b, mode)
                }.getOrNull()
                meters ?: DirectionsPathOptimizer.haversineMeters(a, b)
            } else {
                DirectionsPathOptimizer.haversineMeters(a, b)
            }
        }

        var ordered = nearestNeighborFromOrigin(origin, stops, distanceFn)
        if (ordered.size >= 3) {
            ordered = twoOptImprovePath(origin, ordered, distanceFn)
        }
        return ordered
    }

    private suspend fun nearestNeighborFromOrigin(
        origin: LatLng,
        stops: List<MapPlaceDetail>,
        distanceFn: suspend (LatLng, LatLng) -> Double,
    ): List<MapPlaceDetail> {
        val remaining = stops.toMutableList()
        val tour = ArrayList<MapPlaceDetail>(stops.size)
        var cursor = origin
        while (remaining.isNotEmpty()) {
            var bestIdx = 0
            var bestDist = Double.MAX_VALUE
            remaining.forEachIndexed { i, stop ->
                val d = distanceFn(cursor, stop.latLng)
                if (d < bestDist) {
                    bestDist = d
                    bestIdx = i
                }
            }
            val next = remaining.removeAt(bestIdx)
            tour.add(next)
            cursor = next.latLng
        }
        return tour
    }

    private suspend fun twoOptImprovePath(
        origin: LatLng,
        stops: List<MapPlaceDetail>,
        distanceFn: suspend (LatLng, LatLng) -> Double,
    ): List<MapPlaceDetail> {
        val best = stops.toMutableList()
        var improved = true
        var iterations = 0
        while (improved && iterations < MAX_TWO_OPT_ITERATIONS) {
            improved = false
            iterations++
            for (i in 0 until best.size - 2) {
                for (j in i + 2 until best.size) {
                    val delta = twoOptDelta(origin, best, i, j, distanceFn)
                    if (delta < -1.0) {
                        reverseSegment(best, i + 1, j)
                        improved = true
                    }
                }
            }
        }
        return best
    }

    private suspend fun twoOptDelta(
        origin: LatLng,
        tour: List<MapPlaceDetail>,
        i: Int,
        j: Int,
        distanceFn: suspend (LatLng, LatLng) -> Double,
    ): Double {
        val a = if (i == 0) origin else tour[i - 1].latLng
        val b = tour[i].latLng
        val c = tour[j].latLng
        val d = tour.getOrNull(j + 1)?.latLng
        val before = distanceFn(a, b) + if (d != null) distanceFn(c, d) else 0.0
        val after = distanceFn(a, c) + if (d != null) distanceFn(b, d) else 0.0
        return after - before
    }

    private fun reverseSegment(tour: MutableList<MapPlaceDetail>, from: Int, to: Int) {
        var left = from
        var right = to
        while (left < right) {
            val tmp = tour[left]
            tour[left] = tour[right]
            tour[right] = tmp
            left++
            right--
        }
    }
}
