package com.example.roadguideapp.map

import android.content.Context
import android.util.Log
import com.example.roadguideapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Loads MapLibre style JSON for **client-side** use.
 *
 * **Session probe:** [TileserverReachability] checks the tileserver once per app launch.
 *
 * **Online:** Headway `basic.json` + [PmtilesOverviewStylePatch] (PMTiles z0–11, tileserver z11+).
 * Prefetches sprites/glyphs and writes resolved style to [TileserverLocationDiskCache].
 *
 * **Offline + prior visit:** Cached dual-tier style; z11+ detail from OkHttp disk cache when
 * available; overview PMTiles extended for high-zoom overzoom where cache misses.
 *
 * **Offline first launch:** Bundled `basic.json` + PMTiles (all zooms) + APK sprites/glyphs.
 */
internal object TileserverStyleLoader {

    private const val TAG = "TileserverStyleLoader"
    private const val NETWORK_CONNECT_TIMEOUT_MS = 2_500
    private const val NETWORK_READ_TIMEOUT_MS = 4_000

    suspend fun loadResolvedStyleJson(
        context: Context,
        tileserverBaseUrl: String,
        stylePath: String = AppMapStyle.TILESERVER_STYLE_PATH,
    ): ResolvedMapStyle = withContext(Dispatchers.IO) {
        val origin = tileserverBaseUrl.trimEnd('/')
        val path = stylePath.trim().ifBlank { AppMapStyle.TILESERVER_STYLE_PATH }
        val appContext = context.applicationContext

        TileserverBundledResources.ensureAssetPackMaterialized(appContext)
        PmtilesOverviewSource.awaitReady(appContext)
        val pmtilesUrl = PmtilesOverviewSource.resolveUrl(appContext)
            ?: error(pmtilesUnavailableMessage(appContext))
        val metadata = PmtilesMetadataReader.read(PmtilesOverviewSource.cachedFile(appContext))

        if (!TileserverReachability.probeIfNeeded(origin)) {
            return@withContext loadOfflineStyle(appContext, origin, path, pmtilesUrl, metadata)
        }

        val networkStyle = fetchStyleFromNetworkOrNull(origin, path)
        if (networkStyle != null) {
            val root = JSONObject(networkStyle)
            absolutizeForDirectTileserver(root, origin)
            PmtilesOverviewStylePatch.applyOnlineDualTier(root, pmtilesUrl)
            val out = root.toString()
            TileserverLocationDiskCache.writeResolvedStyleJson(appContext, origin, path, out)
            TileserverBundledResources.prefetchForStyle(appContext, origin, out)
            Log.i(
                TAG,
                "Online dual-tier style: PMTiles z0-${BuildConfig.OVERVIEW_PMTILES_MAX_ZOOM}, " +
                    "tileserver z${BuildConfig.DETAIL_TILES_MIN_ZOOM}+",
            )
            return@withContext ResolvedMapStyle(json = out, mode = ResolvedMapStyle.Mode.Online)
        }

        Log.w(TAG, "Tileserver probe succeeded but style fetch failed — using offline fallbacks.")
        loadOfflineStyle(appContext, origin, path, pmtilesUrl, metadata)
    }

    private fun loadOfflineStyle(
        context: Context,
        origin: String,
        stylePath: String,
        pmtilesUrl: String,
        metadata: PmtilesMetadataReader.Metadata?,
    ): ResolvedMapStyle {
        val cachedStyle = TileserverLocationDiskCache.readResolvedStyleJson(context, origin, stylePath)
        if (cachedStyle != null) {
            val root = JSONObject(cachedStyle)
            val highZoomCap = metadata?.maxZoom ?: BuildConfig.OVERVIEW_PMTILES_MAX_ZOOM
            PmtilesOverviewStylePatch.extendOverviewLayersForHighZoomFallback(root, highZoomCap)
            TileserverBundledResources.applyOfflineResourcesForCachedHybrid(root, context, origin)
            val json = root.toString()
            Log.i(
                TAG,
                "Offline cached hybrid: dual-tier style from disk; z11+ detail from HTTP cache when " +
                    "available, otherwise PMTiles overzoom (overview maxzoom=$highZoomCap).",
            )
            return ResolvedMapStyle(json = json, mode = ResolvedMapStyle.Mode.CachedHybrid)
        }

        Log.i(TAG, "Offline bundled: PMTiles at all zooms + assets/map/basic.json + bundled sprites/glyphs.")
        return buildBundledOfflineStyle(context, origin, pmtilesUrl, metadata)
    }

    private fun buildBundledOfflineStyle(
        context: Context,
        origin: String,
        pmtilesUrl: String,
        metadata: PmtilesMetadataReader.Metadata?,
    ): ResolvedMapStyle {
        val json = OfflineMapStyleBuilder.build(context, origin, pmtilesUrl, metadata)
        return ResolvedMapStyle(json = json, mode = ResolvedMapStyle.Mode.BundledOffline)
    }

    private fun fetchStyleFromNetworkOrNull(
        tileserverOrigin: String,
        primaryPath: String,
        extraFallbacks: List<String> = emptyList(),
    ): String? {
        val candidates = buildList {
            add(primaryPath.trim().ifBlank { AppMapStyle.TILESERVER_STYLE_PATH })
            extraFallbacks.forEach { path ->
                if (path !in this) add(path)
            }
            if (primaryPath != AppMapStyle.TILESERVER_STYLE_PATH &&
                AppMapStyle.TILESERVER_STYLE_PATH !in this
            ) {
                add(AppMapStyle.TILESERVER_STYLE_PATH)
            }
            AppMapStyle.TILESERVER_STYLE_PATH_FALLBACKS.forEach { path ->
                if (path !in this) add(path)
            }
        }
        for (stylePath in candidates) {
            val styleUrl = tileserverOrigin + stylePath
            val conn = URL(styleUrl).openConnection() as HttpURLConnection
            conn.connectTimeout = NETWORK_CONNECT_TIMEOUT_MS
            conn.readTimeout = NETWORK_READ_TIMEOUT_MS
            conn.instanceFollowRedirects = true
            try {
                val code = conn.responseCode
                if (code in 200..299) {
                    return conn.inputStream.bufferedReader().use { it.readText() }
                }
            } catch (_: Exception) {
                // try next candidate
            } finally {
                conn.disconnect()
            }
        }
        return null
    }

    private fun absolutizeForDirectTileserver(node: Any?, tileserverOrigin: String) {
        when (node) {
            is JSONObject -> {
                val keys = node.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    when (val value = node.get(key)) {
                        is String -> node.put(key, rewriteStyleUrl(value, tileserverOrigin))
                        is JSONObject, is JSONArray -> absolutizeForDirectTileserver(value, tileserverOrigin)
                        else -> Unit
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    when (val value = node.get(i)) {
                        is String -> node.put(i, rewriteStyleUrl(value, tileserverOrigin))
                        is JSONObject, is JSONArray -> absolutizeForDirectTileserver(value, tileserverOrigin)
                        else -> Unit
                    }
                }
            }
        }
    }

    internal fun rewriteStyleUrl(value: String, tileserverOrigin: String): String {
        if (!value.startsWith("/")) return value
        return tileserverOrigin.trimEnd('/') + value
    }

    private fun pmtilesUnavailableMessage(context: Context): String {
        val assetPath = BuildConfig.OVERVIEW_PMTILES_ASSET_PATH
        return if (!PmtilesOverviewSource.isAssetBundled(context)) {
            "Overview PMTiles is missing from the APK. Place GreaterLondon.pmtiles in the project " +
                "root and rebuild so $assetPath is packaged."
        } else {
            "Overview PMTiles could not be prepared on this device. Free storage space and restart the app."
        }
    }
}
