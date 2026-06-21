package com.example.roadguideapp.goldhunt.panorama.rewards

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panorama_hunt_reward_hook")
internal data class PanoramaHuntRewardHookEntity(
    @PrimaryKey val eventId: String,
    val huntId: String,
    val hookType: String,
    val hookKey: String,
    val grantedAtMs: Long,
    val schemaVersion: Int = PanoramaHuntRewardSchema.VERSION,
)
