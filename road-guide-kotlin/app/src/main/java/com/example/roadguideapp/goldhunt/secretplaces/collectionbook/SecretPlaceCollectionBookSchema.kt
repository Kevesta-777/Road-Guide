package com.example.roadguideapp.goldhunt.secretplaces.collectionbook

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

internal object SecretPlaceCollectionBookSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val EXPLORER_LEVEL = "explorerLevel"
        const val ACHIEVEMENT = "achievement"
        const val STORY_FRAGMENT = "storyFragment"
        const val PANORAMA_HUNT = "panoramaHunt"
        const val LEGENDARY_RELIC = "legendaryRelic"
    }

    object AchievementKeys {
        const val PREFIX = "secret_place_collection_book"

        fun firstDiscovery(category: SecretPlaceCategory): String =
            "$PREFIX:${category.id.lowercase()}:first_discovery"

        fun firstCompletion(category: SecretPlaceCategory): String =
            "$PREFIX:${category.id.lowercase()}:first_completion"

        fun completeAllCategories(): String = "$PREFIX:all_categories_complete"
    }

    object StoryFragmentKeys {
        const val PREFIX = "story_fragment_secret_place_collection"

        fun forCategory(category: SecretPlaceCategory): String? = category.storyFragmentKey
    }

    object ExplorerLevelKeys {
        fun minimumForCategory(category: SecretPlaceCategory): Int = category.difficultyLevel
    }
}
