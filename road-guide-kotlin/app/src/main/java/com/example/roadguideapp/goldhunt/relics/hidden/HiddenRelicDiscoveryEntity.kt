package com.example.roadguideapp.goldhunt.relics.hidden

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_relic_discovery")
internal data class HiddenRelicDiscoveryEntity(
    @PrimaryKey val relicId: String,
    val visibility: String,
    val revealedAtMs: Long?,
    val firstPieceId: String?,
    val storyFragmentKey: String?,
    val storyFragmentRecorded: Boolean,
    val achievementKey: String?,
    val achievementRecorded: Boolean,
    val schemaVersion: Int = HiddenRelicDiscoverySchema.VERSION,
    val extensionJson: String = HiddenRelicDiscoverySchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
