package com.example.roadguideapp.goldhunt.panorama.statistics

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panorama_hunt_session")
internal data class PanoramaHuntSessionEntity(
    @PrimaryKey val huntId: String,
    val huntType: String,
    val secretPlaceId: String,
    val panoramaId: String,
    val startedAtMs: Long,
    val schemaVersion: Int = PanoramaHuntStatisticsSchema.VERSION,
)
