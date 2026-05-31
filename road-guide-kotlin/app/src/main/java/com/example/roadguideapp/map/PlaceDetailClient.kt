package com.example.roadguideapp.map

import com.example.roadguideapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** Fetches public business-edited place content for the place detail sheet. */
internal object PlaceDetailClient {

    private val httpClient = OkHttpClient()

    sealed class FetchResult {
        data class Success(val detail: PlaceBusinessDetail?) : FetchResult()
        data class Failure(val message: String) : FetchResult()
    }

    fun fetch(externalRef: String): FetchResult {
        val encodedRef = java.net.URLEncoder.encode(externalRef, Charsets.UTF_8.name())
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/places/detail?externalRef=$encodedRef")
            .get()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use FetchResult.Failure(
                        body.optString("error").ifBlank { "Failed to load place details." },
                    )
                }
                if (!body.optBoolean("hasBusinessData", false)) {
                    return@use FetchResult.Success(null)
                }
                val poiJson = body.optJSONObject("poi")
                    ?: return@use FetchResult.Success(null)
                val photosJson = body.optJSONArray("photos") ?: org.json.JSONArray()
                val photos = buildList {
                    for (index in 0 until photosJson.length()) {
                        val item = photosJson.optJSONObject(index) ?: continue
                        add(
                            PlaceBusinessPhoto(
                                id = item.optString("id"),
                                url = BusinessPoiClient.resolveMediaUrl(item.optString("url")),
                                caption = item.optString("caption"),
                            ),
                        )
                    }
                }
                FetchResult.Success(
                    PlaceBusinessDetail(
                        description = poiJson.optString("description"),
                        photos = photos,
                        metadata = BusinessPoiMetadata.fromJsonObject(poiJson.optJSONObject("metadata")),
                        name = poiJson.optString("name"),
                        address = poiJson.optString("address"),
                    ),
                )
            }
        }.getOrElse {
            FetchResult.Failure("Could not connect to backend.")
        }
    }

    private fun baseUrl(): String = BuildConfig.MAIN_BACKEND_BASE_URL.trim().trimEnd('/')
}
