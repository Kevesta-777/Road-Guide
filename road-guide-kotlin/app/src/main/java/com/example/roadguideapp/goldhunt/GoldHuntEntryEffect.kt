package com.example.roadguideapp.goldhunt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.roadguideapp.goldhunt.overlays.TreasureOverlay
import com.example.roadguideapp.map.MapScreenController
import kotlinx.coroutines.delay

@Composable
internal fun GoldHuntEntryEffect(
    controller: MapScreenController,
    goldHunt: GoldHuntController,
) {
    LaunchedEffect(goldHunt.entryPhase) {
        when (goldHunt.entryPhase) {
            GoldHuntEntryPhase.Zooming -> {
                delay(GoldHuntEntryTransition.ZOOM_DURATION_MS.toLong())
                goldHunt.advanceEntryToRevealing()
            }
            GoldHuntEntryPhase.Revealing -> {
                val duration = GoldHuntEntryTransition.TREASURE_POP_IN_MS.toLong()
                val start = System.currentTimeMillis()
                while (true) {
                    val elapsed = System.currentTimeMillis() - start
                    val t = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                    val scale = GoldHuntEntryTransition.TREASURE_POP_IN_START_SCALE +
                        (1f - GoldHuntEntryTransition.TREASURE_POP_IN_START_SCALE) *
                        GoldHuntEntryTransition.easeOutBack(t)
                    goldHunt.updateTreasurePopInScale(scale)
                    val style = controller.mapRuntime?.second
                    val map = controller.mapLibreMap
                    if (style != null && map != null) {
                        TreasureOverlay.updateIconScale(style, scale)
                    }
                    if (t >= 1f) break
                    delay(16)
                }
                goldHunt.updateTreasurePopInScale(1f)
                controller.mapRuntime?.second?.let { style ->
                    TreasureOverlay.updateIconScale(style, 1f)
                }
                val sparkleRemainder =
                    GoldHuntEntryTransition.SPARKLE_DURATION_MS -
                        GoldHuntEntryTransition.TREASURE_POP_IN_MS
                if (sparkleRemainder > 0) {
                    delay(sparkleRemainder.toLong())
                }
                goldHunt.advanceEntryToComplete()
            }
            else -> Unit
        }
    }
}
