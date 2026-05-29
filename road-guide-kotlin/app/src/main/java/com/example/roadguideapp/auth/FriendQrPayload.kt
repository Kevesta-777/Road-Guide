package com.example.roadguideapp.auth

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Encodes/decodes friend invite QR payloads:
 * `roadguide://friend/v1?pid=<profileId>&name=<optional display name>`
 */
object FriendQrPayload {

    private const val SCHEME = "roadguide"
    private const val HOST = "friend"
    private const val PATH = "/v1"
    private const val QUERY_PID = "pid"
    private const val QUERY_NAME = "name"

    /** Builds a QR payload, or null when [profileId] is not a valid UUID. */
    fun encode(profileId: String, displayName: String? = null): String? {
        val trimmedId = profileId.trim()
        if (!OfflineFriendsStore.isValidProfileId(trimmedId)) return null
        val encodedId = URLEncoder.encode(trimmedId, StandardCharsets.UTF_8)
        val builder = StringBuilder()
            .append(SCHEME)
            .append("://")
            .append(HOST)
            .append(PATH)
            .append('?')
            .append(QUERY_PID)
            .append('=')
            .append(encodedId)
        val name = displayName?.trim().orEmpty()
        if (name.isNotEmpty()) {
            builder
                .append('&')
                .append(QUERY_NAME)
                .append('=')
                .append(URLEncoder.encode(name, StandardCharsets.UTF_8))
        }
        return builder.toString()
    }

    fun decode(raw: String): FriendQrPayloadData? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        if (!trimmed.startsWith("$SCHEME://$HOST")) return null
        val queryStart = trimmed.indexOf('?')
        if (queryStart < 0) return null
        val path = trimmed.substring(SCHEME.length + 3 + HOST.length, queryStart)
        if (path != PATH && path != "v1" && path != "/v1") return null
        val params = parseQuery(trimmed.substring(queryStart + 1))
        val profileId = params[QUERY_PID]?.trim().orEmpty()
        if (!OfflineFriendsStore.isValidProfileId(profileId)) return null
        val displayName = params[QUERY_NAME]?.trim()?.takeIf { it.isNotEmpty() }
        return FriendQrPayloadData(profileId = profileId, displayName = displayName)
    }

    private fun parseQuery(query: String): Map<String, String> {
        if (query.isEmpty()) return emptyMap()
        return query.split("&").mapNotNull { part ->
            val separator = part.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            val key = URLDecoder.decode(part.substring(0, separator), StandardCharsets.UTF_8)
            val value = URLDecoder.decode(part.substring(separator + 1), StandardCharsets.UTF_8)
            key to value
        }.toMap()
    }
}

data class FriendQrPayloadData(
    val profileId: String,
    val displayName: String?,
)
