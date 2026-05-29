package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

internal object DirectionsRouteGeometry {

    fun boundsFor(points: List<LatLng>): LatLngBounds? {
        if (points.isEmpty()) return null
        val builder = LatLngBounds.Builder()
        points.forEach { builder.include(it) }
        return builder.build()
    }

    /**
     * Returns a prefix of [points] along the polyline up to [progress] in `[0, 1]` (by arc length).
     */
    fun slicePolylineByProgress(points: List<LatLng>, progress: Float): List<LatLng> {
        if (points.isEmpty()) return emptyList()
        if (points.size == 1 || progress <= 0f) return listOf(points.first(), points.first())
        if (progress >= 1f) return points

        val cumulative = DoubleArray(points.size)
        var total = 0.0
        cumulative[0] = 0.0
        for (i in 1 until points.size) {
            total += DirectionsPathOptimizer.haversineMeters(points[i - 1], points[i])
            cumulative[i] = total
        }
        if (total <= 0.0) return listOf(points.first(), points.first())

        val target = total * progress.toDouble()
        var segmentIndex = 1
        while (segmentIndex < cumulative.size && cumulative[segmentIndex] < target) {
            segmentIndex++
        }
        if (segmentIndex >= points.size) return points

        val segStart = cumulative[segmentIndex - 1]
        val segEnd = cumulative[segmentIndex]
        val segLength = (segEnd - segStart).coerceAtLeast(1e-9)
        val t = ((target - segStart) / segLength).toFloat().coerceIn(0f, 1f)
        val a = points[segmentIndex - 1]
        val b = points[segmentIndex]
        val end = interpolate(a, b, t)

        val out = ArrayList<LatLng>(segmentIndex + 1)
        for (i in 0 until segmentIndex) {
            out.add(points[i])
        }
        out.add(end)
        return out
    }

    private fun interpolate(a: LatLng, b: LatLng, t: Float): LatLng {
        val u = t.toDouble().coerceIn(0.0, 1.0)
        return LatLng(
            a.latitude + (b.latitude - a.latitude) * u,
            a.longitude + (b.longitude - a.longitude) * u,
        )
    }
}
