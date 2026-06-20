package com.example.roadguideapp.goldhunt.secretplace

import org.maplibre.android.geometry.LatLng

internal data class SecretPlaceSpec(
    val secretPlaceId: String,
    val type: SecretPlaceType,
    val rarity: SecretPlaceRarity,
    val lat: Double,
    val lng: Double,
    val regionL1Id: String,
    val regionL2Id: String,
    val displayName: String,
    val treasureNestSlots: Int,
    val storyFragmentId: String?,
    val placementKind: String,
    val generatorVersion: Int,
) {
    val latLng: LatLng get() = LatLng(lat, lng)
}
