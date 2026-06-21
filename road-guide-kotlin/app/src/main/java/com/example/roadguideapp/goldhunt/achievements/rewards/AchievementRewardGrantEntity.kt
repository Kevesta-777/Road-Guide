package com.example.roadguideapp.goldhunt.achievements.rewards

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_reward_grant")
internal data class AchievementRewardGrantEntity(
    @PrimaryKey val achievementId: String,
    val creditsGranted: Int,
    val xpGranted: Long,
    val storyFragmentKey: String?,
    val storyFragmentRecorded: Boolean,
    val legendaryRelicKey: String?,
    val legendaryRelicRecorded: Boolean,
    val titleKey: String?,
    val titleRecorded: Boolean,
    val badgeKey: String?,
    val badgeRecorded: Boolean,
    val grantedAtMs: Long,
    val schemaVersion: Int = AchievementRewardSchema.VERSION,
    val extensionJson: String = AchievementRewardSchema.EMPTY_EXTENSIONS_JSON,
)
