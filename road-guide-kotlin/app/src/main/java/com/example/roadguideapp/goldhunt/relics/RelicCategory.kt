package com.example.roadguideapp.goldhunt.relics

import com.example.roadguideapp.goldhunt.profile.ExplorerProfileSchema

/**
 * Canonical legendary relic families for the Gold Hunt progression system.
 *
 * Each category defines presentation metadata ([displayName], [iconKey]), a
 * [rarityMultiplier] for reward scaling, and a [minimumExplorerLevel] gate.
 * Achievement, story-fragment, and secret-place hook keys are resolved in
 * [RelicSchema] so this enum stays lightweight.
 */
internal enum class RelicCategory(
    val displayName: String,
    val iconKey: String,
    val rarityMultiplier: Double,
    val minimumExplorerLevel: Int,
) {
    EXPLORER(
        displayName = "Explorer",
        iconKey = "relic_explorer",
        rarityMultiplier = 1.2,
        minimumExplorerLevel = 5,
    ),
    STORY(
        displayName = "Story",
        iconKey = "relic_story",
        rarityMultiplier = 1.4,
        minimumExplorerLevel = 10,
    ),
    SEASONAL_EVENT(
        displayName = "Seasonal Event",
        iconKey = "relic_seasonal_event",
        rarityMultiplier = 1.5,
        minimumExplorerLevel = 10,
    ),
    WARRIOR(
        displayName = "Warrior",
        iconKey = "relic_warrior",
        rarityMultiplier = 1.6,
        minimumExplorerLevel = 15,
    ),
    ANCIENT_CIVILIZATION(
        displayName = "Ancient Civilization",
        iconKey = "relic_ancient_civilization",
        rarityMultiplier = 2.0,
        minimumExplorerLevel = 20,
    ),
    MYTHICAL(
        displayName = "Mythical",
        iconKey = "relic_mythical",
        rarityMultiplier = 2.2,
        minimumExplorerLevel = 18,
    ),
    ROYAL(
        displayName = "Royal",
        iconKey = "relic_royal",
        rarityMultiplier = 2.5,
        minimumExplorerLevel = 25,
    ),
    HIDDEN(
        displayName = "Hidden",
        iconKey = "relic_hidden",
        rarityMultiplier = 3.0,
        minimumExplorerLevel = 30,
    ),
    ;

    val id: String get() = name

    companion object {
        /** Display and catalog order from common to capstone. */
        val ALL_ORDERED: List<RelicCategory> = listOf(
            EXPLORER,
            STORY,
            SEASONAL_EVENT,
            WARRIOR,
            MYTHICAL,
            ANCIENT_CIVILIZATION,
            ROYAL,
            HIDDEN,
        )

        fun fromId(id: String): RelicCategory? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }

    init {
        require(displayName.isNotBlank()) { "$name displayName must not be blank" }
        require(iconKey.isNotBlank()) { "$name iconKey must not be blank" }
        require(rarityMultiplier > 0.0) { "$name rarityMultiplier must be positive" }
        require(minimumExplorerLevel >= ExplorerProfileSchema.DEFAULT_EXPLORER_LEVEL) {
            "$name minimumExplorerLevel must be at least ${ExplorerProfileSchema.DEFAULT_EXPLORER_LEVEL}"
        }
    }
}
