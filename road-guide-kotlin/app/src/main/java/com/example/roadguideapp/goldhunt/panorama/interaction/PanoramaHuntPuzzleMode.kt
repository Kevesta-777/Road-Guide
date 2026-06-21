package com.example.roadguideapp.goldhunt.panorama.interaction

/**
 * Reserved puzzle interaction modes for future panorama hunt puzzles.
 * [Disabled] allows immediate target collection on tap hit.
 */
internal sealed class PanoramaHuntPuzzleMode {
    data object Disabled : PanoramaHuntPuzzleMode()

    /** Collect targets only when tapped in [requiredSlotOrder]. */
    data class Sequence(
        val requiredSlotOrder: List<Int> = emptyList(),
    ) : PanoramaHuntPuzzleMode()

    /** Opaque puzzle key for custom validators added later. */
    data class Custom(
        val puzzleKey: String,
    ) : PanoramaHuntPuzzleMode()
}

internal val PanoramaHuntPuzzleMode.isEnabled: Boolean
    get() = when (this) {
        is PanoramaHuntPuzzleMode.Disabled -> false
        is PanoramaHuntPuzzleMode.Sequence -> true
        is PanoramaHuntPuzzleMode.Custom -> true
    }
