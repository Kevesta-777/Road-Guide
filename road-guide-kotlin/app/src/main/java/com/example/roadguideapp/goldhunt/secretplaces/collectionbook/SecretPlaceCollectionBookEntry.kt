package com.example.roadguideapp.goldhunt.secretplaces.collectionbook

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

internal data class SecretPlaceCollectionBookEntry(
    val category: SecretPlaceCategory,
    val displayName: String,
    val status: SecretPlaceCollectionBookStatus,
    val firstDiscoveredAtMs: Long?,
    val completedAtMs: Long?,
    val totalCreditsEarned: Int,
    val totalXpEarned: Long,
    val discoveredCount: Int,
    val completedCount: Int,
    val minimumExplorerLevel: Int,
    val achievementKey: String?,
    val storyFragmentKey: String?,
    val schemaVersion: Int = SecretPlaceCollectionBookSchema.VERSION,
    val extensionJson: String = SecretPlaceCollectionBookSchema.EMPTY_EXTENSIONS_JSON,
) {
    val isMissing: Boolean get() = status == SecretPlaceCollectionBookStatus.MISSING
    val isDiscovered: Boolean get() = status == SecretPlaceCollectionBookStatus.DISCOVERED
    val isCompleted: Boolean get() = status == SecretPlaceCollectionBookStatus.COMPLETED
}

internal data class SecretPlaceCollectionBookProgress(
    val discoveredCount: Int,
    val completedCount: Int,
    val missingCount: Int,
    val trackableCount: Int,
    val totalCreditsEarned: Int,
    val totalXpEarned: Long,
) {
    val discoveredFraction: Float
        get() = if (trackableCount <= 0) 0f else discoveredCount.toFloat() / trackableCount.toFloat()

    val completionFraction: Float
        get() = if (trackableCount <= 0) 0f else completedCount.toFloat() / trackableCount.toFloat()
}

internal data class SecretPlaceCollectionBook(
    val entries: List<SecretPlaceCollectionBookEntry>,
    val progress: SecretPlaceCollectionBookProgress,
    val schemaVersion: Int = SecretPlaceCollectionBookSchema.VERSION,
)
