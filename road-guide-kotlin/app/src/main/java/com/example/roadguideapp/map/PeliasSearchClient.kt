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
    ): PeliasSearchResponse = withContext(Dispatchers.IO) {
        val cats = categories.trim()
        if (cats.isEmpty()) {
            return@withContext PeliasSearchResponse.Success(emptyList())
        }

        val base = MapServerConfig.peliasApiBaseUrl
        val encodedCategories = URLEncoder.encode(cats, Charsets.UTF_8.name())
        val url =
            "$base/nearby?categories=$encodedCategories" +
                "&point.lat=${point.latitude}&point.lon=${point.longitude}&size=$size"
        requestUrl(url, logQuery = "nearby:$cats")
    }

    /**
     * Pelias `/nearby` constrained to a rectangular viewport (progressive expansion phase).
     */
    suspend fun nearbyInBounds(
        categories: String,
        bounds: LatLngBounds,
        size: Int = DEFAULT_SIZE,
    ): PeliasSearchResponse = withContext(Dispatchers.IO) {
        val cats = categories.trim()
        if (cats.isEmpty()) {
            return@withContext PeliasSearchResponse.Success(emptyList())
        }

        val base = MapServerConfig.peliasApiBaseUrl
        val encodedCategories = URLEncoder.encode(cats, Charsets.UTF_8.name())
        val url =
            "$base/nearby?categories=$encodedCategories" +
                "&boundary.rect.min_lat=${bounds.latitudeSouth}" +
                "&boundary.rect.min_lon=${bounds.longitudeWest}" +
                "&boundary.rect.max_lat=${bounds.latitudeNorth}" +
                "&boundary.rect.max_lon=${bounds.longitudeEast}" +
                "&size=$size"
        requestUrl(url, logQuery = "nearby-bounds:$cats")
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
