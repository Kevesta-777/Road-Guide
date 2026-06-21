package com.example.roadguideapp.goldhunt.panorama

/**
 * Schema metadata for [PanoramaHunt] instances and persistence.
 * Bump [VERSION] when adding fields; use [extensionJson] on entities for experiments first.
 */
internal object PanoramaHuntSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val EXPLORER_LEVEL = "explorerLevel"
        const val STORY_FRAGMENT = "storyFragment"
        const val LEGENDARY_RELIC = "legendaryRelic"
        const val ACHIEVEMENT = "achievement"
        const val HOTSPOT = "hotspot"
    }

    /**
     * Default minimum explorer level gate derived from hunt type difficulty.
     * Instance [PanoramaHunt.minimumExplorerLevel] overrides this when set.
     */
    fun defaultMinimumExplorerLevel(huntType: PanoramaHuntType): Int =
        huntType.difficulty.coerceAtLeast(1)

    fun defaultDifficulty(huntType: PanoramaHuntType): Int =
        huntType.difficulty.coerceAtLeast(1)
}
