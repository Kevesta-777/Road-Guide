package com.example.roadguideapp.goldhunt.panorama.interaction

import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTarget
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetHitTest
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetSet
import com.example.roadguideapp.panorama.interaction.PanoramaTapCoordinateConverter

/**
 * Panorama hunt tap interaction: screen tap → bearing/pitch → target hit detection.
 *
 * Supports single- and multi-target hunts. [puzzleMode] reserves future puzzle flows.
 */
internal class PanoramaHuntInteractionLayer(
    targetSet: PanoramaHiddenTargetSet,
    preCollectedSlots: Set<Int> = emptySet(),
    private val puzzleMode: PanoramaHuntPuzzleMode = PanoramaHuntPuzzleMode.Disabled,
) {
    private val state = PanoramaHuntInteractionState(
        targetSet = targetSet,
        preCollectedSlots = preCollectedSlots,
    )

    val collectedCount: Int
        get() = state.collectedCount

    val remainingCount: Int
        get() = state.remainingCount

    val isComplete: Boolean
        get() = state.isComplete

    fun onTap(event: PanoramaHuntTapEvent): PanoramaHuntTapResult {
        val tapAngles = PanoramaTapCoordinateConverter.screenToAngles(
            screenX = event.screenX,
            screenY = event.screenY,
            viewState = event.viewState,
        )

        if (puzzleMode.isEnabled) {
            return handlePuzzleTap(tapAngles)
        }

        val activeHits = PanoramaHiddenTargetHitTest.findTapHits(
            targets = state.remainingTargets,
            tapBearing = tapAngles.bearing,
            tapPitch = tapAngles.pitch,
        )
        if (activeHits.isEmpty()) {
            val collectedHits = PanoramaHiddenTargetHitTest.findTapHits(
                targets = state.collectedTargets,
                tapBearing = tapAngles.bearing,
                tapPitch = tapAngles.pitch,
            )
            val alreadyCollected = collectedHits.minByOrNull { candidate ->
                PanoramaHiddenTargetHitTest.tapDistance(
                    target = candidate,
                    tapBearing = tapAngles.bearing,
                    tapPitch = tapAngles.pitch,
                )
            }
            if (alreadyCollected != null) {
                return PanoramaHuntTapResult.AlreadyCollected(
                    target = alreadyCollected,
                    tapAngles = tapAngles,
                )
            }
            return PanoramaHuntTapResult.Miss(tapAngles = tapAngles)
        }

        val target = activeHits.minBy { candidate ->
            PanoramaHiddenTargetHitTest.tapDistance(
                target = candidate,
                tapBearing = tapAngles.bearing,
                tapPitch = tapAngles.pitch,
            )
        }
        state.markCollected(target)
        return if (state.isComplete) {
            PanoramaHuntTapResult.HuntComplete(
                lastTarget = target,
                tapAngles = tapAngles,
            )
        } else {
            PanoramaHuntTapResult.Hit(
                target = target,
                tapAngles = tapAngles,
                remainingCount = state.remainingCount,
            )
        }
    }

    private fun handlePuzzleTap(
        tapAngles: com.example.roadguideapp.panorama.interaction.PanoramaTapAngles,
    ): PanoramaHuntTapResult.PuzzleTap {
        val matched = PanoramaHiddenTargetHitTest.findTapHits(
            targets = state.remainingTargets,
            tapBearing = tapAngles.bearing,
            tapPitch = tapAngles.pitch,
        )
        return PanoramaHuntTapResult.PuzzleTap(
            tapAngles = tapAngles,
            puzzleMode = puzzleMode,
            matchedTargets = matched,
        )
    }
}
