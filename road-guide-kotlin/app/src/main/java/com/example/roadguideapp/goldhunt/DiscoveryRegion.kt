package com.example.roadguideapp.goldhunt

import org.maplibre.android.geometry.LatLngBounds

internal data class DiscoveryRegion(
    val id: String,
    val displayName: String,
    val bounds: LatLngBounds,
    val totalCellCount: Int,
)
