package com.example.roadguideapp.goldhunt.events.collectionbook

import com.example.roadguideapp.goldhunt.events.SeasonalEventSchema
import com.example.roadguideapp.goldhunt.events.SeasonalEventType

internal data class SeasonalEventCollectionBookTemplate(
    val eventType: SeasonalEventType,
    val displayName: String,
    val minExplorerLevel: Int,
    val participationAchievementKey: String,
    val completionAchievementKey: String,
    val masteryAchievementKey: String,
    val storyFragmentKey: String,
    val legendaryRelicKey: String,
)

internal object SeasonalEventCollectionBookCatalog {
    val displayTemplates: List<SeasonalEventCollectionBookTemplate> =
        SeasonalEventType.ALL_ORDERED.map { type ->
            SeasonalEventCollectionBookTemplate(
                eventType = type,
                displayName = type.displayName,
                minExplorerLevel = type.minExplorerLevel,
                participationAchievementKey = SeasonalEventSchema.AchievementKeys.firstParticipation(type),
                completionAchievementKey = SeasonalEventSchema.AchievementKeys.completion(type),
                masteryAchievementKey = SeasonalEventSchema.AchievementKeys.mastery(type),
                storyFragmentKey = SeasonalEventSchema.StoryFragmentKeys.forType(type),
                legendaryRelicKey = SeasonalEventSchema.LegendaryRelicKeys.forType(type),
            )
        }

    fun templateFor(type: SeasonalEventType): SeasonalEventCollectionBookTemplate? =
        displayTemplates.firstOrNull { it.eventType == type }
}
