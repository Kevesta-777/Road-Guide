package com.example.roadguideapp.goldhunt.panorama.completion

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panorama_hunt_completion")
internal data class PanoramaHuntCompletionEntity(
    @PrimaryKey val huntId: String,
    val huntType: String,
    val secretPlaceId: String,
    val panoramaId: String,
    val targetsFound: Int,
    val targetsTotal: Int,
    val creditsGranted: Int,
    val xpGranted: Long,
    val completedAtMs: Long,
    val achievementKey: String?,
    val storyFragmentId: String?,
    val legendaryRelicKey: String?,
    val rewardEventId: String,
    val schemaVersion: Int = PanoramaHuntCompletionSchema.VERSION,
    val extensionJson: String = PanoramaHuntCompletionSchema.EMPTY_EXTENSIONS_JSON,
)
