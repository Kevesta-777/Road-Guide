package com.example.roadguideapp.goldhunt.events.progress

import com.example.roadguideapp.goldhunt.events.SeasonalEventType

internal data class SeasonalEventProgress(
    val eventId: String,
    val eventType: SeasonalEventType,
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
) {
    val displayName: String
        get() = eventType.displayName
}
