package com.example.roadguideapp.map

import kotlin.math.abs
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.BackgroundLayer
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory

internal object MapStyleRuntime {

    fun applyTimeOfDay(style: Style, mode: MapTimeOfDay) {
        val p = mode.palette

        (style.getLayer("background") as? BackgroundLayer)?.setProperties(
            PropertyFactory.backgroundColor(p.background),
        )

        forEachOverviewAndDetail(style, "water") { layer ->
            (layer as? FillLayer)?.setProperties(
                PropertyFactory.fillColor(p.water),
                PropertyFactory.fillOutlineColor(p.waterOutline),
            )
        }

        forEachOverviewAndDetail(style, "landcover") { layer ->
            (layer as? FillLayer)?.setProperties(PropertyFactory.fillColor(p.landcover))
        }

        forEachOverviewAndDetail(style, "landuse") { layer ->
            (layer as? FillLayer)?.setProperties(PropertyFactory.fillColor(p.landuse))
        }

        forEachOverviewAndDetail(style, "park") { layer ->
            (layer as? FillLayer)?.setProperties(PropertyFactory.fillColor(p.park))
        }

        MapTransportationStyle.applyRoadPalette(style, p)

        (style.getLayer(AppMapStyle.BUILDING_LAYER_ID) as? FillLayer)?.setProperties(
            PropertyFactory.fillColor(p.buildingFill),
            PropertyFactory.fillOutlineColor(p.buildingOutline),
        )

        if (mode == MapTimeOfDay.Day) {
            BuildingExtrusion.applyBasicJsonExtrusionColor(style)
        } else {
            (style.getLayer(AppMapStyle.BUILDING_3D_LAYER_ID) as? FillExtrusionLayer)?.setProperties(
                PropertyFactory.fillExtrusionColor(MapLibreColors.parseHexOrGray(p.buildingExtrusion)),
            )
        }

        forEachOverviewAndDetail(style, "boundary") { layer ->
            (layer as? LineLayer)?.setProperties(PropertyFactory.lineColor(p.boundary))
        }
    }

    private inline fun forEachOverviewAndDetail(style: Style, baseLayerId: String, block: (Layer) -> Unit) {
        style.getLayer(baseLayerId)?.let(block)
        style.getLayer(PmtilesOverviewStylePatch.overviewLayerId(baseLayerId))?.let(block)
    }

    fun apply3dVisuals(map: MapLibreMap, style: Style, enabled: Boolean) {
        syncBuilding3dVisibility(map, style, enabled)
        apply3dCameraTilt(map, enabled)
        apply3dRotationGestures(map, enabled)
    }

    /**
     * Shows Headway `building_3d` when 3D mode is on and zoom >= 13.
     *
     * On the emulator GLES stack, updating fill-extrusion while bearing changes can SIGSEGV; hide
     * extrusion only while the camera is moving, then restore it at any bearing once idle.
     */
    fun syncBuilding3dVisibility(
        map: MapLibreMap,
        style: Style,
        userWants3d: Boolean,
        suppressForCameraMotion: Boolean = false,
        activeNavigation: Boolean = false,
    ) {
        if (!MapRenderSupport.safeToShowFillExtrusionBuildings()) {
            BuildingExtrusion.setBuilding3dVisible(style, visible = false)
            return
        }

        val zoomAllows3d = map.cameraPosition.zoom >= (AppMapStyle.BUILDING_3D_MIN_ZOOM - 0.01)
        var show3dBuildings = userWants3d && zoomAllows3d
        if (MapRenderSupport.shouldSuppressExtrusionDuringCameraMotion() && !activeNavigation) {
            show3dBuildings = show3dBuildings && !suppressForCameraMotion
        }
        BuildingExtrusion.setBuilding3dVisible(style, visible = show3dBuildings)
    }

    fun resetCompassBearing(map: MapLibreMap, style: Style, userWants3d: Boolean) {
        syncBuilding3dVisibility(
            map = map,
            style = style,
            userWants3d = userWants3d,
            suppressForCameraMotion = true,
        )
        val current = map.cameraPosition
        if (abs(current.bearing) < 0.5) {
            syncBuilding3dVisibility(map, style, userWants3d, suppressForCameraMotion = false)
            return
        }
        val update = CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder(current)
                .bearing(0.0)
                .build(),
        )
        if (MapRenderSupport.isLikelyAndroidEmulator()) {
            map.moveCamera(update)
        } else {
            map.easeCamera(update, 420)
        }
    }

    private fun apply3dCameraTilt(map: MapLibreMap, enabled: Boolean) {
        val allow3dCamera = MapRenderSupport.safeToUse3dCameraTilt()
        map.uiSettings.isTiltGesturesEnabled = enabled && allow3dCamera
        if (!allow3dCamera && enabled) return

        val desiredTilt = if (enabled) 58.0 else 0.0
        val current = map.cameraPosition
        if (abs(current.tilt - desiredTilt) < 0.5) return
        val update = CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder(current)
                .tilt(desiredTilt)
                .build(),
        )
        if (MapRenderSupport.isLikelyAndroidEmulator()) {
            map.moveCamera(update)
        } else {
            map.easeCamera(update, 650)
        }
    }

    private fun apply3dRotationGestures(map: MapLibreMap, userWants3d: Boolean) {
        map.uiSettings.isRotateGesturesEnabled = when {
            !userWants3d -> true
            MapRenderSupport.allowsFingerRotationIn3d() -> true
            else -> false
        }
    }

}
