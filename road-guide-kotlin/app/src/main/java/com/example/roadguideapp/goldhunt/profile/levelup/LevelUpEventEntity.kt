package com.example.roadguideapp.goldhunt.profile.levelup

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Append-only explorer level-up history in `goldhunt.db`.
 * [extensionJson] reserved for achievements, unlocks, radar, and rewards.
 */
@Entity(
    tableName = LevelUpEventSchema.TABLE_NAME,
    indices = [
        Index(value = ["timestampMs"]),
        Index(value = ["newLevel"]),
    ],
)
internal data class LevelUpEventEntity(
    @PrimaryKey val id: String,
    val previousLevel: Int,
    val newLevel: Int,
    val timestampMs: Long,
    val lifetimeXpAtLevelUp: Long = 0L,
    val extensionJson: String = LevelUpEventSchema.EMPTY_EXTENSIONS_JSON,
    val schemaVersion: Int = LevelUpEventSchema.VERSION,
)
