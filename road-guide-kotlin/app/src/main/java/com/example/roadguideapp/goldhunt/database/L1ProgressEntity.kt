package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tracks L0 discoveries per L1 bucket for cluster rewards. */
@Entity(tableName = "l1_progress")
internal data class L1ProgressEntity(
    @PrimaryKey val l1CellId: String,
    val exploredL0Count: Int,
    val clusterRewardGranted: Boolean,
)
