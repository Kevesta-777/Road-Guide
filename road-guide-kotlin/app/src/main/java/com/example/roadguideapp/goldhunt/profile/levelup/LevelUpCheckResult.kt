package com.example.roadguideapp.goldhunt.profile.levelup

/**
 * Outcome of comparing [previousLevel] to [newLevel] after an XP award.
 */
internal data class LevelUpCheckResult(
    val previousLevel: Int,
    val newLevel: Int,
    val leveledUp: Boolean,
    /** Newly persisted events from this check (empty when unchanged or duplicate). */
    val events: List<LevelUpEvent>,
)
