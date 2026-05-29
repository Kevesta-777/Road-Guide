package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

/** Fallback viewport for the bundled Greater London overview extract. */
internal object MapOverviewDefaults {

    /** `[west, south, east, north]` — Greater London approximate extent. */
    val BOUNDS_WEST_SOUTH_EAST_NORTH: DoubleArray = doubleArrayOf(
        -0.55,
        51.28,
        0.35,
        51.69,
    )

    val FIT_BOUNDS: LatLngBounds = LatLngBounds.Builder()
        .include(LatLng(BOUNDS_WEST_SOUTH_EAST_NORTH[1], BOUNDS_WEST_SOUTH_EAST_NORTH[0]))
        .include(LatLng(BOUNDS_WEST_SOUTH_EAST_NORTH[3], BOUNDS_WEST_SOUTH_EAST_NORTH[2]))
        .build()

    val CENTER: LatLng = LatLng(51.5074, -0.1278)

    const val DEFAULT_ZOOM = 9.0
}
