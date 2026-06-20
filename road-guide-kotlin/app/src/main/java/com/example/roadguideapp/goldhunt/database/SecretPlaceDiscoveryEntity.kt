package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "secret_place_discovery")
internal data class SecretPlaceDiscoveryEntity(
    @PrimaryKey val secretPlaceId: String,
    val type: String,
    val rarity: String,
    val lat: Double,
    val lng: Double,
    val regionL1Id: String,
    val regionL2Id: String,
    val displayName: String,
    val creditsGranted: Int,
    val treasureNestSlots: Int,
    val storyFragmentId: String?,
    val placementKind: String,
    val discoveredAt: Long,
    val generatorVersion: Int,
)
