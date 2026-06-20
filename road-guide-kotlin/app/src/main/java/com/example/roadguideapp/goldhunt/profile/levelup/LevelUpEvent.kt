package com.example.roadguideapp.goldhunt.profile.levelup

/**
 * Domain record for a single explorer level increase (storage-only; no UI/rewards).
 */
internal data class LevelUpEvent(
    val id: String,
    val previousLevel: Int,
    val newLevel: Int,
    val timestampMs: Long,
    val lifetimeXpAtLevelUp: Long = 0L,
    val extensionJson: String = LevelUpEventSchema.EMPTY_EXTENSIONS_JSON,
    val schemaVersion: Int = LevelUpEventSchema.VERSION,
)
