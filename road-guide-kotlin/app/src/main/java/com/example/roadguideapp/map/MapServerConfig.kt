package com.example.roadguideapp.map

import com.example.roadguideapp.BuildConfig
import org.json.JSONArray
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

internal object MapServerConfig {
    /**
     * Base URL of the **Headway tileserver** (Martin), not the web frontend.
     * Default Docker image listens on **8000**; publish it from `docker-compose`, e.g.
     * `ports: [ "8000:8000" ]` on the `tileserver` service.
     *
     * - Android emulator → host: `http://10.0.2.2:8000`
     * - Device → LAN IP or `adb reverse tcp:8000 tcp:8000` and `http://127.0.0.1:8000`
     *
     * Override via project-root `.env` ([BuildConfig.HEADWAY_TILESERVER_BASE_URL]).
     */
    val tileserverBaseUrl: String
        get() = BuildConfig.HEADWAY_TILESERVER_BASE_URL.trimEnd('/')

    /**
     * Base URL for Headway Valhalla routing (`/route` on the road graph).
     * Default is the Headway **frontend** nginx proxy (`:8080/valhalla` → `valhalla:8002`).
     * Override via [BuildConfig.HEADWAY_VALHALLA_BASE_URL].
     */
    val valhallaBaseUrl: String
        get() = BuildConfig.HEADWAY_VALHALLA_BASE_URL.trimEnd('/')

    /**
     * Headway web frontend (nginx), default `:8080`. Used for Pelias at `/pelias/v1`.
     * Set [BuildConfig.HEADWAY_FRONTEND_BASE_URL] or derived from [valhallaBaseUrl].
     */
    val headwayFrontendBaseUrl: String
        get() {
            val explicit = BuildConfig.HEADWAY_FRONTEND_BASE_URL.trim().trimEnd('/')
            if (explicit.isNotEmpty()) return explicit
            val valhalla = valhallaBaseUrl
            if (valhalla.endsWith("/valhalla")) {
                return valhalla.removeSuffix("/valhalla")
            }
            return valhalla
        }

    val peliasApiBaseUrl: String
        get() = headwayFrontendBaseUrl + "/pelias/v1"

    /**
     * Base URL for MapLibre style, vector tiles, sprites, and TileJSON.
     * Prefer the **frontend** (:8080) — same origin as the Headway map in the browser.
     */
    val mapApiBaseUrl: String
        get() = headwayFrontendBaseUrl.ifBlank { tileserverBaseUrl }

    /** Bases to try for TileJSON / areamap (frontend first, then direct Martin). */
    fun mapAreamapBaseUrls(): List<String> = buildList {
        val frontend = headwayFrontendBaseUrl
        val tileserver = tileserverBaseUrl
        if (frontend.isNotBlank()) add(frontend)
        if (tileserver.isNotBlank() && tileserver != frontend) add(tileserver)
    }

    fun tileserverStyleUrl(stylePath: String = AppMapStyle.TILESERVER_STYLE_PATH): String =
        mapApiBaseUrl + stylePath

    /**
     * Optional initial camera fit: JSON array `[west, south, east, north]` (same as Headway
     * `maxBounds`). Set in Gradle via [BuildConfig.INITIAL_MAP_MAX_BOUNDS_JSON]; empty string
     * uses world view (center 0,0 zoom 1).
     */
    fun initialMapFitBounds(): LatLngBounds? {
        val raw = BuildConfig.INITIAL_MAP_MAX_BOUNDS_JSON.trim()
        if (raw.isEmpty()) return null
        return runCatching {
            val arr = JSONArray(raw)
            if (arr.length() != 4) return@runCatching null
            val west = arr.getDouble(0)
            val south = arr.getDouble(1)
            val east = arr.getDouble(2)
            val north = arr.getDouble(3)
            LatLngBounds.Builder()
                .include(LatLng(south, west))
                .include(LatLng(north, east))
                .build()
        }.getOrNull()
    }
}
