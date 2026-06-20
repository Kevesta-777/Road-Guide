package com.example.roadguideapp.goldhunt.profile.levelup

/**
 * Schema metadata for [LevelUpEventEntity].
 * Bump [VERSION] when adding columns; use [LevelUpEventEntity.extensionJson] for experiments first.
 */
internal object LevelUpEventSchema {
    const val VERSION = 1
    const val TABLE_NAME = "level_up_event"
    const val EMPTY_EXTENSIONS_JSON = "{}"

    /** Reserved JSON keys for future unlock/reward payloads. */
    object ExtensionKeys {
        const val ACHIEVEMENTS = "achievements"
        const val UNLOCKS = "unlocks"
        const val RADAR = "radar"
        const val REWARDS = "rewards"
    }
}
