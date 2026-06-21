package com.example.roadguideapp.goldhunt.events.achievements

import com.example.roadguideapp.goldhunt.events.SeasonalEventSchema
import com.example.roadguideapp.goldhunt.events.SeasonalEventType

internal object SeasonalEventAchievementCatalog {
    fun eventAchievements(type: SeasonalEventType): List<SeasonalEventAchievementDefinition> =
        listOf(
            participation(type),
            completion(type),
            mastery(type),
        )

    fun categoryAchievements(category: SeasonalEventCategory): List<SeasonalEventAchievementDefinition> =
        listOfNotNull(categoryCollector(category))

    fun customEventAchievements(eventKey: String, displayName: String): List<SeasonalEventAchievementDefinition> =
        listOf(
            SeasonalEventAchievementDefinition(
                key = SeasonalEventSchema.CustomAchievementKeys.firstParticipation(eventKey),
                tier = SeasonalEventAchievementTier.FIRST_PARTICIPATION,
                displayName = "$displayName — first participation",
                targetCount = 1,
                category = SeasonalEventCategory.CUSTOM,
                customEventKey = eventKey,
            ),
            SeasonalEventAchievementDefinition(
                key = SeasonalEventSchema.CustomAchievementKeys.completion(eventKey),
                tier = SeasonalEventAchievementTier.COMPLETION,
                displayName = "$displayName — completed",
                targetCount = 1,
                category = SeasonalEventCategory.CUSTOM,
                customEventKey = eventKey,
            ),
        )

    fun eventTypesInCategory(category: SeasonalEventCategory): List<SeasonalEventType> =
        SeasonalEventType.ALL_ORDERED.filter { SeasonalEventCategory.forType(it) == category }

    fun findByKey(key: String): SeasonalEventAchievementDefinition? {
        if (key.isBlank()) return null
        SeasonalEventType.ALL_ORDERED.forEach { type ->
            eventAchievements(type).firstOrNull { it.key == key }?.let { return it }
        }
        SeasonalEventCategory.ALL_ORDERED.forEach { category ->
            categoryAchievements(category).firstOrNull { it.key == key }?.let { return it }
        }
        return null
    }

    private fun participation(type: SeasonalEventType): SeasonalEventAchievementDefinition =
        SeasonalEventAchievementDefinition(
            key = SeasonalEventSchema.AchievementKeys.firstParticipation(type),
            tier = SeasonalEventAchievementTier.FIRST_PARTICIPATION,
            displayName = "${type.displayName} — first participation",
            targetCount = 1,
            eventType = type,
            category = SeasonalEventCategory.forType(type),
        )

    private fun completion(type: SeasonalEventType): SeasonalEventAchievementDefinition =
        SeasonalEventAchievementDefinition(
            key = SeasonalEventSchema.AchievementKeys.completion(type),
            tier = SeasonalEventAchievementTier.COMPLETION,
            displayName = "${type.displayName} — completed",
            targetCount = 1,
            eventType = type,
            category = SeasonalEventCategory.forType(type),
        )

    private fun mastery(type: SeasonalEventType): SeasonalEventAchievementDefinition =
        SeasonalEventAchievementDefinition(
            key = SeasonalEventSchema.AchievementKeys.mastery(type),
            tier = SeasonalEventAchievementTier.MASTERY,
            displayName = "${type.displayName} — mastery",
            targetCount = SeasonalEventAchievementSchema.masteryTarget(type),
            eventType = type,
            category = SeasonalEventCategory.forType(type),
        )

    private fun categoryCollector(category: SeasonalEventCategory): SeasonalEventAchievementDefinition? {
        val members = eventTypesInCategory(category)
        if (members.isEmpty()) return null
        return SeasonalEventAchievementDefinition(
            key = SeasonalEventSchema.CategoryAchievementKeys.collector(category),
            tier = SeasonalEventAchievementTier.CATEGORY_COLLECTOR,
            displayName = "${category.displayName} collector",
            targetCount = SeasonalEventAchievementSchema.categoryCollectorTarget(category),
            category = category,
        )
    }
}
