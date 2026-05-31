package com.example.roadguideapp.map

import com.example.roadguideapp.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

internal object BusinessPoiClient {
    private val httpClient = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    data class BusinessPoi(
        val id: String,
        val name: String,
        val address: String,
        val description: String,
        val metadata: BusinessPoiMetadata = BusinessPoiMetadata(),
    )

    data class PoiMedia(
        val id: String,
        val kind: String,
        val url: String,
        val caption: String,
    )

    data class MyBusinessPoi(
        val id: String,
        val name: String,
        val address: String,
        val description: String,
        val latitude: Double?,
        val longitude: Double?,
        val externalRef: String?,
    )

    sealed class ListMineResult {
        data class Success(val pois: List<MyBusinessPoi>) : ListMineResult()
        data class Failure(val message: String) : ListMineResult()
    }

    sealed class LoadResult {
        data class Success(val poi: BusinessPoi, val media: List<PoiMedia>) : LoadResult()
        data class Failure(val message: String) : LoadResult()
    }

    sealed class SaveResult {
        data object Success : SaveResult()
        data class Failure(val message: String) : SaveResult()
    }

    sealed class UploadResult {
        data object Success : UploadResult()
        data class Failure(val message: String) : UploadResult()
    }

    sealed class DeleteResult {
        data object Success : DeleteResult()
        data class Failure(val message: String) : DeleteResult()
    }

    fun listMyBusinessPois(bearerToken: String): ListMineResult {
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/business-pois/mine")
            .header("Authorization", "Bearer $bearerToken")
            .get()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use ListMineResult.Failure(body.optString("error").ifBlank { "Failed to load businesses." })
                }
                val poisJson = body.optJSONArray("pois") ?: org.json.JSONArray()
                val pois = buildList {
                    for (index in 0 until poisJson.length()) {
                        val item = poisJson.optJSONObject(index) ?: continue
                        add(
                            MyBusinessPoi(
                                id = item.optString("id"),
                                name = item.optString("name"),
                                address = item.optString("address"),
                                description = item.optString("description"),
                                latitude = item.optDouble("latitude").takeIf { item.has("latitude") && !item.isNull("latitude") },
                                longitude = item.optDouble("longitude").takeIf { item.has("longitude") && !item.isNull("longitude") },
                                externalRef = item.optString("externalRef").takeIf { it.isNotBlank() },
                            ),
                        )
                    }
                }
                ListMineResult.Success(pois)
            }
        }.getOrElse {
            ListMineResult.Failure("Could not connect to backend.")
        }
    }

    fun loadPoi(poiId: String, bearerToken: String): LoadResult {
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/business-pois/$poiId")
            .header("Authorization", "Bearer $bearerToken")
            .get()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use LoadResult.Failure(body.optString("error").ifBlank { "Failed to load business." })
                }
                val poiJson = body.optJSONObject("poi") ?: return@use LoadResult.Failure("Missing business data.")
                val mediaJson = body.optJSONArray("media")
                val media = buildList {
                    if (mediaJson != null) {
                        for (index in 0 until mediaJson.length()) {
                            val item = mediaJson.optJSONObject(index) ?: continue
                            add(
                                PoiMedia(
                                    id = item.optString("id"),
                                    kind = item.optString("kind"),
                                    url = resolveMediaUrl(item.optString("url")),
                                    caption = item.optString("caption"),
                                ),
                            )
                        }
                    }
                }
                LoadResult.Success(
                    poi = BusinessPoi(
                        id = poiJson.optString("id"),
                        name = poiJson.optString("name"),
                        address = poiJson.optString("address"),
                        description = poiJson.optString("description"),
                        metadata = BusinessPoiMetadata.fromJsonObject(poiJson.optJSONObject("metadata")),
                    ),
                    media = media,
                )
            }
        }.getOrElse {
            LoadResult.Failure("Could not connect to backend.")
        }
    }

    fun updatePoi(
        poiId: String,
        name: String,
        address: String,
        description: String,
        metadata: BusinessPoiMetadata,
        bearerToken: String,
    ): SaveResult {
        val payload = JSONObject()
            .put("name", name)
            .put("address", address)
            .put("description", description)
            .put("metadata", metadata.toJsonObject())
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/business-pois/$poiId")
            .header("Authorization", "Bearer $bearerToken")
            .put(payload.toString().toRequestBody(jsonMediaType))
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (response.isSuccessful) {
                    SaveResult.Success
                } else {
                    SaveResult.Failure(body.optString("error").ifBlank { "Failed to save business." })
                }
            }
        }.getOrElse {
            SaveResult.Failure("Could not connect to backend.")
        }
    }

    fun uploadMedia(
        poiId: String,
        kind: String,
        caption: String,
        sortOrder: Int,
        fileName: String,
        mimeType: String,
        fileBytes: ByteArray,
        bearerToken: String,
    ): UploadResult {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("kind", kind)
            .addFormDataPart("caption", caption)
            .addFormDataPart("sortOrder", sortOrder.toString())
            .addFormDataPart(
                "file",
                fileName,
                fileBytes.toRequestBody(mimeType.toMediaType()),
            )
            .build()
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/business-pois/$poiId/media")
            .header("Authorization", "Bearer $bearerToken")
            .post(requestBody)
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (response.isSuccessful) {
                    UploadResult.Success
                } else {
                    UploadResult.Failure(body.optString("error").ifBlank { "Failed to upload media." })
                }
            }
        }.getOrElse {
            UploadResult.Failure("Could not connect to backend.")
        }
    }

    fun deleteMedia(poiId: String, mediaId: String, bearerToken: String): DeleteResult {
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/business-pois/$poiId/media/$mediaId")
            .header("Authorization", "Bearer $bearerToken")
            .delete()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (response.isSuccessful) {
                    DeleteResult.Success
                } else {
                    DeleteResult.Failure(body.optString("error").ifBlank { "Failed to delete media." })
                }
            }
        }.getOrElse {
            DeleteResult.Failure("Could not connect to backend.")
        }
    }

    fun resolveMediaUrl(url: String): String {
        if (url.isBlank()) return url
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        return baseUrl().trimEnd('/') + url
    }

    private fun baseUrl(): String = BuildConfig.MAIN_BACKEND_BASE_URL.trim().trimEnd('/')
}
