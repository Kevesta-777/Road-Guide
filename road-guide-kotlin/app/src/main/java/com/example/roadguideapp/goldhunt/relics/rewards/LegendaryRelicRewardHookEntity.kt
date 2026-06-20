package com.example.roadguideapp.goldhunt.relics.rewards

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "legendary_relic_reward_hook")
internal data class LegendaryRelicRewardHookEntity(
    @PrimaryKey val eventId: String,
    val relicId: String,
    val hookType: String,
    val hookKey: String,
    val grantedAtMs: Long,
    val schemaVersion: Int = LegendaryRelicRewardSchema.VERSION,
)
