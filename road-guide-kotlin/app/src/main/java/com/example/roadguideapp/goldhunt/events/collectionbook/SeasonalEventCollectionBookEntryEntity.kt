package com.example.roadguideapp.goldhunt.events.collectionbook

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seasonal_event_collection_book_entry")
internal data class SeasonalEventCollectionBookEntryEntity(
    @PrimaryKey val eventId: String,
    val eventType: String,
    val cycleYear: Int,
    val displayName: String,
    val status: String,
    val firstParticipatedAtMs: Long? = null,
    val completedAtMs: Long? = null,
    val totalCreditsEarned: Int = 0,
    val totalXpEarned: Long = 0L,
    val fragmentsEarned: Int = 0,
    val achievementsEarned: Int = 0,
    val participationAchievementKey: String? = null,
    val completionAchievementKey: String? = null,
    val masteryAchievementKey: String? = null,
    val storyFragmentKey: String? = null,
    val legendaryRelicKey: String? = null,
    val schemaVersion: Int = SeasonalEventCollectionBookSchema.VERSION,
    val extensionJson: String = SeasonalEventCollectionBookSchema.EMPTY_EXTENSIONS_JSON,
    val updatedAtMs: Long = 0L,
)
