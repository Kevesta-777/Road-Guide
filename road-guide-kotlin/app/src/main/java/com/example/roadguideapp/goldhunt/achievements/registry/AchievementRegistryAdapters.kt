package com.example.roadguideapp.goldhunt.achievements.registry

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementDefinition
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileSchema

internal object AchievementRegistryAdapters {
    fun fromSeasonal(definition: SeasonalEventAchievementDefinition): AchievementDefinition =
        AchievementDefinition(
            key = definition.key,
            category = AchievementCategory.SEASONAL_EVENT,
            displayName = definition.displayName,
            targetCount = definition.targetCount,
            minExplorerLevel = AchievementSchema.CategoryGates.minExplorerLevel(
                AchievementCategory.SEASONAL_EVENT,
            ),
            storyFragmentKey = AchievementSchema.StoryFragmentKeys.forCategory(
                AchievementCategory.SEASONAL_EVENT,
            ),
            legendaryRelicKey = AchievementSchema.LegendaryRelicKeys.forCategory(
                AchievementCategory.SEASONAL_EVENT,
            ),
        )

    fun milestone(
        category: AchievementCategory,
        displayLabel: String,
        count: Int,
    ): AchievementDefinition = AchievementDefinition(
        key = AchievementSchema.MilestoneKeys.forCategory(category, count),
        category = category,
        displayName = "$displayLabel — $count",
        targetCount = count,
        minExplorerLevel = AchievementSchema.CategoryGates.minExplorerLevel(category),
        storyFragmentKey = if (AchievementSchema.CategoryGates.supportsStoryFragments(category)) {
            AchievementSchema.StoryFragmentKeys.forCategory(category)
        } else {
            null
        },
        legendaryRelicKey = if (AchievementSchema.CategoryGates.supportsLegendaryRelics(category)) {
            AchievementSchema.LegendaryRelicKeys.forCategory(category)
        } else {
            null
        },
    )

    fun simple(
        key: String,
        category: AchievementCategory,
        displayName: String,
        targetCount: Int = 1,
        minExplorerLevel: Int = ExplorerProfileSchema.DEFAULT_EXPLORER_LEVEL,
        storyFragmentKey: String? = null,
        legendaryRelicKey: String? = null,
    ): AchievementDefinition = AchievementDefinition(
        key = key,
        category = category,
        displayName = displayName,
        targetCount = targetCount,
        minExplorerLevel = minExplorerLevel,
        storyFragmentKey = storyFragmentKey,
        legendaryRelicKey = legendaryRelicKey,
    )
}
