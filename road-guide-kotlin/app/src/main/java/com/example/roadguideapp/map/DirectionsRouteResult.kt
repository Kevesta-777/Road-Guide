package com.example.roadguideapp.map

import org.maplibre.android.geometry.LatLng

internal data class DirectionsRouteLeg(
    val durationSeconds: Double,
    val midPoint: LatLng,
)

internal data class DirectionsRouteResult(
    val geometry: List<LatLng>,
    val legs: List<DirectionsRouteLeg>,
    val totalDurationSeconds: Double,
    val totalLengthKm: Double,
)
