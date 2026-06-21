package com.example.roadguideapp.goldhunt.clusters

/**
 * Schema metadata for treasure cluster instances and persistence.
 * Bump [VERSION] when adding fields; use [extensionJson] on future entities for experiments first.
 */
internal object ClusterSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val ACHIEVEMENT = "achievement"
        const val STORY_FRAGMENT = "storyFragment"
        const val SECRET_PLACE = "secretPlace"
        const val RADAR = "radar"
        const val SEASONAL_EVENT = "seasonalEvent"
        const val LEGENDARY_RELIC = "legendaryRelic"
    }
}
