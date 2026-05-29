package com.example.roadguideapp.map

import android.content.Context
import org.json.JSONObject

/**
 * Offline MapLibre style using bundled Headway [basic.json], local PMTiles, and persisted
 * sprites/glyphs from [TileserverBundledResources] (or a prior online prefetch).
 */
internal object OfflineMapStyleBuilder {

    fun build(
        context: Context,
        tileserverOrigin: String,
        pmtilesUrl: String,
        metadata: PmtilesMetadataReader.Metadata?,
    ): String {
        val basicJson = TileserverBundledResources.loadBundledBasicStyleJson(context)
            ?: error(
                "Bundled basic.json is required for offline map labels. Set MAP_STYLE_ASSET_RELATIVE_PATH " +
                    "to \"map/basic.json\" in app/build.gradle.kts.",
            )
        val root = JSONObject(basicJson)
        val detailSourceId = PmtilesOverviewStylePatch.findDetailVectorSourceId(
            root.getJSONObject("sources"),
        ) ?: AppMapStyle.OPENMAPTILES_SOURCE_ID

        PmtilesOfflineStyleAdapter.apply(root, pmtilesUrl, metadata, detailSourceId)
        TileserverBundledResources.applyLocalSpriteAndGlyphs(root, context, tileserverOrigin)

        return root.toString()
    }
}
