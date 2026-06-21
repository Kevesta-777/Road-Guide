package com.example.roadguideapp.goldhunt.events.collectionbook

import com.example.roadguideapp.goldhunt.events.SeasonalEventType

internal data class SeasonalEventCollectionBookEntry(
    val eventId: String,
    val eventType: SeasonalEventType,
    val cycleYear: Int,
    val displayName: String,
    val status: SeasonalEventCollectionBookStatus,
    val firstParticipatedAtMs: Long?,
    val completedAtMs: Long?,
    val totalCreditsEarned: Int,
    val totalXpEarned: Long,
    val fragmentsEarned: Int,
    val achievementsEarned: Int,
    val participationAchievementKey: String?,
    val completionAchievementKey: String?,
    val masteryAchievementKey: String?,
    val storyFragmentKey: String?,
    val legendaryRelicKey: String?,
    val participationAchievementEarned: Boolean,
    val completionAchievementEarned: Boolean,
    val masteryAchievementEarned: Boolean,
    val storyFragmentEarned: Boolean,
    val legendaryRelicEarned: Boolean,
    val schemaVersion: Int = SeasonalEventCollectionBookSchema.VERSION,
    val extensionJson: String = SeasonalEventCollectionBookSchema.EMPTY_EXTENSIONS_JSON,
) {
    val isMissing: Boolean get() = status == SeasonalEventCollectionBookStatus.MISSING
    val isParticipated: Boolean get() = status == SeasonalEventCollectionBookStatus.PARTICIPATED
    val isCompleted: Boolean get() = status == SeasonalEventCollectionBookStatus.COMPLETED
}

internal data class SeasonalEventCollectionBookFamilyEntry(
    val eventType: SeasonalEventType,
    val displayName: String,
    val minExplorerLevel: Int,
    val participationAchievementKey: String,
    val completionAchievementKey: String,
    val masteryAchievementKey: String,
    val storyFragmentKey: String,
    val legendaryRelicKey: String,
)

internal data class SeasonalEventCollectionBookProgress(
    val participatedCycles: Int,
    val completedCycles: Int,
    val familiesParticipated: Int,
    val familiesCompleted: Int,
    val familiesMissing: Int,
    val trackableFamilies: Int,
    val totalCreditsEarned: Int,
    val totalXpEarned: Long,
    val totalFragmentsEarned: Int,
    val totalAchievementsEarned: Int,
) {
    val participatedFraction: Float
        get() = if (trackableFamilies <= 0) 0f else familiesParticipated.toFloat() / trackableFamilies.toFloat()

    val completedFraction: Float
        get() = if (trackableFamilies <= 0) 0f else familiesCompleted.toFloat() / trackableFamilies.toFloat()

    val missingFraction: Float
        get() = if (trackableFamilies <= 0) 0f else familiesMissing.toFloat() / trackableFamilies.toFloat()
}

internal data class SeasonalEventCollectionBook(
    val cycleEntries: List<SeasonalEventCollectionBookEntry>,
    val missingFamilies: List<SeasonalEventCollectionBookFamilyEntry>,
    val progress: SeasonalEventCollectionBookProgress,
    val schemaVersion: Int = SeasonalEventCollectionBookSchema.VERSION,
)
