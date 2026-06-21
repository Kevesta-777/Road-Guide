package com.example.roadguideapp.goldhunt.achievements

import com.example.roadguideapp.goldhunt.profile.ExplorerProfileSchema

/**
 * Canonical achievement definition for the unified catalog layer.
 *
 * Domain-specific packages (seasonal events, panorama hunts, secret places) may
 * continue to define their own definitions until migrated. New cross-domain
 * achievements should use this shape so Explorer Profile, collection books, and
 * reward dispatchers share one contract.
 */
internal data class AchievementDefinition(
    val key: String,
    val category: AchievementCategory,
    val displayName: String,
    val targetCount: Int = 1,
    val minExplorerLevel: Int = ExplorerProfileSchema.DEFAULT_EXPLORER_LEVEL,
    val storyFragmentKey: String? = null,
    val legendaryRelicKey: String? = null,
    val schemaVersion: Int = AchievementSchema.VERSION,
    val extensionJson: String = AchievementSchema.EMPTY_EXTENSIONS_JSON,
) {
    init {
        require(key.isNotBlank()) { "key must not be blank" }
        require(displayName.isNotBlank()) { "displayName must not be blank" }
        require(targetCount >= 1) { "targetCount must be at least 1" }
        require(minExplorerLevel >= ExplorerProfileSchema.DEFAULT_EXPLORER_LEVEL) {
            "minExplorerLevel must be at least ${ExplorerProfileSchema.DEFAULT_EXPLORER_LEVEL}"
        }
    }

    val iconKey: String get() = category.iconKey

    val difficultyMultiplier: Double get() = category.difficultyMultiplier

    val hasStoryFragmentHook: Boolean get() = !storyFragmentKey.isNullOrBlank()

    val hasLegendaryRelicHook: Boolean get() = !legendaryRelicKey.isNullOrBlank()
}
