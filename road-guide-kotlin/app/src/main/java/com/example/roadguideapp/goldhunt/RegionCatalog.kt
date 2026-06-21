package com.example.roadguideapp.goldhunt

import com.example.roadguideapp.map.MapOverviewDefaults
import org.maplibre.android.geometry.LatLng

/** Bundled exploration regions (offline denominators for progress %). */
internal object RegionCatalog {

    val defaultRegion: DiscoveryRegion by lazy {
        val bounds = MapOverviewDefaults.FIT_BOUNDS
        DiscoveryRegion(
            id = "greater_london",
            displayName = "Greater London",
            bounds = bounds,
            totalCellCount = DiscoveryGrid.countCellsInBounds(bounds),
        )
    }

    fun regionForLatLng(lat: Double, lng: Double): DiscoveryRegion? {
        val region = defaultRegion
        return if (region.bounds.contains(LatLng(lat, lng))) region else null
    }
}
