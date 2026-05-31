package com.example.roadguideapp.map

import android.Manifest
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.roadguideapp.R
import com.example.roadguideapp.auth.AuthDestination
import com.example.roadguideapp.auth.AuthOverlayHost
import com.example.roadguideapp.auth.OfflineAuthStore
import com.example.roadguideapp.auth.OfflineFriendsStore
import com.example.roadguideapp.auth.UserProfileSheetContent
import com.example.roadguideapp.auth.identifierAbbreviation
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

/**
 * Full-screen MapLibre map with Apple Maps–inspired persistent bottom sheet and map chrome.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
fun MapLibreMbTilesMap(
    lifecycle: Lifecycle,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var authDestination by remember { mutableStateOf<AuthDestination?>(null) }
    var authRevision by remember { mutableIntStateOf(0) }
    val isLoggedIn = remember(authRevision) { OfflineAuthStore.isSessionActive(context) }
    val profileAbbreviation = remember(authRevision, isLoggedIn) {
        if (isLoggedIn) {
            OfflineAuthStore.sessionIdentifier(context)?.let { identifierAbbreviation(it) }
        } else {
            null
        }
    }
    val density = LocalDensity.current
    val cameraEdgePaddingPx = remember(density) {
        with(density) { MapConstants.CAMERA_EDGE_PADDING_DP.dp.roundToPx() }
    }
    val scaleRulerTargetWidthPx = remember(density) {
        with(density) { MapConstants.SCALE_RULER_TARGET_WIDTH_DP.dp.toPx() }
    }

    val sheetStack = remember { AppleMapsSheetStackState() }
    val controller = remember(sheetStack) {
        MapScreenController(context, sheetStack, coroutineScope)
    }
    val onProfileClick: () -> Unit = {
        if (OfflineAuthStore.isSessionActive(context)) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    OfflineAuthStore.refreshUserFromBackend(context)
                }
                authRevision++
                sheetStack.push(AppleMapSheet.UserProfile(), AppleSheetSnap.Large)
            }
        } else {
            authDestination = AuthDestination.SignIn
        }
    }
    val hazeState = remember { HazeState() }
    val layerHeightsDp = remember { mutableStateMapOf<Int, androidx.compose.ui.unit.Dp>() }
    var sheetBlurRadiusPx by remember { mutableFloatStateOf(0f) }
    var syncedSheetHeightDp by remember {
        mutableStateOf(AppleMapsUiTokens.SheetPeekMinDp)
    }
    val syncedStackSnap = sheetStack.currentSyncedSnap()
    val effectiveSheetHeightDp = syncedSheetHeightDp
    val bottomChromePadding = effectiveSheetHeightDp + 12.dp
    val showBottomMapChrome = syncedStackSnap != AppleSheetSnap.Large
    val showTopRightChrome = syncedStackSnap != AppleSheetSnap.Large

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    var mapReady by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var areamapInfo by remember {
        mutableStateOf(TileserverAreamapInfoLoader.Result(null, null, null))
    }
    val configFitBounds = remember { MapServerConfig.initialMapFitBounds() }
    val locationFallbackLatLng = remember(configFitBounds, areamapInfo) {
        configFitBounds?.center ?: areamapInfo.center
    }

    var appearanceClockTick by remember { mutableIntStateOf(0) }
    var isDarkAppearance by remember {
        mutableStateOf(MapTimeOfDay.fromSystemLocalClock().isDarkAppearance())
    }
    val timeOfDay = remember(isDarkAppearance, appearanceClockTick) {
        resolveMapTimeOfDay(
            clock = MapTimeOfDay.fromSystemLocalClock(),
            isDarkAppearance = isDarkAppearance,
        )
    }
    val sheetTheme = remember(timeOfDay) { appleMapsSheetTheme(timeOfDay) }

    var is3d by remember { mutableStateOf(false) }
    val is3dState = rememberUpdatedState(is3d)
    var mapBearingDegrees by remember { mutableFloatStateOf(0f) }
    var mapScaleRuler by remember { mutableStateOf<MapScaleRulerState?>(null) }

    var showChooseMapSheet by remember { mutableStateOf(false) }
    var showMyLocationSheet by remember { mutableStateOf(false) }
    var claimGuidance by remember { mutableStateOf<BusinessClaimClient.RegistrationGuidance?>(null) }
    var claimPoiId by remember { mutableStateOf<String?>(null) }
    var businessEditPoiId by remember { mutableStateOf<String?>(null) }
    var claimRequestInFlight by remember { mutableStateOf(false) }
    val placeClaimButtonModes = remember { mutableStateMapOf<String, PlaceClaimButtonMode>() }
    val resolvedBackendPoiIds = remember { mutableStateMapOf<String, String>() }

    val activeDirections = sheetStack.activeDirections()
    val activeDirectionsState = rememberUpdatedState(activeDirections)
    val activeNearbyCategoryState = rememberUpdatedState(controller.activeNearbyCategory)
    val bottomChromePaddingState = rememberUpdatedState(bottomChromePadding)

    val filteredNearby = controller.filteredNearby
    val nearbyOverlayResultsState = rememberUpdatedState(
        if (controller.activeNearbyCategory != null) {
            filteredNearby.visibleResults
        } else {
            emptyList()
        },
    )
    val nearbyPickHoursByGid = remember(filteredNearby.visiblePicks) {
        filteredNearby.visiblePicks.associate { it.detail.id to it.detail.hoursSummary }
    }

    var hasLocationPermission by remember {
        mutableStateOf(MapAndroidLocation.hasCoarseLocationPermission(context.applicationContext))
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        hasLocationPermission =
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            navigateToBestAvailableLocation(
                context.applicationContext,
                controller.mapLibreMap,
                locationFallbackLatLng,
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(MapConstants.APPEARANCE_REFRESH_INTERVAL_MS)
            appearanceClockTick++
        }
    }

    LaunchedEffect(Unit) {
        areamapInfo = TileserverAreamapInfoLoader.load(context.applicationContext)
    }

    MapStyleLoadEffect(context, controller)

    LaunchedEffect(
        controller.mapLibreMap,
        controller.resolvedStyleJson,
        controller.styleFetchError,
        controller.styleLoadPassComplete,
        areamapInfo,
        configFitBounds,
        controller.mapStyleVariant,
    ) {
        val map = controller.mapLibreMap ?: return@LaunchedEffect
        if (!controller.styleLoadPassComplete) return@LaunchedEffect
        val json = controller.resolvedStyleJson ?: return@LaunchedEffect
        if (controller.styleFetchError != null) return@LaunchedEffect
        val fromStyle = runCatching {
            MapInitialViewportResolver.fromResolvedStyle(json)
        }.getOrElse {
            MapInitialViewportResolver.Viewport()
        }
        val viewport = MapInitialViewportResolver.merge(
            MapInitialViewportResolver.fromGradleBounds(configFitBounds),
            MapInitialViewportResolver.fromAreamap(areamapInfo),
            fromStyle,
        )
        loadError = null
        mapReady = false
        controller.mapRuntime = null
        mapScaleRuler = null
        MapPoiSelectionController.discardActive()

        val fitBounds = viewport?.fitBounds ?: configFitBounds
        val fallbackCenter = viewport?.center
        val fallbackZoom = viewport?.zoom

        val mapView = mapViewRef.value
        val applyStyle = {
            map.setStyle(Style.Builder().fromJson(json)) { style ->
                val mode = resolveMapTimeOfDay(isDarkAppearance = isDarkAppearance)
                runCatching { BuildingExtrusion.prepareStyle(style) }
                MapStyleRuntime.applyTimeOfDay(style, mode)
                fitBounds?.let { bounds ->
                    runCatching { map.setLatLngBoundsForCameraTarget(bounds) }
                }
                mapViewRef.value?.let { mv ->
                    MapViewportFit.scheduleInitialCamera(
                        map = map,
                        mapView = mv,
                        fitBounds = fitBounds,
                        fallbackCenter = fallbackCenter,
                        fallbackZoom = fallbackZoom,
                        paddingPx = cameraEdgePaddingPx,
                    )
                }
                controller.mapRuntime = map to style
                mapReady = true
                loadError = null
                MapDataTierLogger.logStyleMode(controller.mapStyleMode)
                MapDataTierLogger.logZoomTierIfChanged(
                    map.cameraPosition.zoom.toDouble(),
                    controller.mapStyleMode,
                )
                MapStyleRuntime.apply3dVisuals(map, style, is3dState.value)
            }
        }
        if (mapView != null) {
            mapView.post(applyStyle)
        } else {
            applyStyle()
        }
    }

    LaunchedEffect(timeOfDay, controller.mapRuntime) {
        val (_, style) = controller.mapRuntime ?: return@LaunchedEffect
        MapStyleRuntime.applyTimeOfDay(style, timeOfDay)
    }

    LaunchedEffect(is3d, controller.mapRuntime) {
        val (map, style) = controller.mapRuntime ?: return@LaunchedEffect
        val mv = mapViewRef.value
        if (mv != null) {
            mv.post {
                MapStyleRuntime.apply3dVisuals(map, style, is3d)
            }
        } else {
            MapStyleRuntime.apply3dVisuals(map, style, is3d)
        }
    }

    LaunchedEffect(
        controller.mapRuntime,
        activeDirections?.origin?.id,
        activeDirections?.stops?.map { it.id },
        activeDirections?.travelMode,
        bottomChromePadding,
    ) {
        val runtime = controller.mapRuntime ?: return@LaunchedEffect
        val (map, style) = runtime
        val mv = mapViewRef.value
        val directions = activeDirections
        if (directions == null) {
            val clear = { DirectionsRouteOverlay.remove(style) }
            if (mv != null) mv.post(clear) else clear()
            return@LaunchedEffect
        }
        val waypoints = listOf(directions.origin.latLng) + directions.stops.map { it.latLng }
        if (waypoints.size < 2) {
            val clear = { DirectionsRouteOverlay.remove(style) }
            if (mv != null) mv.post(clear) else clear()
            return@LaunchedEffect
        }

        delay(DirectionsRouteAnimation.FETCH_DEBOUNCE_MS)

        val valhallaRoute = ValhallaRouteClient.fetchRoute(waypoints, directions.travelMode)
        val fullGeometry = valhallaRoute?.geometry?.takeIf { it.size >= 2 }
            ?: DirectionsPathOptimizer.buildPolyline(waypoints, segmentsPerLeg = 26)
        val fitPoints = buildList {
            addAll(waypoints)
            addAll(fullGeometry)
        }
        val routeBounds = DirectionsRouteGeometry.boundsFor(fitPoints)

        val bottomPaddingPx = with(density) { bottomChromePaddingState.value.roundToPx() }

        fun applyFrame(progress: Float) {
            DirectionsRouteOverlay.sync(
                style = style,
                origin = directions.origin,
                stops = directions.stops,
                valhallaRoute = valhallaRoute,
                revealProgress = progress,
            )
            controller.mapOverlayCameraTick++
        }

        coroutineScope {
            if (routeBounds != null && mv != null) {
                launch {
                    MapViewportFit.animateToBounds(
                        map = map,
                        mapView = mv,
                        bounds = routeBounds,
                        paddingLeft = cameraEdgePaddingPx,
                        paddingTop = cameraEdgePaddingPx,
                        paddingRight = cameraEdgePaddingPx,
                        paddingBottom = bottomPaddingPx + cameraEdgePaddingPx,
                    )
                }
            }
            launch {
                DirectionsRouteAnimation.animateReveal { progress ->
                    val frame = { applyFrame(progress) }
                    if (mv != null) mv.post(frame) else frame()
                }
            }
        }
    }

    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                mapViewRef.value?.onStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                mapViewRef.value?.onResume()
                appearanceClockTick++
                hasLocationPermission =
                    MapAndroidLocation.hasCoarseLocationPermission(context.applicationContext)
            }

            override fun onPause(owner: LifecycleOwner) {
                mapViewRef.value?.onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                mapViewRef.value?.onStop()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            applyMapBackdropBlur(mapViewRef.value, 0f)
            mapViewRef.value?.onDestroy()
            mapViewRef.value = null
        }
    }

    val zoomIn: () -> Unit = {
        controller.mapRuntime?.first?.let { map ->
            val next = (map.cameraPosition.zoom + 1.0).coerceAtMost(MapConstants.MAX_ZOOM)
            map.easeCamera(CameraUpdateFactory.zoomTo(next), MapConstants.ZOOM_ANIMATION_MS)
        }
    }
    val zoomOut: () -> Unit = {
        controller.mapRuntime?.first?.let { map ->
            val next = (map.cameraPosition.zoom - 1.0).coerceAtLeast(MapConstants.MIN_ZOOM)
            map.easeCamera(CameraUpdateFactory.zoomTo(next), MapConstants.ZOOM_ANIMATION_MS)
        }
    }

    MapSearchAutocompleteEffect(controller)
    MapNearbyHighlightsEffect(controller)
    MapNearbyCameraFitEffect(
        controller = controller,
        sheetSnap = syncedStackSnap,
        bottomChromePadding = bottomChromePadding,
        cameraEdgePaddingPx = cameraEdgePaddingPx,
    )
    MapPlaceSelectionOverlayEffect(context, controller)

    val onMapPlaceTapRef = remember { mutableStateOf<(LatLng) -> Unit>({}) }
    SideEffect {
        onMapPlaceTapRef.value = { latLng -> controller.onMapPlaceTapped(latLng) }
    }

    LaunchedEffect(sheetBlurRadiusPx, mapViewRef.value) {
        mapViewRef.value?.post {
            applyMapBackdropBlur(mapViewRef.value, sheetBlurRadiusPx)
        }
    }

    val activeSheetSurface = sheetTheme.sheetSurface

    Box(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().hazeSource(state = hazeState)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                            MapView(ctx).also { mapView ->
                                mapView.layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                                mapView.onCreate(null)
                                mapViewRef.value = mapView
                                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                                    mapView.onStart()
                                }
                                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                    mapView.onResume()
                                }
                                mapView.addOnDidFailLoadingMapListener { message ->
                                    loadError = message
                                    mapReady = false
                                    controller.mapRuntime = null
                                    mapScaleRuler = null
                                    MapPoiSelectionController.discardActive()
                                }
                                mapView.getMapAsync { map ->
                                    controller.mapLibreMap = map
                                    controller.mapView = mapView
                                    mapReady = false
                                    loadError = null
                                    map.setMinZoomPreference(MapConstants.MIN_ZOOM)
                                    map.setMaxZoomPreference(MapConstants.MAX_ZOOM)
                                    map.uiSettings.apply {
                                        isCompassEnabled = false
                                        isLogoEnabled = false
                                        isAttributionEnabled = false
                                    }
                                    fun publishBearing() {
                                        mapView.post {
                                            mapBearingDegrees = map.cameraPosition.bearing.toFloat()
                                        }
                                    }
                                    fun publishScaleRuler() {
                                        mapView.post {
                                            mapScaleRuler = MapScaleRulerCalculator.calculate(
                                                map = map,
                                                mapView = mapView,
                                                targetWidthPx = scaleRulerTargetWidthPx,
                                            )
                                        }
                                    }
                                    fun syncExtrusionDuring3d(suppressForCameraMotion: Boolean) {
                                        val runtime = controller.mapRuntime ?: return
                                        if (!is3dState.value) return
                                        MapStyleRuntime.syncBuilding3dVisibility(
                                            map = runtime.first,
                                            style = runtime.second,
                                            userWants3d = true,
                                            suppressForCameraMotion = suppressForCameraMotion,
                                        )
                                    }
                                    fun publishMapOverlayPositions() {
                                        val needsOverlay = activeDirectionsState.value != null ||
                                            (
                                                activeNearbyCategoryState.value != null &&
                                                    nearbyOverlayResultsState.value.isNotEmpty()
                                                )
                                        if (!needsOverlay) return
                                        mapView.post { controller.mapOverlayCameraTick++ }
                                    }
                                    map.addOnCameraMoveStartedListener {
                                        syncExtrusionDuring3d(suppressForCameraMotion = true)
                                    }
                                    map.addOnCameraMoveListener {
                                        publishBearing()
                                        publishScaleRuler()
                                        publishMapOverlayPositions()
                                        syncExtrusionDuring3d(suppressForCameraMotion = true)
                                    }
                                    publishBearing()
                                    publishScaleRuler()
                                    map.addOnCameraIdleListener {
                                        publishBearing()
                                        publishScaleRuler()
                                        publishMapOverlayPositions()
                                        syncExtrusionDuring3d(suppressForCameraMotion = false)
                                        MapDataTierLogger.logZoomTierIfChanged(
                                            map.cameraPosition.zoom.toDouble(),
                                            controller.mapStyleMode,
                                        )
                                    }
                                    map.addOnMapClickListener { latLng ->
                                        onMapPlaceTapRef.value(latLng)
                                        true
                                    }
                                }
                            }
                        },
                update = { },
            )

            DirectionsWaypointMarkersOverlay(
                directions = activeDirections,
                map = controller.mapLibreMap,
                cameraTick = controller.mapOverlayCameraTick,
            )

            NearbyCategoryMarkersOverlay(
                category = controller.activeNearbyCategory,
                results = filteredNearby.visibleResults,
                map = controller.mapLibreMap,
                cameraTick = controller.mapOverlayCameraTick,
                onMarkerClick = { controller.openNearbyPlace(it) },
            )

            val displayError = controller.styleFetchError ?: loadError
            if (!mapReady && displayError == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (displayError != null) {
                Text(
                    text = displayError,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }

        val stackLayers = sheetStack.layers
        stackLayers.forEachIndexed { index, layer ->
            key(layer.sheet.stackId, layer.presentationKey) {
                val scrollState = rememberScrollState()
                val isFrozen = index < stackLayers.lastIndex
                val isBottomLayer = index == 0

                AppleMapsSheetStackLayer(
                    layer = layer,
                    isFrozen = isFrozen,
                    syncedHeightOverride = if (isFrozen && layer.sheet.isSyncedStackSheet()) {
                        syncedSheetHeightDp
                    } else {
                        null
                    },
                    scrollState = scrollState,
                    onSnapChange = { sheetStack.updateSnap(index, it) },
                    onHeightChange = { height ->
                        layerHeightsDp[index] = height
                        if (!isFrozen && index == stackLayers.lastIndex) {
                            syncedSheetHeightDp = height
                        } else if (layer.sheet.isSyncedStackSheet() && !isFrozen) {
                            syncedSheetHeightDp = height
                        }
                    },
                    onBlurRadiusPxChange = { blur ->
                        if (isBottomLayer) sheetBlurRadiusPx = blur
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(1f + index),
                    sheetSurfaceColor = activeSheetSurface,
                    hazeState = if (isBottomLayer && !layer.sheet.usesOpaqueSheetSurface()) {
                        hazeState
                    } else {
                        null
                    },
                ) { contentScrollEnabled, sheetGestures, sheetModifier ->
                    when (val sheet = layer.sheet) {
                        AppleMapSheet.Home -> {
                            AppleMapsPersistentSheetContent(
                                sheetTheme = sheetTheme,
                                scrollState = scrollState,
                                contentScrollEnabled = contentScrollEnabled,
                                sheetGestures = sheetGestures,
                                profileAbbreviation = profileAbbreviation,
                                isLoggedIn = isLoggedIn,
                                onProfileClick = onProfileClick,
                                isSearchActive = controller.isSearchActive,
                                searchQuery = controller.searchQuery,
                                onSearchCancel = { controller.exitSearchMode() },
                                onSearchFocus = { controller.enterSearchMode() },
                                onSearchQueryChange = { value ->
                                    if (value.isEmpty() && controller.isSearchActive) {
                                        controller.deactivateSearchMode()
                                    } else {
                                        controller.onSearchQueryChanged(value)
                                    }
                                },
                                onNearbyShortcutClick = { shortcut ->
                                    controller.startNearbyCategoryBrowse(shortcut)
                                },
                                activeNearbyCategory = controller.activeNearbyCategory,
                                nearbyBrowseResults = filteredNearby.visibleResults,
                                nearbyBrowseLoading = controller.nearbyBrowseLoading,
                                nearbyBrowseError = controller.nearbyBrowseError,
                                nearbyFilterState = controller.nearbyFilterState,
                                nearbyAvailableChains = filteredNearby.availableChains,
                                nearbyPickHoursByGid = nearbyPickHoursByGid,
                                onNearbyFilterChange = { controller.updateNearbyFilter(it) },
                                onNearbyBrowseDone = { controller.exitNearbyBrowse() },
                                onNearbyPlaceSelected = { controller.openNearbyPlace(it) },
                                onSearchClear = { controller.clearSearchQueryOnly() },
                                searchSuggestions = controller.searchSuggestions,
                                searchLoading = controller.searchLoading,
                                searchError = controller.searchError,
                                onSearchResultSelected = { controller.focusSearchResult(it) },
                                onSearchSubmit = { query ->
                                    val trimmed = query.trim()
                                    if (trimmed.isEmpty()) return@AppleMapsPersistentSheetContent
                                    coroutineScope.launch {
                                        val map = controller.mapLibreMap
                                        val pick = controller.searchSuggestions.firstOrNull()
                                            ?: when (
                                                val response = PeliasSearchClient.search(
                                                    trimmed,
                                                    focus = map?.cameraPosition?.target,
                                                    mapZoom = map?.cameraPosition?.zoom,
                                                )
                                            ) {
                                                is PeliasSearchResponse.Success ->
                                                    response.results.firstOrNull()
                                                is PeliasSearchResponse.Failure -> {
                                                    controller.searchError = response.message
                                                    null
                                                }
                                            }
                                        if (pick != null) {
                                            controller.focusSearchResult(pick)
                                        }
                                    }
                                },
                                modifier = sheetModifier,
                            )
                        }

                        is AppleMapSheet.PlaceDetail -> {
                            val place = sheet.place
                            val directions = activeDirections
                            val primaryRouteAction =
                                if (directions == null || place.id == directions.origin.id) {
                                    PlaceDetailPrimaryRouteAction.Directions
                                } else {
                                    PlaceDetailPrimaryRouteAction.AddStop
                                }
                            val claimButtonMode = placeClaimButtonModes[place.id] ?: PlaceClaimButtonMode.Claim

                            LaunchedEffect(place.id, isLoggedIn, authRevision) {
                                if (!isLoggedIn) {
                                    placeClaimButtonModes[place.id] = PlaceClaimButtonMode.Claim
                                    return@LaunchedEffect
                                }
                                val token = OfflineAuthStore.sessionToken(context) ?: return@LaunchedEffect
                                placeClaimButtonModes[place.id] = PlaceClaimButtonMode.Loading
                                val externalRef = place.businessPoiExternalRef()
                                val resolveResult = withContext(Dispatchers.IO) {
                                    BusinessClaimClient.resolvePoi(
                                        externalRef = externalRef,
                                        name = place.name,
                                        address = place.address,
                                        latitude = place.latLng.latitude,
                                        longitude = place.latLng.longitude,
                                        bearerToken = token,
                                    )
                                }
                                val resolvedPoiId = when (resolveResult) {
                                    is BusinessClaimClient.ResolveResult.Failure -> {
                                        placeClaimButtonModes[place.id] = PlaceClaimButtonMode.Claim
                                        return@LaunchedEffect
                                    }
                                    is BusinessClaimClient.ResolveResult.Success -> resolveResult.poiId
                                }
                                resolvedBackendPoiIds[place.id] = resolvedPoiId
                                val statusResult = withContext(Dispatchers.IO) {
                                    BusinessClaimClient.fetchClaimStatus(resolvedPoiId, token)
                                }
                                placeClaimButtonModes[place.id] = when (statusResult) {
                                    is BusinessClaimClient.ClaimStatusResult.Success ->
                                        if (statusResult.status.canEditBusiness) {
                                            PlaceClaimButtonMode.BusinessEdit
                                        } else {
                                            PlaceClaimButtonMode.Claim
                                        }
                                    is BusinessClaimClient.ClaimStatusResult.Failure ->
                                        PlaceClaimButtonMode.Claim
                                }
                            }

                            AppleMapsPlaceDetailSheetContent(
                                place = place,
                                scrollState = scrollState,
                                contentScrollEnabled = contentScrollEnabled,
                                sheetGestures = sheetGestures,
                                onClose = {
                                    sheetStack.clearToHome()
                                    controller.selectedPlace = null
                                },
                                sheetTheme = sheetTheme,
                                primaryRouteAction = primaryRouteAction,
                                claimButtonMode = claimButtonMode,
                                onPrimaryRouteClick = {
                                    val dirs = activeDirections
                                    if (dirs == null || place.id == dirs.origin.id) {
                                        sheetStack.push(
                                            AppleMapSheet.Directions(place, emptyList()),
                                        )
                                    } else {
                                        sheetStack.updateDirections(
                                            dirs.origin,
                                            dirs.stops + place,
                                        )
                                        if (index == stackLayers.lastIndex) {
                                            sheetStack.pop()
                                        }
                                    }
                                },
                                onClaimPlaceClick = {
                                    if (!OfflineAuthStore.isSessionActive(context)) {
                                        authDestination = AuthDestination.SignIn
                                        return@AppleMapsPlaceDetailSheetContent
                                    }
                                    val token = OfflineAuthStore.sessionToken(context)
                                    if (token.isNullOrBlank()) {
                                        authDestination = AuthDestination.SignIn
                                        return@AppleMapsPlaceDetailSheetContent
                                    }
                                    if (claimButtonMode == PlaceClaimButtonMode.BusinessEdit) {
                                        val resolvedPoiId = resolvedBackendPoiIds[place.id]
                                        if (resolvedPoiId != null) {
                                            businessEditPoiId = resolvedPoiId
                                            return@AppleMapsPlaceDetailSheetContent
                                        }
                                    }
                                    coroutineScope.launch {
                                        val externalRef = place.businessPoiExternalRef()
                                        val resolveResult = withContext(Dispatchers.IO) {
                                            BusinessClaimClient.resolvePoi(
                                                externalRef = externalRef,
                                                name = place.name,
                                                address = place.address,
                                                latitude = place.latLng.latitude,
                                                longitude = place.latLng.longitude,
                                                bearerToken = token,
                                            )
                                        }
                                        val resolvedPoiId = when (resolveResult) {
                                            is BusinessClaimClient.ResolveResult.Failure -> {
                                                Toast.makeText(
                                                    context,
                                                    resolveResult.message,
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                                return@launch
                                            }
                                            is BusinessClaimClient.ResolveResult.Success -> resolveResult.poiId
                                        }
                                        resolvedBackendPoiIds[place.id] = resolvedPoiId
                                        val statusResult = withContext(Dispatchers.IO) {
                                            BusinessClaimClient.fetchClaimStatus(resolvedPoiId, token)
                                        }
                                        when (statusResult) {
                                            is BusinessClaimClient.ClaimStatusResult.Failure -> {
                                                Toast.makeText(
                                                    context,
                                                    statusResult.message,
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                            is BusinessClaimClient.ClaimStatusResult.Success -> {
                                                val status = statusResult.status
                                                placeClaimButtonModes[place.id] =
                                                    if (status.canEditBusiness) {
                                                        PlaceClaimButtonMode.BusinessEdit
                                                    } else {
                                                        PlaceClaimButtonMode.Claim
                                                    }
                                                if (status.canEditBusiness) {
                                                    businessEditPoiId = resolvedPoiId
                                                } else {
                                                    claimPoiId = resolvedPoiId
                                                    claimGuidance = status.registrationGuidance
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = sheetModifier,
                            )
                        }

                        is AppleMapSheet.UserProfile -> {
                            LaunchedEffect(Unit) {
                                val refreshed = withContext(Dispatchers.IO) {
                                    OfflineFriendsStore.refreshFromBackend(context)
                                }
                                if (refreshed) {
                                    authRevision++
                                }
                            }
                            val friends = remember(authRevision) { OfflineFriendsStore.listFriends(context) }
                            UserProfileSheetContent(
                                sheetTheme = sheetTheme,
                                scrollState = scrollState,
                                contentScrollEnabled = contentScrollEnabled,
                                sheetGestures = sheetGestures,
                                identifier = OfflineAuthStore.sessionIdentifier(context).orEmpty(),
                                profileId = OfflineAuthStore.profileId(context).orEmpty(),
                                abbreviation = profileAbbreviation.orEmpty(),
                                friendsCount = friends.size,
                                selectedBusinessPoiId = sheet.selectedBusinessPoiId,
                                onClose = { sheetStack.pop() },
                                onClearBusinessSelection = { sheetStack.updateUserProfileSelection(null) },
                                onBusinessPoiSelected = { poi ->
                                    sheetStack.updateUserProfileSelection(poi.id)
                                    controller.focusOwnedBusinessPoi(poi)
                                },
                                onResetCredentials = { authDestination = AuthDestination.ResetCredentials },
                                onCreateNewAccount = { authDestination = AuthDestination.SignUp },
                                onSignOut = {
                                    OfflineAuthStore.endSession(context)
                                    authRevision++
                                    sheetStack.pop()
                                },
                                onMyQrCode = { authDestination = AuthDestination.MyQrCode },
                                onAddFriend = { authDestination = AuthDestination.AddFriendMenu },
                                onFriendsList = { authDestination = AuthDestination.Friends },
                                modifier = sheetModifier,
                            )
                        }

                        is AppleMapSheet.Directions -> {
                            AppleMapsDirectionsPanel(
                                origin = sheet.origin,
                                stops = sheet.stops,
                                travelMode = sheet.travelMode,
                                onTravelModeChange = { mode ->
                                    sheetStack.updateDirections(
                                        sheet.origin,
                                        sheet.stops,
                                        travelMode = mode,
                                    )
                                },
                                sheetGestures = sheetGestures,
                                sheetTheme = sheetTheme,
                                scrollState = scrollState,
                                contentScrollEnabled = contentScrollEnabled,
                                onAddStopRowClick = {
                                    sheetStack.push(AppleMapSheet.AddStop)
                                },
                                onDismiss = {
                                    sheetStack.removeDirectionsAndAddStop()
                                    controller.selectedPlace = sheetStack.topPlaceDetail()?.place
                                },
                                modifier = sheetModifier.fillMaxWidth(),
                            )
                        }

                        AppleMapSheet.AddStop -> {
                            AppleMapsAddStopPanel(
                                sheetGestures = sheetGestures,
                                sheetTheme = sheetTheme,
                                scrollState = scrollState,
                                contentScrollEnabled = contentScrollEnabled,
                                onCancel = { sheetStack.pop() },
                                onMyLocationClick = {
                                    if (!hasLocationPermission) {
                                        showMyLocationSheet = true
                                    } else {
                                        navigateToBestAvailableLocation(
                                            context.applicationContext,
                                            controller.mapLibreMap,
                                            locationFallbackLatLng,
                                        )
                                    }
                                },
                                modifier = sheetModifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

        if (mapReady) {
            mapScaleRuler?.let { scale ->
                MapScaleRuler(
                    state = scale,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .zIndex(0.5f)
                        .statusBarsPadding()
                        .padding(start = 12.dp, top = 8.dp),
                )
            }
        }

        if (showTopRightChrome) {
            AppleMapsTopRightChrome(
                sheetTheme = sheetTheme,
                isDarkAppearance = isDarkAppearance,
                is3d = is3d,
                mapBearingDegrees = mapBearingDegrees,
                onChooseMapClick = { showChooseMapSheet = true },
                onMyLocationClick = { showMyLocationSheet = true },
                onToggleAppearanceClick = { isDarkAppearance = !isDarkAppearance },
                onToggle3dClick = { is3d = !is3d },
                onCompassClick = {
                    controller.mapRuntime?.let { (map, style) ->
                        MapStyleRuntime.resetCompassBearing(map, style, userWants3d = is3d)
                    }
                },
                onCompassBearingDrag = { bearingDeg ->
                    controller.mapRuntime?.first?.let { map ->
                        val current = map.cameraPosition
                        map.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder(current)
                                    .bearing(bearingDeg.toDouble())
                                    .build(),
                            ),
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(10f)
                    .statusBarsPadding()
                    .padding(end = 12.dp, top = 8.dp),
            )
        }

        if (showBottomMapChrome) {
            MapZoomPillControl(
                onZoomIn = zoomIn,
                onZoomOut = zoomOut,
                zoomInContentDescription = stringResource(R.string.zoom_in),
                zoomOutContentDescription = stringResource(R.string.zoom_out),
                surfaceColor = sheetTheme.mapControlGlass,
                iconTint = sheetTheme.mapControlIcon,
                dividerColor = sheetTheme.mapControlDivider,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 12.dp, bottom = bottomChromePadding),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = bottomChromePadding)
                    .size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = sheetTheme.mapControlGlass,
                shadowElevation = 6.dp,
            ) {
                IconButton(
                    onClick = { controller.showLookAround = true },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TravelExplore,
                        contentDescription = stringResource(R.string.apple_look_around),
                        tint = sheetTheme.mapControlIcon,
                    )
                }
            }
        }

        if (showChooseMapSheet) {
            AppleMapsChooseMapModal(
                sheetTheme = sheetTheme,
                currentVariant = controller.mapStyleVariant,
                onDismiss = { showChooseMapSheet = false },
                onStyleSelected = { variant ->
                    controller.onMapStyleVariantSelected(variant)
                    showChooseMapSheet = false
                },
            )
        }

        if (controller.showLookAround) {
            LookAroundModal(
                sheetTheme = sheetTheme,
                target = controller.lookAroundTarget(),
                onDismiss = { controller.showLookAround = false },
                onCenterMap = {
                    controller.lookAroundTarget().latLng?.let { latLng ->
                        controller.mapLibreMap?.easeCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                latLng,
                                MapConstants.SEARCH_RESULT_ZOOM.coerceIn(
                                    MapConstants.MIN_ZOOM,
                                    MapConstants.MAX_ZOOM,
                                ),
                            ),
                            MapConstants.ZOOM_ANIMATION_MS,
                        )
                    }
                },
            )
        }

        if (showMyLocationSheet) {
            AppleMapsMyLocationModal(
                sheetTheme = sheetTheme,
                onDismiss = { showMyLocationSheet = false },
                hasPermission = hasLocationPermission,
                onRequestPermission = {
                    locationPermissionLauncher.launch(LocationPermissions)
                },
                onGoToLocation = {
                    navigateToBestAvailableLocation(
                        context.applicationContext,
                        controller.mapLibreMap,
                        locationFallbackLatLng,
                    )
                },
            )
        }

        authDestination?.let { destination ->
            AuthOverlayHost(
                destination = destination,
                onNavigate = { authDestination = it },
                onDismiss = { authDestination = null },
                onAuthChanged = {
                    authRevision++
                    coroutineScope.launch(Dispatchers.IO) {
                        OfflineFriendsStore.refreshFromBackend(context)
                        authRevision++
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(50f),
            )
        }

        businessEditPoiId?.let { poiId ->
            BusinessDetailEditScreen(
                poiId = poiId,
                onBack = { businessEditPoiId = null },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(60f),
            )
        }

        claimGuidance?.let { guidance ->
            AlertDialog(
                onDismissRequest = { if (!claimRequestInFlight) claimGuidance = null },
                title = { Text(text = stringResource(R.string.claim_registration_title)) },
                text = {
                    Text(
                        text = buildString {
                            appendLine(stringResource(R.string.claim_registration_message))
                            appendLine()
                            appendLine("${stringResource(R.string.claim_contact_phone)}: ${guidance.contactPhone}")
                            appendLine("${stringResource(R.string.claim_agent_address)}: ${guidance.registrationAgentAddress}")
                            appendLine("${stringResource(R.string.claim_hours)}: ${guidance.availableRegistrationHours}")
                            append("${stringResource(R.string.claim_instructions)}: ${guidance.additionalInstructions}")
                        },
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = !claimRequestInFlight,
                        onClick = {
                            val poiId = claimPoiId ?: return@TextButton
                            val token = OfflineAuthStore.sessionToken(context) ?: return@TextButton
                            claimRequestInFlight = true
                            coroutineScope.launch {
                                val requestResult = withContext(Dispatchers.IO) {
                                    BusinessClaimClient.createClaimRequest(poiId, token)
                                }
                                claimRequestInFlight = false
                                when (requestResult) {
                                    BusinessClaimClient.ClaimRequestResult.Success -> {
                                        claimGuidance = null
                                        claimPoiId = null
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.claim_request_submitted),
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    }
                                    is BusinessClaimClient.ClaimRequestResult.Failure -> {
                                        Toast.makeText(
                                            context,
                                            requestResult.message,
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    }
                                }
                            }
                        },
                    ) {
                        Text(text = stringResource(R.string.claim_submit_request))
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !claimRequestInFlight,
                        onClick = {
                            claimGuidance = null
                            claimPoiId = null
                        },
                    ) {
                        Text(text = stringResource(R.string.claim_close))
                    }
                },
            )
        }

    }
}

private fun navigateToBestAvailableLocation(
    appCtx: Context,
    map: MapLibreMap?,
    fallbackCenter: LatLng?,
) {
    if (map == null) return
    val fromSystem = MapAndroidLocation.getLastKnownLatLng(appCtx)
    val target = fromSystem ?: fallbackCenter ?: return
    val zoom = map.cameraPosition.zoom.toDouble().coerceAtLeast(MapConstants.LOCATION_FALLBACK_ZOOM)
    map.easeCamera(
        CameraUpdateFactory.newLatLngZoom(target, zoom),
        620,
    )
}
