package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discovery_milestone")
internal data class DiscoveryMilestoneEntity(
    @PrimaryKey val milestoneId: String,
    val unlockedAt: Long,
    val creditAmount: Int,
)
