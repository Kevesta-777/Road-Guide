package com.example.roadguideapp.auth

import com.example.roadguideapp.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.Instant

internal object FriendsClient {
    private val httpClient = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    data class FriendProfile(
        val profileId: String,
        val identifier: String,
        val name: String,
    ) {
        val displayName: String
            get() = name.trim().ifBlank { identifier.trim() }
    }

    data class FriendEntry(
        val profileId: String,
        val displayName: String,
        val addedAtEpochMs: Long,
    )

    sealed class ProfileLookupResult {
        data class Success(val profile: FriendProfile) : ProfileLookupResult()
        data class Failure(val message: String, val statusCode: Int) : ProfileLookupResult()
    }

    sealed class FriendsListResult {
        data class Success(val friends: List<FriendEntry>) : FriendsListResult()
        data class Failure(val message: String) : FriendsListResult()
    }

    sealed class AddFriendApiResult {
        data class Success(val friend: FriendProfile) : AddFriendApiResult()
        data class Failure(val message: String, val statusCode: Int) : AddFriendApiResult()
    }

    sealed class RemoveFriendApiResult {
        data object Success : RemoveFriendApiResult()
        data class Failure(val message: String, val statusCode: Int = 0) : RemoveFriendApiResult()
    }

    fun lookupProfile(profileId: String, bearerToken: String): ProfileLookupResult {
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/users/profile/${profileId.trim()}")
            .header("Authorization", "Bearer $bearerToken")
            .get()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use ProfileLookupResult.Failure(
                        message = body.optString("error").ifBlank { "Profile lookup failed." },
                        statusCode = response.code,
                    )
                }
                parseProfile(body.optJSONObject("profile"))
                    ?.let { ProfileLookupResult.Success(it) }
                    ?: ProfileLookupResult.Failure("Invalid profile response.", response.code)
            }
        }.getOrElse {
            ProfileLookupResult.Failure("Could not connect to backend.", 0)
        }
    }

    fun listFriends(bearerToken: String): FriendsListResult {
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/friends")
            .header("Authorization", "Bearer $bearerToken")
            .get()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use FriendsListResult.Failure(
                        body.optString("error").ifBlank { "Failed to load friends." },
                    )
                }
                val array = body.optJSONArray("friends") ?: return@use FriendsListResult.Success(emptyList())
                val friends = buildList {
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        val profile = parseProfile(item) ?: continue
                        add(
                            FriendEntry(
                                profileId = profile.profileId,
                                displayName = profile.displayName,
                                addedAtEpochMs = parseInstantEpochMs(item.optString("addedAt")),
                            ),
                        )
                    }
                }
                FriendsListResult.Success(friends)
            }
        }.getOrElse {
            FriendsListResult.Failure("Could not connect to backend.")
        }
    }

    fun addFriend(profileId: String, bearerToken: String): AddFriendApiResult {
        val payload = JSONObject().put("profileId", profileId.trim())
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/friends")
            .header("Authorization", "Bearer $bearerToken")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                if (!response.isSuccessful) {
                    return@use AddFriendApiResult.Failure(
                        message = body.optString("error").ifBlank { "Failed to add friend." },
                        statusCode = response.code,
                    )
                }
                parseProfile(body.optJSONObject("friend"))
                    ?.let { AddFriendApiResult.Success(it) }
                    ?: AddFriendApiResult.Failure("Invalid add-friend response.", response.code)
            }
        }.getOrElse {
            AddFriendApiResult.Failure("Could not connect to backend.", 0)
        }
    }

    fun removeFriend(profileId: String, bearerToken: String): RemoveFriendApiResult {
        val request = Request.Builder()
            .url("${baseUrl()}/api/v1/friends/${profileId.trim()}")
            .header("Authorization", "Bearer $bearerToken")
            .delete()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    RemoveFriendApiResult.Success
                } else {
                    val bodyText = response.body?.string().orEmpty()
                    val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else JSONObject()
                    RemoveFriendApiResult.Failure(
                        body.optString("error").ifBlank { "Failed to remove friend." },
                        response.code,
                    )
                }
            }
        }.getOrElse {
            RemoveFriendApiResult.Failure("Could not connect to backend.")
        }
    }

    private fun parseProfile(obj: JSONObject?): FriendProfile? {
        if (obj == null) return null
        val profileId = obj.optString("profileId").trim()
        if (!OfflineFriendsStore.isValidProfileId(profileId)) return null
        return FriendProfile(
            profileId = profileId,
            identifier = obj.optString("identifier").trim(),
            name = obj.optString("name").trim(),
        )
    }

    private fun parseInstantEpochMs(raw: String): Long {
        if (raw.isBlank()) return System.currentTimeMillis()
        return runCatching { Instant.parse(raw).toEpochMilli() }.getOrDefault(System.currentTimeMillis())
    }

    private fun baseUrl(): String = BuildConfig.MAIN_BACKEND_BASE_URL.trim().trimEnd('/')
}
