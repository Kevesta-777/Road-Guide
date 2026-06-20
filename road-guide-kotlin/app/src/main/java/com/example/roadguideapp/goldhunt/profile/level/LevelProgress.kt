package com.example.roadguideapp.goldhunt.profile.level

/**
 * Progress within the current explorer level band toward the next level.
 * Pure calculation output; not persisted.
 */
internal data class LevelProgress(
    val currentLevel: Int,
    val totalXp: Long,
    /** XP earned within [currentLevel] (totalXp minus this level's entry threshold). */
    val xpIntoLevel: Long,
    /** XP still required to reach [currentLevel + 1]. */
    val xpToNextLevel: Long,
    /** [xpIntoLevel] / span of current level band, in [0.0, 1.0]. */
    val progressFraction: Double,
)
