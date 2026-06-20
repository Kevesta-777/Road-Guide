package com.example.roadguideapp.goldhunt.treasure

import org.maplibre.android.geometry.LatLng

internal data class TreasureSpec(
    val treasureId: String,
    val type: TreasureType,
    val lat: Double,
    val lng: Double,
    val creditAmount: Int,
    val placementKind: String,
    val regionL1Id: String,
) {
    val latLng: LatLng get() = LatLng(lat, lng)
}
