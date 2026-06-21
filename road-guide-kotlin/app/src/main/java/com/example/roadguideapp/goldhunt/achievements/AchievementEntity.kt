package com.example.roadguideapp.goldhunt.achievements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement")
internal data class AchievementEntity(
    @PrimaryKey val achievementId: String,
    val title: String,
    val description: String,
    val category: String,
    val targetValue: Int,
    val currentValue: Int,
    val completed: Boolean,
    val completionDateMs: Long?,
    val rewardCredits: Int,
    val rewardXp: Long,
    val legendaryRelicKey: String?,
    val titleKey: String?,
    val badgeKey: String?,
    val schemaVersion: Int = AchievementSchema.VERSION,
    val extensionJson: String = AchievementSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
