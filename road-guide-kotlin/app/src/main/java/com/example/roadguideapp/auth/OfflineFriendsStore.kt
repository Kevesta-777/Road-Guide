package com.example.roadguideapp.auth

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Friends list synced with the backend and cached locally for offline display.
 */
object OfflineFriendsStore {

    private const val ROOT_DIR = "offline_friends"
    private const val FRIENDS_FILE = "friends.json"

    private val profileIdRegex =
        Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

    fun isValidProfileId(profileId: String): Boolean =
        profileIdRegex.matches(profileId.trim())

    fun listFriends(context: Context): List<OfflineFriend> {
        val array = readFriendsArray(context) ?: return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val profileId = obj.optString("profileId").trim()
                if (!isValidProfileId(profileId)) continue
                add(
                    OfflineFriend(
                        profileId = profileId,
                        displayName = obj.optString("displayName").trim().takeIf { it.isNotEmpty() },
                        addedAtEpochMs = obj.optLong("addedAtEpochMs", 0L),
                    ),
                )
            }
        }
    }

    fun friendCount(context: Context): Int = listFriends(context).size

    fun hasFriend(context: Context, profileId: String): Boolean =
        listFriends(context).any { it.profileId.equals(profileId.trim(), ignoreCase = true) }

    fun refreshFromBackend(context: Context): Boolean {
        val token = OfflineAuthStore.sessionToken(context) ?: return false
        return when (val result = FriendsClient.listFriends(token)) {
            is FriendsClient.FriendsListResult.Success -> {
                writeFriends(
                    context,
                    result.friends.map {
                        OfflineFriend(
                            profileId = it.profileId,
                            displayName = it.displayName,
                            addedAtEpochMs = it.addedAtEpochMs,
                        )
                    },
                )
                true
            }
            is FriendsClient.FriendsListResult.Failure -> false
        }
    }

    fun resolveProfile(context: Context, profileId: String): ResolveProfileResult {
        val normalizedId = profileId.trim()
        if (normalizedId.isEmpty()) return ResolveProfileResult.Failure(FriendError.EmptyProfileId)
        if (!isValidProfileId(normalizedId)) {
            return ResolveProfileResult.Failure(FriendError.InvalidProfileId)
        }
        val selfId = OfflineAuthStore.profileId(context)
        if (selfId != null && selfId.equals(normalizedId, ignoreCase = true)) {
            return ResolveProfileResult.Failure(FriendError.CannotAddSelf)
        }
        val token = OfflineAuthStore.sessionToken(context)
            ?: return ResolveProfileResult.Failure(FriendError.NotSignedIn)
        return when (val result = FriendsClient.lookupProfile(normalizedId, token)) {
            is FriendsClient.ProfileLookupResult.Success -> {
                ResolveProfileResult.Success(result.profile.displayName)
            }
            is FriendsClient.ProfileLookupResult.Failure -> {
                when (result.statusCode) {
                    404 -> ResolveProfileResult.Failure(FriendError.ProfileNotFound)
                    else -> ResolveProfileResult.Failure(FriendError.NetworkError)
                }
            }
        }
    }

    fun addFriend(
        context: Context,
        profileId: String,
        displayName: String? = null,
    ): AddFriendResult {
        val normalizedId = profileId.trim()
        if (normalizedId.isEmpty()) return AddFriendResult.Failure(FriendError.EmptyProfileId)
        if (!isValidProfileId(normalizedId)) {
            return AddFriendResult.Failure(FriendError.InvalidProfileId)
        }
        val selfId = OfflineAuthStore.profileId(context)
        if (selfId != null && selfId.equals(normalizedId, ignoreCase = true)) {
            return AddFriendResult.Failure(FriendError.CannotAddSelf)
        }
        if (hasFriend(context, normalizedId)) {
            return AddFriendResult.Failure(FriendError.AlreadyFriends)
        }
        val token = OfflineAuthStore.sessionToken(context)
            ?: return AddFriendResult.Failure(FriendError.NotSignedIn)

        when (val result = FriendsClient.addFriend(normalizedId, token)) {
            is FriendsClient.AddFriendApiResult.Success -> {
                val resolvedName = displayName?.trim()?.takeIf { it.isNotEmpty() }
                    ?: result.friend.displayName
                val friends = listFriends(context).toMutableList()
                friends.add(
                    OfflineFriend(
                        profileId = normalizedId,
                        displayName = resolvedName,
                        addedAtEpochMs = System.currentTimeMillis(),
                    ),
                )
                writeFriends(context, friends)
                return AddFriendResult.Success
            }
            is FriendsClient.AddFriendApiResult.Failure -> {
                return when (result.statusCode) {
                    404 -> AddFriendResult.Failure(FriendError.ProfileNotFound)
                    409 -> AddFriendResult.Failure(FriendError.AlreadyFriends)
                    400 -> {
                        if (result.message.contains("yourself", ignoreCase = true)) {
                            AddFriendResult.Failure(FriendError.CannotAddSelf)
                        } else {
                            AddFriendResult.Failure(FriendError.NetworkError)
                        }
                    }
                    else -> AddFriendResult.Failure(FriendError.NetworkError)
                }
            }
        }
    }

    fun removeFriend(context: Context, profileId: String): RemoveFriendResult {
        val normalizedId = profileId.trim()
        val token = OfflineAuthStore.sessionToken(context)
            ?: return RemoveFriendResult.Failure(FriendError.NotSignedIn)
        return when (val apiResult = FriendsClient.removeFriend(normalizedId, token)) {
            FriendsClient.RemoveFriendApiResult.Success -> {
                removeFriendLocally(context, normalizedId)
                RemoveFriendResult.Success
            }
            is FriendsClient.RemoveFriendApiResult.Failure -> {
                if (apiResult.statusCode == 404) {
                    removeFriendLocally(context, normalizedId)
                    RemoveFriendResult.Success
                } else {
                    RemoveFriendResult.Failure(FriendError.NetworkError)
                }
            }
        }
    }

    private fun removeFriendLocally(context: Context, profileId: String) {
        val remaining = listFriends(context).filterNot {
            it.profileId.equals(profileId, ignoreCase = true)
        }
        writeFriends(context, remaining)
    }

    private fun readFriendsArray(context: Context): JSONArray? {
        val file = friendsFile(context)
        if (!file.isFile) return null
        return runCatching {
            val root = JSONObject(file.readText(Charsets.UTF_8))
            root.optJSONArray("friends") ?: JSONArray()
        }.getOrNull()
    }

    private fun writeFriends(context: Context, friends: List<OfflineFriend>) {
        val array = JSONArray()
        friends.forEach { friend ->
            array.put(
                JSONObject()
                    .put("profileId", friend.profileId)
                    .put("displayName", friend.displayName.orEmpty())
                    .put("addedAtEpochMs", friend.addedAtEpochMs),
            )
        }
        val payload = JSONObject().put("friends", array)
        writeAtomically(friendsFile(context), payload.toString())
    }

    private fun friendsRoot(context: Context): File =
        File(context.applicationContext.filesDir, ROOT_DIR).also { it.mkdirs() }

    private fun friendsFile(context: Context): File =
        friendsRoot(context).resolve(FRIENDS_FILE)

    private fun writeAtomically(target: File, content: String) {
        val parent = target.parentFile ?: return
        if (!parent.exists()) parent.mkdirs()
        val tmp = File(parent, "${target.name}.tmp")
        tmp.outputStream().bufferedWriter(Charsets.UTF_8).use { it.write(content) }
        if (!tmp.renameTo(target)) {
            tmp.copyTo(target, overwrite = true)
            tmp.delete()
        }
    }
}

data class OfflineFriend(
    val profileId: String,
    val displayName: String?,
    val addedAtEpochMs: Long,
)

sealed class ResolveProfileResult {
    data class Success(val displayName: String) : ResolveProfileResult()
    data class Failure(val error: FriendError) : ResolveProfileResult()
}

sealed class AddFriendResult {
    data object Success : AddFriendResult()
    data class Failure(val error: FriendError) : AddFriendResult()
}

sealed class RemoveFriendResult {
    data object Success : RemoveFriendResult()
    data class Failure(val error: FriendError) : RemoveFriendResult()
}

enum class FriendError {
    EmptyProfileId,
    InvalidProfileId,
    AlreadyFriends,
    CannotAddSelf,
    ProfileNotFound,
    NotSignedIn,
    NetworkError,
}

fun friendErrorMessage(context: android.content.Context, error: FriendError): String =
    when (error) {
        FriendError.EmptyProfileId -> context.getString(com.example.roadguideapp.R.string.friends_error_empty_id)
        FriendError.InvalidProfileId -> context.getString(com.example.roadguideapp.R.string.friends_error_invalid_id)
        FriendError.AlreadyFriends -> context.getString(com.example.roadguideapp.R.string.friends_error_already_added)
        FriendError.CannotAddSelf -> context.getString(com.example.roadguideapp.R.string.friends_error_cannot_add_self)
        FriendError.ProfileNotFound -> context.getString(com.example.roadguideapp.R.string.friends_error_profile_not_found)
        FriendError.NotSignedIn -> context.getString(com.example.roadguideapp.R.string.friends_error_not_signed_in)
        FriendError.NetworkError -> context.getString(com.example.roadguideapp.R.string.friends_error_network)
    }
