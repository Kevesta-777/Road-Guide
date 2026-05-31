package com.example.roadguideapp.map

import com.example.roadguideapp.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

internal object BusinessClaimClient {
    private val httpClient = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    data class RegistrationGuidance(
        val contactPhone: String,
        val registrationAgentAddress: String,
        val availableRegistrationHours: String,
        val additionalInstructions: String,
    )

    data class ClaimStatus(
        val canEditBusiness: Boolean,
        val registrationGuidance: RegistrationGuidance?,
        val redirectToEditPath: String?,
    )

    sealed class ClaimStatusResult {
        data class Success(val status: ClaimStatus) : ClaimStatusResult()
        data class Failure(val message: String) : ClaimStatusResult()
    }

    sealed class ClaimRequestResult {
        data object Success : ClaimRequestResult()
        data class Failure(val message: String) : ClaimRequestResult()
    }

    sealed class ResolveResult {
        data class Success(val poiId: String, val canEditBusiness: Boolean = false) : ResolveResult()
        data class Failure(val message: String) : ResolveResult()
    }

    /**
     * Maps a map-feature place to a backend `business_pois` row, creating it on first use.
     * [externalRef] must be stable for the same physical place across map reloads.
     */
    fun resolvePoi(
        externalRef: String,
        name: String,
        address: String,
        latitude: Double,
        longitude: Double,
        bearerToken: String,
    ): ResolveResult {
        val payload = JSONObject()
            .put("externalRef", externalRef)
            .put("name", name)
            .put("address", address)
            .put("latitude", latitude)
            .put("longitude", longitude)
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/business-pois/resolve")
            .header("Authorization", "Bearer $bearerToken")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use ResolveResult.Failure(body.optString("error").ifBlank { "Failed to resolve place." })
                }
                val poiId = body.optJSONObject("poi")?.optString("id").orEmpty()
                if (poiId.isBlank()) {
                    ResolveResult.Failure("Backend did not return a place id.")
                } else {
                    ResolveResult.Success(
                        poiId = poiId,
                        canEditBusiness = body.optBoolean("canEditBusiness"),
                    )
                }
            }
        }.getOrElse {
            ResolveResult.Failure("Could not connect to backend.")
        }
    }

    fun fetchClaimStatus(poiId: String, bearerToken: String): ClaimStatusResult {
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/business-claims/$poiId")
            .header("Authorization", "Bearer $bearerToken")
            .get()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use ClaimStatusResult.Failure(body.optString("error").ifBlank { "Request failed." })
                }
                val guidanceJson = body.optJSONObject("registrationGuidance")
                val guidance = if (guidanceJson != null) {
                    RegistrationGuidance(
                        contactPhone = guidanceJson.optString("contactPhone"),
                        registrationAgentAddress = guidanceJson.optString("registrationAgentAddress"),
                        availableRegistrationHours = guidanceJson.optString("availableRegistrationHours"),
                        additionalInstructions = guidanceJson.optString("additionalInstructions"),
                    )
                } else {
                    null
                }
                val claimAction = body.optJSONObject("claimButtonAction")
                val redirectPath = claimAction?.optString("targetPath")?.takeIf { it.isNotBlank() }
                    ?: body.optString("redirectToEditPath").takeIf { it.isNotBlank() }
                ClaimStatusResult.Success(
                    ClaimStatus(
                        canEditBusiness = body.optBoolean("canEditBusiness"),
                        registrationGuidance = guidance,
                        redirectToEditPath = redirectPath,
                    ),
                )
            }
        }.getOrElse {
            ClaimStatusResult.Failure("Could not connect to backend.")
        }
    }

    fun createClaimRequest(poiId: String, bearerToken: String): ClaimRequestResult {
        val payload = JSONObject().put("message", "Requested from mobile place detail.")
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/business-claims/$poiId/requests")
            .header("Authorization", "Bearer $bearerToken")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (response.isSuccessful) {
                    ClaimRequestResult.Success
                } else {
                    ClaimRequestResult.Failure(body.optString("error").ifBlank { "Failed to submit request." })
                }
            }
        }.getOrElse {
            ClaimRequestResult.Failure("Could not connect to backend.")
        }
    }

    private fun baseUrl(): String = BuildConfig.MAIN_BACKEND_BASE_URL.trim().trimEnd('/')
}

