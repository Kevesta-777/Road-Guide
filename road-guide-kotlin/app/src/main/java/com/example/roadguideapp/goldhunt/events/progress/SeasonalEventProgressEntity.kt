package com.example.roadguideapp.goldhunt.events.progress

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seasonal_event_progress")
internal data class SeasonalEventProgressEntity(
    @PrimaryKey val eventId: String,
    val eventType: String,
    val cycleYear: Int,
    val treasuresCollected: Int = 0,
    val xpEarned: Long = 0L,
    val creditsEarned: Int = 0,
    val fragmentsEarned: Int = 0,
    val completionPercent: Double = 0.0,
    val isCompleted: Boolean = false,
    val schemaVersion: Int = SeasonalEventProgressSchema.VERSION,
    val extensionJson: String = SeasonalEventProgressSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
