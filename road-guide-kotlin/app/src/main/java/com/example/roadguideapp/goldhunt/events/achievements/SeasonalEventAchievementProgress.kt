package com.example.roadguideapp.goldhunt.events.achievements

internal data class SeasonalEventAchievementProgress(
    val definition: SeasonalEventAchievementDefinition,
    val currentCount: Int,
    val updatedAtMs: Long = 0L,
) {
    val isComplete: Boolean get() = definition.isComplete(currentCount)
    val progressFraction: Double get() = definition.progressFraction(currentCount)
}

internal data class SeasonalEventAchievementProgressSnapshot(
    val eventTypeKey: String,
    val achievements: List<SeasonalEventAchievementProgress>,
)

internal data class SeasonalEventTreasureAchievementResult(
    val participationProgress: SeasonalEventAchievementProgress?,
    val masteryProgress: SeasonalEventAchievementProgress?,
)

internal data class SeasonalEventCompletionAchievementResult(
    val eventId: String,
    val completionProgress: SeasonalEventAchievementProgress?,
    val categoryProgress: List<SeasonalEventAchievementProgress>,
    val creditsGranted: Int,
    val xpGranted: Long,
    val isNewGrant: Boolean,
) {
    companion object {
        val skipped = SeasonalEventCompletionAchievementResult(
            eventId = "",
            completionProgress = null,
            categoryProgress = emptyList(),
            creditsGranted = 0,
            xpGranted = 0L,
            isNewGrant = false,
        )
    }
}
