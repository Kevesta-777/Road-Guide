package com.example.roadguideapp.goldhunt.treasure.rarity

/**
 * Schema metadata for [TreasureRarity].
 * Bump [VERSION] when adding enum fields; use extension keys for experiments first.
 */
internal object TreasureRaritySchema {
    const val VERSION = 1

    /** Reserved keys for future presentation / gameplay payloads. */
    object ExtensionKeys {
        const val ICON_EFFECT = "iconEffect"
        const val ANIMATION = "animation"
        const val RADAR_HIGHLIGHT = "radarHighlight"
        const val ACHIEVEMENT = "achievement"
    }
}
