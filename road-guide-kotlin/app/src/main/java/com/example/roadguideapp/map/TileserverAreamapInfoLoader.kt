package com.example.roadguideapp.map

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import java.net.HttpURLConnection
import java.net.URL

/**
 * Reads TileJSON for the Headway extract so the map camera fits the same region as the web UI.
 */
internal object TileserverAreamapInfoLoader {

    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 45_000

    data class Result(
        val fitBounds: LatLngBounds?,
        val center: LatLng?,
        val zoom: Float?,
    )

    suspend fun load(context: Context, baseUrls: List<String> = MapServerConfig.mapAreamapBaseUrls()): Result =
        withContext(Dispatchers.IO) {
            for (origin in baseUrls.map { it.trimEnd('/') }.distinct()) {
                val cachedRaw = TileserverLocationDiskCache.readAreamapJson(context, origin)
                if (cachedRaw != null) {
                    runCatching { parseTileJson(cachedRaw) }
                        .getOrNull()
                        ?.takeIf { it.fitBounds != null || it.center != null }
                        ?.let { return@withContext it }
                }
            }

            val pathCandidates = listOf(
                "/tileserver/data/default.json",
                "/areamap",
            )
            for (origin in baseUrls.map { it.trimEnd('/') }.distinct()) {
                for (path in pathCandidates) {
                    val url = origin + path
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.connectTimeout = CONNECT_TIMEOUT_MS
                    conn.readTimeout = READ_TIMEOUT_MS
                    conn.instanceFollowRedirects = true
                    try {
                        val code = conn.responseCode
                        if (code !in 200..299) continue
                        val text = conn.inputStream.bufferedReader().use { it.readText() }
                        val parsed = parseTileJson(text)
                        if (parsed.fitBounds == null && parsed.center == null) continue
                        TileserverLocationDiskCache.writeAreamapJson(context, origin, text)
                        return@withContext parsed
                    } catch (_: Exception) {
                        continue
                    } finally {
                        conn.disconnect()
                    }
                }
            }
            val fromPmtiles = PmtilesMetadataReader.read(PmtilesOverviewSource.cachedFile(context))
            if (fromPmtiles != null) {
                return@withContext Result(
                    fitBounds = fromPmtiles.fitBounds ?: MapOverviewDefaults.FIT_BOUNDS,
                    center = fromPmtiles.center ?: MapOverviewDefaults.CENTER,
                    zoom = fromPmtiles.maxZoom?.toFloat()?.coerceAtMost(12f)
                        ?: MapOverviewDefaults.DEFAULT_ZOOM.toFloat(),
                )
            }
            Result(
                fitBounds = MapOverviewDefaults.FIT_BOUNDS,
                center = MapOverviewDefaults.CENTER,
                zoom = MapOverviewDefaults.DEFAULT_ZOOM.toFloat(),
            )
        }

    internal fun parseTileJson(jsonText: String): Result {
        val viewport = MapInitialViewportResolver.parseTileJsonText(jsonText)
        return Result(
            fitBounds = viewport.fitBounds,
            center = viewport.center,
            zoom = viewport.zoom,
        )
    }
}
