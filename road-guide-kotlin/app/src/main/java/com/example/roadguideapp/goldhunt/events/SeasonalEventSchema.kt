package com.example.roadguideapp.goldhunt.events

import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventCategory

/**
 * Schema metadata for seasonal live events.
 * Bump [VERSION] when adding persistence columns; use [EMPTY_EXTENSIONS_JSON] for experiments first.
 */
internal object SeasonalEventSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"
    const val BASE_MULTIPLIER = 1.0

    object ExtensionKeys {
        const val ACHIEVEMENT = "achievement"
        const val STORY_FRAGMENT = "storyFragment"
        const val LEGENDARY_RELIC = "legendaryRelic"
        const val PARTICIPATION = "participation"
        const val PROGRESS = "progress"
    }

    object AchievementKeys {
        const val PREFIX = "seasonal_event"

        fun firstParticipation(type: SeasonalEventType): String =
            "$PREFIX:${type.id.lowercase()}:first_participation"

        fun completion(type: SeasonalEventType): String =
            "$PREFIX:${type.id.lowercase()}:completed"

        fun mastery(type: SeasonalEventType): String =
            "$PREFIX:${type.id.lowercase()}:mastery"
    }

    object StoryFragmentKeys {
        const val PREFIX = "story_fragment_seasonal_event"

        fun forType(type: SeasonalEventType): String =
            "$PREFIX:${type.id.lowercase()}"
    }

    object LegendaryRelicKeys {
        const val PREFIX = "legendary_relic_seasonal_event"

        fun forType(type: SeasonalEventType): String =
            "$PREFIX:${type.id.lowercase()}"
    }

    object EventIds {
        const val PREFIX = "seasonal:event"

        fun instance(type: SeasonalEventType, cycleYear: Int): String =
            "$PREFIX:${type.id.lowercase()}:$cycleYear"

        fun participation(type: SeasonalEventType, year: Int): String =
            "$PREFIX:${type.id.lowercase()}:participation:$year"

        fun rewardGrant(type: SeasonalEventType, grantKey: String, year: Int): String =
            "$PREFIX:${type.id.lowercase()}:reward:$grantKey:$year"
    }

    object SeedPrefixes {
        const val EVENT = "seasonal_event"

        fun forInstance(type: SeasonalEventType, cycleYear: Int): String =
            "$EVENT:v$VERSION:${type.id.lowercase()}:$cycleYear"
    }

    object ExplorerLevelKeys {
        const val PREFIX = "seasonal_event_level"

        fun unlocked(type: SeasonalEventType): String =
            "$PREFIX:${type.id.lowercase()}:unlocked"

        fun customUnlocked(eventKey: String): String =
            "$PREFIX:custom:${eventKey.lowercase()}:unlocked"
    }

    object CustomEventIds {
        const val PREFIX = "seasonal:custom"

        fun annualInstance(eventKey: String, cycleYear: Int): String =
            "$PREFIX:${eventKey.lowercase()}:$cycleYear"

        fun fixedInstance(eventKey: String, startDateEpochDay: Long): String =
            "$PREFIX:${eventKey.lowercase()}:fixed:$startDateEpochDay"

        fun participation(eventKey: String, suffix: String): String =
            "$PREFIX:${eventKey.lowercase()}:participation:$suffix"
    }

    object CategoryAchievementKeys {
        const val PREFIX = "seasonal_event_category"

        fun collector(category: SeasonalEventCategory): String =
            "$PREFIX:${category.name.lowercase()}:collector"
    }

    object CustomAchievementKeys {
        const val PREFIX = "seasonal_custom_event"

        fun firstParticipation(eventKey: String): String =
            "$PREFIX:${eventKey.lowercase()}:first_participation"

        fun completion(eventKey: String): String =
            "$PREFIX:${eventKey.lowercase()}:completed"

        fun mastery(eventKey: String): String =
            "$PREFIX:${eventKey.lowercase()}:mastery"
    }

    object CustomStoryFragmentKeys {
        const val PREFIX = "story_fragment_seasonal_custom_event"

        fun forKey(eventKey: String): String =
            "$PREFIX:${eventKey.lowercase()}"
    }

    object CustomLegendaryRelicKeys {
        const val PREFIX = "legendary_relic_seasonal_custom_event"

        fun forKey(eventKey: String): String =
            "$PREFIX:${eventKey.lowercase()}"
    }

    object CustomSeedPrefixes {
        const val EVENT = "seasonal_custom_event"

        fun annual(eventKey: String, cycleYear: Int): String =
            "$EVENT:v$VERSION:${eventKey.lowercase()}:$cycleYear"

        fun fixed(eventKey: String, startDateEpochDay: Long): String =
            "$EVENT:v$VERSION:${eventKey.lowercase()}:fixed:$startDateEpochDay"
    }
}
