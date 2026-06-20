package com.example.roadguideapp.goldhunt.achievements.badges

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_badge_entry")
internal data class AchievementBadgeEntryEntity(
    @PrimaryKey val badgeKey: String,
    val achievementId: String,
    val title: String,
    val iconKey: String,
    val rarity: String,
    val unlockDateMs: Long? = null,
    val schemaVersion: Int = AchievementBadgeSchema.VERSION,
    val extensionJson: String = AchievementBadgeSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
