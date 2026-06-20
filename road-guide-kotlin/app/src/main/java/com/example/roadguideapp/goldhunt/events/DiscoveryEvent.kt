package com.example.roadguideapp.goldhunt.events

internal data class DiscoveryEvent(
    val cellId: Long,
    val lat: Double,
    val lng: Double,
    val regionId: String,
    val timestampMs: Long,
)
