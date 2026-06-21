package com.example.roadguideapp.goldhunt.achievements.rewards

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_reward_hook")
internal data class AchievementRewardHookEntity(
    @PrimaryKey val eventId: String,
    val achievementId: String,
    val hookType: String,
    val hookKey: String,
    val grantedAtMs: Long,
    val schemaVersion: Int = AchievementRewardSchema.VERSION,
)
