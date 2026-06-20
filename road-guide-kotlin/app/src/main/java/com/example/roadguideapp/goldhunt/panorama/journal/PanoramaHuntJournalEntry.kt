package com.example.roadguideapp.goldhunt.panorama.journal

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType

internal data class PanoramaHuntJournalEntry(
    val huntType: PanoramaHuntType,
    val displayName: String,
    val status: PanoramaHuntJournalStatus,
    val firstDiscoveredAtMs: Long?,
    val completedAtMs: Long?,
    val totalRewardsEarned: Int,
    val totalXpEarned: Long,
    val discoveredCount: Int,
    val completedCount: Int,
    val achievementKey: String?,
    val storyFragmentKey: String?,
    val legendaryRelicKey: String?,
    val achievementUnlocked: Boolean,
    val storyFragmentUnlocked: Boolean,
    val legendaryRelicUnlocked: Boolean,
    val schemaVersion: Int = PanoramaHuntJournalSchema.VERSION,
    val extensionJson: String = PanoramaHuntJournalSchema.EMPTY_EXTENSIONS_JSON,
) {
    val isMissing: Boolean get() = status == PanoramaHuntJournalStatus.MISSING
    val isDiscovered: Boolean get() = status == PanoramaHuntJournalStatus.DISCOVERED
    val isCompleted: Boolean get() = status == PanoramaHuntJournalStatus.COMPLETED
}
