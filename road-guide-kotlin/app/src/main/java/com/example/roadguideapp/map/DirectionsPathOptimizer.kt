package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Greedy **nearest-neighbor** ordering for intermediate stops from a fixed start ([origin]).
 * Used only to reduce total travel distance on the drawn route when the user adds multiple
 * unordered stops; the directions sheet may still list stops in add order.
 */
internal object DirectionsPathOptimizer {

    fun nearestNeighborStops(origin: LatLng, stops: List<MapPlaceDetail>): List<MapPlaceDetail> {
        if (stops.size <= 1) return stops
        val remaining = stops.toMutableList()
        val ordered = ArrayList<MapPlaceDetail>(stops.size)
        var cursor = origin
        while (remaining.isNotEmpty()) {
            var bestIdx = 0
            var bestDist = Double.POSITIVE_INFINITY
            remaining.forEachIndexed { i, stop ->
                val d = haversineMeters(cursor, stop.latLng)
                if (d < bestDist) {
                    bestDist = d
                    bestIdx = i
                }
            }
            val next = remaining.removeAt(bestIdx)
            ordered.add(next)
            cursor = next.latLng
        }
        return ordered
    }

    internal fun greatCircleMidpoint(a: LatLng, b: LatLng): LatLng {
        val seg = greatCircleSegment(a, b, 2)
        return seg[1]
    }

    /**
     * Builds a dense [LatLng] polyline along [waypoints] with great-circle interpolation
     * between consecutive vertices ([segmentsPerLeg] points per leg, inclusive of endpoints).
     */
    fun buildPolyline(
        waypoints: List<LatLng>,
        segmentsPerLeg: Int = 20,
    ): List<LatLng> {
        if (waypoints.size < 2) return waypoints
        val out = ArrayList<LatLng>(waypoints.size * segmentsPerLeg)
        for (i in 0 until waypoints.lastIndex) {
            val a = waypoints[i]
            val b = waypoints[i + 1]
            val seg = greatCircleSegment(a, b, segmentsPerLeg)
            if (out.isNotEmpty()) seg.drop(1).forEach { out.add(it) } else out.addAll(seg)
        }
        return out
    }

    private fun greatCircleSegment(from: LatLng, to: LatLng, segments: Int): List<LatLng> {
        if (segments < 1) return listOf(from, to)
        val lat1 = Math.toRadians(from.latitude)
        val lon1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lon2 = Math.toRadians(to.longitude)
        val d = greatCircleAngularDistanceRad(lat1, lon1, lat2, lon2)
        if (d < 1e-7) return List(segments + 1) { from }
        val sinD = sin(d)
        val out = ArrayList<LatLng>(segments + 1)
        for (i in 0..segments) {
            val t = i / segments.toDouble()
            val a = sin((1 - t) * d) / sinD
            val b = sin(t * d) / sinD
            val x = a * cos(lat1) * cos(lon1) + b * cos(lat2) * cos(lon2)
            val y = a * cos(lat1) * sin(lon1) + b * cos(lat2) * sin(lon2)
            val z = a * sin(lat1) + b * sin(lat2)
            val lat = atan2(z, sqrt(x * x + y * y))
            val lon = atan2(y, x)
            out.add(LatLng(Math.toDegrees(lat), Math.toDegrees(lon)))
        }
        return out
    }

    private fun greatCircleAngularDistanceRad(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * atan2(sqrt(h), sqrt(1 - h))
    }

    internal fun haversineMeters(a: LatLng, b: LatLng): Double {
        val r = 6371008.8
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(h), sqrt(1 - h))
    }
}
