package com.example.roadguideapp.goldhunt.radar

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "radar_type_cooldown_state")
internal data class RadarTypeCooldownStateEntity(
    @PrimaryKey val radarTypeId: String,
    val lastScanTimeMs: Long = RadarSchema.NEVER_SCANNED_MS,
    val scanCount: Int = 0,
    val successfulScanCount: Int = 0,
    val schemaVersion: Int = RadarSchema.COOLDOWN_SCHEMA_VERSION,
    val updatedAtMs: Long = 0L,
)
