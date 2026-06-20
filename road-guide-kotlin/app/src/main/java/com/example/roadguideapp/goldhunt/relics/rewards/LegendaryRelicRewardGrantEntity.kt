package com.example.roadguideapp.goldhunt.relics.rewards

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "legendary_relic_reward_grant")
internal data class LegendaryRelicRewardGrantEntity(
    @PrimaryKey val relicId: String,
    val creditsGranted: Int,
    val xpGranted: Long,
    val storyFragmentKey: String?,
    val storyFragmentRecorded: Boolean,
    val titleKey: String?,
    val titleRecorded: Boolean,
    val badgeKey: String?,
    val badgeRecorded: Boolean,
    val cosmeticKey: String?,
    val cosmeticRecorded: Boolean,
    val grantedAtMs: Long,
    val schemaVersion: Int = LegendaryRelicRewardSchema.VERSION,
    val extensionJson: String = LegendaryRelicRewardSchema.EMPTY_EXTENSIONS_JSON,
)
