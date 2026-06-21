package com.example.roadguideapp.goldhunt.events.progress

import com.example.roadguideapp.goldhunt.events.SeasonalEventType
internal object SeasonalEventProgressMapper {
    fun toDomain(entity: SeasonalEventProgressEntity): SeasonalEventProgress? {
        val eventType = SeasonalEventType.fromId(entity.eventType) ?: return null
        return SeasonalEventProgress(
            eventId = entity.eventId,
            eventType = eventType,
            cycleYear = entity.cycleYear,
            treasuresCollected = entity.treasuresCollected.coerceAtLeast(0),
            xpEarned = entity.xpEarned.coerceAtLeast(0L),
            creditsEarned = entity.creditsEarned.coerceAtLeast(0),
            fragmentsEarned = entity.fragmentsEarned.coerceAtLeast(0),
            completionPercent = entity.completionPercent.coerceIn(0.0, 100.0),
            isCompleted = entity.isCompleted,
            schemaVersion = entity.schemaVersion,
            extensionJson = entity.extensionJson,
            updatedAtMs = entity.updatedAtMs,
        )
    }

    fun toEntity(progress: SeasonalEventProgress): SeasonalEventProgressEntity =
        SeasonalEventProgressEntity(
            eventId = progress.eventId,
            eventType = progress.eventType.id,
            cycleYear = progress.cycleYear,
            treasuresCollected = progress.treasuresCollected,
            xpEarned = progress.xpEarned,
            creditsEarned = progress.creditsEarned,
            fragmentsEarned = progress.fragmentsEarned,
            completionPercent = progress.completionPercent,
            isCompleted = progress.isCompleted,
            schemaVersion = progress.schemaVersion,
            extensionJson = progress.extensionJson,
            updatedAtMs = progress.updatedAtMs,
        )
}
