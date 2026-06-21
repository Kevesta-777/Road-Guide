package com.example.roadguideapp.goldhunt.panorama.journal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panorama_hunt_journal_entry")
internal data class PanoramaHuntJournalEntryEntity(
    @PrimaryKey val catalogKey: String,
    val status: String,
    val firstDiscoveredAtMs: Long? = null,
    val completedAtMs: Long? = null,
    val totalRewardsEarned: Int = 0,
    val totalXpEarned: Long = 0L,
    val discoveredCount: Int = 0,
    val completedCount: Int = 0,
    val achievementKey: String? = null,
    val storyFragmentId: String? = null,
    val legendaryRelicKey: String? = null,
    val schemaVersion: Int = PanoramaHuntJournalSchema.VERSION,
    val extensionJson: String = PanoramaHuntJournalSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
