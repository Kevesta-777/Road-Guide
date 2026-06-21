package com.example.roadguideapp.goldhunt.events.achievements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seasonal_event_achievement_completion")
internal data class SeasonalEventAchievementCompletionEntity(
    @PrimaryKey val eventId: String,
    val eventType: String,
    val cycleYear: Int,
    val achievementKey: String,
    val creditsGranted: Int,
    val xpGranted: Long,
    val category: String?,
    val completedAtMs: Long,
    val schemaVersion: Int = SeasonalEventAchievementSchema.VERSION,
    val extensionJson: String = SeasonalEventAchievementSchema.EMPTY_EXTENSIONS_JSON,
)
