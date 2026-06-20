package com.example.roadguideapp.goldhunt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.roadguideapp.goldhunt.overlays.GoldHuntTreasureMapStyle
import com.example.roadguideapp.goldhunt.overlays.TreasureOverlay
import com.example.roadguideapp.map.MapScreenController
import kotlinx.coroutines.delay

@Composable
internal fun GoldHuntZoomTreasureRevealEffect(
    controller: MapScreenController,
    goldHunt: GoldHuntController,
    map3dEnabled: Boolean,
) {
    val context = LocalContext.current
    LaunchedEffect(goldHunt.zoomTreasureRevealNonce) {
        if (goldHunt.zoomTreasureRevealNonce <= 0) return@LaunchedEffect
        val density = context.resources.displayMetrics.density
        val duration = GoldHuntZoomTreasureRevealTransition.POP_IN_MS.toLong()
        val start = System.currentTimeMillis()
        goldHunt.updateTreasureAppearProgress(0f)
        while (true) {
            val elapsed = System.currentTimeMillis() - start
            val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            goldHunt.updateTreasureAppearProgress(progress)
            val map = controller.mapLibreMap
            val style = controller.mapRuntime?.second
            if (style != null && map != null) {
                val display3d = GoldHuntTreasureMapStyle.isDisplay3d(
                    map3dEnabled = map3dEnabled,
                    cameraTiltDeg = map.cameraPosition.tilt,
                )
                TreasureOverlay.applyAppearVisuals(style, progress, display3d, density)
            }
            if (progress >= 1f) break
            delay(16)
        }
        goldHunt.updateTreasureAppearProgress(1f)
        val style = controller.mapRuntime?.second
        val map = controller.mapLibreMap
        if (style != null && map != null) {
            val display3d = GoldHuntTreasureMapStyle.isDisplay3d(
                map3dEnabled = map3dEnabled,
                cameraTiltDeg = map.cameraPosition.tilt,
            )
            TreasureOverlay.applyAppearVisuals(style, 1f, display3d, density)
        }
    }
}
