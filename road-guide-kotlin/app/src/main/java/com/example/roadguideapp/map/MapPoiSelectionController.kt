package com.example.roadguideapp.map

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.PropertyValue
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

/**
 * POI selection: temporarily excludes the tapped feature from its template symbol layer, then draws
 * (1) a small anchor dot, (2) the same sprite ~2× with an upward offset, and (3) larger name text,
 * using the template layer’s icon/text paint so each POI type keeps its style icon and colors.
 */
internal data class MapPlacePick(
    val detail: MapPlaceDetail,
    val feature: Feature,
    val templateLayerId: String,
)

internal object MapPoiSelectionController {

    private const val DOT_LAYER_ID = "roadguide_poi_sel_dot"
    private const val SYM_LAYER_ID = "roadguide_poi_sel_symbol"
    private const val NEARBY_DOT_PREFIX = "roadguide_nearby_dot_"
    private const val NEARBY_SYM_PREFIX = "roadguide_nearby_sym_"
    private const val NEARBY_CATEGORY_SOURCE_ID = "roadguide_nearby_category_src"
    private const val NEARBY_CATEGORY_SYMBOL_LAYER_ID = "roadguide_nearby_category_symbol"
    private const val SEARCH_SOURCE_ID = "roadguide_search_place_src"
    private const val SEARCH_DOT_LAYER_ID = "roadguide_search_place_dot"
    private const val SEARCH_SYM_LAYER_ID = "roadguide_search_place_symbol"

    private data class Active(
        val style: Style,
        val baseLayerId: String,
        val originalFilter: Expression?,
        val overlayLayerIds: List<String>,
    )

    private data class NearbyGroup(
        val baseLayerId: String,
        val originalFilter: Expression?,
        val overlayLayerIds: List<String>,
    )

    private data class NearbyActive(
        val style: Style,
        val groups: List<NearbyGroup>,
        val categoryOverlayIds: List<String> = emptyList(),
    )

    private data class SearchActive(
        val style: Style,
        val placeId: String,
    )

    private var active: Active? = null
    private var nearbyActive: NearbyActive? = null
    private var searchActive: SearchActive? = null

    fun discardActive() {
        active = null
        nearbyActive = null
        searchActive = null
    }

    fun clear(style: Style) {
        clearSearchPlace(style)
        val a = active ?: return
        if (a.style !== style) {
            active = null
            return
        }
        runCatching {
            val sym = style.getLayer(a.baseLayerId) as? SymbolLayer ?: return@runCatching
            restoreTemplateFilter(sym, a.originalFilter)
        }
        for (id in a.overlayLayerIds.asReversed()) {
            runCatching { style.removeLayer(id) }
        }
        active = null
    }

    fun clearSearchPlace(style: Style) {
        val search = searchActive ?: return
        if (search.style !== style) {
            searchActive = null
            return
        }
        for (id in listOf(SEARCH_SYM_LAYER_ID, SEARCH_DOT_LAYER_ID)) {
            runCatching { style.removeLayer(id) }
        }
        runCatching { style.removeSource(SEARCH_SOURCE_ID) }
        searchActive = null
    }

    fun clearNearbyHighlights(style: Style) {
        removeNearbyCategoryOverlay(style)
        val nearby = nearbyActive ?: return
        if (nearby.style !== style) {
            nearbyActive = null
            return
        }
        for (group in nearby.groups) {
            runCatching {
                val sym = style.getLayer(group.baseLayerId) as? SymbolLayer ?: return@runCatching
                restoreTemplateFilter(sym, group.originalFilter)
            }
            for (id in group.overlayLayerIds.asReversed()) {
                runCatching { style.removeLayer(id) }
            }
        }
        nearbyActive = null
    }

    /** Removes legacy MapLibre symbol layers; nearby markers render in [NearbyCategoryMarkersOverlay]. */
    fun clearNearbyCategoryMapMarkers(style: Style) {
        removeNearbyCategoryOverlay(style)
    }

    /** Idempotent teardown for GeoJSON nearby markers (safe if state was lost after a crash). */
    private fun removeNearbyCategoryOverlay(style: Style) {
        runCatching { style.removeLayer(NEARBY_CATEGORY_SYMBOL_LAYER_ID) }
        runCatching { style.removeLayer("roadguide_nearby_category_circle") }
        runCatching { style.removeLayer("roadguide_nearby_category_label") }
        runCatching { style.removeSource(NEARBY_CATEGORY_SOURCE_ID) }
    }

    private fun restoreTemplateFilter(layer: SymbolLayer, original: Expression?) {
        if (original != null) {
            layer.setFilter(original)
        } else {
            // Style default: no filter — use a no-op filter that matches all features.
            layer.setFilter(Expression.eq(Expression.literal(1), Expression.literal(1)))
        }
    }

    /**
     * Resolves which openmaptiles symbol layer was hit and builds [MapPlacePick] for overlay install.
     */
    fun resolvePick(
        context: Context,
        style: Style,
        map: MapLibreMap,
        screenPoint: PointF,
        clickLatLng: LatLng,
    ): MapPlacePick? {
        val layerIds = enumeratePoiTemplateLayerIds(style)
        if (layerIds.isEmpty()) return null
        for (layerId in layerIds) {
            val feats = map.queryRenderedFeatures(screenPoint, layerId)
            for (f in feats) {
                val detail = MapPlaceDetail.fromMapFeature(context, f, clickLatLng) ?: continue
                return MapPlacePick(detail = detail, feature = f, templateLayerId = layerId)
            }
        }
        return null
    }

    /**
     * Resolves a map POI near [latLng] (for Pelias hits or list selection without a screen tap).
     */
    fun resolvePickNear(
        context: Context,
        style: Style,
        map: MapLibreMap,
        latLng: LatLng,
    ): MapPlacePick? {
        val screen = map.projection.toScreenLocation(latLng)
        resolvePick(context, style, map, screen, latLng)?.let { return it }

        val pad = 14f
        val box = RectF(
            screen.x - pad,
            screen.y - pad,
            screen.x + pad,
            screen.y + pad,
        )
        for (layerId in enumeratePoiTemplateLayerIds(style)) {
            val feats = map.queryRenderedFeatures(box, layerId)
            for (feature in feats) {
                val detail = MapPlaceDetail.fromMapFeature(context, feature, latLng) ?: continue
                return MapPlacePick(detail = detail, feature = feature, templateLayerId = layerId)
            }
        }
        return null
    }

    /** Icon scale for category browse (many POIs on map at once). */
    private const val NEARBY_BROWSE_ICON_SCALE = 1.5f

    /**
     * Highlights many POIs with slightly enlarged icons (no name labels).
     */
    fun applyNearbyHighlights(
        style: Style,
        picks: List<MapPlacePick>,
        density: Float,
    ): Boolean {
        clearNearbyHighlights(style)
        clear(style)
        if (picks.isEmpty()) return false

        val liftPx = (-18f * density.coerceIn(1f, 4f)).coerceIn(-28f, -10f)
        val groups = ArrayList<NearbyGroup>()

        for ((templateLayerId, layerPicks) in picks.groupBy { it.templateLayerId }) {
            val template = style.getLayer(templateLayerId) as? SymbolLayer ?: continue
            val sourceLayer = resolveSourceLayer(template) ?: continue
            val matches = layerPicks.mapNotNull { matchExpressionForFeature(it.feature) }
            if (matches.isEmpty()) continue

            val highlightMatch = combineMatchExpressions(matches)
            val originalFilter = runCatching { template.filter }.getOrNull()
            val combinedExclude = excludeSelectedExpression(originalFilter, highlightMatch)

            runCatching { template.setFilter(combinedExclude) }
                .onFailure { continue }

            val suffix = sanitizeLayerSuffix(templateLayerId)
            val dotId = NEARBY_DOT_PREFIX + suffix
            val symId = NEARBY_SYM_PREFIX + suffix

            val dot = CircleLayer(dotId, AppMapStyle.OPENMAPTILES_SOURCE_ID)
                .withSourceLayer(sourceLayer)
                .withFilter(highlightMatch)
                .withProperties(
                    PropertyFactory.circleRadius(4.2f),
                    PropertyFactory.circleOpacity(0.95f),
                    circleColorFromTemplate(template),
                    PropertyFactory.circleStrokeWidth(1.2f),
                    PropertyFactory.circleStrokeColor(Expression.literal("#FFFFFF")),
                    PropertyFactory.circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
                )
            copyLayerZoomRange(template, dot)

            val overlay = SymbolLayer(symId, AppMapStyle.OPENMAPTILES_SOURCE_ID)
                .withSourceLayer(sourceLayer)
                .withFilter(highlightMatch)
            copyLayerZoomRange(template, overlay)

            val props = buildNearbyBrowseOverlaySymbolProperties(template, liftPx)
            if (props.isEmpty()) {
                restoreTemplateFilter(template, originalFilter)
                continue
            }
            overlay.withProperties(*props.toTypedArray())

            runCatching {
                style.addLayer(dot)
                style.addLayer(overlay)
            }.onFailure {
                restoreTemplateFilter(template, originalFilter)
                continue
            }

            groups.add(
                NearbyGroup(
                    baseLayerId = templateLayerId,
                    originalFilter = originalFilter,
                    overlayLayerIds = listOf(dotId, symId),
                ),
            )
        }

        if (groups.isEmpty()) return false
        nearbyActive = NearbyActive(style = style, groups = groups)
        return true
    }

    /**
     * Pelias-only selection (no vector POI hit): enlarged icon + name label via GeoJSON overlay.
     */
    fun applySearchPlace(style: Style, place: MapPlaceDetail, density: Float): Boolean {
        clearNearbyHighlights(style)
        clear(style)

        val point = Point.fromLngLat(place.latLng.longitude, place.latLng.latitude)
        val feature = Feature.fromGeometry(point)
        feature.addStringProperty("name", place.name)

        val liftPx = (-32f * density.coerceIn(1f, 4f)).coerceIn(-46f, -18f)
        val template = enumeratePoiTemplateLayerIds(style)
            .firstOrNull()
            ?.let { style.getLayer(it) as? SymbolLayer }

        runCatching {
            style.addSource(GeoJsonSource(SEARCH_SOURCE_ID, feature))
        }.onFailure { return false }

        val dot = CircleLayer(SEARCH_DOT_LAYER_ID, SEARCH_SOURCE_ID)
            .withProperties(
                PropertyFactory.circleRadius(4.2f),
                PropertyFactory.circleOpacity(0.95f),
                PropertyFactory.circleColor(Expression.literal("#8D6E63")),
                PropertyFactory.circleStrokeWidth(1.2f),
                PropertyFactory.circleStrokeColor(Expression.literal("#FFFFFF")),
                PropertyFactory.circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
            )

        val overlay = SymbolLayer(SEARCH_SYM_LAYER_ID, SEARCH_SOURCE_ID)
        val props = ArrayList<PropertyValue<*>>()
        template?.let { tmpl ->
            fun PropertyValue<*>.toExpression(): Expression? = propertyValueToExpression(this)
            tmpl.iconImage.toExpression()?.let { img ->
                props.add(PropertyFactory.iconImage(img))
                props.add(PropertyFactory.iconAllowOverlap(true))
                props.add(PropertyFactory.iconIgnorePlacement(true))
                tmpl.iconColor.toExpression()?.let { props.add(PropertyFactory.iconColor(it)) }
                val baseSize = tmpl.iconSize.toExpression() ?: Expression.literal(1.0)
                props.add(
                    PropertyFactory.iconSize(Expression.product(baseSize, Expression.literal(4.0))),
                )
                props.add(PropertyFactory.iconTranslate(arrayOf(0f, liftPx)))
                props.add(PropertyFactory.iconHaloColor(Expression.literal("#FFFFFF")))
                props.add(PropertyFactory.iconHaloWidth(2.2f))
                props.add(PropertyFactory.iconHaloBlur(0.35f))
            }
        }
        props.add(PropertyFactory.textField(Expression.get("name")))
        props.add(PropertyFactory.textAllowOverlap(true))
        props.add(PropertyFactory.textIgnorePlacement(true))
        props.add(PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP))
        props.add(PropertyFactory.textOffset(arrayOf(0f, 0.45f)))
        props.add(PropertyFactory.textSize(14f))
        props.add(PropertyFactory.textColor(Expression.literal("#1C1C1E")))
        props.add(PropertyFactory.textHaloColor(Expression.literal("#FFFFFF")))
        props.add(PropertyFactory.textHaloWidth(2f))
        overlay.withProperties(*props.toTypedArray())

        runCatching {
            style.addLayer(dot)
            style.addLayer(overlay)
        }.onFailure {
            clearSearchPlace(style)
            return false
        }

        searchActive = SearchActive(style = style, placeId = place.id)
        return true
    }

    fun apply(style: Style, pick: MapPlacePick, density: Float): Boolean {
        clearNearbyHighlights(style)
        clear(style)
        val template = style.getLayer(pick.templateLayerId) as? SymbolLayer ?: return false
        val sourceLayer = resolveSourceLayer(template) ?: return false

        val originalFilter = runCatching { template.filter }.getOrNull()
        val match = matchExpressionForFeature(pick.feature) ?: return false
        val combinedExclude = excludeSelectedExpression(originalFilter, match)

        runCatching { template.setFilter(combinedExclude) }
            .onFailure { return false }

        val liftPx = (-32f * density.coerceIn(1f, 4f)).coerceIn(-46f, -18f)

        val dot = CircleLayer(DOT_LAYER_ID, AppMapStyle.OPENMAPTILES_SOURCE_ID)
            .withSourceLayer(sourceLayer)
            .withFilter(match)
            .withProperties(
                PropertyFactory.circleRadius(4.2f),
                PropertyFactory.circleOpacity(0.95f),
                circleColorFromTemplate(template),
                PropertyFactory.circleStrokeWidth(1.2f),
                PropertyFactory.circleStrokeColor(Expression.literal("#FFFFFF")),
                PropertyFactory.circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
            )
        copyLayerZoomRange(template, dot)

        val overlay = SymbolLayer(SYM_LAYER_ID, AppMapStyle.OPENMAPTILES_SOURCE_ID)
            .withSourceLayer(sourceLayer)
            .withFilter(match)
        copyLayerZoomRange(template, overlay)

        val props = buildOverlaySymbolProperties(template, liftPx)
        if (props.isEmpty()) {
            restoreTemplateFilter(template, originalFilter)
            return false
        }
        overlay.withProperties(*props.toTypedArray())

        runCatching {
            style.addLayer(dot)
            style.addLayer(overlay)
        }.onFailure {
            restoreTemplateFilter(template, originalFilter)
            return false
        }

        active = Active(
            style = style,
            baseLayerId = pick.templateLayerId,
            originalFilter = originalFilter,
            overlayLayerIds = listOf(DOT_LAYER_ID, SYM_LAYER_ID),
        )
        return true
    }

    internal fun poiTemplateLayerIds(style: Style): List<String> = enumeratePoiTemplateLayerIds(style)

    private fun enumeratePoiTemplateLayerIds(style: Style): List<String> {
        val ordered = style.layers
        val out = ArrayList<String>()
        for (i in ordered.indices.reversed()) {
            val layer = ordered[i]
            if (layer !is SymbolLayer) continue
            if (layer.sourceId != AppMapStyle.OPENMAPTILES_SOURCE_ID) continue
            if (layer.id.startsWith("roadguide_")) continue
            val sl = resolveSourceLayer(layer) ?: continue
            val poiLike = layer.id.contains("poi", ignoreCase = true) ||
                sl.contains("poi", ignoreCase = true)
            if (!poiLike) continue
            out.add(layer.id)
        }
        return out
    }

    private fun resolveSourceLayer(layer: SymbolLayer): String? {
        val sl = runCatching { layer.sourceLayer }.getOrNull() ?: return null
        return sl.takeIf { it.isNotBlank() }
    }

    private fun propertyValueIsExpression(pv: PropertyValue<*>): Boolean {
        return try {
            PropertyValue::class.java.getMethod("isExpression").invoke(pv) as Boolean
        } catch (_: Exception) {
            false
        }
    }

    private fun propertyValueLiteral(pv: PropertyValue<*>): Any? {
        return try {
            if (PropertyValue::class.java.getMethod("isValue").invoke(pv) as Boolean) {
                PropertyValue::class.java.getMethod("getValue").invoke(pv)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun propertyValueToExpression(pv: PropertyValue<*>): Expression? {
        return try {
            when {
                PropertyValue::class.java.getMethod("isExpression").invoke(pv) as Boolean ->
                    PropertyValue::class.java.getMethod("getExpression").invoke(pv) as Expression
                PropertyValue::class.java.getMethod("isValue").invoke(pv) as Boolean -> {
                    val v = PropertyValue::class.java.getMethod("getValue").invoke(pv)
                    Expression.literal(v as Any)
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun copyLayerZoomRange(from: SymbolLayer, to: Layer) {
        runCatching {
            to.minZoom = from.minZoom
            to.maxZoom = from.maxZoom
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun circleColorFromTemplate(template: SymbolLayer): PropertyValue<String> {
        val iconColor = runCatching { template.iconColor }.getOrNull()
        return when {
            iconColor == null ->
                PropertyFactory.circleColor(Expression.literal("#8D6E63")) as PropertyValue<String>
            propertyValueIsExpression(iconColor) ->
                PropertyFactory.circleColor(
                    propertyValueToExpression(iconColor)!!,
                ) as PropertyValue<String>
            propertyValueLiteral(iconColor) != null ->
                PropertyFactory.circleColor(propertyValueLiteral(iconColor) as String) as PropertyValue<String>
            else ->
                PropertyFactory.circleColor(Expression.literal("#8D6E63")) as PropertyValue<String>
        }
    }

    private fun buildNearbyBrowseOverlaySymbolProperties(
        template: SymbolLayer,
        iconLiftPx: Float,
    ): List<PropertyValue<*>> {
        val props = ArrayList<PropertyValue<*>>()

        fun PropertyValue<*>.toExpression(): Expression? = propertyValueToExpression(this)

        template.iconImage.toExpression()?.let { img ->
            props.add(PropertyFactory.iconImage(img))
            props.add(PropertyFactory.iconAllowOverlap(true))
            props.add(PropertyFactory.iconIgnorePlacement(true))
            template.iconColor.toExpression()?.let { props.add(PropertyFactory.iconColor(it)) }
            template.iconOpacity.toExpression()?.let { props.add(PropertyFactory.iconOpacity(it)) }
            template.iconHaloColor.toExpression()?.let { props.add(PropertyFactory.iconHaloColor(it)) }
            template.iconHaloWidth.toExpression()?.let { props.add(PropertyFactory.iconHaloWidth(it)) }
            template.iconHaloBlur.toExpression()?.let { props.add(PropertyFactory.iconHaloBlur(it)) }
            template.iconPitchAlignment.toExpression()?.let { props.add(PropertyFactory.iconPitchAlignment(it)) }
            template.iconRotationAlignment.toExpression()?.let {
                props.add(PropertyFactory.iconRotationAlignment(it))
            }
            template.iconPadding.toExpression()?.let { props.add(PropertyFactory.iconPadding(it)) }
            template.iconKeepUpright.toExpression()?.let { props.add(PropertyFactory.iconKeepUpright(it)) }

            val baseSize = template.iconSize.toExpression() ?: Expression.literal(1.0)
            props.add(
                PropertyFactory.iconSize(
                    Expression.product(baseSize, Expression.literal(NEARBY_BROWSE_ICON_SCALE.toDouble())),
                ),
            )
            props.add(PropertyFactory.iconTranslate(arrayOf(0f, iconLiftPx)))
            props.add(PropertyFactory.iconHaloColor(Expression.literal("#FFFFFF")))
            props.add(PropertyFactory.iconHaloWidth(1.4f))
            props.add(PropertyFactory.iconHaloBlur(0.2f))
        }

        if (props.isEmpty()) return emptyList()

        props.add(PropertyFactory.symbolSortKey(50_000f))
        template.symbolPlacement.toExpression()?.let { props.add(PropertyFactory.symbolPlacement(it)) }
        template.symbolSpacing.toExpression()?.let { props.add(PropertyFactory.symbolSpacing(it)) }
        template.symbolAvoidEdges.toExpression()?.let { props.add(PropertyFactory.symbolAvoidEdges(it)) }
        return props
    }

    private fun buildOverlaySymbolProperties(
        template: SymbolLayer,
        iconLiftPx: Float,
    ): List<PropertyValue<*>> {
        val props = ArrayList<PropertyValue<*>>()

        fun PropertyValue<*>.toExpression(): Expression? = propertyValueToExpression(this)

        template.iconImage.toExpression()?.let { img ->
            props.add(PropertyFactory.iconImage(img))
            props.add(PropertyFactory.iconAllowOverlap(true))
            props.add(PropertyFactory.iconIgnorePlacement(true))
            template.iconColor.toExpression()?.let { props.add(PropertyFactory.iconColor(it)) }
            template.iconOpacity.toExpression()?.let { props.add(PropertyFactory.iconOpacity(it)) }
            template.iconHaloColor.toExpression()?.let { props.add(PropertyFactory.iconHaloColor(it)) }
            template.iconHaloWidth.toExpression()?.let { props.add(PropertyFactory.iconHaloWidth(it)) }
            template.iconHaloBlur.toExpression()?.let { props.add(PropertyFactory.iconHaloBlur(it)) }
            template.iconPitchAlignment.toExpression()?.let { props.add(PropertyFactory.iconPitchAlignment(it)) }
            template.iconRotationAlignment.toExpression()?.let {
                props.add(PropertyFactory.iconRotationAlignment(it))
            }
            template.iconPadding.toExpression()?.let { props.add(PropertyFactory.iconPadding(it)) }
            template.iconKeepUpright.toExpression()?.let { props.add(PropertyFactory.iconKeepUpright(it)) }

            val baseSize = template.iconSize.toExpression() ?: Expression.literal(1.0)
            props.add(
                PropertyFactory.iconSize(Expression.product(baseSize, Expression.literal(4.0))),
            )
            props.add(PropertyFactory.iconTranslate(arrayOf(0f, iconLiftPx)))
            props.add(PropertyFactory.iconHaloColor(Expression.literal("#FFFFFF")))
            props.add(PropertyFactory.iconHaloWidth(2.2f))
            props.add(PropertyFactory.iconHaloBlur(0.35f))
        }

        template.textField.toExpression()?.let {
            props.add(PropertyFactory.textField(it))
            props.add(PropertyFactory.textAllowOverlap(true))
            props.add(PropertyFactory.textIgnorePlacement(true))
            template.textFont.toExpression()?.let { tf -> props.add(PropertyFactory.textFont(tf)) }
            template.textColor.toExpression()?.let { c -> props.add(PropertyFactory.textColor(c)) }
            template.textHaloColor.toExpression()?.let { c -> props.add(PropertyFactory.textHaloColor(c)) }
            template.textHaloWidth.toExpression()?.let { w -> props.add(PropertyFactory.textHaloWidth(w)) }
            template.textHaloBlur.toExpression()?.let { b -> props.add(PropertyFactory.textHaloBlur(b)) }
            template.textOpacity.toExpression()?.let { o -> props.add(PropertyFactory.textOpacity(o)) }
            template.textPitchAlignment.toExpression()?.let { p -> props.add(PropertyFactory.textPitchAlignment(p)) }
            template.textRotationAlignment.toExpression()?.let { p ->
                props.add(PropertyFactory.textRotationAlignment(p))
            }
            template.textTransform.toExpression()?.let { t -> props.add(PropertyFactory.textTransform(t)) }
            template.textOptional.toExpression()?.let { o -> props.add(PropertyFactory.textOptional(o)) }
            template.textPadding.toExpression()?.let { p -> props.add(PropertyFactory.textPadding(p)) }
            template.textJustify.toExpression()?.let { j -> props.add(PropertyFactory.textJustify(j)) }
            props.add(PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP))
            props.add(PropertyFactory.textOffset(arrayOf(0f, 0.45f)))

            val baseText = template.textSize.toExpression() ?: Expression.literal(12.0)
            props.add(PropertyFactory.textSize(baseText))
        }

        if (props.isEmpty()) return emptyList()

        props.add(PropertyFactory.symbolSortKey(100_000f))
        template.symbolPlacement.toExpression()?.let { props.add(PropertyFactory.symbolPlacement(it)) }
        template.symbolSpacing.toExpression()?.let { props.add(PropertyFactory.symbolSpacing(it)) }
        template.symbolAvoidEdges.toExpression()?.let { props.add(PropertyFactory.symbolAvoidEdges(it)) }
        return props
    }

    private fun sanitizeLayerSuffix(layerId: String): String =
        layerId.replace(Regex("[^a-zA-Z0-9_]"), "_")

    private fun combineMatchExpressions(matches: List<Expression>): Expression =
        when (matches.size) {
            1 -> matches.first()
            else -> Expression.any(*matches.toTypedArray())
        }

    private fun excludeSelectedExpression(original: Expression?, match: Expression): Expression {
        val hide = Expression.not(match)
        return if (original == null) {
            hide
        } else {
            Expression.all(original, hide)
        }
    }

    private fun matchExpressionForFeature(feature: Feature): Expression? {
        val props = feature.properties() ?: return null
        val idEl = props.get("id")
        if (idEl != null && !idEl.isJsonNull && idEl.isJsonPrimitive) {
            return idMatchExpression(idEl.asJsonPrimitive)
        }
        return nameClassMatchExpression(props)
    }

    private fun idMatchExpression(prim: JsonPrimitive): Expression {
        return if (prim.isNumber) {
            Expression.eq(Expression.get("id"), Expression.literal(prim.asDouble))
        } else {
            Expression.eq(Expression.toString(Expression.get("id")), Expression.literal(prim.asString))
        }
    }

    private fun nameClassMatchExpression(props: JsonObject): Expression? {
        val name = props.readStringKey("name")
            ?: props.readStringKey("name_en")
            ?: props.readStringKey("name:en")
            ?: return null
        val className = props.readStringKey("class")
        val subclass = props.readStringKey("subclass")
        val parts = ArrayList<Expression>()
        parts.add(Expression.eq(Expression.get("name"), Expression.literal(name)))
        if (className != null) {
            parts.add(Expression.eq(Expression.get("class"), Expression.literal(className)))
        }
        if (subclass != null) {
            parts.add(Expression.eq(Expression.get("subclass"), Expression.literal(subclass)))
        }
        return Expression.all(*parts.toTypedArray())
    }

    private fun JsonObject.readStringKey(key: String): String? {
        val el = get(key) ?: return null
        if (el.isJsonNull) return null
        return el.asString?.takeIf { it.isNotBlank() }
    }
}
