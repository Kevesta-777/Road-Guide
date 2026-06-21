package com.example.roadguideapp.map

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

/**
 * Fetches road-network routes from Headway Valhalla (`/route` on the routing graph).
 *
 * Use [MapServerConfig.valhallaBaseUrl], which defaults to the Headway frontend nginx proxy
 * (`http://host:8080/valhalla` → `valhalla:8002` in docker-compose).
 */
internal object ValhallaRouteClient {

    private const val TAG = "ValhallaRouteClient"
    private const val POLYLINE_PRECISION = 6

    suspend fun fetchRoute(
        waypoints: List<LatLng>,
        mode: DirectionsTravelMode,
    ): DirectionsRouteResult? = withContext(Dispatchers.IO) {
        if (waypoints.size < 2) return@withContext null
        val base = MapServerConfig.valhallaBaseUrl
        val queryJson = buildRouteQuery(waypoints, mode)
        val url = URI(
            "$base/route?json=${URLEncoder.encode(queryJson, Charsets.UTF_8.name())}",
        ).toURL()
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 60_000
        conn.instanceFollowRedirects = true
        try {
            val code = conn.responseCode
            val body = if (code in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            if (code !in 200..299) {
                Log.w(TAG, "Valhalla /route HTTP $code: ${body.take(200)}")
                return@withContext null
            }
            parseRouteResponse(body)
        } catch (e: Exception) {
            Log.w(TAG, "Valhalla /route request failed", e)
            null
        } finally {
            conn.disconnect()
        }
    }

    private fun buildRouteQuery(waypoints: List<LatLng>, mode: DirectionsTravelMode): String {
        val locations = JSONArray()
        waypoints.forEach { pt ->
            locations.put(
                JSONObject()
                    .put("lat", pt.latitude)
                    .put("lon", pt.longitude),
            )
        }
        return JSONObject()
            .put("locations", locations)
            .put("costing", mode.valhallaCosting)
            .put("alternates", 0)
            .put("units", "kilometers")
            .toString()
    }

    private fun parseRouteResponse(body: String): DirectionsRouteResult? {
        val root = JSONObject(body)
        if (root.has("error_code") || root.has("error")) return null
        val trip = root.optJSONObject("trip") ?: return null
        val legsJson = trip.optJSONArray("legs") ?: return null
        if (legsJson.length() == 0) return null

        val combined = ArrayList<LatLng>()
        val legs = ArrayList<DirectionsRouteLeg>(legsJson.length())
        for (i in 0 until legsJson.length()) {
            val legObj = legsJson.optJSONObject(i) ?: continue
            val shape = legObj.optString("shape")
            if (shape.isEmpty()) continue
            val segment = ValhallaPolylineDecoder.decode(shape, POLYLINE_PRECISION)
            if (segment.isEmpty()) continue
            val summary = legObj.optJSONObject("summary")
            val duration = summary?.optDouble("time") ?: 0.0
            val mid = segment[segment.size / 2]
            legs.add(DirectionsRouteLeg(durationSeconds = duration, midPoint = mid))
            if (combined.isEmpty()) {
                combined.addAll(segment)
            } else {
                combined.addAll(segment.drop(1))
            }
        }
        if (combined.size < 2) return null

        val tripSummary = trip.optJSONObject("summary")
        val totalTime = tripSummary?.optDouble("time") ?: legs.sumOf { it.durationSeconds }
        val totalLength = tripSummary?.optDouble("length") ?: 0.0
        return DirectionsRouteResult(
            geometry = combined,
            legs = legs,
            totalDurationSeconds = totalTime,
            totalLengthKm = totalLength,
        )
    }
}
