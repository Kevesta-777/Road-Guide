package com.example.roadguideapp.goldhunt.panorama.completion

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panorama_hunt_target_finding")
internal data class PanoramaHuntTargetFindingEntity(
    @PrimaryKey val targetId: String,
    val huntId: String,
    val slotIndex: Int,
    val foundAtMs: Long,
    val schemaVersion: Int = PanoramaHuntCompletionSchema.VERSION,
    val extensionJson: String = PanoramaHuntCompletionSchema.EMPTY_EXTENSIONS_JSON,
)
