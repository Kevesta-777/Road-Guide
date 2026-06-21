package com.example.roadguideapp.map

import android.util.Log
import com.example.roadguideapp.BuildConfig
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lightweight tileserver availability probe, cached for the app process lifetime.
 *
 * Call [probeIfNeeded] once per session (e.g. before the first map style load). When the server
 * is down, [TileserverStyleLoader] skips network style fetch and uses offline fallbacks.
 */
internal object TileserverReachability {

    private const val TAG = "TileserverReachability"
    private const val PROBE_CONNECT_TIMEOUT_MS = 2_500
    private const val PROBE_READ_TIMEOUT_MS = 3_000

    @Volatile
    private var probed: Boolean = false

    @Volatile
    private var reachable: Boolean = false

    fun wasProbed(): Boolean = probed

    fun isReachable(): Boolean = reachable

    /**
     * Probes the tileserver once per process if not already probed.
     * @return true when the style endpoint responds with HTTP 2xx.
     */
    @Synchronized
    fun probeIfNeeded(tileserverOrigin: String): Boolean {
        if (probed) return reachable
        val origin = tileserverOrigin.trim().trimEnd('/')
        reachable = if (origin.isEmpty()) {
            false
        } else {
            probeOrigin(origin)
        }
        probed = true
        Log.i(TAG, "Tileserver probe: reachable=$reachable origin=$origin")
        return reachable
    }

    /**
     * Re-checks tileserver availability (e.g. after connectivity recovery or app resume).
     * Updates the cached result used by [probeIfNeeded] and sprite interceptors.
     */
    @Synchronized
    fun reprobe(tileserverOrigin: String): Boolean {
        val origin = tileserverOrigin.trim().trimEnd('/')
        reachable = if (origin.isEmpty()) {
            false
        } else {
            probeOrigin(origin)
        }
        probed = true
        Log.i(TAG, "Tileserver reprobe: reachable=$reachable origin=$origin")
        return reachable
    }

    /** For unit tests only. */
    internal fun resetForTests() {
        probed = false
        reachable = false
    }

    private fun probeOrigin(origin: String): Boolean {
        val paths = buildList {
            add(AppMapStyle.TILESERVER_STYLE_PATH)
            addAll(AppMapStyle.TILESERVER_STYLE_PATH_FALLBACKS)
        }.distinct()
        for (path in paths) {
            if (probeUrl(origin + path)) return true
        }
        return false
    }

    private fun probeUrl(url: String): Boolean {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = PROBE_CONNECT_TIMEOUT_MS
            readTimeout = PROBE_READ_TIMEOUT_MS
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
        }
        return try {
            conn.responseCode in 200..299
        } catch (_: Exception) {
            false
        } finally {
            conn.disconnect()
        }
    }
}
