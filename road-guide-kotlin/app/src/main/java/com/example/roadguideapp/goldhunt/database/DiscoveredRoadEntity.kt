package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "discovered_road",
    indices = [
        Index("regionL1Id"),
        Index("streetGroupKey"),
    ],
)
internal data class DiscoveredRoadEntity(
    @PrimaryKey val roadKey: String,
    val source: String,
    val graphImportId: String? = null,
    val closestNode: Int? = null,
    val streetGroupKey: String? = null,
    val regionL1Id: String? = null,
    val regionL2Id: String? = null,
    val firstDiscoveredAt: Long,
    val lastVisitedAt: Long,
    val firstLat: Double,
    val firstLng: Double,
    val endLat: Double,
    val endLng: Double,
)
