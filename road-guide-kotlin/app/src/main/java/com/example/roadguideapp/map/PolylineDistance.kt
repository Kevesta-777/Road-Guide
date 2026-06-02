package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import kotlin.math.cos
import kotlin.math.sin

/** Distance and bounds helpers for route-corridor nearby search. */
internal object PolylineDistance {

    fun distanceToPolylineMeters(point: LatLng, polyline: List<LatLng>): Double {
        if (polyline.isEmpty()) return Double.POSITIVE_INFINITY
        if (polyline.size == 1) {
            return DirectionsPathOptimizer.haversineMeters(point, polyline.first())
        }
        var min = Double.POSITIVE_INFINITY
        for (index in 0 until polyline.lastIndex) {
            min = minOf(
                min,
                distanceToSegmentMeters(point, polyline[index], polyline[index + 1]),
            )
        }
        return min
    }

    /**
     * Sample points along [polyline] spaced by roughly [spacingMeters] (great-circle).
     */
    fun sampleAlongPolyline(
        polyline: List<LatLng>,
        spacingMeters: Double = 1_500.0,
    ): List<LatLng> {
        if (polyline.isEmpty()) return emptyList()
        if (polyline.size == 1) return listOf(polyline.first())

        val samples = ArrayList<LatLng>()
        samples.add(polyline.first())
        var cursor = polyline.first()
        var segmentIndex = 0
        var traversedOnSegment = 0.0

        while (segmentIndex < polyline.lastIndex) {
            val next = polyline[segmentIndex + 1]
            val segmentLength = DirectionsPathOptimizer.haversineMeters(cursor, next)
            if (segmentLength <= 1e-3) {
                segmentIndex++
                cursor = next
                traversedOnSegment = 0.0
                continue
            }
            val remainingOnSegment = segmentLength - traversedOnSegment
            if (remainingOnSegment >= spacingMeters) {
                val t = ((traversedOnSegment + spacingMeters) / segmentLength).toFloat().coerceIn(0f, 1f)
                val sample = interpolate(cursor, next, t)
                samples.add(sample)
                cursor = sample
                traversedOnSegment += spacingMeters
                continue
            }
            segmentIndex++
            cursor = next
            traversedOnSegment = 0.0
        }
        val last = polyline.last()
        if (samples.last() != last) samples.add(last)
        return samples
    }

    fun boundsWithBuffer(polyline: List<LatLng>, bufferMeters: Double): LatLngBounds? {
        if (polyline.isEmpty()) return null
        var south = polyline.first().latitude
        var north = south
        var west = polyline.first().longitude
        var east = west
        for (point in polyline) {
            south = minOf(south, point.latitude)
            north = maxOf(north, point.latitude)
            west = minOf(west, point.longitude)
            east = maxOf(east, point.longitude)
        }
        val midLat = (south + north) / 2.0
        val latPad = metersToLatitudeDegrees(bufferMeters)
        val lonPad = metersToLongitudeDegrees(bufferMeters, midLat)
        return LatLngBounds.from(
            (south - latPad).coerceIn(-85.0, 85.0),
            (west - lonPad).coerceIn(-180.0, 180.0),
            (north + latPad).coerceIn(-85.0, 85.0),
            (east + lonPad).coerceIn(-180.0, 180.0),
        )
    }

    private fun distanceToSegmentMeters(point: LatLng, a: LatLng, b: LatLng): Double {
        val segmentLength = DirectionsPathOptimizer.haversineMeters(a, b)
        if (segmentLength <= 1e-3) return DirectionsPathOptimizer.haversineMeters(point, a)
        val t = projectionFactor(point, a, b).coerceIn(0.0, 1.0)
        val projected = interpolate(a, b, t.toFloat())
        return DirectionsPathOptimizer.haversineMeters(point, projected)
    }

    private fun projectionFactor(point: LatLng, a: LatLng, b: LatLng): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lon1 = Math.toRadians(a.longitude)
        val lat2 = Math.toRadians(b.latitude)
        val lon2 = Math.toRadians(b.longitude)
        val latP = Math.toRadians(point.latitude)
        val lonP = Math.toRadians(point.longitude)
        val dx = lat2 - lat1
        val dy = lon2 - lon1
        val denom = dx * dx + dy * dy
        if (denom <= 1e-12) return 0.0
        return ((latP - lat1) * dx + (lonP - lon1) * dy) / denom
    }

    private fun interpolate(a: LatLng, b: LatLng, t: Float): LatLng {
        val u = t.toDouble().coerceIn(0.0, 1.0)
        return LatLng(
            a.latitude + (b.latitude - a.latitude) * u,
            a.longitude + (b.longitude - a.longitude) * u,
        )
    }

    private fun metersToLatitudeDegrees(meters: Double): Double = meters / 111_320.0

    private fun metersToLongitudeDegrees(meters: Double, latitude: Double): Double {
        val scale = cos(Math.toRadians(latitude)).coerceAtLeast(0.01)
        return meters / (111_320.0 * scale)
    }
}
