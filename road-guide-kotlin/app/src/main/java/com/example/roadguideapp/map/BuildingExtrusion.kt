package com.example.roadguideapp.map

import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory

/**
 * Controls Headway `basic.json` **building_3d** (`fill-extrusion`) — real building height from
 * tile `render_height` / `render_min_height`, not a simplified client layer.
 */
internal object BuildingExtrusion {

    /**
     * Called once when the style loads: hide 3D buildings until the user toggles 3D, drop any
     * legacy `building_extrusion` layer, and install a fallback only if the style omits `building_3d`.
     */
    fun prepareStyle(style: Style) {
        removeLegacyRuntimeLayer(style)
        ensureBuilding3dLayer(style)
        setBuilding3dVisible(style, visible = false)
    }

    fun applyBasicJsonExtrusionColor(style: Style) {
        (style.getLayer(AppMapStyle.BUILDING_3D_LAYER_ID) as? FillExtrusionLayer)?.setProperties(
            PropertyFactory.fillExtrusionColor(basicJsonExtrusionColor()),
        )
    }

    fun setBuilding3dVisible(style: Style, visible: Boolean) {
        if (!MapRenderSupport.safeToShowFillExtrusionBuildings()) {
            visibility(style, Property.NONE)
            return
        }
        visibility(style, if (visible) Property.VISIBLE else Property.NONE)
    }

    private fun visibility(style: Style, visibility: String) {
        (style.getLayer(AppMapStyle.BUILDING_3D_LAYER_ID) as? FillExtrusionLayer)?.setProperties(
            PropertyFactory.visibility(visibility),
        )
    }

    private fun removeLegacyRuntimeLayer(style: Style) {
        if (style.getLayer(AppMapStyle.LEGACY_BUILDING_EXTRUSION_LAYER_ID) == null) return
        runCatching { style.removeLayer(AppMapStyle.LEGACY_BUILDING_EXTRUSION_LAYER_ID) }
    }

    private fun ensureBuilding3dLayer(style: Style) {
        if (style.getSource(AppMapStyle.OPENMAPTILES_SOURCE_ID) == null) return
        if (style.getLayer(AppMapStyle.BUILDING_3D_LAYER_ID) != null) return
        val layer = FillExtrusionLayer(
            AppMapStyle.BUILDING_3D_LAYER_ID,
            AppMapStyle.OPENMAPTILES_SOURCE_ID,
        ).withProperties(
            PropertyFactory.fillExtrusionColor(basicJsonExtrusionColor()),
            PropertyFactory.fillExtrusionOpacity(1f),
            PropertyFactory.fillExtrusionHeight(basicJsonExtrusionHeight()),
            PropertyFactory.fillExtrusionBase(basicJsonExtrusionBase()),
        ).withSourceLayer(AppMapStyle.BUILDING_SOURCE_LAYER)
        layer.minZoom = AppMapStyle.BUILDING_3D_MIN_ZOOM
        layer.maxZoom = AppMapStyle.BUILDING_3D_MAX_ZOOM
        layer.setFilter(
            Expression.neq(Expression.get("hide_3d"), Expression.literal(true)),
        )
        runCatching {
            style.addLayerAbove(layer, AppMapStyle.BUILDING_LAYER_ID)
        }.onFailure {
            runCatching { style.addLayer(layer) }
        }
    }

    /** Same paint as `building_3d` in Headway `basic.json`. */
    private fun basicJsonExtrusionColor(): Expression = Expression.interpolate(
        Expression.linear(),
        Expression.coalesce(
            Expression.get("render_height"),
            Expression.literal(10),
        ),
        Expression.stop(0, Expression.literal("hsl(40, 5.5%, 87%)")),
        Expression.stop(50, Expression.literal("hsl(40, 5.5%, 82%)")),
        Expression.stop(150, Expression.literal("hsl(40, 5.5%, 75%)")),
    )

    private fun basicJsonExtrusionHeight(): Expression = Expression.coalesce(
        Expression.get("render_height"),
        Expression.literal(10),
    )

    private fun basicJsonExtrusionBase(): Expression = Expression.coalesce(
        Expression.get("render_min_height"),
        Expression.literal(0),
    )
}
