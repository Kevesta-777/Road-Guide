package com.example.roadguideapp.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.roadguideapp.goldhunt.DiscoveryConfig
import com.example.roadguideapp.goldhunt.GoldHuntSessionController

@Composable
internal fun FogOfDiscoveryMapEffect(
    controller: MapScreenController,
    goldHunt: GoldHuntSessionController,
    isGoldHuntActive: Boolean,
    isNavigationActive: Boolean,
) {
    LaunchedEffect(
        isGoldHuntActive,
        goldHunt.loadComplete,
        goldHunt.store.revision,
        controller.mapRuntime,
        controller.mapView,
        controller.mapOverlayCameraTick,
        isNavigationActive,
    ) {
        if (!isGoldHuntActive || !goldHunt.loadComplete) {
            val runtime = controller.mapRuntime
            if (runtime != null) {
                FogOfDiscoveryOverlay.remove(runtime.second)
            }
            return@LaunchedEffect
        }
        val (map, style) = controller.mapRuntime ?: return@LaunchedEffect
        val mv = controller.mapView ?: return@LaunchedEffect
        val bounds = MapViewportBounds.visibleBounds(map, mv) ?: return@LaunchedEffect
        val zoom = map.cameraPosition.zoom.toDouble()
        val opacity = if (isNavigationActive) {
            DiscoveryConfig.FOG_OPACITY_NAVIGATION
        } else {
            DiscoveryConfig.FOG_OPACITY_EXPLORE
        }
        FogOfDiscoveryOverlay.sync(
            style = style,
            store = goldHunt.store,
            viewportBounds = bounds,
            regionBounds = goldHunt.region.bounds,
            mapZoom = zoom,
            enabled = true,
            fogOpacity = opacity,
        )
    }
}
