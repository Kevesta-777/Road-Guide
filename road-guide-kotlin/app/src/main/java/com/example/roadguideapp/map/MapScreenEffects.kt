package com.example.roadguideapp.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.maplibre.android.maps.Style

@Composable
internal fun MapStyleLoadEffect(
    context: Context,
    controller: MapScreenController,
) {
    LaunchedEffect(controller.styleReloadToken) {
        controller.styleFetchError = null
        controller.styleLoadPassComplete = false
        val variant = controller.mapStyleVariant
        coroutineScope {
            val styleDeferred = async {
                runCatching {
                    TileserverStyleLoader.loadResolvedStyleJson(
                        context.applicationContext,
                        MapServerConfig.mapApiBaseUrl,
                        variant.stylePath,
                    )
                }
            }
            styleDeferred.await().fold(
                onSuccess = { resolved ->
                    controller.resolvedStyleJson = resolved.json
                    controller.mapStyleMode = resolved.mode
                    controller.styleFetchError = null
                    MapDataTierLogger.logStyleMode(resolved.mode)
                },
                onFailure = { e ->
                    if (variant != MapStyleVariant.Standard) {
                        controller.styleFetchError =
                            "${context.getString(com.example.roadguideapp.R.string.apple_map_style_unavailable)}: ${variant.name}\n${e.message}"
                        controller.mapStyleVariant = MapStyleVariant.Standard
                        MapStylePreferences.write(context, MapStyleVariant.Standard)
                        controller.styleReloadToken++
                    } else {
                        controller.resolvedStyleJson = null
                        controller.mapStyleMode = ResolvedMapStyle.Mode.Online
                        controller.styleFetchError =
                            "Could not load map style.\n${e.message}"
                    }
                },
            )
        }
        controller.styleLoadPassComplete = true
    }
}

@Composable
internal fun MapSearchAutocompleteEffect(
    controller: MapScreenController,
) {
    LaunchedEffect(controller.searchQuery, controller.mapLibreMap, controller.isSearchActive, controller.activeNearbyCategory) {
        if (!controller.isSearchActive || controller.activeNearbyCategory != null) return@LaunchedEffect
        val query = controller.searchQuery.trim()
        if (query.isEmpty()) {
            controller.searchSuggestions = emptyList()
            controller.searchLoading = false
            controller.searchError = null
            return@LaunchedEffect
        }
        controller.searchLoading = true
        controller.searchError = null
        delay(MapConstants.SEARCH_DEBOUNCE_MS)
        if (controller.searchQuery.trim() != query) return@LaunchedEffect
        val map = controller.mapLibreMap
        val focus = map?.cameraPosition?.target
        val zoom = map?.cameraPosition?.zoom
        when (val response = PeliasSearchClient.autocomplete(query, focus = focus, mapZoom = zoom)) {
            is PeliasSearchResponse.Success -> {
                if (controller.searchQuery.trim() == query) {
                    controller.searchSuggestions = response.results
                    controller.searchError = null
                    controller.searchLoading = false
                }
            }
            is PeliasSearchResponse.Failure -> {
                if (controller.searchQuery.trim() == query) {
                    controller.searchSuggestions = emptyList()
                    controller.searchError = response.message
                    controller.searchLoading = false
                }
            }
        }
    }
}

@Composable
internal fun MapNearbyCameraFitEffect(
    controller: MapScreenController,
    sheetSnap: AppleSheetSnap,
    bottomChromePadding: Dp,
    cameraEdgePaddingPx: Int,
) {
    val density = LocalDensity.current
    LaunchedEffect(
        controller.activeNearbyCategory,
        controller.nearbyMapResults,
        controller.nearbyFilterState,
        controller.mapRuntime,
        sheetSnap,
        bottomChromePadding,
        cameraEdgePaddingPx,
    ) {
        if (controller.activeNearbyCategory == null) return@LaunchedEffect
        if (sheetSnap != AppleSheetSnap.Peek) return@LaunchedEffect
        val results = controller.filteredNearby.visibleResults
        if (results.isEmpty()) return@LaunchedEffect
        val map = controller.mapLibreMap ?: return@LaunchedEffect
        val mapView = controller.mapView ?: return@LaunchedEffect
        val bottomPaddingPx = with(density) { bottomChromePadding.roundToPx() }
        controller.fitCameraToNearbyResults(
            map = map,
            mapView = mapView,
            results = results,
            cameraEdgePaddingPx = cameraEdgePaddingPx,
            bottomPaddingPx = bottomPaddingPx,
        )
    }
}

@Composable
internal fun MapNearbyHighlightsEffect(controller: MapScreenController) {
    LaunchedEffect(
        controller.activeNearbyCategory,
        controller.nearbyMapResults,
        controller.nearbyFilterState,
        controller.mapRuntime,
    ) {
        if (controller.activeNearbyCategory != null) {
            controller.syncNearbyMapHighlights()
        }
    }
}

@Composable
internal fun MapPlaceSelectionOverlayEffect(
    context: Context,
    controller: MapScreenController,
) {
    LaunchedEffect(controller.selectedPlace, controller.mapRuntime, controller.lastMapPlacePick) {
        val style = controller.mapRuntime?.second ?: return@LaunchedEffect
        val place = controller.selectedPlace
        if (place == null) {
            controller.lastMapPlacePick = null
            if (controller.activeNearbyCategory == null) {
                MapPoiSelectionController.clear(style)
            }
            return@LaunchedEffect
        }
        val pick = controller.lastMapPlacePick?.takeIf { it.detail == place }
        val density = context.resources.displayMetrics.density
        if (pick != null) {
            MapPoiSelectionController.apply(style, pick, density)
        } else {
            MapPoiSelectionController.applySearchPlace(style, place, density)
        }
    }
}
