package com.example.roadguideapp.goldhunt.radar

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row radar profile persisted in `goldhunt.db`.
 * [extensionJson] reserves achievement flags, seasonal hooks, and future upgrades.
 */
@Entity(tableName = "radar_profile")
internal data class RadarProfileEntity(
    @PrimaryKey val id: Int = RadarSchema.SINGLETON_ID,
    val currentRadarTypeId: String = RadarSchema.DEFAULT_RADAR_TYPE_ID,
    val unlockLevel: Int = RadarSchema.DEFAULT_UNLOCK_LEVEL,
    val lastScanTimeMs: Long = RadarSchema.NEVER_SCANNED_MS,
    val totalScans: Int = 0,
    val successfulScans: Int = 0,
    val schemaVersion: Int = RadarSchema.VERSION,
    val extensionJson: String = RadarSchema.EMPTY_EXTENSIONS_JSON,
    val createdAtMs: Long = 0L,
    val updatedAtMs: Long = 0L,
)
