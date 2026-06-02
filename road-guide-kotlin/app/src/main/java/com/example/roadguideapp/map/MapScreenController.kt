package com.example.roadguideapp.map

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * Business logic and mutable UI state for the map screen, kept out of [MapLibreMbTilesMap].
 */
@Stable
internal class MapScreenController(
    private val context: Context,
    val sheetStack: AppleMapsSheetStackState,
    private val scope: CoroutineScope,
) {
    private companion object {
        const val TAG = "MapScreenController"
    }
    var mapLibreMap: MapLibreMap? = null
    var mapView: MapView? = null
    var mapRuntime: Pair<MapLibreMap, Style>? = null

    var mapStyleVariant by mutableStateOf(MapStylePreferences.read(context))
    var styleReloadToken by mutableIntStateOf(0)
    var resolvedStyleJson by mutableStateOf<String?>(null)
    var styleFetchError by mutableStateOf<String?>(null)
    /** How the active map style sources tiles (see [ResolvedMapStyle.Mode]). */
    var mapStyleMode by mutableStateOf(ResolvedMapStyle.Mode.Online)

    /** True when only bundled PMTiles + local style are used (no cached tileserver detail). */
    val isOfflineMapMode: Boolean
        get() = mapStyleMode == ResolvedMapStyle.Mode.BundledOffline
    var styleLoadPassComplete by mutableStateOf(false)

    var selectedPlace by mutableStateOf<MapPlaceDetail?>(null)
    var lastMapPlacePick by mutableStateOf<MapPlacePick?>(null)
    var searchQuery by mutableStateOf("")
    var searchSuggestions by mutableStateOf<List<PeliasSearchResult>>(emptyList())
    var searchLoading by mutableStateOf(false)
    var searchError by mutableStateOf<String?>(null)
    var searchMarkerLocation by mutableStateOf<LatLng?>(null)
    var isSearchActive by mutableStateOf(false)

    var activeNearbyCategory by mutableStateOf<AppleNearbyShortcut?>(null)
    var nearbySearchContext by mutableStateOf<NearbySearchContext>(NearbySearchContext.MapCenter)
    var activeRouteGeometry by mutableStateOf<List<LatLng>?>(null)
    var nearbyMapResults by mutableStateOf<List<PeliasSearchResult>>(emptyList())
    var nearbyMapPicks by mutableStateOf<List<MapPlacePick>>(emptyList())
    var nearbyBrowseLoading by mutableStateOf(false)
    var nearbyBrowseError by mutableStateOf<String?>(null)
    var nearbyFilterState by mutableStateOf(NearbyResultsFilter.State())

    var showLookAround by mutableStateOf(false)
    var mapOverlayCameraTick by mutableIntStateOf(0)

    val filteredNearby: NearbyResultsFilter.Result
        get() = NearbyResultsFilter.apply(nearbyMapResults, nearbyMapPicks, nearbyFilterState)

    fun expandHomeSheetToLarge() {
        sheetStack.updateAllSyncedSnaps(AppleSheetSnap.Large)
    }

    fun collapseHomeSheetToPeek() {
        sheetStack.updateAllSyncedSnaps(AppleSheetSnap.Peek)
    }

    fun exitNearbyBrowse() {
        activeNearbyCategory = null
        nearbySearchContext = NearbySearchContext.MapCenter
        nearbyMapResults = emptyList()
        nearbyMapPicks = emptyList()
        nearbyBrowseLoading = false
        nearbyBrowseError = null
        nearbyFilterState = NearbyResultsFilter.State()
        if (!isSearchActive) {
            searchQuery = ""
        }
        mapRuntime?.second?.let { MapPoiSelectionController.clearNearbyHighlights(it) }
        if (!sheetStack.hasOverlay) {
            collapseHomeSheetToPeek()
        }
    }

    fun syncNearbyMapHighlights() {
        mapRuntime?.second?.let { MapPoiSelectionController.clearNearbyCategoryMapMarkers(it) }
        mapOverlayCameraTick++
    }

    fun focusPlaceOnMap(place: MapPlaceDetail) {
        val map = mapLibreMap ?: return
        val style = mapRuntime?.second
        selectedPlace = place
        lastMapPlacePick = null
        style?.let {
            MapPoiSelectionController.applySearchPlace(it, place, context.resources.displayMetrics.density)
        }
        map.easeCamera(
            CameraUpdateFactory.newLatLngZoom(
                place.latLng,
                MapConstants.SEARCH_RESULT_ZOOM.coerceIn(MapConstants.MIN_ZOOM, MapConstants.MAX_ZOOM),
            ),
            MapConstants.ZOOM_ANIMATION_MS,
        )
        mapOverlayCameraTick++
    }

    fun focusOwnedBusinessPoi(poi: BusinessPoiClient.MyBusinessPoi) {
        val lat = poi.latitude ?: return
        val lng = poi.longitude ?: return
        val place = MapPlaceDetail(
            id = poi.externalRef?.removePrefix("feature:")?.takeIf { it.isNotBlank() } ?: poi.id,
            name = poi.name,
            category = "",
            locality = "",
            hoursSummary = "",
            isOpenNow = false,
            website = null,
            phone = null,
            address = poi.address,
            latLng = LatLng(lat, lng),
            backendPoiId = poi.id,
            storedExternalRef = poi.externalRef,
            isClaimEligible = true,
        )
        selectPlaceLikeMapTap(null, place)
    }

    private fun selectPlaceLikeMapTap(pick: MapPlacePick?, place: MapPlaceDetail) {
        val map = mapLibreMap ?: return
        val style = mapRuntime?.second
        if (activeNearbyCategory != null) {
            exitNearbyBrowse()
        }
        searchQuery = place.name
        searchSuggestions = emptyList()
        searchLoading = false
        searchError = null
        isSearchActive = false
        searchMarkerLocation = null
        lastMapPlacePick = pick
        selectedPlace = place
        style?.let {
            if (pick != null) {
                MapPoiSelectionController.apply(it, pick, context.resources.displayMetrics.density)
            } else {
                MapPoiSelectionController.applySearchPlace(it, place, context.resources.displayMetrics.density)
            }
        }
        sheetStack.push(AppleMapSheet.PlaceDetail(place), targetSnap = AppleSheetSnap.Mid)
        map.easeCamera(
            CameraUpdateFactory.newLatLngZoom(
                place.latLng,
                MapConstants.SEARCH_RESULT_ZOOM.coerceIn(MapConstants.MIN_ZOOM, MapConstants.MAX_ZOOM),
            ),
            MapConstants.ZOOM_ANIMATION_MS,
        )
        resolvePlaceAddressAsync(place)
    }

    private fun resolvePlaceAddressAsync(place: MapPlaceDetail) {
        if (!PlaceAddressResolver.needsReverseGeocode(place.address)) return
        scope.launch {
            val enriched = withContext(Dispatchers.IO) {
                PlaceAddressResolver.enrichPlace(context, place)
            }
            if (enriched.address == place.address && enriched.locality == place.locality) return@launch
            val merged = enriched.copy(isClaimEligible = place.isClaimEligible || enriched.isClaimEligible)
            if (selectedPlace?.id == place.id) {
                selectedPlace = merged
            }
            sheetStack.updatePlaceDetail(place.id, merged)
        }
    }

    /**
     * After zooming to a Pelias hit, re-query vector tiles for the matching POI so we can show the
     * enlarged template icon + name (same as a direct map tap).
     */
    private fun scheduleResolveVectorPickAfterZoom(latLng: LatLng, expectedPlaceId: String) {
        mapView?.postDelayed({
            val map = mapLibreMap ?: return@postDelayed
            val style = mapRuntime?.second ?: return@postDelayed
            if (mapRuntime?.first !== map) return@postDelayed
            if (selectedPlace?.id != expectedPlaceId) return@postDelayed
            val resolved = MapPoiSelectionController.resolvePickNear(context, style, map, latLng)
                ?: return@postDelayed
            lastMapPlacePick = resolved
            val priorClaimEligible = selectedPlace?.isClaimEligible == true
            val mergedDetail = resolved.detail.copy(
                isClaimEligible = resolved.detail.isClaimEligible || priorClaimEligible,
            )
            selectedPlace = mergedDetail
            sheetStack.updatePlaceDetail(expectedPlaceId, mergedDetail)
            MapPoiSelectionController.apply(
                style,
                resolved,
                context.resources.displayMetrics.density,
            )
            resolvePlaceAddressAsync(resolved.detail)
            mapOverlayCameraTick++
        }, MapConstants.ZOOM_ANIMATION_MS.toLong() + 80L)
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        if (!isSearchActive) return
        if (query.isBlank()) {
            collapseHomeSheetToPeek()
        } else {
            expandHomeSheetToLarge()
        }
    }

    fun enterSearchMode() {
        if (!isSearchActive) isSearchActive = true
        // Keep the home sheet at peek while typing (Apple Maps–style).
    }

    fun exitSearchMode() {
        searchQuery = ""
        searchSuggestions = emptyList()
        searchLoading = false
        searchError = null
        searchMarkerLocation = null
        isSearchActive = false
        exitNearbyBrowse()
        collapseHomeSheetToPeek()
    }

    fun deactivateSearchMode() {
        searchSuggestions = emptyList()
        searchLoading = false
        searchError = null
        isSearchActive = false
        collapseHomeSheetToPeek()
    }

    fun clearSearchQueryOnly() {
        searchQuery = ""
        deactivateSearchMode()
    }

    fun focusSearchResult(result: PeliasSearchResult) {
        val map = mapLibreMap ?: return
        val style = mapRuntime?.second ?: return

        searchQuery = result.label
        searchSuggestions = emptyList()
        searchLoading = false
        searchError = null
        isSearchActive = false
        exitNearbyBrowse()
        searchMarkerLocation = null

        val pick = MapPoiSelectionController.resolvePickNear(context, style, map, result.latLng)
        if (pick != null) {
            focusMapPlacePick(pick)
            return
        }

        val place = result.toMapPlaceDetail(context)
        lastMapPlacePick = null
        selectedPlace = place
        MapPoiSelectionController.clear(style)
        MapPoiSelectionController.applySearchPlace(style, place, context.resources.displayMetrics.density)
        sheetStack.push(AppleMapSheet.PlaceDetail(place), targetSnap = AppleSheetSnap.Mid)
        map.easeCamera(
            CameraUpdateFactory.newLatLngZoom(
                result.latLng,
                MapConstants.SEARCH_RESULT_ZOOM.coerceIn(MapConstants.MIN_ZOOM, MapConstants.MAX_ZOOM),
            ),
            MapConstants.ZOOM_ANIMATION_MS,
        )
        scheduleResolveVectorPickAfterZoom(result.latLng, place.id)
        resolvePlaceAddressAsync(place)
    }

    fun focusMapPlacePick(pick: MapPlacePick) {
        selectPlaceLikeMapTap(pick, pick.detail)
    }

    fun openNearbyPlace(result: PeliasSearchResult) {
        val map = mapLibreMap ?: return
        val style = mapRuntime?.second ?: return
        val pick = resolveNearbyPick(result, style, map)
        val place = pick?.detail ?: result.toMapPlaceDetail(context)
        selectPlaceLikeMapTap(pick, place)
        if (pick == null) {
            scheduleResolveVectorPickAfterZoom(result.latLng, place.id)
        }
    }

    private fun resolveNearbyPick(
        result: PeliasSearchResult,
        style: Style,
        map: MapLibreMap,
    ): MapPlacePick? {
        filteredNearby.visiblePicks.find { it.detail.id == result.gid }?.let { return it }
        nearbyMapPicks.find { it.detail.id == result.gid }?.let { return it }
        return MapPoiSelectionController.resolvePickNear(context, style, map, result.latLng)
    }

    fun onMapPlaceTapped(latLng: LatLng) {
        val map = mapLibreMap ?: return
        val style = mapRuntime?.second ?: return
        val screenPoint = map.projection.toScreenLocation(latLng)

        if (activeNearbyCategory != null) {
            val directPick = MapPoiSelectionController.resolvePick(context, style, map, screenPoint, latLng)
            if (directPick != null) {
                selectPlaceLikeMapTap(directPick, directPick.detail)
                return
            }
            val nearest = NearbyCategorySearchEngine.findNearestResult(
                results = filteredNearby.visibleResults,
                tap = latLng,
            )
            if (nearest != null) {
                openNearbyPlace(nearest)
                return
            }
        }
        val pick = MapPoiSelectionController.resolvePick(context, style, map, screenPoint, latLng)
        val place = pick?.detail
            ?: MapPlaceDetail.fromRenderedFeatures(
                context,
                map.queryRenderedFeatures(screenPoint),
                latLng,
            )?.copy(isClaimEligible = false)
            ?: return

        selectPlaceLikeMapTap(pick?.takeIf { it.detail == place }, place)
    }

    fun onMapStyleVariantSelected(variant: MapStyleVariant) {
        if (variant == mapStyleVariant) return
        MapStylePreferences.write(context, variant)
        mapStyleVariant = variant
        styleReloadToken++
    }

    fun lookAroundTarget(): LookAroundTarget {
        val place = selectedPlace
        if (place != null) {
            return LookAroundTarget(
                latLng = place.latLng,
                title = place.name,
                subtitle = place.address.ifBlank { place.locality },
                externalRef = place.businessPoiExternalRef(),
            )
        }
        val center = mapLibreMap?.cameraPosition?.target
        val mapCenterTitle = context.getString(com.example.roadguideapp.R.string.apple_look_around_map_center)
        return LookAroundTarget(
            latLng = center,
            title = mapCenterTitle,
            subtitle = center?.let { "${it.latitude}, ${it.longitude}" }.orEmpty(),
            externalRef = lookAroundExternalRef(center, mapCenterTitle),
        )
    }

    fun resolveDefaultNearbySearchContext(): NearbySearchContext {
        val mapCenter = mapLibreMap?.cameraPosition?.target
        return NearbySearchContext.resolveDefault(
            mapCenter = mapCenter ?: LatLng(0.0, 0.0),
            selectedPlace = selectedPlace ?: placeDetailForNearbyScope(),
            routeGeometry = activeRouteGeometry,
            hasActiveDirections = sheetStack.activeDirections() != null,
        )
    }

    fun buildNearbyScopeOptions(): List<NearbyScopeOption> {
        val placeContext = resolvePlaceContextForNearby()
        val routeGeometry = activeRouteGeometry
        val routeContext =
            if (sheetStack.activeDirections() != null && routeGeometry != null && routeGeometry.size >= 2) {
                NearbySearchContext.AlongRoute(routeGeometry)
            } else {
                null
            }
        return nearbyScopeOptions(
            mapCenterLabel = context.getString(com.example.roadguideapp.R.string.apple_nearby_scope_map_center),
            placeFallbackLabel = context.getString(com.example.roadguideapp.R.string.apple_nearby_scope_place),
            routeLabel = context.getString(com.example.roadguideapp.R.string.apple_nearby_scope_route),
            placeContext = placeContext,
            routeContext = routeContext,
        )
    }

    private fun resolvePlaceContextForNearby(): NearbySearchContext.NearPlace? {
        (nearbySearchContext as? NearbySearchContext.NearPlace)?.let { return it }
        selectedPlace?.let { return NearbySearchContext.NearPlace(it.latLng, it.name) }
        placeDetailForNearbyScope()?.let { return NearbySearchContext.NearPlace(it.latLng, it.name) }
        return null
    }

    private fun placeDetailForNearbyScope(): MapPlaceDetail? {
        val top = sheetStack.topLayer?.sheet
        if (top is AppleMapSheet.PlaceDetail) return top.place
        val directions = sheetStack.activeDirections() ?: return null
        return directions.stops.lastOrNull() ?: directions.origin
    }

    fun applyNearbySearchContext(context: NearbySearchContext) {
        if (nearbySearchContext == context) return
        nearbySearchContext = context
        refreshNearbyBrowseIfActive()
    }

    fun startNearbyCategoryBrowse(
        shortcut: AppleNearbyShortcut,
        searchContext: NearbySearchContext? = null,
    ) {
        val resolvedContext = searchContext ?: resolveDefaultNearbySearchContext()
        nearbySearchContext = resolvedContext
        scope.launch {
            runNearbyCategorySearch(shortcut, resolvedContext, resetUi = true)
        }
    }

    internal fun refreshNearbyBrowseIfActive() {
        val category = activeNearbyCategory ?: return
        scope.launch {
            runNearbyCategorySearch(category, nearbySearchContext, resetUi = false)
        }
    }

    private suspend fun runNearbyCategorySearch(
        shortcut: AppleNearbyShortcut,
        searchContext: NearbySearchContext,
        resetUi: Boolean,
    ) {
        val map = mapLibreMap ?: return
        val mapView = mapView ?: return
        val style = mapRuntime?.second

        if (resetUi) {
            isSearchActive = false
            searchSuggestions = emptyList()
            searchLoading = false
            searchError = null
            activeNearbyCategory = shortcut
            searchQuery = context.getString(shortcut.labelRes)
            nearbyFilterState = NearbyResultsFilter.State()
            searchMarkerLocation = null
            selectedPlace = null
            lastMapPlacePick = null
            if (style != null) {
                MapPoiSelectionController.clearNearbyHighlights(style)
                MapPoiSelectionController.clear(style)
            }
            sheetStack.clearToHome()
            expandHomeSheetToLarge()
        }

        nearbyBrowseLoading = true
        nearbyBrowseError = null
        if (resetUi) {
            nearbyMapResults = emptyList()
        }

        try {
            val outcome = if (style != null) {
                NearbyCategorySearchEngine.search(
                    context = context,
                    style = style,
                    map = map,
                    mapView = mapView,
                    category = shortcut,
                    searchContext = searchContext,
                )
            } else {
                NearbyCategorySearchEngine.SearchOutcome(emptyList(), emptyMap())
            }

            nearbyMapResults = outcome.ranked.map { it.result }
            nearbyMapPicks = outcome.picksByGid.values.toList()
            nearbyBrowseError = outcome.errorMessage
            syncNearbyMapHighlights()
            if (outcome.ranked.isNotEmpty()) {
                nearbyBrowseError = null
                if (resetUi) collapseHomeSheetToPeek()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Nearby category browse failed", e)
            nearbyMapResults = emptyList()
            nearbyMapPicks = emptyList()
            nearbyBrowseError = e.message ?: "Nearby search failed"
            style?.let { MapPoiSelectionController.clearNearbyHighlights(it) }
        } finally {
            nearbyBrowseLoading = false
        }
    }

    fun fitCameraToNearbyResults(
        map: MapLibreMap,
        mapView: MapView,
        results: List<PeliasSearchResult>,
        cameraEdgePaddingPx: Int,
        bottomPaddingPx: Int,
    ) {
        val resultBounds = NearbyCategorySearch.boundsForResults(results)
        val contextBounds = when (val ctx = nearbySearchContext) {
            is NearbySearchContext.AlongRoute ->
                PolylineDistance.boundsWithBuffer(ctx.polyline, ctx.radiusMeters)
            is NearbySearchContext.NearPlace ->
                PolylineDistance.boundsWithBuffer(
                    listOf(ctx.location),
                    NearbySearchContext.NEAR_PLACE_BOUNDS_PADDING_METERS,
                )
            NearbySearchContext.MapCenter -> null
        }
        val bounds = when {
            resultBounds != null && contextBounds != null -> {
                val builder = LatLngBounds.Builder()
                includeBoundsCorners(builder, resultBounds)
                includeBoundsCorners(builder, contextBounds)
                builder.build()
            }
            resultBounds != null -> resultBounds
            contextBounds != null -> contextBounds
            else -> return
        }
        mapOverlayCameraTick++
        MapViewportFit.animateToBounds(
            map = map,
            mapView = mapView,
            bounds = bounds,
            paddingLeft = cameraEdgePaddingPx,
            paddingTop = cameraEdgePaddingPx,
            paddingRight = cameraEdgePaddingPx,
            paddingBottom = bottomPaddingPx + cameraEdgePaddingPx,
            durationMs = MapConstants.ZOOM_ANIMATION_MS,
        )
    }

    fun updateNearbyFilter(state: NearbyResultsFilter.State) {
        nearbyFilterState = state
        syncNearbyMapHighlights()
    }

    private fun includeBoundsCorners(builder: LatLngBounds.Builder, bounds: LatLngBounds) {
        builder.include(LatLng(bounds.latitudeSouth, bounds.longitudeWest))
        builder.include(LatLng(bounds.latitudeNorth, bounds.longitudeEast))
    }
}

internal data class LookAroundTarget(
    val latLng: LatLng?,
    val title: String,
    val subtitle: String,
    val externalRef: String? = null,
)
