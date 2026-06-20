package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discovered_street")
internal data class DiscoveredStreetEntity(
    @PrimaryKey val streetGroupKey: String,
    val firstDiscoveredAt: Long,
    val regionL1Id: String? = null,
)
