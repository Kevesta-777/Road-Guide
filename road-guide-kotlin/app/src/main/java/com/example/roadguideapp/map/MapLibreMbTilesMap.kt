package com.example.roadguideapp.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.roadguideapp.offlinegraph.OfflineGraphEngine
import com.example.roadguideapp.offlinegraph.toDisplayString
import com.example.roadguideapp.offlinegraph.userMessage
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
    val resolvedBackendPoiIds = remember { mutableStateMapOf<String, String>() }
    var routePlanTick by remember { mutableIntStateOf(0) }
    var showOfflineGraphImportAlert by remember { mutableStateOf(false) }
    var showOfflineRoutingRequiredAlert by remember { mutableStateOf(false) }
    var graphImportInProgress by remember { mutableStateOf(false) }
    var graphRestoreInProgress by remember { mutableStateOf(false) }
    var graphImportStatusMessage by remember { mutableStateOf("") }
    var graphImportProgressPercent by remember { mutableStateOf<Int?>(null) }
    var offlineGraphLoaded by remember { mutableStateOf(false) }
    var activeRouteResult by remember { mutableStateOf<DirectionsRouteResult?>(null) }
    var activeRouteSource by remember { mutableStateOf<DirectionsRouteSource?>(null) }
    var isRouteCalculating by remember { mutableStateOf(false) }
    var isRouteRefining by remember { mutableStateOf(false) }
    var isNavigationActive by remember { mutableStateOf(false) }
    var navVehiclePosition by remember { mutableStateOf<LatLng?>(null) }
    var navVehicleBearing by remember { mutableFloatStateOf(0f) }
    var navCameraHolder by remember { mutableStateOf<DirectionsNavigationCamera?>(null) }
    var navTiltDegrees by remember { mutableDoubleStateOf(DirectionsNavConfig.DEFAULT_TILT_DEG) }
    val navDisplaySmoother = remember { DirectionsNavigationDisplaySmoother() }
    var navRouteGeometry by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val activeDirections = sheetStack.activeDirections()

    fun clearActiveRouteOverlay() {
        activeRouteResult = null
        activeRouteSource = null
        controller.mapRuntime?.let { (_, style) ->
            val mv = mapViewRef.value
            val clear = { DirectionsRouteOverlay.remove(style) }
            if (mv != null) mv.post(clear) else clear()
        }
        controller.mapOverlayCameraTick = controller.mapOverlayCameraTick + 1
    }

    val onAddStopRequested: () -> Unit = {
        clearActiveRouteOverlay()
        sheetStack.popAddStopOverlays()
        val offlineReady = offlineGraphLoaded && OfflineGraphEngine.isLoaded()
        if (offlineReady) {
            sheetStack.push(AppleMapSheet.AddStop)
        } else {
            showOfflineRoutingRequiredAlert = true
        }
    }

    val activeRouteResultState = rememberUpdatedState(activeRouteResult)
    val activeDirectionsForNav = rememberUpdatedState(activeDirections)
    val isNavigationActiveState = rememberUpdatedState(isNavigationActive)
    val navRouteGeometryState = rememberUpdatedState(navRouteGeometry)
    val endNavigationRef = remember { mutableStateOf<() -> Unit>({}) }
    val navUserZoomGestureRef = remember { mutableStateOf(false) }

    val navigationEngine = remember(coroutineScope) {
        DirectionsNavigationEngine(
            onUpdate = { frame, progress ->
                coroutineScope.launch(Dispatchers.Main.immediate) {
                    if (!isNavigationActiveState.value) return@launch
                    val routeLine = navRouteGeometryState.value
                    val displayFrame = DirectionsNavigationFrameResolver.resolveDisplayFrame(
                        engineFrame = frame,
                        route = routeLine,
                        smoother = navDisplaySmoother,
                    )
                    navVehiclePosition = LatLng(displayFrame.lat, displayFrame.lng)
                    navVehicleBearing = displayFrame.bearingDegrees.toFloat()
                    val runtime = controller.mapRuntime
                    val directions = activeDirectionsForNav.value
                    val route = activeRouteResultState.value
                    if (runtime != null && directions != null && route != null && routeLine.size >= 2) {
                        val (_, style) = runtime
                        val tripOrigin = directions.tripWaypoints.first()
                        val tripStops = directions.tripWaypoints.drop(1)
                        if (!isNavigationActiveState.value) return@launch
                        if (!navUserZoomGestureRef.value) {
                            navCameraHolder?.follow(displayFrame)
                        }
                        DirectionsNavigationFrameResolver.syncNavigationVisuals(
                            style = style,
                            route = routeLine,
                            frame = displayFrame,
                            origin = tripOrigin,
                            stops = tripStops,
                            valhallaRoute = route,
                        )
                        controller.mapOverlayCameraTick = controller.mapOverlayCameraTick + 1
                    }
                }
            },
            onRouteComplete = {
                coroutineScope.launch(Dispatchers.Main.immediate) {
                    if (!isNavigationActiveState.value) return@launch
                    Toast.makeText(
                        context,
                        context.getString(R.string.directions_navigation_arrived),
                        Toast.LENGTH_SHORT,
                    ).show()
                    endNavigationRef.value()
                }
            },
        )
    }

    fun currentNavFrame(): DirectionsNavFrame? {
        val pos = navVehiclePosition ?: return null
        return DirectionsNavFrame(
            lat = pos.latitude,
            lng = pos.longitude,
            bearingDegrees = navVehicleBearing.toDouble(),
            cumulativeDistanceM = navigationEngine.currentDistanceM(),
        )
    }

    fun refreshNavCamera() {
        currentNavFrame()?.let { frame -> navCameraHolder?.refreshCameraNow(frame) }
    }

    fun endNavigationSession() {
        isNavigationActive = false
        navigationEngine.stop()
        navDisplaySmoother.clear()
        navRouteGeometry = emptyList()
        navCameraHolder?.exit()
        navCameraHolder = null
        navVehiclePosition = null
        navVehicleBearing = 0f
        controller.mapRuntime?.let { (map, style) ->
            DirectionsNavigationVehicleLayer.remove(style)
            map.uiSettings.apply {
                isScrollGesturesEnabled = true
                isRotateGesturesEnabled = true
                isTiltGesturesEnabled = true
                isZoomGesturesEnabled = true
            }
            MapStyleRuntime.apply3dVisuals(map, style, is3d)
            val directions = activeDirections
            val route = activeRouteResult
            if (directions != null && route != null && route.geometry.size >= 2) {
                val tripOrigin = directions.tripWaypoints.first()
                val tripStops = directions.tripWaypoints.drop(1)
                DirectionsRouteOverlay.sync(
                    style = style,
                    origin = tripOrigin,
                    stops = tripStops,
                    valhallaRoute = route,
                    revealProgress = 1f,
                )
            }
        }
        controller.mapOverlayCameraTick = controller.mapOverlayCameraTick + 1
    }

    fun startNavigationSession() {
        val route = activeRouteResult
        val directions = activeDirections
        val runtime = controller.mapRuntime
        if (route == null || directions == null || runtime == null || route.geometry.size < 2) {
            Toast.makeText(
                context,
                context.getString(R.string.directions_navigation_failed),
                Toast.LENGTH_LONG,
            ).show()
            return
        }
        val (map, style) = runtime
        isNavigationActive = true
        is3d = true
        sheetStack.updateAllSyncedSnaps(AppleSheetSnap.Peek)
        MapStyleRuntime.apply3dVisuals(map, style, enabled = true)
        map.uiSettings.apply {
            isScrollGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isZoomGesturesEnabled = true
        }
        val mapViewHeight = mapViewRef.value?.height ?: 0
        val camera = DirectionsNavigationCamera(map) {
            (mapViewRef.value?.height ?: mapViewHeight).coerceAtLeast(1)
        }
        navCameraHolder = camera
        navTiltDegrees = DirectionsNavConfig.DEFAULT_TILT_DEG
        val navGeometry = route.geometry
        navRouteGeometry = navGeometry
        val initialSpeed = DirectionsNavConfig.speedMps(directions.travelMode)
        navigationEngine.speedMps = initialSpeed
        navigationEngine.loadGeometry(navGeometry, resetPosition = true)
        val firstFrame = DirectionsNavFrame(
            lat = navGeometry.first().latitude,
            lng = navGeometry.first().longitude,
            bearingDegrees = bearingDegrees(
                navGeometry[0].latitude,
                navGeometry[0].longitude,
                navGeometry[1].latitude,
                navGeometry[1].longitude,
            ),
            cumulativeDistanceM = 0.0,
        )
        navDisplaySmoother.reset(firstFrame, navGeometry)
        val displayFrame = DirectionsNavigationFrameResolver.resolveDisplayFrame(
            engineFrame = firstFrame,
            route = navGeometry,
            smoother = navDisplaySmoother,
        )
        navVehiclePosition = LatLng(displayFrame.lat, displayFrame.lng)
        navVehicleBearing = displayFrame.bearingDegrees.toFloat()
        val tripOrigin = directions.tripWaypoints.first()
        val tripStops = directions.tripWaypoints.drop(1)
        DirectionsNavigationFrameResolver.syncNavigationVisuals(
            style = style,
            route = navGeometry,
            frame = displayFrame,
            origin = tripOrigin,
            stops = tripStops,
            valhallaRoute = route,
        )
        camera.enter(displayFrame)
        if (!navigationEngine.start()) {
            Toast.makeText(
                context,
                context.getString(R.string.directions_navigation_failed),
                Toast.LENGTH_LONG,
            ).show()
            endNavigationSession()
            return
        }
        Toast.makeText(
            context,
            context.getString(R.string.directions_navigation_started),
            Toast.LENGTH_SHORT,
        ).show()
    }

    endNavigationRef.value = { endNavigationSession() }

    val showBottomMapChrome = !isNavigationActive && syncedStackSnap != AppleSheetSnap.Large
    val showTopRightChrome = !isNavigationActive && syncedStackSnap != AppleSheetSnap.Large

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

    fun persistGraphUri(uri: android.net.Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) {
            // Some providers do not allow persistable permissions.
        }
    }

    fun runOfflineGraphImport(
        initialStatusRes: Int,
        import: suspend () -> Result<String>,
    ) {
        coroutineScope.launch {
            graphImportInProgress = true
            graphImportProgressPercent = null
            showOfflineGraphImportAlert = false
            graphImportStatusMessage = context.getString(initialStatusRes)
            mapViewRef.value?.onPause()
            val importResult = withContext(Dispatchers.IO) {
                runCatching { import().getOrThrow() }
            }
            mapViewRef.value?.onResume()
            graphImportInProgress = false
            graphImportStatusMessage = ""
            graphImportProgressPercent = null
            importResult.fold(
                onSuccess = {
                    offlineGraphLoaded = OfflineGraphEngine.isLoaded()
                    Toast.makeText(
                        context,
                        context.getString(R.string.directions_offline_import_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                    if (activeDirections != null && OfflineGraphEngine.isLoaded()) {
                        routePlanTick++
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(400)
                            routePlanTick++
                        }
                    } else {
                        routePlanTick++
                    }
                },
                onFailure = { error ->
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.directions_offline_import_failed,
                            error.userMessage(),
                        ),
                        Toast.LENGTH_LONG,
                    ).show()
                },
            )
        }
    }

    val graphFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri == null) {
            graphImportInProgress = false
            return@rememberLauncherForActivityResult
        }
        persistGraphUri(uri)
        runOfflineGraphImport(R.string.directions_offline_import_copying_folder) {
            OfflineGraphEngine.importGraphFolder(
                context = context,
                resolver = context.contentResolver,
                folderTreeUri = uri,
            ) { progress ->
                coroutineScope.launch(Dispatchers.Main.immediate) {
                    graphImportStatusMessage = progress.toDisplayString(context)
                    graphImportProgressPercent = progress.percent
                }
            }
        }
    }

    val graphZipPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            graphImportInProgress = false
            return@rememberLauncherForActivityResult
        }
        persistGraphUri(uri)
        runOfflineGraphImport(R.string.directions_offline_import_extracting) {
            OfflineGraphEngine.importGraphZip(
                context = context,
                resolver = context.contentResolver,
                zipUri = uri,
            ) { progress ->
                coroutineScope.launch(Dispatchers.Main.immediate) {
                    graphImportStatusMessage = progress.toDisplayString(context)
                    graphImportProgressPercent = progress.percent
                }
            }
        }
    }

    LaunchedEffect(offlineGraphLoaded) {
        if (OfflineGraphEngine.isLoaded()) {
            offlineGraphLoaded = true
        }
    }

    LaunchedEffect(Unit) {
        if (!DirectionsRoutingService.hasSavedGraph(context)) return@LaunchedEffect
        if (OfflineGraphEngine.isLoaded()) {
            offlineGraphLoaded = true
            return@LaunchedEffect
        }
        graphRestoreInProgress = true
        graphImportStatusMessage = context.getString(R.string.directions_offline_import_restoring)
        val loaded = withContext(Dispatchers.IO) {
            DirectionsRoutingService.awaitOfflineGraphReady(context) { progress ->
                coroutineScope.launch(Dispatchers.Main.immediate) {
                    graphImportStatusMessage = progress.toDisplayString(context)
                    graphImportProgressPercent = progress.percent
                }
            }
        }
        graphRestoreInProgress = false
        graphImportStatusMessage = ""
        graphImportProgressPercent = null
        offlineGraphLoaded = loaded
        if (loaded && activeDirections != null) {
            routePlanTick++
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
        activeRouteResult,
        activeDirections?.tripWaypoints?.map { it.id },
        isNavigationActive,
    ) {
        if (isNavigationActive) return@LaunchedEffect
        val runtime = controller.mapRuntime ?: return@LaunchedEffect
        val (_, style) = runtime
        val directions = activeDirections ?: return@LaunchedEffect
        val route = activeRouteResult ?: return@LaunchedEffect
        if (route.geometry.size < 2) return@LaunchedEffect
        val mv = mapViewRef.value
        val tripOrigin = directions.tripWaypoints.first()
        val tripStops = directions.tripWaypoints.drop(1)
        val redraw = Runnable {
            DirectionsRouteOverlay.sync(
                style = style,
                origin = tripOrigin,
                stops = tripStops,
                valhallaRoute = route,
                revealProgress = 1f,
            )
            controller.mapOverlayCameraTick = controller.mapOverlayCameraTick + 1
        }
        if (mv != null) mv.post(redraw) else redraw.run()
    }

    LaunchedEffect(
        controller.mapRuntime,
        graphImportInProgress,
        graphRestoreInProgress,
    ) {
        if (!graphImportInProgress && !graphRestoreInProgress) return@LaunchedEffect
        val runtime = controller.mapRuntime ?: return@LaunchedEffect
        val (_, style) = runtime
        val mv = mapViewRef.value
        activeRouteResult = null
        activeRouteSource = null
        val clear = { DirectionsRouteOverlay.remove(style) }
        if (mv != null) mv.post(clear) else clear()
    }

    LaunchedEffect(
        controller.mapRuntime,
        activeDirections?.origin?.id,
        activeDirections?.stops?.map { it.id },
        activeDirections?.tripWaypoints?.map { it.id },
        activeDirections?.travelMode,
        bottomChromePadding,
        routePlanTick,
        offlineGraphLoaded,
        graphImportInProgress,
        graphRestoreInProgress,
        isNavigationActive,
    ) {
        if (isNavigationActive) return@LaunchedEffect
        if (graphImportInProgress || graphRestoreInProgress) return@LaunchedEffect
        val runtime = controller.mapRuntime ?: return@LaunchedEffect
        val (map, style) = runtime
        val mv = mapViewRef.value
        val directions = activeDirections
        if (directions == null) {
            controller.activeRouteGeometry = null
            activeRouteResult = null
            activeRouteSource = null
            isRouteCalculating = false
            isRouteRefining = false
            val clear = { DirectionsRouteOverlay.remove(style) }
            if (mv != null) mv.post(clear) else clear()
            return@LaunchedEffect
        }
        if (directions.tripWaypoints.size < 2) {
            activeRouteResult = null
            activeRouteSource = null
            isRouteCalculating = false
            isRouteRefining = false
            val clear = { DirectionsRouteOverlay.remove(style) }
            if (mv != null) mv.post(clear) else clear()
            return@LaunchedEffect
        }

        delay(DirectionsRouteAnimation.FETCH_DEBOUNCE_MS)
        isRouteCalculating = true
        isRouteRefining = DirectionsRoutingService.hasSavedGraph(context) &&
            !OfflineGraphEngine.isLoaded()

        val valhallaRoute = ValhallaRouteClient.fetchRoute(waypoints, directions.travelMode)
        val fullGeometry = valhallaRoute?.geometry?.takeIf { it.size >= 2 }
            ?: DirectionsPathOptimizer.buildPolyline(waypoints, segmentsPerLeg = 26)
        controller.activeRouteGeometry = fullGeometry.takeIf { it.size >= 2 }
        if (controller.activeNearbyCategory != null &&
            controller.nearbySearchContext is NearbySearchContext.AlongRoute &&
            controller.activeRouteGeometry != null
        ) {
            controller.applyNearbySearchContext(
                NearbySearchContext.AlongRoute(controller.activeRouteGeometry!!),
            )
        }
        val fitPoints = buildList {
            addAll(waypoints)
            addAll(fullGeometry)
        val planOutcome = withContext(Dispatchers.IO) {
            DirectionsRoutingService.planDirectionsRoute(
                context = context,
                origin = directions.origin,
                stops = directions.stops,
                mode = directions.travelMode,
                tripWaypoints = directions.tripWaypoints,
            ) { progress ->
                coroutineScope.launch(Dispatchers.Main.immediate) {
                    if (!OfflineGraphEngine.isLoaded()) {
                        graphRestoreInProgress = true
                        graphImportStatusMessage = progress.toDisplayString(context)
                        graphImportProgressPercent = progress.percent
                    }
                }
            }
        }

        graphRestoreInProgress = false
        graphImportStatusMessage = ""
        graphImportProgressPercent = null
        isRouteCalculating = false
        isRouteRefining = false

        val useFullTrip = directions.tripWaypoints.size >= 2
        val displayStops = if (
            !useFullTrip &&
            planOutcome.optimizedStops.map { it.id } != directions.stops.map { it.id }
        ) {
            sheetStack.updateDirections(
                directions.origin,
                planOutcome.optimizedStops,
                travelMode = directions.travelMode,
            )
            planOutcome.optimizedStops
        } else {
            directions.stops
        }

        val route = planOutcome.result
        if (route == null || route.geometry.size < 2) {
            activeRouteResult = null
            activeRouteSource = null
            val clear = { DirectionsRouteOverlay.remove(style) }
            if (mv != null) mv.post(clear) else clear()
            if (routePlanTick > 0) {
                val offlineGraphReady = OfflineGraphEngine.isLoaded()
                val message = when {
                    DirectionsRoutingService.hasSavedGraph(context) && offlineGraphReady ->
                        context.getString(R.string.directions_offline_route_failed)
                    planOutcome.source == DirectionsRouteSource.Unavailable && !offlineGraphReady ->
                        null
                    planOutcome.source == DirectionsRouteSource.Unavailable ->
                        context.getString(R.string.directions_routing_unavailable)
                    else -> context.getString(R.string.directions_route_failed)
                }
                message?.let { text ->
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                }
            }
            return@LaunchedEffect
        }

        activeRouteResult = route
        activeRouteSource = planOutcome.source
        offlineGraphLoaded = OfflineGraphEngine.isLoaded()

        val bottomPaddingPx = with(density) { bottomChromePaddingState.value.roundToPx() }

        val tripOrigin = directions.tripWaypoints.first()
        val tripStops = directions.tripWaypoints.drop(1)

        fun applyFrame(progress: Float) {
            DirectionsRouteOverlay.sync(
                style = style,
                origin = tripOrigin,
                stops = tripStops,
                valhallaRoute = route,
                revealProgress = progress,
            )
            controller.mapOverlayCameraTick = controller.mapOverlayCameraTick + 1
        }

        // Draw full route immediately so it stays visible if reveal animation is interrupted.
        val showFullRoute = Runnable { applyFrame(1f) }
        if (mv != null) {
            mv.post(showFullRoute)
        } else {
            showFullRoute.run()
        }

        val fitPoints = buildList {
            addAll(directions.tripWaypoints.map { it.latLng })
            addAll(route.geometry)
        }
        val routeBounds = DirectionsRouteGeometry.boundsFor(fitPoints)

        val revealDuration = when (planOutcome.source) {
            DirectionsRouteSource.Preview -> DirectionsRouteAnimation.REFINE_REVEAL_DURATION_MS
            else -> DirectionsRouteAnimation.REVEAL_DURATION_MS
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
                DirectionsRouteAnimation.animateReveal(durationMs = revealDuration) { progress ->
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
            navigationEngine.stop()
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
    val navZoomIn: () -> Unit = {
        navCameraHolder?.adjustZoom(DirectionsNavConfig.ZOOM_STEP)
        refreshNavCamera()
    }
    val navZoomOut: () -> Unit = {
        navCameraHolder?.adjustZoom(-DirectionsNavConfig.ZOOM_STEP)
        refreshNavCamera()
    }
    val navTiltUp: () -> Unit = {
        navCameraHolder?.adjustTilt(DirectionsNavConfig.TILT_STEP_DEG)
        navTiltDegrees = navCameraHolder?.tiltDegrees ?: navTiltDegrees
        refreshNavCamera()
    }
    val navTiltDown: () -> Unit = {
        navCameraHolder?.adjustTilt(-DirectionsNavConfig.TILT_STEP_DEG)
        navTiltDegrees = navCameraHolder?.tiltDegrees ?: navTiltDegrees
        refreshNavCamera()
    }

    val isNavigationActiveRef = rememberUpdatedState(isNavigationActive)
    val navCameraHolderRef = rememberUpdatedState(navCameraHolder)
    val navVehiclePositionRef = rememberUpdatedState(navVehiclePosition)
    val navVehicleBearingRef = rememberUpdatedState(navVehicleBearing)

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
                                    map.addOnCameraMoveStartedListener { reason ->
                                        if (
                                            isNavigationActiveRef.value &&
                                            reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE
                                        ) {
                                            navUserZoomGestureRef.value = true
                                        }
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
                                        if (isNavigationActiveRef.value && navUserZoomGestureRef.value) {
                                            navUserZoomGestureRef.value = false
                                            val pos = navVehiclePositionRef.value
                                            val camera = navCameraHolderRef.value
                                            if (pos != null && camera != null) {
                                                val frame = DirectionsNavFrame(
                                                    lat = pos.latitude,
                                                    lng = pos.longitude,
                                                    bearingDegrees = navVehicleBearingRef.value.toDouble(),
                                                    cumulativeDistanceM = 0.0,
                                                )
                                                camera.syncUserZoomFromMap(
                                                    frame,
                                                    map.cameraPosition.zoom,
                                                )
                                                camera.follow(frame)
                                            }
                                        }
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

            if (!isNavigationActive) {
                DirectionsWaypointMarkersOverlay(
                    directions = activeDirections,
                    map = controller.mapLibreMap,
                    cameraTick = controller.mapOverlayCameraTick,
                )
            }

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
                            val currentDestinationId = directions?.destination?.id
                            val primaryRouteAction =
                                if (
                                    directions == null ||
                                    place.id == directions.origin.id ||
                                    place.id == currentDestinationId
                                ) {
                                    PlaceDetailPrimaryRouteAction.Directions
                                } else {
                                    PlaceDetailPrimaryRouteAction.AddStop
                                }
                            val claimKey = place.stableClaimKey()
                            var claimButtonMode by remember(claimKey, place.isClaimEligible) {
                                mutableStateOf(
                                    when {
                                        !place.isClaimEligible && place.backendPoiId.isNullOrBlank() ->
                                            PlaceClaimButtonMode.Hidden
                                        isLoggedIn -> PlaceClaimButtonMode.Loading
                                        else -> PlaceClaimButtonMode.Claim
                                    },
                                )
                            }
                            var resolvedBackendPoiId by remember(claimKey) {
                                mutableStateOf(place.backendPoiId)
                            }

                            LaunchedEffect(claimKey, place.isClaimEligible, place.backendPoiId, isLoggedIn, authRevision) {
                                val resolution = PlaceClaimResolver.resolve(context, place, isLoggedIn)
                                claimButtonMode = resolution.mode
                                resolvedBackendPoiId = resolution.backendPoiId
                                resolution.backendPoiId?.let { poiId ->
                                    resolvedBackendPoiIds[claimKey] = poiId
                                }
                            }

                            AppleMapsPlaceDetailSheetContent(
                                place = place,
                                scrollState = scrollState,
                                contentScrollEnabled = contentScrollEnabled,
                                sheetGestures = sheetGestures,
                                onClose = {
                                    if (sheetStack.layers.size > 1) {
                                        sheetStack.pop()
                                    } else {
                                        sheetStack.clearToHome()
                                    }
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
                                        val rotated = dirs.withRotatedDestination(place)
                                        sheetStack.updateDirections(
                                            rotated.origin,
                                            rotated.stops,
                                            travelMode = dirs.travelMode,
                                            tripWaypoints = rotated.tripWaypoints,
                                        )
                                        sheetStack.popAddStopOverlays()
                                        coroutineScope.launch {
                                            if (DirectionsRoutingService.canRoute(context)) {
                                                routePlanTick++
                                            } else {
                                                onAddStopRequested()
                                            }
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
                                        val poiId = resolvedBackendPoiId
                                            ?: resolvedBackendPoiIds[claimKey]
                                        if (poiId != null) {
                                            businessEditPoiId = poiId
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
                                                category = place.category,
                                                latitude = place.latLng.latitude,
                                                longitude = place.latLng.longitude,
                                                bearerToken = token,
                                            )
                                        }
                                        when (val result = resolveResult) {
                                            is BusinessClaimClient.ResolveResult.Failure -> {
                                                Toast.makeText(
                                                    context,
                                                    result.message,
                                                    Toast.LENGTH_LONG,
                                                ).show()
                                            }
                                            is BusinessClaimClient.ResolveResult.Success -> {
                                                resolvedBackendPoiIds[claimKey] = result.poiId
                                                resolvedBackendPoiId = result.poiId
                                                if (result.canEditBusiness) {
                                                    claimButtonMode = PlaceClaimButtonMode.BusinessEdit
                                                    businessEditPoiId = result.poiId
                                                    return@launch
                                                }
                                                val statusResult = withContext(Dispatchers.IO) {
                                                    BusinessClaimClient.fetchClaimStatus(result.poiId, token)
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
                                                        claimButtonMode =
                                                            if (status.canEditBusiness) {
                                                                PlaceClaimButtonMode.BusinessEdit
                                                            } else {
                                                                PlaceClaimButtonMode.Claim
                                                            }
                                                        if (status.canEditBusiness) {
                                                            businessEditPoiId = result.poiId
                                                        } else {
                                                            claimPoiId = result.poiId
                                                            claimGuidance = status.registrationGuidance
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                onNearbyShortcutClick = { shortcut ->
                                    controller.startNearbyCategoryBrowse(
                                        shortcut,
                                        controller.nearbyContextForPlace(place),
                                    )
                                },
                                modifier = sheetModifier,
                            )
                        }

                        AppleMapSheet.NearbyBrowse -> {
                            val category = controller.activeNearbyCategory
                            if (category != null) {
                                NearbyBrowseSheetContent(
                                    sheetTheme = sheetTheme,
                                    scrollState = scrollState,
                                    contentScrollEnabled = contentScrollEnabled,
                                    sheetGestures = sheetGestures,
                                    category = category,
                                    results = filteredNearby.visibleResults,
                                    loading = controller.nearbyBrowseLoading,
                                    errorMessage = controller.nearbyBrowseError,
                                    filterState = controller.nearbyFilterState,
                                    availableChains = filteredNearby.availableChains,
                                    pickHoursByGid = nearbyPickHoursByGid,
                                    scopeOptions = controller.buildNearbyScopeOptions(),
                                    searchContext = controller.nearbySearchContext,
                                    onScopeSelected = { controller.applyNearbySearchContext(it) },
                                    onFilterChange = { controller.updateNearbyFilter(it) },
                                    onResultSelected = { controller.openNearbyPlace(it) },
                                    onClose = { controller.exitNearbyBrowse() },
                                    modifier = sheetModifier,
                                )
                            }
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
                                tripLegCount = (sheet.tripWaypoints.size - 1).coerceAtLeast(0),
                                travelMode = sheet.travelMode,
                                onTravelModeChange = { mode ->
                                    sheetStack.updateDirections(
                                        sheet.origin,
                                        sheet.stops,
                                        travelMode = mode,
                                    )
                                    routePlanTick++
                                },
                                sheetGestures = sheetGestures,
                                sheetTheme = sheetTheme,
                                scrollState = scrollState,
                                contentScrollEnabled = contentScrollEnabled,
                                onAddStopRowClick = onAddStopRequested,
                                onDismiss = {
                                    if (isNavigationActive) {
                                        endNavigationSession()
                                    }
                                    sheetStack.removeDirectionsAndAddStop()
                                    controller.selectedPlace = sheetStack.topPlaceDetail()?.place
                                    activeRouteResult = null
                                    activeRouteSource = null
                                },
                                routeResult = activeRouteResult,
                                routeSource = activeRouteSource,
                                isRouteCalculating = isRouteCalculating,
                                isRouteRefining = isRouteRefining,
                                offlineGraphLoaded = offlineGraphLoaded,
                                onImportGraphClick = {
                                    showOfflineGraphImportAlert = true
                                },
                                onNearbyShortcutClick = { shortcut ->
                                    controller.startNearbyCategoryBrowse(
                                        shortcut,
                                        controller.nearbyContextForDirections(),
                                    )
                                },
                                isNavigationActive = isNavigationActive,
                                onStartNavigation = { startNavigationSession() },
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

        if (isNavigationActive) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .zIndex(14f)
                    .navigationBarsPadding()
                    .padding(
                        end = 12.dp,
                        bottom = bottomChromePadding + 8.dp,
                    ),
                horizontalAlignment = Alignment.End,
            ) {
                MapTiltPillControl(
                    onTiltUp = navTiltUp,
                    onTiltDown = navTiltDown,
                    tiltUpContentDescription = stringResource(R.string.nav_tilt_up),
                    tiltDownContentDescription = stringResource(R.string.nav_tilt_down),
                )
                Spacer(modifier = Modifier.height(8.dp))
                MapZoomPillControl(
                    onZoomIn = navZoomIn,
                    onZoomOut = navZoomOut,
                    zoomInContentDescription = stringResource(R.string.zoom_in),
                    zoomOutContentDescription = stringResource(R.string.zoom_out),
                    surfaceColor = NavigationMapControlSurfaceColor,
                    iconTint = NavigationMapControlIconTint,
                    dividerColor = NavigationMapControlDividerColor,
                )
            }
            Button(
                onClick = { endNavigationSession() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(15f)
                    .statusBarsPadding()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = sheetTheme.mapControlGlass,
                    contentColor = sheetTheme.primaryText,
                ),
            ) {
                Text(
                    text = stringResource(R.string.apple_directions_end_navigation),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
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

        OfflineGraphImportProgressOverlay(
            visible = graphImportInProgress || graphRestoreInProgress,
            statusMessage = graphImportStatusMessage.ifBlank {
                stringResource(
                    if (graphRestoreInProgress) {
                        R.string.directions_offline_import_restoring
                    } else {
                        R.string.directions_offline_import_in_progress
                    },
                )
            },
            progressPercent = graphImportProgressPercent,
        )

        if (showOfflineGraphImportAlert) {
            OfflineGraphImportAlertDialog(
                onDismiss = { showOfflineGraphImportAlert = false },
                onImportFolderClick = {
                    graphImportInProgress = true
                    graphFolderPicker.launch(null)
                },
                onImportZipClick = {
                    graphImportInProgress = true
                    graphZipPicker.launch(
                        arrayOf(
                            "application/zip",
                            "application/x-zip-compressed",
                            "*/*",
                        ),
                    )
                },
                isImporting = graphImportInProgress,
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

        // Last in stack: modal window above map (AndroidView), sheets, auth, and other dialogs.
        OfflineRoutingRequiredModal(
            visible = showOfflineRoutingRequiredAlert,
            onDismiss = { showOfflineRoutingRequiredAlert = false },
        )
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
