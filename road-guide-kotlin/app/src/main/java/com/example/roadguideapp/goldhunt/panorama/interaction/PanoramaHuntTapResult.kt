package com.example.roadguideapp.goldhunt.panorama.interaction

import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTarget
import com.example.roadguideapp.panorama.interaction.PanoramaTapAngles

/**
 * Outcome of a panorama hunt tap.
 */
internal sealed class PanoramaHuntTapResult {
    abstract val tapAngles: PanoramaTapAngles

    data class Miss(
        override val tapAngles: PanoramaTapAngles,
    ) : PanoramaHuntTapResult()

    data class Hit(
        val target: PanoramaHiddenTarget,
        override val tapAngles: PanoramaTapAngles,
        val remainingCount: Int,
    ) : PanoramaHuntTapResult()

    data class AlreadyCollected(
        val target: PanoramaHiddenTarget,
        override val tapAngles: PanoramaTapAngles,
    ) : PanoramaHuntTapResult()

    data class HuntComplete(
        val lastTarget: PanoramaHiddenTarget,
        override val tapAngles: PanoramaTapAngles,
    ) : PanoramaHuntTapResult()

    /** Future puzzle flows consume taps without auto-collecting targets. */
    data class PuzzleTap(
        override val tapAngles: PanoramaTapAngles,
        val puzzleMode: PanoramaHuntPuzzleMode,
        val matchedTargets: List<PanoramaHiddenTarget> = emptyList(),
    ) : PanoramaHuntTapResult()
}
