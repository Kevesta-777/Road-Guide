package com.example.roadguideapp.goldhunt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.roadguideapp.goldhunt.overlays.GoldHuntTreasureMapStyle
import com.example.roadguideapp.goldhunt.overlays.TreasureOverlay
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportLogBuilder
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportNumberLogBuilder
import com.example.roadguideapp.map.MapScreenController
import kotlinx.coroutines.delay

@Composable
internal fun GoldHuntZoomTreasureDisappearEffect(
    context: android.content.Context,
    controller: MapScreenController,
    goldHunt: GoldHuntController,
    map3dEnabled: Boolean,
) {
    val appContext = context.applicationContext
    LaunchedEffect(goldHunt.zoomTreasureDisappearNonce) {
        if (goldHunt.zoomTreasureDisappearNonce <= 0) return@LaunchedEffect
        val density = appContext.resources.displayMetrics.density
        val duration = GoldHuntZoomTreasureRevealTransition.DISAPPEAR_MS.toLong()
        val start = System.currentTimeMillis()
        goldHunt.updateTreasureDisappearProgress(0f)
        while (true) {
            val elapsed = System.currentTimeMillis() - start
            val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            goldHunt.updateTreasureDisappearProgress(progress)
            val map = controller.mapLibreMap
            val style = controller.mapRuntime?.second
            if (style != null && map != null) {
                val display3d = GoldHuntTreasureMapStyle.isDisplay3d(
                    map3dEnabled = map3dEnabled,
                    cameraTiltDeg = map.cameraPosition.tilt,
                )
                TreasureOverlay.applyDisappearVisuals(style, progress, display3d, density)
            }
            if (progress >= 1f) break
            delay(16)
        }
        val map = controller.mapLibreMap
        val style = controller.mapRuntime?.second
        val zoom = map?.cameraPosition?.zoom?.toDouble() ?: GoldHuntConfig.TREASURE_DISPLAY_MIN_ZOOM
        style?.let { TreasureOverlay.remove(it) }
        goldHunt.updateTreasureValueLog(TreasureViewportLogBuilder.fromTreasures(emptyList(), zoom))
        goldHunt.updateTreasureNumberLog(TreasureViewportNumberLogBuilder.fromTreasures(emptyList(), zoom))
        goldHunt.completeTreasureDisappear()
    }
}
