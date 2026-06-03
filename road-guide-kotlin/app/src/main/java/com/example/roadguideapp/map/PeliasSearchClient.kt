package com.example.roadguideapp.map

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

/**
 * Pelias geocoder exposed by Headway frontend nginx at `{frontend}/pelias/v1/…`.
 */
internal object PeliasSearchClient {

    private const val TAG = "PeliasSearchClient"
    private const val MIN_AUTOCOMPLETE_QUERY_LENGTH = 1
    private const val MIN_SEARCH_QUERY_LENGTH = 2
    private const val DEFAULT_SIZE = 10
    private const val MIN_FOCUS_ZOOM = 6.0
    /** Pelias `/nearby` caps `boundary.circle.radius` (km); larger values are clamped server-side. */
    private const val MAX_NEARBY_RADIUS_KM = 5.0

    suspend fun autocomplete(
        text: String,
        focus: LatLng? = null,
        mapZoom: Double? = null,
        size: Int = DEFAULT_SIZE,
    ): PeliasSearchResponse = searchEndpoint(
        path = "autocomplete",
        text = text,
        focus = focus,
        mapZoom = mapZoom,
        size = size,
        minQueryLength = MIN_AUTOCOMPLETE_QUERY_LENGTH,
    )

    suspend fun search(
        text: String,
        focus: LatLng? = null,
        mapZoom: Double? = null,
        size: Int = DEFAULT_SIZE,
        layers: String? = null,
    ): PeliasSearchResponse = searchEndpoint(
        path = "search",
        text = text,
        focus = focus,
        mapZoom = mapZoom,
        size = size,
        layers = layers,
    )

    /**
     * Category browse near a point (no free-text query). Uses Pelias `/v1/nearby`.
     */
    suspend fun nearby(
        categories: String,
        point: LatLng,
        size: Int = DEFAULT_SIZE,
        radiusKm: Double? = null,
    ): PeliasSearchResponse = withContext(Dispatchers.IO) {
        val cats = categories.trim()
        if (cats.isEmpty()) {
            return@withContext PeliasSearchResponse.Success(emptyList())
        }

        val base = MapServerConfig.peliasApiBaseUrl
        val encodedCategories = URLEncoder.encode(cats, Charsets.UTF_8.name())
        val radiusParam = radiusKm?.let { km ->
            "&boundary.circle.radius=${km.coerceIn(0.05, MAX_NEARBY_RADIUS_KM)}"
        }.orEmpty()
        val url =
            "$base/nearby?categories=$encodedCategories" +
                "&point.lat=${point.latitude}&point.lon=${point.longitude}" +
                "&size=$size$radiusParam"
        requestUrl(url, logQuery = "nearby:$cats")
    }

    /**
     * Pelias `/nearby` requires `point.lat` / `point.lon`; it does not accept `boundary.rect.*`
     * (those return HTTP 400). Approximate a viewport with a circle at the bounds center.
     */
    suspend fun nearbyInBounds(
        categories: String,
        bounds: LatLngBounds,
        size: Int = DEFAULT_SIZE,
    ): PeliasSearchResponse = withContext(Dispatchers.IO) {
        val center = LatLng(
            (bounds.latitudeNorth + bounds.latitudeSouth) / 2.0,
            (bounds.longitudeEast + bounds.longitudeWest) / 2.0,
        )
        val radiusKm = radiusKmCoveringBounds(bounds, center).coerceAtMost(MAX_NEARBY_RADIUS_KM)
        nearby(categories, center, size, radiusKm)
    }

    /**
     * Category browse along a route polyline via Pelias `/nearby` point samples (corridor filter
     * is applied client-side). Full-route bounding boxes are not used — Pelias rejects rect
     * on `/nearby` and caps circle radius (~5 km).
     */
    suspend fun nearbyAlongPolyline(
        categories: String,
        polyline: List<LatLng>,
        corridorMeters: Double,
        maxSamples: Int = 10,
        size: Int = DEFAULT_SIZE,
    ): PeliasSearchResponse = withContext(Dispatchers.IO) {
        val cats = categories.trim()
        if (cats.isEmpty() || polyline.size < 2) {
            return@withContext PeliasSearchResponse.Success(emptyList())
        }

        val sampleRadiusKm = (corridorMeters / 1_000.0).coerceIn(0.05, MAX_NEARBY_RADIUS_KM)
        val samples = PolylineDistance.sampleEvenlyAlongPolyline(polyline, maxSamples)
        val perSampleSize = ((size + samples.size - 1) / samples.size).coerceAtLeast(3).coerceAtMost(size)

        val buckets = ArrayList<List<PeliasSearchResult>>(samples.size)
        var firstFailure: PeliasSearchResponse.Failure? = null

        for (sample in samples) {
            when (
                val pointResponse = nearby(cats, sample, perSampleSize, radiusKm = sampleRadiusKm)
            ) {
                is PeliasSearchResponse.Success -> buckets.add(pointResponse.results)
                is PeliasSearchResponse.Failure -> {
                    if (firstFailure == null) firstFailure = pointResponse
                    buckets.add(emptyList())
                }
            }
        }

        val merged = mergeRoundRobin(buckets, limit = size)
        if (merged.isEmpty()) {
            return@withContext firstFailure ?: PeliasSearchResponse.Success(emptyList())
        }
        PeliasSearchResponse.Success(merged)
    }

    /** Interleave per-sample hits so the first [limit] results span the route, not only its start. */
    internal fun mergeRoundRobin(
        buckets: List<List<PeliasSearchResult>>,
        limit: Int,
    ): List<PeliasSearchResult> {
        if (limit <= 0 || buckets.isEmpty()) return emptyList()
        val merged = ArrayList<PeliasSearchResult>(limit)
        val seen = HashSet<String>()
        var round = 0
        while (merged.size < limit) {
            var addedThisRound = false
            for (bucket in buckets) {
                if (round >= bucket.size) continue
                val result = bucket[round]
                if (seen.add(result.gid)) {
                    merged.add(result)
                    addedThisRound = true
                    if (merged.size >= limit) break
                }
            }
            if (!addedThisRound) break
            round++
        }
        return merged
    }

    private fun radiusKmCoveringBounds(bounds: LatLngBounds, center: LatLng): Double {
        val corners = listOf(
            LatLng(bounds.latitudeNorth, bounds.longitudeWest),
            LatLng(bounds.latitudeNorth, bounds.longitudeEast),
            LatLng(bounds.latitudeSouth, bounds.longitudeWest),
            LatLng(bounds.latitudeSouth, bounds.longitudeEast),
        )
        val maxMeters = corners.maxOf { DirectionsPathOptimizer.haversineMeters(center, it) }
        return (maxMeters / 1_000.0).coerceAtLeast(0.05)
    }

    /**
     * Reverse geocode a map coordinate into a Pelias address label.
     */
    suspend fun reverse(
        point: LatLng,
        size: Int = 1,
    ): PeliasSearchResponse = withContext(Dispatchers.IO) {
        val base = MapServerConfig.peliasApiBaseUrl
        val url =
            "$base/reverse?point.lat=${point.latitude}&point.lon=${point.longitude}&size=$size"
        requestUrl(url, logQuery = "reverse:${point.latitude},${point.longitude}")
    }

    private suspend fun searchEndpoint(
        path: String,
        text: String,
        focus: LatLng?,
        mapZoom: Double?,
        size: Int,
        layers: String? = null,
        minQueryLength: Int = MIN_SEARCH_QUERY_LENGTH,
    ): PeliasSearchResponse = withContext(Dispatchers.IO) {
        val query = text.trim()
        if (query.length < minQueryLength) {
            return@withContext PeliasSearchResponse.Success(emptyList())
        }

        val base = MapServerConfig.peliasApiBaseUrl
        val encodedText = URLEncoder.encode(query, Charsets.UTF_8.name())
        val focusParams = if (focus != null && (mapZoom == null || mapZoom > MIN_FOCUS_ZOOM)) {
            "&focus.point.lon=${focus.longitude}&focus.point.lat=${focus.latitude}"
        } else {
            ""
        }
        val layersParam = layers?.trim()?.takeIf { it.isNotEmpty() }?.let { layersValue ->
            "&layers=${URLEncoder.encode(layersValue, Charsets.UTF_8.name())}"
        }.orEmpty()
        val url = "$base/$path?text=$encodedText&size=$size$focusParams$layersParam"
        requestUrl(url, logQuery = query)
    }

    private suspend fun requestUrl(url: String, logQuery: String): PeliasSearchResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "GET $url")

        val conn = (URI(url).toURL().openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 25_000
            instanceFollowRedirects = true
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }

        try {
            val code = conn.responseCode
            val body = if (code in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            if (code !in 200..299) {
                Log.w(TAG, "HTTP $code for $url: ${body.take(200)}")
                return@withContext PeliasSearchResponse.Failure(
                    "Search server returned HTTP $code. Is Headway frontend running on ${MapServerConfig.headwayFrontendBaseUrl}?",
                )
            }
            val results = PeliasSearchResult.parseCollection(body)
            Log.d(TAG, "Got ${results.size} results for \"$logQuery\"")
            PeliasSearchResponse.Success(results)
        } catch (e: Exception) {
            Log.w(TAG, "Request failed for $url", e)
            PeliasSearchResponse.Failure(
                "Could not reach search server at ${MapServerConfig.headwayFrontendBaseUrl}. " +
                    "Run Headway and adb reverse tcp:8080 tcp:8080 if needed.",
            )
        } finally {
            conn.disconnect()
        }
    }
}

internal sealed class PeliasSearchResponse {
    data class Success(val results: List<PeliasSearchResult>) : PeliasSearchResponse()
    data class Failure(val message: String) : PeliasSearchResponse()
}
