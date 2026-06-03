package com.example.roadguideapp.auth

import com.example.roadguideapp.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

internal object CompanionClient {
    private val httpClient = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    data class DriverPost(
        val id: String,
        val driverId: String,
        val driverName: String,
        val from: String,
        val to: String,
        val route: String,
        val date: String,
        val time: String,
        val seats: Int,
        val seatsBooked: Int,
        val pricePerSeat: Double,
        val vehicle: String,
        val preferences: String,
    ) {
        val availableSeats: Int
            get() = (seats - seatsBooked).coerceAtLeast(0)

        fun routeStops(): List<String> {
            val summary = route.trim()
            if (summary.isNotEmpty()) {
                return summary.split("→")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
            return buildList {
                if (from.isNotBlank()) add(from.trim())
                if (to.isNotBlank() && to.trim() != from.trim()) add(to.trim())
            }
        }
    }

    sealed class ListDriverPostsResult {
        data class Success(val posts: List<DriverPost>) : ListDriverPostsResult()
        data class Failure(val message: String) : ListDriverPostsResult()
    }

    sealed class CreateDriverPostResult {
        data class Success(val id: String) : CreateDriverPostResult()
        data class Failure(val message: String, val statusCode: Int = 0) : CreateDriverPostResult()
    }

    sealed class BookDriverPostResult {
        data class Success(val bookingId: String) : BookDriverPostResult()
        data class Failure(val message: String, val statusCode: Int = 0) : BookDriverPostResult()
    }

    fun listDriverPosts(bearerToken: String): ListDriverPostsResult {
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/companion/driver-posts")
            .header("Authorization", "Bearer $bearerToken")
            .get()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use ListDriverPostsResult.Failure(
                        body.optString("error").ifBlank { "Failed to load rides." },
                    )
                }
                val array = body.optJSONArray("posts") ?: JSONArray()
                ListDriverPostsResult.Success(parseDriverPosts(array))
            }
        }.getOrElse {
            ListDriverPostsResult.Failure("Could not connect to backend.")
        }
    }

    fun createDriverPost(
        bearerToken: String,
        from: String,
        to: String,
        waypoints: List<String>,
        routeSummary: String,
        departAtIso: String,
        seats: Int,
        pricePerSeat: Double,
        vehicle: String,
        preferences: String,
    ): CreateDriverPostResult {
        val payload = JSONObject()
            .put("from", from.trim())
            .put("to", to.trim())
            .put(
                "waypoints",
                JSONArray(
                    waypoints
                        .map { it.trim() }
                        .filter { it.isNotEmpty() },
                ),
            )
            .put("route", routeSummary.trim())
            .put("departAt", departAtIso.trim())
            .put("seats", seats)
            .put("pricePerSeat", pricePerSeat)
            .put("vehicle", vehicle.trim())
            .put("preferences", preferences.trim())
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/companion/driver-posts")
            .header("Authorization", "Bearer $bearerToken")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use CreateDriverPostResult.Failure(
                        message = body.optString("error").ifBlank { "Failed to create ride." },
                        statusCode = response.code,
                    )
                }
                val postId = body.optString("id").trim()
                if (postId.isBlank()) {
                    CreateDriverPostResult.Failure("Invalid create response.", response.code)
                } else {
                    CreateDriverPostResult.Success(postId)
                }
            }
        }.getOrElse {
            CreateDriverPostResult.Failure("Could not connect to backend.")
        }
    }

    fun bookDriverPost(
        bearerToken: String,
        postId: String,
        seats: Int = 1,
    ): BookDriverPostResult {
        val payload = JSONObject().put("seats", seats.coerceAtLeast(1))
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/companion/driver-posts/${postId.trim()}/book")
            .header("Authorization", "Bearer $bearerToken")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use BookDriverPostResult.Failure(
                        message = body.optString("error").ifBlank { "Failed to book seat." },
                        statusCode = response.code,
                    )
                }
                val bookingId = body.optString("bookingId").trim()
                if (bookingId.isBlank()) {
                    BookDriverPostResult.Failure("Invalid booking response.", response.code)
                } else {
                    BookDriverPostResult.Success(bookingId)
                }
            }
        }.getOrElse {
            BookDriverPostResult.Failure("Could not connect to backend.")
        }
    }

    private fun parseDriverPosts(array: JSONArray): List<DriverPost> = buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id").trim()
            if (id.isBlank()) continue
            add(
                DriverPost(
                    id = id,
                    driverId = item.optString("driverId").trim(),
                    driverName = item.optString("driver").trim().ifBlank { "Unknown driver" },
                    from = item.optString("from").trim(),
                    to = item.optString("to").trim(),
                    route = item.optString("route").trim(),
                    date = item.optString("date").trim(),
                    time = item.optString("time").trim(),
                    seats = item.optInt("seats", 0),
                    seatsBooked = item.optInt("seatsBooked", 0),
                    pricePerSeat = item.optDouble("pricePerSeat", 0.0).takeIf { it.isFinite() } ?: 0.0,
                    vehicle = item.optString("vehicle").trim(),
                    preferences = item.optString("preferences").trim(),
                ),
            )
        }
    }

    private fun baseUrl(): String = BuildConfig.MAIN_BACKEND_BASE_URL.trim().trimEnd('/')
}

