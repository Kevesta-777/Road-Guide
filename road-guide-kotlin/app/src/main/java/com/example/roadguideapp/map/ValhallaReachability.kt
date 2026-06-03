package com.example.roadguideapp.map

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

/** Probes whether the configured Valhalla endpoint responds. */
internal object ValhallaReachability {

    private val mutex = Mutex()
    private var probed = false
    private var reachable = false

    suspend fun probeIfNeeded(): Boolean = mutex.withLock {
        if (probed) return reachable
        reachable = withContext(Dispatchers.IO) { ping() }
        probed = true
        reachable
    }

    fun isReachable(): Boolean = reachable

    private fun ping(): Boolean {
        val base = MapServerConfig.valhallaBaseUrl
        val url = try {
            URI("$base/status").toURL()
        } catch (_: Exception) {
            return false
        }
        return try {
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5_000
            conn.readTimeout = 5_000
            conn.requestMethod = "GET"
            val code = conn.responseCode
            conn.disconnect()
            code in 200..499
        } catch (_: Exception) {
            false
        }
    }
}
