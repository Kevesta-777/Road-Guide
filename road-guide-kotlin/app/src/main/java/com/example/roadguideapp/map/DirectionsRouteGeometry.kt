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
     * Inserts points so long straight segments follow road curvature more closely on the map.
     */
    fun densifyForRoadDisplay(
        points: List<LatLng>,
        maxSegmentLengthM: Double = DirectionsNavConfig.ROUTE_DENSIFY_SEGMENT_M,
    ): List<LatLng> {
        if (points.size < 2) return points
        val out = ArrayList<LatLng>(points.size * 3)
        out.add(points.first())
        for (i in 1 until points.size) {
            val a = points[i - 1]
            val b = points[i]
            val dist = DirectionsPathOptimizer.haversineMeters(a, b)
            if (dist < 0.05) continue
            val steps = (dist / maxSegmentLengthM).toInt().coerceAtLeast(1)
            val bearing = bearingDegrees(a.latitude, a.longitude, b.latitude, b.longitude)
            for (step in 1..steps) {
                val alongM = dist * step / steps
                val (lat, lng) = destinationPointNav(a.latitude, a.longitude, bearing, alongM)
                out.add(LatLng(lat, lng))
            }
        }
        if (out.size < 2) {
            out.add(points.last())
        }
        return out
    }

    /** Router geometry cleaned and densified for map display / navigation simulation. */
    fun prepareForMapDisplay(points: List<LatLng>): List<LatLng> {
        if (points.size < 2) return points
        return densifyForRoadDisplay(deduplicateVertices(points))
    }

    private fun deduplicateVertices(points: List<LatLng>): List<LatLng> {
        if (points.size < 2) return points
        val minSepM = DirectionsNavConfig.ROUTE_DEDUPE_MIN_M
        val cornerDeg = DirectionsNavConfig.ROUTE_CORNER_MIN_BEARING_DEG
        val out = ArrayList<LatLng>(points.size)
        out.add(points.first())
        for (i in 1 until points.size) {
            val pt = points[i]
            val last = out.last()
            val dist = DirectionsPathOptimizer.haversineMeters(last, pt)
            if (dist < minSepM && i < points.lastIndex) {
                val next = points[i + 1]
                val bearingIn = bearingDegrees(last.latitude, last.longitude, pt.latitude, pt.longitude)
                val bearingOut = bearingDegrees(pt.latitude, pt.longitude, next.latitude, next.longitude)
                if (kotlin.math.abs(deltaAngleDegrees(bearingIn, bearingOut)) >= cornerDeg) {
                    out.add(pt)
                }
                continue
            }
            if (dist >= minSepM || i == points.lastIndex) {
                out.add(pt)
            }
        }
        if (out.size < 2) return listOf(points.first(), points.last())
        return out
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

    /** Returns the suffix of [points] from [progress] to the end (route still ahead of the vehicle). */
    fun slicePolylineRemaining(points: List<LatLng>, progress: Float): List<LatLng> {
        if (points.isEmpty()) return emptyList()
        if (progress <= 0f) return points
        if (progress >= 1f) {
            val end = points.last()
            return listOf(end, end)
        }

        val traveled = slicePolylineByProgress(points, progress)
        val split = traveled.last()
        var startIdx = 0
        var bestDist = Double.MAX_VALUE
        for (i in points.indices) {
            val d = DirectionsPathOptimizer.haversineMeters(points[i], split)
            if (d < bestDist) {
                bestDist = d
                startIdx = i
            }
        }

        val out = ArrayList<LatLng>(points.size - startIdx + 1)
        out.add(split)
        for (i in startIdx + 1 until points.size) {
            out.add(points[i])
        }
        if (out.size < 2) {
            out.add(points.last())
        }
        return out
    }

    data class PolylineProjection(
        val point: LatLng,
        val segmentIndex: Int,
        val segmentBearing: Double,
    )

    /** Closest point on [points] to [position] (orthogonal projection onto segments). */
    fun projectOntoPolyline(points: List<LatLng>, position: LatLng): PolylineProjection {
        if (points.isEmpty()) {
            return PolylineProjection(position, 0, 0.0)
        }
        if (points.size == 1) {
            return PolylineProjection(points.first(), 0, 0.0)
        }

        var bestDist = Double.MAX_VALUE
        var bestPoint = points.first()
        var bestIdx = 0
        var bestBearing = bearingDegrees(
            points[0].latitude,
            points[0].longitude,
            points[1].latitude,
            points[1].longitude,
        )

        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            val (proj, _) = projectOntoSegment(a, b, position)
            val d = DirectionsPathOptimizer.haversineMeters(position, proj)
            if (d < bestDist) {
                bestDist = d
                bestPoint = proj
                bestIdx = i
                bestBearing = bearingDegrees(
                    a.latitude,
                    a.longitude,
                    b.latitude,
                    b.longitude,
                )
            }
        }
        return PolylineProjection(bestPoint, bestIdx, bestBearing)
    }

    /** Route line from [position] on [points] through to the destination. */
    fun slicePolylineFromPosition(
        points: List<LatLng>,
        position: LatLng,
        trimAheadM: Double = 0.0,
    ): List<LatLng> {
        if (points.isEmpty()) return emptyList()
        if (points.size == 1) return listOf(points.first(), points.first())

        val projection = projectOntoPolyline(points, position)
        val startDistance = arcLengthAtProjection(points, projection) + trimAheadM.coerceAtLeast(0.0)
        return slicePolylineFromArcDistance(points, startDistance)
    }

    /**
     * Remaining route for navigation: same arc length as the marker, with the first vertex exactly at [anchor].
     */
    fun sliceRemainingRouteAtMarker(
        points: List<LatLng>,
        anchor: LatLng,
        distanceAlongRouteM: Double,
        trimAheadM: Double = 0.0,
    ): List<LatLng> {
        if (points.isEmpty()) return emptyList()
        val startDistance = (distanceAlongRouteM + trimAheadM.coerceAtLeast(0.0))
            .coerceIn(0.0, totalArcLengthM(points))
        val sliced = slicePolylineFromArcDistance(points, startDistance)
        if (sliced.isEmpty()) return emptyList()
        val out = ArrayList(sliced)
        out[0] = anchor
        if (out.size < 2) {
            out.add(points.last())
        }
        return out
    }

    /** Stable heading from [distanceM] toward a point [lookAheadM] ahead on the polyline. */
    fun bearingAlongRouteM(
        points: List<LatLng>,
        distanceM: Double,
        lookAheadM: Double,
    ): Double {
        if (points.size < 2) return 0.0
        val total = totalArcLengthM(points)
        val from = sampleAtDistanceM(points, distanceM.coerceIn(0.0, total))
        val aheadDist = (distanceM + lookAheadM.coerceAtLeast(1.0)).coerceAtMost(total)
        val to = sampleAtDistanceM(points, aheadDist)
        return bearingDegrees(from.lat, from.lng, to.lat, to.lng)
    }

    fun totalArcLengthM(points: List<LatLng>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 1 until points.size) {
            total += DirectionsPathOptimizer.haversineMeters(points[i - 1], points[i])
        }
        return total
    }

    /** Point and segment bearing at [distanceM] along the polyline. */
    fun sampleAtDistanceM(points: List<LatLng>, distanceM: Double): DirectionsNavFrame {
        if (points.isEmpty()) {
            return DirectionsNavFrame(0.0, 0.0, 0.0, 0.0)
        }
        if (points.size == 1) {
            val p = points.first()
            return DirectionsNavFrame(p.latitude, p.longitude, 0.0, distanceM)
        }
        val total = totalArcLengthM(points)
        if (distanceM <= 0.0) {
            val bearing = bearingDegrees(
                points[0].latitude,
                points[0].longitude,
                points[1].latitude,
                points[1].longitude,
            )
            return DirectionsNavFrame(
                points[0].latitude,
                points[0].longitude,
                bearing,
                0.0,
            )
        }
        if (distanceM >= total) {
            val last = points.last()
            val prev = points[points.size - 2]
            val bearing = bearingDegrees(
                prev.latitude,
                prev.longitude,
                last.latitude,
                last.longitude,
            )
            return DirectionsNavFrame(last.latitude, last.longitude, bearing, distanceM)
        }

        var traversed = 0.0
        for (i in 1 until points.size) {
            val a = points[i - 1]
            val b = points[i]
            val segLen = DirectionsPathOptimizer.haversineMeters(a, b)
            if (traversed + segLen >= distanceM) {
                val alongM = (distanceM - traversed).coerceIn(0.0, segLen)
                val segBearing = bearingDegrees(a.latitude, a.longitude, b.latitude, b.longitude)
                val (lat, lng) = destinationPointNav(a.latitude, a.longitude, segBearing, alongM)
                return DirectionsNavFrame(lat, lng, segBearing, distanceM)
            }
            traversed += segLen
        }
        val last = points.last()
        return DirectionsNavFrame(last.latitude, last.longitude, 0.0, distanceM)
    }

    fun slicePolylineFromArcDistance(points: List<LatLng>, distanceFromStartM: Double): List<LatLng> {
        if (points.isEmpty()) return emptyList()
        if (points.size == 1) return listOf(points.first(), points.first())
        val total = totalArcLengthM(points)
        val start = distanceFromStartM.coerceIn(0.0, total)
        if (start >= total - 0.01) {
            val end = points.last()
            return listOf(end, end)
        }

        val startPoint = sampleAtDistanceM(points, start)
        val startLatLng = LatLng(startPoint.lat, startPoint.lng)
        var vertexIndex = 0
        var traversed = 0.0
        for (i in 1 until points.size) {
            val segLen = DirectionsPathOptimizer.haversineMeters(points[i - 1], points[i])
            if (traversed + segLen >= start) {
                vertexIndex = i
                break
            }
            traversed += segLen
            vertexIndex = i
        }

        val out = ArrayList<LatLng>(points.size - vertexIndex + 1)
        out.add(startLatLng)
        for (i in vertexIndex until points.size) {
            out.add(points[i])
        }
        if (out.size < 2) {
            out.add(points.last())
        }
        return out
    }

    private fun arcLengthAtProjection(points: List<LatLng>, projection: PolylineProjection): Double {
        if (points.size < 2) return 0.0
        var length = 0.0
        for (i in 0 until projection.segmentIndex) {
            length += DirectionsPathOptimizer.haversineMeters(points[i], points[i + 1])
        }
        length += DirectionsPathOptimizer.haversineMeters(
            points[projection.segmentIndex],
            projection.point,
        )
        return length
    }

    private fun projectOntoSegment(a: LatLng, b: LatLng, p: LatLng): Pair<LatLng, Double> {
        val ax = a.longitude
        val ay = a.latitude
        val bx = b.longitude
        val by = b.latitude
        val px = p.longitude
        val py = p.latitude
        val dx = bx - ax
        val dy = by - ay
        val len2 = dx * dx + dy * dy
        if (len2 < 1e-18) return a to 0.0
        val t = ((px - ax) * dx + (py - ay) * dy) / len2
        val tc = t.coerceIn(0.0, 1.0)
        return LatLng(ay + dy * tc, ax + dx * tc) to tc
    }

    private fun interpolate(a: LatLng, b: LatLng, t: Float): LatLng {
        val u = t.toDouble().coerceIn(0.0, 1.0)
        return LatLng(
            a.latitude + (b.latitude - a.latitude) * u,
            a.longitude + (b.longitude - a.longitude) * u,
        )
    }
}
