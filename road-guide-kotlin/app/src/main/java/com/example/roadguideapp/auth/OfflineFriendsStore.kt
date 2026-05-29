package com.example.roadguideapp.auth

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Local-only friends list: profile IDs saved on this device (no server sync).
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
        val friends = listFriends(context).toMutableList()
        friends.add(
            OfflineFriend(
                profileId = normalizedId,
                displayName = displayName?.trim()?.takeIf { it.isNotEmpty() },
                addedAtEpochMs = System.currentTimeMillis(),
            ),
        )
        writeFriends(context, friends)
        return AddFriendResult.Success
    }

    fun removeFriend(context: Context, profileId: String) {
        val normalizedId = profileId.trim()
        val remaining = listFriends(context).filterNot {
            it.profileId.equals(normalizedId, ignoreCase = true)
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

sealed class AddFriendResult {
    data object Success : AddFriendResult()
    data class Failure(val error: FriendError) : AddFriendResult()
}

enum class FriendError {
    EmptyProfileId,
    InvalidProfileId,
    AlreadyFriends,
    CannotAddSelf,
}

fun friendErrorMessage(context: android.content.Context, error: FriendError): String =
    when (error) {
        FriendError.EmptyProfileId -> context.getString(com.example.roadguideapp.R.string.friends_error_empty_id)
        FriendError.InvalidProfileId -> context.getString(com.example.roadguideapp.R.string.friends_error_invalid_id)
        FriendError.AlreadyFriends -> context.getString(com.example.roadguideapp.R.string.friends_error_already_added)
        FriendError.CannotAddSelf -> context.getString(com.example.roadguideapp.R.string.friends_error_cannot_add_self)
    }
