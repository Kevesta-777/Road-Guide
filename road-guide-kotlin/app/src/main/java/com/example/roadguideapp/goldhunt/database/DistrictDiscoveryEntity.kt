package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "district_discovery")
internal data class DistrictDiscoveryEntity(
    @PrimaryKey val districtKey: String,
    val firstDiscoveredAt: Long,
    val displayName: String? = null,
)
