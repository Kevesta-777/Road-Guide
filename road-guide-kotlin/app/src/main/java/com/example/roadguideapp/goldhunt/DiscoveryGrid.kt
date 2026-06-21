package com.example.roadguideapp.goldhunt

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import kotlin.math.floor

/**
 * Fixed lat/lng grid for offline discovery cells (H3-like usage without extra native deps).
 */
internal object DiscoveryGrid {

    fun cellId(lat: Double, lng: Double, stepMultiplier: Int = 1): Long {
        val step = DiscoveryConfig.CELL_SIZE_DEG * stepMultiplier.coerceAtLeast(1)
        val x = floor(lng / step).toLong()
        val y = floor(lat / step).toLong()
        return pack(x, y)
    }

    fun unpack(cellId: Long): Pair<Long, Long> {
        val xRaw = cellId shr 32
        val yRaw = cellId and 0xFFFF_FFFFL
        return decodeSignedGridIndex(xRaw) to decodeSignedGridIndex(yRaw)
    }

    fun disk(lat: Double, lng: Double, ring: Int, stepMultiplier: Int = 1): List<Long> {
        val center = cellId(lat, lng, stepMultiplier)
        if (ring <= 0) return listOf(center)
        val (cx, cy) = unpack(center)
        val out = ArrayList<Long>((2 * ring + 1) * (2 * ring + 1))
        for (dx in -ring..ring) {
            for (dy in -ring..ring) {
                out.add(pack(cx + dx, cy + dy))
            }
        }
        return out
    }

    fun cellBounds(cellId: Long, stepMultiplier: Int = 1): LatLngBounds? {
        val step = DiscoveryConfig.CELL_SIZE_DEG * stepMultiplier.coerceAtLeast(1)
        val (x, y) = unpack(cellId)
        val west = x * step
        val south = y * step
        val east = west + step
        val north = south + step
        if (south >= north || west >= east) return null
        if (north < -85.0 || south > 85.0 || east < -180.0 || west > 180.0) return null
        return runCatching {
            LatLngBounds.from(
                south.coerceIn(-85.0, 85.0),
                west.coerceIn(-180.0, 180.0),
                north.coerceIn(-85.0, 85.0),
                east.coerceIn(-180.0, 180.0),
            )
        }.getOrNull()
    }

    fun cellsInBounds(bounds: LatLngBounds, stepMultiplier: Int = 1): Sequence<Long> = sequence {
        val step = DiscoveryConfig.CELL_SIZE_DEG * stepMultiplier.coerceAtLeast(1)
        val south = bounds.latitudeSouth.coerceAtMost(bounds.latitudeNorth)
        val north = bounds.latitudeNorth.coerceAtLeast(bounds.latitudeSouth)
        val west = bounds.longitudeWest.coerceAtMost(bounds.longitudeEast)
        val east = bounds.longitudeEast.coerceAtLeast(bounds.longitudeWest)
        val minX = floor(west / step).toLong()
        val maxX = floor(east / step).toLong()
        val minY = floor(south / step).toLong()
        val maxY = floor(north / step).toLong()
        if (minX > maxX || minY > maxY) return@sequence
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                yield(pack(x, y))
            }
        }
    }

    /** Intersects viewport with the active exploration region before iterating cells. */
    fun cellsInViewport(
        viewport: LatLngBounds,
        region: LatLngBounds,
        stepMultiplier: Int = 1,
    ): Sequence<Long> {
        val intersected = intersectBounds(viewport, region) ?: return emptySequence()
        return cellsInBounds(intersected, stepMultiplier)
    }

    fun intersectBounds(a: LatLngBounds, b: LatLngBounds): LatLngBounds? {
        val south = maxOf(a.latitudeSouth, b.latitudeSouth)
        val north = minOf(a.latitudeNorth, b.latitudeNorth)
        val west = maxOf(a.longitudeWest, b.longitudeWest)
        val east = minOf(a.longitudeEast, b.longitudeEast)
        if (south >= north || west >= east) return null
        return LatLngBounds.from(south, west, north, east)
    }

    fun countCellsInBounds(bounds: LatLngBounds, stepMultiplier: Int = 1): Int {
        val step = DiscoveryConfig.CELL_SIZE_DEG * stepMultiplier.coerceAtLeast(1)
        val minX = floor(bounds.longitudeWest / step).toLong()
        val maxX = floor(bounds.longitudeEast / step).toLong()
        val minY = floor(bounds.latitudeSouth / step).toLong()
        val maxY = floor(bounds.latitudeNorth / step).toLong()
        val dx = (maxX - minX + 1).coerceAtLeast(0)
        val dy = (maxY - minY + 1).coerceAtLeast(0)
        val product = dx.toDouble() * dy.toDouble()
        return when {
            product > Int.MAX_VALUE -> Int.MAX_VALUE
            product < 1.0 -> 0
            else -> product.toInt()
        }
    }

    fun renderStepMultiplier(mapZoom: Double): Int = when {
        mapZoom < 10.0 -> 8
        mapZoom < 12.0 -> 4
        mapZoom < 14.0 -> 2
        else -> 1
    }

    fun haversineMeters(aLat: Double, aLng: Double, bLat: Double, bLng: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(bLat - aLat)
        val dLng = Math.toRadians(bLng - aLng)
        val lat1 = Math.toRadians(aLat)
        val lat2 = Math.toRadians(bLat)
        val h = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
            kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
        return 2 * r * kotlin.math.asin(kotlin.math.sqrt(h.coerceIn(0.0, 1.0)))
    }

    private fun pack(x: Long, y: Long): Long {
        val ux = encodeSignedGridIndex(x)
        val uy = encodeSignedGridIndex(y)
        return (ux shl 32) or uy
    }

    private fun encodeSignedGridIndex(index: Long): Long {
        require(index in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            "Grid index out of range: $index"
        }
        return index.toInt().toLong() and 0xFFFF_FFFFL
    }

    private fun decodeSignedGridIndex(raw: Long): Long {
        val unsigned = raw and 0xFFFF_FFFFL
        return if (unsigned >= 0x8000_0000L) {
            unsigned - 0x1_0000_0000L
        } else {
            unsigned
        }
    }
}
