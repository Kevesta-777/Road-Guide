package com.example.roadguideapp.goldhunt.panorama.interaction

import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTarget
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetSet
import com.example.roadguideapp.goldhunt.panorama.targets.PanoramaHiddenTargetType
import com.example.roadguideapp.panorama.interaction.PanoramaViewState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PanoramaHuntInteractionLayerTest {
    private val viewState = PanoramaViewState(
        yaw = 200f,
        pitch = 5f,
        fieldOfView = 90f,
        viewportAspect = 1f,
        viewportWidth = 1000,
        viewportHeight = 1000,
    )

    @Test
    fun onTap_centerOnTarget_collectsSingleTarget() {
        val target = targetAt(bearing = 200f, pitch = 5f, slot = 0)
        val layer = PanoramaHuntInteractionLayer(targetSet(target))
        val result = layer.onTap(tapAtCenter())
        assertTrue(result is PanoramaHuntTapResult.HuntComplete)
        assertTrue(layer.isComplete)
    }

    @Test
    fun onTap_miss_returnsMiss() {
        val target = targetAt(bearing = 20f, pitch = -30f, slot = 0)
        val layer = PanoramaHuntInteractionLayer(targetSet(target))
        val result = layer.onTap(tapAtCenter())
        assertTrue(result is PanoramaHuntTapResult.Miss)
    }

    @Test
    fun onTap_multiTarget_collectsIncrementally() {
        val first = targetAt(bearing = 200f, pitch = 5f, slot = 0)
        val second = targetAt(bearing = 20f, pitch = -10f, slot = 1)
        val layer = PanoramaHuntInteractionLayer(targetSet(first, second))

        val firstResult = layer.onTap(tapAtCenter())
        assertTrue(firstResult is PanoramaHuntTapResult.Hit)
        assertEquals(1, (firstResult as PanoramaHuntTapResult.Hit).remainingCount)

        val againAtFirst = layer.onTap(tapAtCenter())
        assertTrue(againAtFirst is PanoramaHuntTapResult.AlreadyCollected)

        val secondResult = layer.onTap(
            PanoramaHuntTapEvent(
                screenX = 500f,
                screenY = 500f,
                viewState = viewState.copy(yaw = 20f, pitch = -10f),
            ),
        )
        assertTrue(secondResult is PanoramaHuntTapResult.HuntComplete)
    }

    @Test
    fun onTap_alreadyCollected_returnsAlreadyCollected() {
        val target = targetAt(bearing = 200f, pitch = 5f, slot = 0)
        val layer = PanoramaHuntInteractionLayer(targetSet(target))
        layer.onTap(tapAtCenter())
        val again = layer.onTap(tapAtCenter())
        assertTrue(again is PanoramaHuntTapResult.AlreadyCollected)
    }

    @Test
    fun preCollectedSlots_restoreSessionProgress() {
        val first = targetAt(bearing = 200f, pitch = 5f, slot = 0)
        val second = targetAt(bearing = 20f, pitch = -10f, slot = 1)
        val layer = PanoramaHuntInteractionLayer(
            targetSet = targetSet(first, second),
            preCollectedSlots = setOf(0),
        )
        assertEquals(1, layer.collectedCount)
        assertEquals(1, layer.remainingCount)
    }

    @Test
    fun onTap_puzzleMode_doesNotAutoCollect() {
        val target = targetAt(bearing = 200f, pitch = 5f, slot = 0)
        val layer = PanoramaHuntInteractionLayer(
            targetSet = targetSet(target),
            puzzleMode = PanoramaHuntPuzzleMode.Custom(puzzleKey = "demo"),
        )
        val result = layer.onTap(tapAtCenter())
        assertTrue(result is PanoramaHuntTapResult.PuzzleTap)
        assertEquals(1, (result as PanoramaHuntTapResult.PuzzleTap).matchedTargets.size)
        assertEquals(0, layer.collectedCount)
    }

    private fun tapAtCenter() = PanoramaHuntTapEvent(
        screenX = 500f,
        screenY = 500f,
        viewState = viewState,
    )

    private fun targetAt(
        bearing: Float,
        pitch: Float,
        slot: Int,
    ): PanoramaHiddenTarget = PanoramaHiddenTarget(
        targetId = "pht:v1:hunt:s$slot",
        huntId = "ph:v1:sp:test:HIDDEN_SYMBOL:pano1:seed1",
        slotIndex = slot,
        bearing = bearing,
        pitch = pitch,
        radius = 12f,
        targetType = PanoramaHiddenTargetType.SYMBOL,
    )

    private fun targetSet(vararg targets: PanoramaHiddenTarget): PanoramaHiddenTargetSet =
        PanoramaHiddenTargetSet(
            huntId = "ph:v1:sp:test:HIDDEN_SYMBOL:pano1:seed1",
            targets = targets.toList(),
        )
}
