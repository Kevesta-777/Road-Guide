package com.example.roadguideapp.goldhunt.clusters.encyclopedia

import com.example.roadguideapp.goldhunt.clusters.ClusterType

internal object ClusterEncyclopediaSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val ACHIEVEMENT = "achievement"
        const val STORY_FRAGMENT = "storyFragment"
        const val LEGENDARY_RELIC = "legendaryRelic"
    }

    object AchievementKeys {
        const val PREFIX = "cluster_encyclopedia"

        fun firstDiscovery(type: ClusterType): String =
            "$PREFIX:${type.id.lowercase()}:first_discovery"

        fun firstCompletion(type: ClusterType): String =
            "$PREFIX:${type.id.lowercase()}:first_completion"

        fun completeAllFamilies(): String = "$PREFIX:all_families_complete"
    }

    object StoryFragmentKeys {
        const val PREFIX = "story_fragment_cluster"

        fun forType(type: ClusterType): String? = type.storyFragmentKey
    }
}
