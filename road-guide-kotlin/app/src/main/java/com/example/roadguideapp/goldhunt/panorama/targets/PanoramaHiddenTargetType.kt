package com.example.roadguideapp.goldhunt.panorama.targets

import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType

/**
 * Visual/interaction family for a hidden panorama target.
 * [LEGENDARY_RELIC] is reserved for rare relic-hunt rolls (future viewer effects).
 */
internal enum class PanoramaHiddenTargetType(
    val displayName: String,
    val supportsAnimation: Boolean = false,
) {
    SYMBOL("Symbol"),
    OBJECT("Object", supportsAnimation = true),
    PUZZLE_MARKER("Puzzle Marker", supportsAnimation = true),
    CODE_RUNE("Code Rune"),
    RELIC("Relic"),
    LEGENDARY_RELIC("Legendary Relic"),
    ;

    companion object {
        fun defaultFor(huntType: PanoramaHuntType): PanoramaHiddenTargetType = when (huntType) {
            PanoramaHuntType.HIDDEN_SYMBOL -> SYMBOL
            PanoramaHuntType.OBJECT_HUNT -> OBJECT
            PanoramaHuntType.PANORAMA_PUZZLE -> PUZZLE_MARKER
            PanoramaHuntType.SECRET_CODE -> CODE_RUNE
            PanoramaHuntType.RELIC_HUNT -> RELIC
        }
    }
}
