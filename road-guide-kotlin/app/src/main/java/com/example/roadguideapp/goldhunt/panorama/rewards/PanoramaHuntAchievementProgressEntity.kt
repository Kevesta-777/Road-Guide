package com.example.roadguideapp.goldhunt.panorama.rewards

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panorama_hunt_achievement_progress")
internal data class PanoramaHuntAchievementProgressEntity(
    @PrimaryKey val achievementKey: String,
    val completionCount: Int,
    val updatedAtMs: Long,
    val schemaVersion: Int = PanoramaHuntRewardSchema.VERSION,
)
