package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collected_treasure")
internal data class CollectedTreasureEntity(
    @PrimaryKey val treasureId: String,
    val type: String,
    val lat: Double,
    val lng: Double,
    val creditAmount: Int,
    val placementKind: String,
    val regionL1Id: String?,
    val collectedAt: Long,
    val generatorVersion: Int,
)
