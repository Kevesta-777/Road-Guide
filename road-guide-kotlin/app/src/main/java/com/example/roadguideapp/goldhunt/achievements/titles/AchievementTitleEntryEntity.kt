package com.example.roadguideapp.goldhunt.achievements.titles

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_title_entry")
internal data class AchievementTitleEntryEntity(
    @PrimaryKey val titleKey: String,
    val achievementId: String,
    val displayTitle: String,
    val rarity: String,
    val unlockDateMs: Long? = null,
    val schemaVersion: Int = AchievementTitleSchema.VERSION,
    val extensionJson: String = AchievementTitleSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
