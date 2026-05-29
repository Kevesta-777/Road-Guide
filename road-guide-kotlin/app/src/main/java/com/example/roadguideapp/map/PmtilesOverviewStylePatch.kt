package com.example.roadguideapp.map

import com.example.roadguideapp.BuildConfig
import org.json.JSONArray
import org.json.JSONObject

/**
 * Applies the two-tier zoom split from the product architecture:
 *
 * - Zoom 0–[OVERVIEW_PMTILES_MAX_ZOOM]: bundled PMTiles (`overview` source) — boundaries, main roads, etc.
 * - Zoom [DETAIL_TILES_MIN_ZOOM]+: Headway tileserver (`openmaptiles` / detail source) — full vector detail.
 *
 * When the tileserver is **offline**, use [OfflineMapStyleBuilder] or cached dual-tier style.
 */
internal object PmtilesOverviewStylePatch {

    const val OVERVIEW_SOURCE_ID = "overview"
    const val OVERVIEW_LAYER_PREFIX = "overview_"

    val OVERVIEW_SOURCE_LAYERS: Set<String> = setOf(
        "boundary",
        "water",
        "waterway",
        "water_name",
        "landcover",
        "landuse",
        "park",
        "transportation",
        "transportation_name",
        "place",
    )

    private val DETAIL_ONLY_SOURCE_LAYERS: Set<String> = setOf(
        "building",
        "poi",
        "housenumber",
        "aerodrome_label",
        "aeroway",
        "mountain_peak",
    )

    /**
     * Patches a Headway style loaded from the tileserver so low zoom uses PMTiles and high zoom
     * uses the tileserver only. Must run whenever the server is reachable.
     */
    fun applyOnlineDualTier(
        root: JSONObject,
        pmtilesUrl: String,
        overviewMaxZoom: Int = BuildConfig.OVERVIEW_PMTILES_MAX_ZOOM,
        detailMinZoom: Int = BuildConfig.DETAIL_TILES_MIN_ZOOM.coerceAtLeast(
            BuildConfig.OVERVIEW_PMTILES_MAX_ZOOM,
        ),
    ) {
        if (!BuildConfig.OVERVIEW_PMTILES_ENABLED) return

        val sources = root.optJSONObject("sources") ?: return
        val detailSourceId = findDetailVectorSourceId(sources) ?: return
        val detailSource = sources.optJSONObject(detailSourceId) ?: return
        if (detailSource.optString("type") != "vector") return

        root.put("layers", stripOverviewLayers(root.optJSONArray("layers") ?: JSONArray()))
        sources.remove(OVERVIEW_SOURCE_ID)

        sources.put(
            OVERVIEW_SOURCE_ID,
            JSONObject().apply {
                put("type", "vector")
                put("url", pmtilesUrl)
                put("maxzoom", overviewMaxZoom)
            },
        )
        detailSource.put("minzoom", detailMinZoom)

        val layers = root.optJSONArray("layers") ?: return
        val rebuilt = JSONArray()
        for (i in 0 until layers.length()) {
            val layer = layers.getJSONObject(i)
            if (shouldCloneOverviewLayer(layer, detailSourceId)) {
                rebuilt.put(cloneOverviewLayer(layer, overviewMaxZoom))
            }
            if (usesDetailSource(layer, detailSourceId)) {
                bumpMinZoom(layer, detailMinZoom)
            }
            rebuilt.put(layer)
        }
        root.put("layers", rebuilt)
    }

    /**
     * When the tileserver is offline, keep overview (PMTiles) layers visible at high zoom so
     * MapLibre can overzoom PMTiles where cached detail vector tiles are unavailable.
     */
    fun extendOverviewLayersForHighZoomFallback(
        root: JSONObject,
        highZoomCap: Int = 22,
    ) {
        root.optJSONObject("sources")
            ?.optJSONObject(OVERVIEW_SOURCE_ID)
            ?.put("maxzoom", highZoomCap)
        val layers = root.optJSONArray("layers") ?: return
        for (i in 0 until layers.length()) {
            val layer = layers.optJSONObject(i) ?: continue
            if (!isOverviewLayerId(layer.optString("id"))) continue
            layer.put("maxzoom", highZoomCap)
        }
    }

    /** @deprecated Use [applyOnlineDualTier] with an explicit PMTiles URL from [PmtilesOverviewSource]. */
    fun applyIfEnabled(context: android.content.Context, root: JSONObject) {
        val pmtilesUrl = PmtilesOverviewSource.resolveUrl(context.applicationContext) ?: return
        applyOnlineDualTier(root, pmtilesUrl)
    }

    fun findDetailVectorSourceId(sources: JSONObject): String? {
        if (sources.has(AppMapStyle.OPENMAPTILES_SOURCE_ID)) {
            val source = sources.optJSONObject(AppMapStyle.OPENMAPTILES_SOURCE_ID)
            if (source?.optString("type") == "vector") {
                return AppMapStyle.OPENMAPTILES_SOURCE_ID
            }
        }
        val keys = sources.keys()
        while (keys.hasNext()) {
            val id = keys.next()
            if (id == OVERVIEW_SOURCE_ID) continue
            val source = sources.optJSONObject(id) ?: continue
            if (source.optString("type") != "vector") continue
            val url = source.optString("url").lowercase()
            if (url.contains("tileserver") || url.endsWith(".json") || url.contains("/data/")) {
                return id
            }
        }
        return null
    }

    fun overviewLayerId(baseLayerId: String): String = OVERVIEW_LAYER_PREFIX + baseLayerId

    fun isOverviewLayerId(layerId: String): Boolean = layerId.startsWith(OVERVIEW_LAYER_PREFIX)

    private fun stripOverviewLayers(layers: JSONArray): JSONArray {
        val rebuilt = JSONArray()
        for (i in 0 until layers.length()) {
            val layer = layers.optJSONObject(i) ?: continue
            if (!isOverviewLayerId(layer.optString("id"))) {
                rebuilt.put(layer)
            }
        }
        return rebuilt
    }

    private fun shouldCloneOverviewLayer(layer: JSONObject, detailSourceId: String): Boolean {
        if (!usesDetailSource(layer, detailSourceId)) return false
        val sourceLayer = layer.optString("source-layer")
        if (sourceLayer in DETAIL_ONLY_SOURCE_LAYERS) return false
        if (sourceLayer.isNotBlank() && sourceLayer in OVERVIEW_SOURCE_LAYERS) return true
        val id = layer.optString("id")
        return id in OVERVIEW_LAYER_IDS_BY_ID
    }

    private fun usesDetailSource(layer: JSONObject, detailSourceId: String): Boolean {
        return layer.optString("source") == detailSourceId
    }

    private fun cloneOverviewLayer(layer: JSONObject, overviewMaxZoom: Int): JSONObject {
        val clone = JSONObject(layer.toString())
        clone.put("id", overviewLayerId(layer.getString("id")))
        clone.put("source", OVERVIEW_SOURCE_ID)
        clone.put("maxzoom", overviewMaxZoom)
        clone.remove("minzoom")
        return clone
    }

    private fun bumpMinZoom(layer: JSONObject, detailMinZoom: Int) {
        val existing = layer.optDouble("minzoom", Double.NaN)
        val bumped = if (existing.isFinite()) {
            maxOf(existing, detailMinZoom.toDouble())
        } else {
            detailMinZoom.toDouble()
        }
        layer.put("minzoom", bumped)
    }

    private val OVERVIEW_LAYER_IDS_BY_ID: Set<String> = setOf(
        "water",
        "waterway",
        "landcover",
        "landuse",
        "park",
        "boundary",
        "transportation-casing",
        "transportation",
        "place",
        "place-city",
        "place-town",
        "place-village",
        "place-suburb",
        "place-neighbourhood",
        "place-state",
        "place-country",
    )
}
