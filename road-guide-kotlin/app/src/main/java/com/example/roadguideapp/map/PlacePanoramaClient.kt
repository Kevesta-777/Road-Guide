package com.example.roadguideapp.map

import com.example.roadguideapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** Fetches admin-approved panoramas for a map place (public API, no auth). */
internal object PlacePanoramaClient {

    private val httpClient = OkHttpClient()

    data class ApprovedPanorama(
        val id: String,
        val url: String,
        val caption: String,
    )

    sealed class FetchResult {
        data class Success(val panoramas: List<ApprovedPanorama>) : FetchResult()
        data class Failure(val message: String) : FetchResult()
    }

    fun fetchApproved(externalRef: String): FetchResult {
        val encodedRef = java.net.URLEncoder.encode(externalRef, Charsets.UTF_8.name())
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/places/panoramas?externalRef=$encodedRef")
            .get()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use FetchResult.Failure(
                        body.optString("error").ifBlank { "Failed to load panoramas." },
                    )
                }
                val items = body.optJSONArray("panoramas") ?: org.json.JSONArray()
                val panoramas = buildList {
                    for (index in 0 until items.length()) {
                        val item = items.optJSONObject(index) ?: continue
                        add(
                            ApprovedPanorama(
                                id = item.optString("id"),
                                url = item.optString("url"),
                                caption = item.optString("caption"),
                            ),
                        )
                    }
                }
                FetchResult.Success(panoramas)
            }
        }.getOrElse {
            FetchResult.Failure("Could not connect to backend.")
        }
    }

    private fun baseUrl(): String = BuildConfig.MAIN_BACKEND_BASE_URL.trim().trimEnd('/')
}
