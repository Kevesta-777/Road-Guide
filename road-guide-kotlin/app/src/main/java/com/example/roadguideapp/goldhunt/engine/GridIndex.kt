package com.example.roadguideapp.goldhunt.engine

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

/**
 * Fixed-degree hierarchical grid anchored to play-region southwest corner.
 */
internal data class GridCell(
    val id: String,
    val level: Int,
    val ix: Int,
    val iy: Int,
    val center: LatLng,
)

internal object GridIndex {
    fun cellSizeDeg(level: Int): Double = when (level) {
        0 -> GoldHuntConfig.L0_CELL_DEG
        1 -> GoldHuntConfig.L1_CELL_DEG
        2 -> GoldHuntConfig.L2_CELL_DEG
        else -> GoldHuntConfig.L0_CELL_DEG
    }

    fun encode(lat: Double, lng: Double, level: Int, region: PlayRegion): GridCell? {
        if (!region.contains(lat, lng)) return null
        val cellDeg = cellSizeDeg(level)
        val ix = ((lng - region.west) / cellDeg).toInt().coerceAtLeast(0)
        val iy = ((lat - region.south) / cellDeg).toInt().coerceAtLeast(0)
        val maxIx = region.cellCountX(level) - 1
        val maxIy = region.cellCountY(level) - 1
        if (ix > maxIx || iy > maxIy) return null
        val centerLng = region.west + (ix + 0.5) * cellDeg
        val centerLat = region.south + (iy + 0.5) * cellDeg
        return GridCell(
            id = cellId(level, ix, iy),
            level = level,
            ix = ix,
            iy = iy,
            center = LatLng(centerLat, centerLng),
        )
    }

    fun parentL1(cellL0: GridCell, region: PlayRegion): GridCell? {
        val ix = cellL0.ix / 10
        val iy = cellL0.iy / 10
        return encode(
            lat = region.south + (iy + 0.5) * GoldHuntConfig.L1_CELL_DEG,
            lng = region.west + (ix + 0.5) * GoldHuntConfig.L1_CELL_DEG,
            level = 1,
            region = region,
        )
    }

    fun parentL2(cellL0: GridCell, region: PlayRegion): GridCell? {
        val ix = cellL0.ix / 100
        val iy = cellL0.iy / 100
        return encode(
            lat = region.south + (iy + 0.5) * GoldHuntConfig.L2_CELL_DEG,
            lng = region.west + (ix + 0.5) * GoldHuntConfig.L2_CELL_DEG,
            level = 2,
            region = region,
        )
    }

    fun cellId(level: Int, ix: Int, iy: Int): String = "L$level:$ix:$iy"

    fun polygonCorners(cell: GridCell, region: PlayRegion): List<LatLng> {
        val cellDeg = cellSizeDeg(cell.level)
        val west = region.west + cell.ix * cellDeg
        val south = region.south + cell.iy * cellDeg
        val east = west + cellDeg
        val north = south + cellDeg
        return listOf(
            LatLng(south, west),
            LatLng(south, east),
            LatLng(north, east),
            LatLng(north, west),
            LatLng(south, west),
        )
    }

    fun cellsInBounds(
        bounds: LatLngBounds,
        level: Int,
        region: PlayRegion,
        maxCells: Int,
    ): List<GridCell> {
        val cellDeg = cellSizeDeg(level)
        val ixMin = ((bounds.longitudeWest - region.west) / cellDeg).toInt().coerceAtLeast(0)
        val ixMax = ((bounds.longitudeEast - region.west) / cellDeg).toInt()
        val iyMin = ((bounds.latitudeSouth - region.south) / cellDeg).toInt().coerceAtLeast(0)
        val iyMax = ((bounds.latitudeNorth - region.south) / cellDeg).toInt()
        val cappedIxMax = ixMax.coerceAtMost(region.cellCountX(level) - 1)
        val cappedIyMax = iyMax.coerceAtMost(region.cellCountY(level) - 1)
        if (ixMin > cappedIxMax || iyMin > cappedIyMax) return emptyList()
        val out = ArrayList<GridCell>(maxCells.coerceAtMost(512))
        for (ix in ixMin..cappedIxMax) {
            for (iy in iyMin..cappedIyMax) {
                if (out.size >= maxCells) return out
                val centerLng = region.west + (ix + 0.5) * cellDeg
                val centerLat = region.south + (iy + 0.5) * cellDeg
                out += GridCell(
                    id = cellId(level, ix, iy),
                    level = level,
                    ix = ix,
                    iy = iy,
                    center = LatLng(centerLat, centerLng),
                )
            }
        }
        return out
    }
}

internal data class PlayRegion(
    val west: Double,
    val south: Double,
    val east: Double,
    val north: Double,
    val totalCellsL0: Long,
) {
    fun contains(lat: Double, lng: Double): Boolean =
        lat in south..north && lng in west..east

    fun cellCountX(level: Int): Int {
        val cellDeg = GridIndex.cellSizeDeg(level)
        return kotlin.math.ceil((east - west) / cellDeg).toInt().coerceAtLeast(1)
    }

    fun cellCountY(level: Int): Int {
        val cellDeg = GridIndex.cellSizeDeg(level)
        return kotlin.math.ceil((north - south) / cellDeg).toInt().coerceAtLeast(1)
    }

    fun toBounds(): LatLngBounds = LatLngBounds.Builder()
        .include(LatLng(south, west))
        .include(LatLng(north, east))
        .build()
}
