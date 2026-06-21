#!/usr/bin/env python3
from pathlib import Path

GRAPH_DATA = Path(__file__).resolve().parent
REPO_ROOT = GRAPH_DATA.parent

src = (GRAPH_DATA / "_git_MapLibreMbTilesMap_utf8.kt").read_text(encoding="utf-8")
out = REPO_ROOT / "road-guide-kotlin/app/src/main/java/com/example/roadguideapp/map/MapLibreMbTilesMap.kt"

IMPORT_BLOCK = """
import androidx.activity.ComponentActivity
import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.GoldHuntController
import com.example.roadguideapp.goldhunt.GoldHuntEntryEffect
import com.example.roadguideapp.goldhunt.GoldHuntLocationEffect
import com.example.roadguideapp.goldhunt.GoldHuntMapEffects
import com.example.roadguideapp.goldhunt.GoldHuntMapZoom
import com.example.roadguideapp.goldhunt.GoldHuntSecretPlaceEffect
import com.example.roadguideapp.goldhunt.GoldHuntTreasureEffect
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceCollectOutcome
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceMapPick
import com.example.roadguideapp.goldhunt.treasure.TreasureCollectOutcome
import com.example.roadguideapp.goldhunt.treasure.TreasureMapPick
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportLogBuilder
import com.example.roadguideapp.goldhunt.treasure.TreasureViewportNumberLogBuilder
import com.example.roadguideapp.goldhunt.ui.GoldHuntHud
import com.example.roadguideapp.goldhunt.ui.GoldHuntHudLayout
import com.example.roadguideapp.goldhunt.ui.GoldHuntMapButton
import com.example.roadguideapp.goldhunt.ui.GoldHuntWorkflowAlertHost
"""

GOLD_VARS = """
    var showGoldHuntRoutingAlert by remember { mutableStateOf(false) }
    var showGoldHuntZoomAlert by remember { mutableStateOf(false) }
    var pendingGoldHuntActivation by remember { mutableStateOf(false) }
    var mapZoomGrade by remember { mutableStateOf<MapZoomGradeState?>(null) }

    val goldHunt = remember { GoldHuntController(context) }
    val goldHuntActiveState = rememberUpdatedState(goldHunt.isActive)
    val hostActivity = context as ComponentActivity

    DisposableEffect(lifecycle, goldHunt) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                if (hostActivity.isFinishing) {
                    goldHunt.shutdown()
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            if (!hostActivity.isChangingConfigurations) {
                goldHunt.shutdown()
            }
        }
    }

    LaunchedEffect(Unit) {
        goldHunt.initialize()
    }

    val activateGoldHunt: () -> Unit = {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                goldHunt.initialize()
                withContext(Dispatchers.Main) {
                    if (!goldHunt.isActive) {
                        goldHunt.enterMode()
                    }
                }
                goldHunt.refreshProfile()
                withContext(Dispatchers.Main) {
                    goldHunt.markTreasureDirty()
                    goldHunt.markSecretDirty()
                }
            } catch (e: Exception) {
                android.util.Log.e("MapLibreMbTilesMap", "Gold Hunt activation failed", e)
                withContext(Dispatchers.Main) {
                    goldHunt.exitMode()
                }
            }
        }
    }

    val requestGoldHuntActivation: () -> Unit = {
        when {
            DirectionsRoutingService.isOfflineRoutingConfigured(context) ->
                activateGoldHunt()
            ValhallaReachability.wasProbed() && !ValhallaReachability.isReachable() ->
                showGoldHuntRoutingAlert = true
            else -> coroutineScope.launch {
                val routingReady = withContext(Dispatchers.IO) {
                    DirectionsRoutingService.goldHuntRoutingAvailable(context)
                }
                if (routingReady) {
                    activateGoldHunt()
                } else {
                    showGoldHuntRoutingAlert = true
                }
            }
        }
    }

    val toggleGoldHunt: () -> Unit = {
        if (goldHunt.isActive) {
            goldHunt.exitMode()
        } else if (!hasLocationPermission) {
            pendingGoldHuntActivation = true
            locationPermissionLauncher.launch(LocationPermissions)
        } else {
            requestGoldHuntActivation()
        }
    }
"""

MAP_TAP = """
    val mapTap: (LatLng) -> Unit = mapTap@{ latLng ->
        if (!goldHunt.isActive) {
            controller.onMapPlaceTapped(latLng)
            return@mapTap
        }
        val map = controller.mapLibreMap ?: return@mapTap
        val zoom = map.cameraPosition.zoom.toDouble()
        if (GoldHuntMapZoom.secretsVisibleAt(zoom)) {
            val secretSpec = SecretPlaceMapPick.resolve(map, latLng)
            if (secretSpec != null) {
                coroutineScope.launch {
                    goldHunt.collectSecretPlaceOnTap(secretSpec, zoom)
                }
                return@mapTap
            }
        }
        if (GoldHuntMapZoom.treasuresVisibleAt(zoom)) {
            val treasureSpec = TreasureMapPick.resolve(map, latLng)
            if (treasureSpec != null) {
                coroutineScope.launch {
                    when (val outcome = goldHunt.collectTreasureOnTap(treasureSpec)) {
                        is TreasureCollectOutcome.Collected -> {
                            if (outcome.credits > 0) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.gold_hunt_treasure_collected, outcome.credits),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                        TreasureCollectOutcome.AlreadyCollected -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.gold_hunt_treasure_already_collected),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                        TreasureCollectOutcome.NotReady -> Unit
                    }
                }
                return@mapTap
            }
        } else {
            showGoldHuntZoomAlert = true
            return@mapTap
        }
        controller.onMapPlaceTapped(latLng)
    }
"""

EFFECTS = """
    GoldHuntLocationEffect(context, controller, goldHunt, hasLocationPermission)
    GoldHuntTreasureEffect(context, controller, goldHunt, map3dEnabled = is3d)
    GoldHuntSecretPlaceEffect(context, controller, goldHunt, map3dEnabled = is3d)
    GoldHuntEntryEffect(controller, goldHunt)
"""

UI_BLOCK = """
        GoldHuntHud(
            goldHunt = goldHunt,
            isDarkAppearance = isDarkAppearance,
            bottomPadding = bottomChromePadding,
            zoomGrade = if (goldHunt.isActive) mapZoomGrade else null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(11f)
                .statusBarsPadding()
                .padding(top = GoldHuntHudLayout.scoreBarTopPadding),
        )

        GoldHuntWorkflowAlertHost(goldHunt = goldHunt)

        if (showGoldHuntZoomAlert) {
            AlertDialog(
                onDismissRequest = { showGoldHuntZoomAlert = false },
                text = {
                    Text(text = stringResource(R.string.gold_hunt_treasure_zoom_required))
                },
                confirmButton = {
                    TextButton(onClick = { showGoldHuntZoomAlert = false }) {
                        Text(text = stringResource(R.string.apple_ok))
                    }
                },
            )
        }

        if (showGoldHuntRoutingAlert) {
            AlertDialog(
                onDismissRequest = { showGoldHuntRoutingAlert = false },
                text = {
                    Text(text = stringResource(R.string.gold_hunt_routing_required))
                },
                confirmButton = {
                    TextButton(onClick = { showGoldHuntRoutingAlert = false }) {
                        Text(text = stringResource(R.string.apple_ok))
                    }
                },
            )
        }
"""

# Apply patches
src = src.replace(
    "import com.example.roadguideapp.auth.identifierAbbreviation",
    "import com.example.roadguideapp.auth.identifierAbbreviation" + IMPORT_BLOCK,
)
src = src.replace(
    "    var showOfflineRoutingRequiredAlert by remember { mutableStateOf(false) }",
    "    var showOfflineRoutingRequiredAlert by remember { mutableStateOf(false) }" + GOLD_VARS,
)
src = src.replace(
    """        if (hasLocationPermission) {
            navigateToBestAvailableLocation(
                context.applicationContext,
                controller.mapLibreMap,
                locationFallbackLatLng,
            )
        }
    }""",
    """        if (hasLocationPermission) {
            navigateToBestAvailableLocation(
                context.applicationContext,
                controller.mapLibreMap,
                locationFallbackLatLng,
            )
            if (pendingGoldHuntActivation) {
                pendingGoldHuntActivation = false
                requestGoldHuntActivation()
            }
        }
    }""",
)
src = src.replace(
    """    val onMapPlaceTapRef = remember { mutableStateOf<(LatLng) -> Unit>({}) }
    SideEffect {
        onMapPlaceTapRef.value = { latLng -> controller.onMapPlaceTapped(latLng) }
    }""",
    MAP_TAP + """
    val onMapPlaceTapRef = remember { mutableStateOf<(LatLng) -> Unit>({}) }
    SideEffect {
        onMapPlaceTapRef.value = mapTap
    }""",
)
src = src.replace(
    "    MapPlaceSelectionOverlayEffect(context, controller)",
    "    MapPlaceSelectionOverlayEffect(context, controller)\n" + EFFECTS,
)

# Camera helpers in map factory
src = src.replace(
    """                                    fun publishMapOverlayPositions() {
                                        val needsOverlay = activeDirectionsState.value != null ||
                                            (
                                                activeNearbyCategoryState.value != null &&
                                                    nearbyOverlayResultsState.value.isNotEmpty()
                                                )
                                        if (!needsOverlay) return
                                        mapView.post { controller.mapOverlayCameraTick++ }
                                    }""",
    """                                    fun publishZoomGrade() {
                                        mapView.post {
                                            mapZoomGrade = MapZoomGradeCalculator.fromMap(map, mapView)
                                        }
                                    }
                                    fun publishMapOverlayPositions() {
                                        val needsOverlay = activeDirectionsState.value != null ||
                                            (
                                                activeNearbyCategoryState.value != null &&
                                                    nearbyOverlayResultsState.value.isNotEmpty()
                                                )
                                        val goldHuntActive = goldHuntActiveState.value
                                        if (!needsOverlay && !goldHuntActive) return
                                        mapView.post { controller.mapOverlayCameraTick++ }
                                    }
                                    fun hideGoldHuntMarkersBelowMinZoom() {
                                        if (!goldHuntActiveState.value) return
                                        val style = controller.mapRuntime?.second ?: return
                                        val zoom = map.cameraPosition.zoom.toDouble()
                                        if (!GoldHuntMapZoom.treasuresVisibleAt(zoom)) {
                                            TreasureOverlay.remove(style)
                                            mapView.post {
                                                goldHunt.updateTreasureValueLog(
                                                    TreasureViewportLogBuilder.fromTreasures(emptyList(), zoom),
                                                )
                                                goldHunt.updateTreasureNumberLog(
                                                    TreasureViewportNumberLogBuilder.fromTreasures(emptyList(), zoom),
                                                )
                                            }
                                        }
                                        if (!GoldHuntMapZoom.secretsVisibleAt(zoom)) {
                                            SecretPlaceOverlay.remove(style)
                                        }
                                    }""",
)

src = src.replace(
    """                                    map.addOnCameraMoveListener {
                                        publishBearing()
                                        publishScaleRuler()
                                        publishMapOverlayPositions()
                                        syncExtrusionDuring3d(suppressForCameraMotion = true)
                                    }""",
    """                                    map.addOnCameraMoveListener {
                                        publishBearing()
                                        publishScaleRuler()
                                        publishZoomGrade()
                                        hideGoldHuntMarkersBelowMinZoom()
                                        publishMapOverlayPositions()
                                        syncExtrusionDuring3d(suppressForCameraMotion = true)
                                    }""",
)

src = src.replace(
    """                                    map.addOnCameraIdleListener {
                                        publishBearing()
                                        publishScaleRuler()
                                        publishMapOverlayPositions()""",
    """                                    map.addOnCameraIdleListener {
                                        publishBearing()
                                        publishScaleRuler()
                                        publishZoomGrade()
                                        hideGoldHuntMarkersBelowMinZoom()
                                        publishMapOverlayPositions()""",
)

# Scale ruler / overlay block
src = src.replace(
    """        if (mapReady) {
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
        }""",
    """        if (mapReady) {
            val treasureValueLog = if (goldHunt.isActive) null else goldHunt.treasureValueLog
            val treasureNumberLog = if (goldHunt.isActive) null else goldHunt.treasureNumberLog
            val showZoomGradeOverlay = mapZoomGrade != null && !goldHunt.isActive
            val hasMapOverlayMetrics =
                mapScaleRuler != null ||
                    showZoomGradeOverlay ||
                    treasureValueLog != null ||
                    treasureNumberLog != null
            if (hasMapOverlayMetrics) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .zIndex(0.5f)
                        .statusBarsPadding()
                        .padding(
                            start = 12.dp,
                            top = if (goldHunt.isActive) {
                                GoldHuntHudLayout.scaleRulerTopBelowScoreBar
                            } else {
                                8.dp
                            },
                        ),
                ) {
                    var overlayTopGap = false
                    mapScaleRuler?.let { scale ->
                        MapScaleRuler(state = scale)
                        overlayTopGap = true
                    }
                    if (showZoomGradeOverlay) {
                        mapZoomGrade?.let { grade ->
                            MapZoomGradeDisplay(
                                state = grade,
                                modifier = Modifier.padding(top = if (overlayTopGap) 6.dp else 0.dp),
                            )
                        }
                    }
                }
            }
        }""",
)

# Top right chrome zIndex
src = src.replace(
    """                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(10f)
                    .statusBarsPadding()
                    .padding(end = 12.dp, top = 8.dp),
            )
        }

        if (showBottomMapChrome) {""",
    """                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(if (goldHunt.isActive) 13f else 10f)
                    .statusBarsPadding()
                    .padding(
                        end = 12.dp,
                        top = if (goldHunt.isActive) {
                            GoldHuntHudLayout.scoreBarTopPadding
                        } else {
                            8.dp
                        },
                    ),
            )
        }

        if (showBottomMapChrome) {""",
)

# Gold hunt button bottom left
src = src.replace(
    """            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = bottomChromePadding)
                    .size(48.dp),""",
    """            GoldHuntMapButton(
                sheetTheme = sheetTheme,
                isActive = goldHunt.isActive,
                onClick = toggleGoldHunt,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = bottomChromePadding + 60.dp),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = bottomChromePadding)
                    .size(48.dp),""",
)

src = src.replace(
    """        // Last in stack: modal window above map (AndroidView), sheets, auth, and other dialogs.
        OfflineRoutingRequiredModal(""",
    UI_BLOCK + """
        // Last in stack: modal window above map (AndroidView), sheets, auth, and other dialogs.
        OfflineRoutingRequiredModal(""",
)

out.write_text(src, encoding="utf-8")
print(f"Wrote {out} ({len(src.splitlines())} lines)")
