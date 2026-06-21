package com.example.roadguideapp.goldhunt.treasure.encyclopedia

import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinitionKey

/**
 * Schema metadata for [TreasureEncyclopediaEntryEntity].
 */
internal object TreasureEncyclopediaSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object AchievementKeys {
        const val PREFIX = "treasure_encyclopedia"
        fun firstDiscovery(key: TreasureDefinitionKey): String = "$PREFIX:${key.name.lowercase()}:first"
        fun completeStandardSet(): String = "$PREFIX:standard_complete"
        fun discoveryCount(count: Int): String = "$PREFIX:discovery_count_$count"
    }
}
