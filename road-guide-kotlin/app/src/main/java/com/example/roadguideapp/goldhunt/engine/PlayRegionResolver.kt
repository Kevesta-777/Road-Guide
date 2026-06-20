package com.example.roadguideapp.goldhunt.engine

import android.content.Context
import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.map.MapOverviewDefaults
import com.example.roadguideapp.map.MapServerConfig
import com.example.roadguideapp.map.PmtilesMetadataReader
import com.example.roadguideapp.map.PmtilesOverviewSource
import org.maplibre.android.geometry.LatLngBounds

internal object PlayRegionResolver {
    fun resolve(context: Context): PlayRegion {
        val bounds = pmtilesBounds(context)
            ?: MapServerConfig.initialMapFitBounds()
            ?: MapOverviewDefaults.FIT_BOUNDS
        val west = bounds.longitudeWest
        val south = bounds.latitudeSouth
        val east = bounds.longitudeEast
        val north = bounds.latitudeNorth
        val nx = kotlin.math.ceil((east - west) / GoldHuntConfig.L0_CELL_DEG).toLong()
        val ny = kotlin.math.ceil((north - south) / GoldHuntConfig.L0_CELL_DEG).toLong()
        return PlayRegion(
            west = west,
            south = south,
            east = east,
            north = north,
            totalCellsL0 = (nx * ny).coerceAtLeast(1L),
        )
    }

    private fun pmtilesBounds(context: Context): LatLngBounds? {
        val file = PmtilesOverviewSource.cachedFile(context.applicationContext)
        if (!file.isFile) return null
        return PmtilesMetadataReader.read(file)?.fitBounds
    }
}
