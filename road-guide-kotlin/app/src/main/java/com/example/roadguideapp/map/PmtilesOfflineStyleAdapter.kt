package com.example.roadguideapp.map

import com.example.roadguideapp.BuildConfig
import org.json.JSONArray
import org.json.JSONObject

/**
 * Adapts Headway [basic.json] for offline PMTiles: all vector layers use the overview source,
 * remote-only auxiliary sources are removed, and viewport metadata is applied.
 */
internal object PmtilesOfflineStyleAdapter {

    val REMOTE_ONLY_SOURCE_IDS: Set<String> = setOf("landcover", "terrain")

    fun apply(
        root: JSONObject,
        pmtilesUrl: String,
        metadata: PmtilesMetadataReader.Metadata?,
        detailSourceId: String = AppMapStyle.OPENMAPTILES_SOURCE_ID,
    ) {
        val sources = if (root.has("sources")) {
            root.getJSONObject("sources")
        } else {
            JSONObject().also { root.put("sources", it) }
        }

        REMOTE_ONLY_SOURCE_IDS.forEach { sources.remove(it) }
        sources.remove(PmtilesOverviewStylePatch.OVERVIEW_SOURCE_ID)
        sources.remove(detailSourceId)

        val maxZoom = metadata?.maxZoom ?: BuildConfig.OVERVIEW_PMTILES_MAX_ZOOM
        sources.put(
            PmtilesOverviewStylePatch.OVERVIEW_SOURCE_ID,
            JSONObject().apply {
                put("type", "vector")
                put("url", pmtilesUrl)
                put("maxzoom", maxZoom)
            },
        )

        val layers = if (root.has("layers")) root.getJSONArray("layers") else JSONArray()
        root.put("layers", repointLayers(layers, detailSourceId))
        applyViewport(root, metadata)
        root.put("name", "RoadGuide offline (PMTiles + bundled style)")
    }

    private fun repointLayers(layers: JSONArray, detailSourceId: String): JSONArray {
        val rebuilt = JSONArray()
        for (i in 0 until layers.length()) {
            val layer = layers.getJSONObject(i)
            val id = if (layer.has("id")) layer.getString("id") else ""
            if (PmtilesOverviewStylePatch.isOverviewLayerId(id)) continue
            val source = if (layer.has("source")) layer.getString("source") else ""
            if (source in REMOTE_ONLY_SOURCE_IDS) continue
            if (source == detailSourceId) {
                layer.put("source", PmtilesOverviewStylePatch.OVERVIEW_SOURCE_ID)
                layer.remove("minzoom")
            }
            rebuilt.put(layer)
        }
        return rebuilt
    }

    private fun applyViewport(root: JSONObject, metadata: PmtilesMetadataReader.Metadata?) {
        val fitBounds = metadata?.fitBounds
        if (fitBounds != null) {
            root.put(
                "bounds",
                JSONArray().apply {
                    put(fitBounds.longitudeWest)
                    put(fitBounds.latitudeSouth)
                    put(fitBounds.longitudeEast)
                    put(fitBounds.latitudeNorth)
                },
            )
        } else {
            val b = MapOverviewDefaults.BOUNDS_WEST_SOUTH_EAST_NORTH
            root.put(
                "bounds",
                JSONArray().apply {
                    put(b[0])
                    put(b[1])
                    put(b[2])
                    put(b[3])
                },
            )
        }
        val center = metadata?.center
        root.put(
            "center",
            JSONArray().apply {
                if (center != null) {
                    put(center.longitude)
                    put(center.latitude)
                } else {
                    put(MapOverviewDefaults.CENTER.longitude)
                    put(MapOverviewDefaults.CENTER.latitude)
                }
                put(MapOverviewDefaults.DEFAULT_ZOOM)
            },
        )
    }
}
