package com.example.roadguideapp.goldhunt.secretplaces.categories

/**
 * Reserved keys and extension metadata for secret-place category systems.
 * Achievement, story, panorama, and relic unlockers consume these identifiers later.
 */
internal object SecretPlaceCategorySchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val ACHIEVEMENT = "achievement"
        const val STORY_FRAGMENT = "storyFragment"
        const val PANORAMA_HUNT = "panoramaHunt"
        const val LEGENDARY_RELIC = "legendaryRelic"
    }

    object AchievementKeys {
        const val PREFIX = "secret_place_category"

        fun firstDiscovery(category: SecretPlaceCategory): String =
            "$PREFIX:${category.id.lowercase()}:first_discovery"

        fun firstVisit(category: SecretPlaceCategory): String =
            "$PREFIX:${category.id.lowercase()}:first_visit"

        fun completeAllCategories(): String = "$PREFIX:all_categories_complete"
    }

    object StoryFragmentKeys {
        const val PREFIX = "story_fragment_secret_place"

        fun forCategory(category: SecretPlaceCategory): String? = category.storyFragmentKey
    }

    object PanoramaHuntKeys {
        const val PREFIX = "panorama_hunt_secret_place"

        fun forCategory(category: SecretPlaceCategory): String =
            category.panoramaHuntKey ?: "$PREFIX:${category.id.lowercase()}"
    }

    object LegendaryRelicKeys {
        const val PREFIX = "legendary_relic_secret_place"

        fun forCategory(category: SecretPlaceCategory): String? = category.legendaryRelicKey
    }
}
