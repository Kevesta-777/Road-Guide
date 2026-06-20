package com.example.roadguideapp.goldhunt.relics.hidden

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_relic_discovery_hook")
internal data class HiddenRelicDiscoveryHookEntity(
    @PrimaryKey val eventId: String,
    val relicId: String,
    val hookType: String,
    val hookKey: String,
    val grantedAtMs: Long,
    val schemaVersion: Int = HiddenRelicDiscoverySchema.VERSION,
)
