package com.example.roadguideapp.goldhunt.panorama

/**
 * Reserved keys and extension metadata for panorama hunt systems.
 * Achievement, story, and relic unlockers consume these identifiers later.
 */
internal object PanoramaHuntTypeSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val ACHIEVEMENT = "achievement"
        const val STORY_FRAGMENT = "storyFragment"
        const val LEGENDARY_RELIC = "legendaryRelic"
        const val HOTSPOT = "hotspot"
        const val CLUE_CHAIN = "clueChain"
    }

    object AchievementKeys {
        const val PREFIX = "panorama_hunt_type"

        fun firstCompletion(type: PanoramaHuntType): String =
            "$PREFIX:${type.id.lowercase()}:first_completion"

        fun completeAllTypes(): String = "$PREFIX:all_types_complete"
    }

    object StoryFragmentKeys {
        const val PREFIX = "story_fragment_panorama_hunt"

        fun forType(type: PanoramaHuntType): String? = type.storyFragmentKey
    }

    object LegendaryRelicKeys {
        const val PREFIX = "legendary_relic_panorama_hunt"

        fun forType(type: PanoramaHuntType): String? = type.legendaryRelicKey
    }

    object HuntInstanceKeys {
        const val PREFIX = "panorama_hunt"

        fun forSecretPlace(secretPlaceId: String, type: PanoramaHuntType): String =
            "$PREFIX:place:$secretPlaceId:${type.id.lowercase()}"

        fun forCategoryAnchor(categoryId: String, type: PanoramaHuntType): String =
            "$PREFIX:category:$categoryId:${type.id.lowercase()}"
    }
}
