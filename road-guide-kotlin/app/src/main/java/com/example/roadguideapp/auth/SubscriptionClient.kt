package com.example.roadguideapp.auth

// Reserved for a future in-app subscription page. Premium gating in Companion Finder
// is commented out in CompanionFinderScreen and CompanionOfferRideScreen until then.

import com.example.roadguideapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

internal object SubscriptionClient {
    private val httpClient = OkHttpClient()

    data class SubscriptionStatus(
        val active: Boolean,
        val planId: String,
        val planName: String,
        val planType: String,
        val expiresAt: String?,
        val canOfferRide: Boolean,
    )

    sealed class StatusResult {
        data class Success(val status: SubscriptionStatus) : StatusResult()
        data class Failure(val message: String) : StatusResult()
    }

    fun getStatus(bearerToken: String): StatusResult {
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/subscriptions/status")
            .header("Authorization", "Bearer $bearerToken")
            .get()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use StatusResult.Failure(
                        body.optString("error").ifBlank { "Failed to load subscription status." },
                    )
                }
                StatusResult.Success(
                    SubscriptionStatus(
                        active = body.optBoolean("active", false),
                        planId = body.optString("planId", "plan-free"),
                        planName = body.optString("planName", "Free"),
                        planType = body.optString("planType", "free"),
                        expiresAt = body.optString("expiresAt").takeIf { it.isNotBlank() },
                        canOfferRide = body.optBoolean("canOfferRide", false),
                    ),
                )
            }
        }.getOrElse {
            StatusResult.Failure("Could not connect to backend.")
        }
    }

    private fun baseUrl(): String = BuildConfig.MAIN_BACKEND_BASE_URL.trim().trimEnd('/')
}
