package com.example.roadguideapp.goldhunt.relics.hidden

import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicSchema

/**
 * Schema metadata for hidden relic discovery and revelation hooks.
 */
internal object HiddenRelicDiscoverySchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val VISIBILITY = "visibility"
        const val REVEALED_AT_MS = "revealedAtMs"
    }

    object HookTypes {
        const val STORY_FRAGMENT = "storyFragment"
        const val ACHIEVEMENT = "achievement"
    }

    object EventPrefixes {
        const val REVEAL = "hidden_relic_discovery:reveal"
        const val STORY = "hidden_relic_discovery:story"
        const val ACHIEVEMENT = "hidden_relic_discovery:achievement"
    }

    object Placeholders {
        const val HIDDEN_NAME = "???"
        const val HIDDEN_DESCRIPTION =
            "Discover the first piece to reveal this hidden relic."
        const val HIDDEN_CATEGORY_LABEL = "Hidden relic"
    }

    fun revealEventId(relicId: String): String = "${EventPrefixes.REVEAL}:$relicId"

    fun storyFragmentEventId(relicId: String): String = "${EventPrefixes.STORY}:$relicId"

    fun achievementEventId(relicId: String): String = "${EventPrefixes.ACHIEVEMENT}:$relicId"

    fun storyFragmentKeyFor(relicId: String, category: RelicCategory): String? =
        if (RelicSchema.CategoryGates.supportsStoryFragments(category)) {
            RelicSchema.StoryFragmentKeys.revealForRelic(relicId)
        } else {
            null
        }

    fun achievementKeyFor(category: RelicCategory): String =
        RelicSchema.AchievementKeys.firstDiscovery(category)
}
