package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "region_progress")
internal data class RegionProgressEntity(
    @PrimaryKey val regionId: String,
    val level: Int,
    val discoveredRoads: Int,
    val exploredCells: Int,
    val completionPercent: Double,
    val rewardGranted: Boolean,
    val updatedAt: Long,
)
