package com.example.roadguideapp.goldhunt.secretplaces

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

/**
 * Schema metadata for [SecretPlace] instances and persistence.
 * Bump [VERSION] when adding fields; use [extensionJson] on entities for experiments first.
 */
internal object SecretPlaceSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val EXPLORER_LEVEL = "explorerLevel"
        const val TREASURE_CLUSTER = "treasureCluster"
        const val STORY_FRAGMENT = "storyFragment"
        const val LEGENDARY_RELIC = "legendaryRelic"
        const val ACHIEVEMENT = "achievement"
        const val PANORAMA_HUNT = "panoramaHunt"
    }

    /**
     * Default minimum explorer level gate derived from category difficulty.
     * Instance [SecretPlace.minimumExplorerLevel] overrides this when set.
     */
    fun defaultMinimumExplorerLevel(category: SecretPlaceCategory): Int =
        category.difficultyLevel.coerceAtLeast(1)
}
