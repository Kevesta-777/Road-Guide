package com.example.roadguideapp.map

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import java.net.HttpURLConnection
import java.net.URL

/**
 * Resolves where the map camera should start (bounds / center / zoom).
 * Headway’s web UI on :8080 uses TileJSON bounds; without them the app fell back to (0,0) zoom 1.
 */
internal object MapInitialViewportResolver {

    data class Viewport(
        val fitBounds: LatLngBounds? = null,
        val center: LatLng? = null,
        val zoom: Float? = null,
    ) {
        val isUsable: Boolean
            get() = fitBounds != null || (center != null && zoom != null)
    }

    fun merge(vararg candidates: Viewport?): Viewport {
        var bounds: LatLngBounds? = null
        var center: LatLng? = null
        var zoom: Float? = null
        for (candidate in candidates) {
            if (candidate == null) continue
            if (bounds == null && candidate.fitBounds != null) {
                bounds = candidate.fitBounds
            }
            if (center == null && candidate.center != null) {
                center = candidate.center
            }
            if (zoom == null && candidate.zoom != null) {
                zoom = candidate.zoom
            }
        }
        return Viewport(bounds, center, zoom)
    }

    fun fromAreamap(result: TileserverAreamapInfoLoader.Result): Viewport =
        Viewport(
            fitBounds = result.fitBounds,
            center = result.center,
            zoom = result.zoom,
        )

    fun fromGradleBounds(bounds: LatLngBounds?): Viewport =
        Viewport(fitBounds = bounds)

    /**
     * Reads MapLibre style `center` / `bounds` and follows the first vector source TileJSON URL.
     */
    suspend fun fromResolvedStyle(styleJson: String): Viewport = withContext(Dispatchers.IO) {
        val root = runCatching { JSONObject(styleJson) }.getOrNull() ?: return@withContext Viewport()
        var viewport = Viewport(
            fitBounds = root.optJSONArray("bounds")?.toBounds(),
            center = root.optJSONArray("center")?.toCenter(),
            zoom = root.optJSONArray("center")?.toZoom(),
        )
        if (viewport.fitBounds != null) return@withContext viewport

        val sources = root.optJSONObject("sources") ?: return@withContext viewport
        val keys = sources.keys()
        while (keys.hasNext()) {
            val source = sources.optJSONObject(keys.next()) ?: continue
            if (source.optString("type") != "vector") continue
            val tileJsonUrl = source.optString("url").takeIf { it.isNotBlank() } ?: continue
            if (!isHttpTileJsonUrl(tileJsonUrl)) continue
            val fromTileJson = fetchTileJsonViewport(tileJsonUrl) ?: continue
            viewport = merge(viewport, fromTileJson)
            if (viewport.fitBounds != null || viewport.center != null) return@withContext viewport
        }
        viewport
    }

    private fun isHttpTileJsonUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.startsWith("http://") || lower.startsWith("https://")
    }

    private fun fetchTileJsonViewport(url: String): Viewport? {
        if (!isHttpTileJsonUrl(url)) return null
        val conn = runCatching {
            URL(url).openConnection() as HttpURLConnection
        }.getOrNull() ?: return null
        conn.connectTimeout = 15_000
        conn.readTimeout = 45_000
        conn.instanceFollowRedirects = true
        return try {
            if (conn.responseCode !in 200..299) return null
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(text)
            Viewport(
                fitBounds = root.optJSONArray("bounds")?.toBounds(),
                center = root.optJSONArray("center")?.toCenter(),
                zoom = root.optJSONArray("center")?.toZoom()
                    ?: root.optDouble("minzoom", Double.NaN).takeIf { it.isFinite() }?.toFloat()
                        ?.coerceAtLeast(4f),
            )
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    /** Parses TileJSON or MapLibre style JSON without network. */
    fun parseTileJsonText(jsonText: String): Viewport {
        val root = runCatching { JSONObject(jsonText) }.getOrNull() ?: return Viewport()
        return Viewport(
            fitBounds = root.optJSONArray("bounds")?.toBounds(),
            center = root.optJSONArray("center")?.toCenter(),
            zoom = root.optJSONArray("center")?.toZoom(),
        )
    }

    private fun JSONArray.toBounds(): LatLngBounds? {
        if (length() != 4) return null
        val west = getDouble(0)
        val south = getDouble(1)
        val east = getDouble(2)
        val north = getDouble(3)
        if (!listOf(west, south, east, north).all { it.isFinite() }) return null
        if (west == east || south == north) return null
        return LatLngBounds.Builder()
            .include(LatLng(south, west))
            .include(LatLng(north, east))
            .build()
    }

    private fun JSONArray.toCenter(): LatLng? {
        if (length() < 2) return null
        val lon = getDouble(0)
        val lat = getDouble(1)
        if (!lon.isFinite() || !lat.isFinite()) return null
        return LatLng(lat, lon)
    }

    private fun JSONArray.toZoom(): Float? {
        if (length() < 3) return null
        val z = getDouble(2)
        if (!z.isFinite()) return null
        return z.toFloat().coerceIn(MapConstants.MIN_ZOOM.toFloat(), MapConstants.MAX_ZOOM.toFloat())
    }
}
