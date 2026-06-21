package com.example.roadguideapp.goldhunt.panorama.journal

internal data class PanoramaHuntJournalProgress(
    val discoveredCount: Int,
    val completedCount: Int,
    val missingCount: Int,
    val trackableCount: Int,
    val totalRewardsEarned: Int,
    val totalXpEarned: Long,
) {
    val discoveredFraction: Float
        get() = if (trackableCount <= 0) 0f else discoveredCount.toFloat() / trackableCount

    val completionFraction: Float
        get() = if (trackableCount <= 0) 0f else completedCount.toFloat() / trackableCount

    val missingFraction: Float
        get() = if (trackableCount <= 0) 0f else missingCount.toFloat() / trackableCount
}

internal data class PanoramaHuntJournal(
    val entries: List<PanoramaHuntJournalEntry>,
    val progress: PanoramaHuntJournalProgress,
)
