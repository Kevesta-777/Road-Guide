package com.example.roadguideapp.goldhunt.events.achievements

internal object SeasonalEventCategoryAchievementEvaluator {
    suspend fun evaluateCategoryCollectors(
        category: SeasonalEventCategory,
        repository: SeasonalEventAchievementRepository,
        timestampMs: Long,
    ): List<SeasonalEventAchievementProgress> {
        val definition = SeasonalEventAchievementCatalog.categoryAchievements(category)
            .firstOrNull() ?: return emptyList()
        val current = repository.load(definition)
        if (current.isComplete) return emptyList()

        val completedMembers = SeasonalEventAchievementCatalog.eventTypesInCategory(category)
            .count { type ->
                val completion = SeasonalEventAchievementCatalog.eventAchievements(type)
                    .first { it.tier == SeasonalEventAchievementTier.COMPLETION }
                repository.load(completion).isComplete
            }
        if (completedMembers < definition.targetCount) return emptyList()

        return listOfNotNull(
            repository.increment(
                definition = definition,
                increment = definition.targetCount - current.currentCount,
                timestampMs = timestampMs,
            ),
        )
    }
}
