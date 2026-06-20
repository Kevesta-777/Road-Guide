package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "secret_place_region_progress")
internal data class SecretPlaceRegionProgressEntity(
    @PrimaryKey val regionL1Id: String,
    val secretsDiscovered: Int,
    val rewardGranted: Boolean,
    val updatedAt: Long,
)
