package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "explored_cell",
    indices = [Index("level")],
)
internal data class ExploredCellEntity(
    @PrimaryKey val cellId: String,
    val level: Int,
    val discoveredAt: Long,
    val lat: Double,
    val lng: Double,
)
