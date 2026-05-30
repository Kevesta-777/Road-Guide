package com.example.roadguideapp.auth

import android.content.Context
import com.example.roadguideapp.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Backend-auth session cache for this device.
 * Credentials/session are persisted locally, while authentication is validated by backend APIs.
 */
object OfflineAuthStore {

    private const val ROOT_DIR = "offline_auth"
    private const val CREDENTIALS_FILE = "credentials.json"
    private const val SESSION_FILE = "session.json"
    private const val KEY_PROFILE_ID = "profileId"
    private const val KEY_TOKEN = "token"
    private const val KEY_EMAIL = "email"
    private const val KEY_NAME = "name"
    private const val KEY_ROLE = "role"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val httpClient = OkHttpClient()

    const val MIN_IDENTIFIER_LENGTH = 2
    /** Must match road-guide-backend minimum (8). */
    const val MIN_PASSWORD_LENGTH = 8

    fun hasAccount(context: Context): Boolean = readCredentials(context) != null

    fun isSessionActive(context: Context): Boolean = readSession(context)?.optString(KEY_TOKEN)?.isNotBlank() == true

    fun sessionIdentifier(context: Context): String? =
        readSession(context)?.optString("identifier")?.takeIf { it.isNotEmpty() }

    fun sessionToken(context: Context): String? =
        readSession(context)?.optString(KEY_TOKEN)?.takeIf { it.isNotEmpty() }

    fun sessionRole(context: Context): String? =
        readSession(context)?.optString(KEY_ROLE)?.takeIf { it.isNotEmpty() }
        ?: readCredentials(context)?.optString(KEY_ROLE)?.takeIf { it.isNotEmpty() }

    fun isBusinessUser(context: Context): Boolean = sessionRole(context) == "business"

    fun storedIdentifier(context: Context): String? =
        readCredentials(context)?.optString("identifier")?.takeIf { it.isNotEmpty() }

    /** Stable public ID for QR and adding friends; persisted in credentials. */
    fun profileId(context: Context): String? {
        val credentials = readCredentials(context) ?: return null
        return ensureProfileId(context, credentials)
    }

    fun authenticate(context: Context, identifier: String, password: String): AuthResult {
        val idError = validateIdentifier(identifier)
        if (idError != null) return AuthResult.Failure(idError)
        if (password.isEmpty()) return AuthResult.Failure(AuthError.EmptyPassword)

        val normalizedId = identifier.trim()
        val payload = JSONObject()
            .put("email", toBackendEmail(normalizedId))
            .put("password", password)
        val response = postJson("/auth/login", payload)
        if (!response.ok) {
            return when {
                response.statusCode == 0 -> AuthResult.Failure(AuthError.NetworkError)
                response.statusCode == 401 || response.statusCode == 404 ->
                    AuthResult.Failure(AuthError.InvalidCredentials)
                else -> serverFailure(response)
            }
        }
        val body = response.body ?: return AuthResult.Failure(AuthError.NetworkError)
        val user = body.optJSONObject("user") ?: return AuthResult.Failure(AuthError.NetworkError)
        val token = body.optString(KEY_TOKEN).trim()
        if (token.isEmpty()) return AuthResult.Failure(AuthError.NetworkError)
        writeCredentials(
            context = context,
            identifier = normalizedId,
            profileId = user.optString("id").ifBlank { UUID.randomUUID().toString() },
            email = user.optString("email"),
            name = user.optString("name"),
            role = user.optString("role").ifBlank { "visitor" },
        )
        writeSession(context, normalizedId, token, user.optString("role").ifBlank { "visitor" })
        return AuthResult.Success
    }

    fun signUp(context: Context, identifier: String, password: String): AuthResult {
        val idError = validateIdentifier(identifier)
        if (idError != null) return AuthResult.Failure(idError)
        val passwordError = validatePassword(password)
        if (passwordError != null) return AuthResult.Failure(passwordError)

        val normalizedId = identifier.trim()
        val payload = JSONObject()
            .put("email", toBackendEmail(normalizedId))
            .put("name", normalizedId)
            .put("password", password)
        val registerResponse = postJson("/auth/register", payload)
        if (!registerResponse.ok) {
            return when {
                registerResponse.statusCode == 0 -> AuthResult.Failure(AuthError.NetworkError)
                registerResponse.statusCode == 409 -> AuthResult.Failure(AuthError.AccountAlreadyExists)
                else -> serverFailure(registerResponse)
            }
        }
        val body = registerResponse.body ?: return AuthResult.Failure(AuthError.NetworkError)
        val user = body.optJSONObject("user") ?: return AuthResult.Failure(AuthError.NetworkError)
        val token = body.optString(KEY_TOKEN).trim()
        if (token.isEmpty()) return AuthResult.Failure(AuthError.NetworkError)
        writeCredentials(
            context = context,
            identifier = normalizedId,
            profileId = user.optString("id").ifBlank { UUID.randomUUID().toString() },
            email = user.optString("email"),
            name = user.optString("name"),
            role = user.optString("role").ifBlank { "visitor" },
        )
        writeSession(context, normalizedId, token, user.optString("role").ifBlank { "visitor" })
        return AuthResult.Success
    }

    fun resetCredentials(
        context: Context,
        currentIdentifier: String,
        currentPassword: String,
        newIdentifier: String,
        newPassword: String,
    ): AuthResult {
        return AuthResult.Failure(AuthError.UnsupportedOperation)
    }

    fun endSession(context: Context) {
        sessionFile(context).delete()
    }

    /** Refreshes cached role/name from backend `/auth/me` when a session token exists. */
    fun refreshUserFromBackend(context: Context): Boolean {
        val token = sessionToken(context) ?: return false
        val base = BuildConfig.MAIN_BACKEND_BASE_URL.trim().trimEnd('/')
        val request = Request.Builder()
            .url("$base/api/v1/auth/me")
            .header("Authorization", "Bearer $token")
            .get()
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use false
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else return@use false
                val user = body.optJSONObject("user") ?: return@use false
                val role = user.optString("role").ifBlank { "visitor" }
                val session = readSession(context) ?: return@use false
                session.put(KEY_ROLE, role)
                writeAtomically(sessionFile(context), session.toString())
                readCredentials(context)?.let { credentials ->
                    credentials.put(KEY_ROLE, role)
                    credentials.put(KEY_NAME, user.optString("name"))
                    writeAtomically(credentialsFile(context), credentials.toString())
                }
                true
            }
        }.getOrDefault(false)
    }

    /** Removes stored credentials and session so a new account can be created. */
    fun clearAccount(context: Context) {
        credentialsFile(context).delete()
        sessionFile(context).delete()
    }

    internal fun validateIdentifier(identifier: String): AuthError? {
        val trimmed = identifier.trim()
        return when {
            trimmed.isEmpty() -> AuthError.EmptyIdentifier
            trimmed.length < MIN_IDENTIFIER_LENGTH -> AuthError.IdentifierTooShort
            else -> null
        }
    }

    internal fun validatePassword(password: String): AuthError? {
        return when {
            password.isEmpty() -> AuthError.EmptyPassword
            password.length < MIN_PASSWORD_LENGTH -> AuthError.PasswordTooShort
            else -> null
        }
    }

    private fun ensureProfileId(context: Context, credentials: JSONObject): String {
        val existing = credentials.optString(KEY_PROFILE_ID).trim()
        if (OfflineFriendsStore.isValidProfileId(existing)) return existing
        val newId = UUID.randomUUID().toString()
        val updated = JSONObject(credentials.toString()).put(KEY_PROFILE_ID, newId)
        writeAtomically(credentialsFile(context), updated.toString())
        return newId
    }

    private fun writeCredentials(
        context: Context,
        identifier: String,
        profileId: String,
        email: String,
        name: String,
        role: String,
    ) {
        val payload = JSONObject()
            .put("identifier", identifier.trim())
            .put(KEY_PROFILE_ID, profileId)
            .put(KEY_EMAIL, email)
            .put(KEY_NAME, name)
            .put(KEY_ROLE, role)
        writeAtomically(credentialsFile(context), payload.toString())
    }

    private fun writeSession(context: Context, identifier: String, token: String, role: String) {
        val payload = JSONObject()
            .put("identifier", identifier)
            .put(KEY_TOKEN, token)
            .put(KEY_ROLE, role)
        writeAtomically(sessionFile(context), payload.toString())
    }

    private fun readCredentials(context: Context): JSONObject? {
        val file = credentialsFile(context)
        if (!file.isFile) return null
        return runCatching { JSONObject(file.readText(Charsets.UTF_8)) }.getOrNull()
    }

    private fun readSession(context: Context): JSONObject? {
        val file = sessionFile(context)
        if (!file.isFile) return null
        return runCatching { JSONObject(file.readText(Charsets.UTF_8)) }.getOrNull()
    }

    private fun authRoot(context: Context): File =
        File(context.applicationContext.filesDir, ROOT_DIR).also { it.mkdirs() }

    private fun credentialsFile(context: Context): File =
        authRoot(context).resolve(CREDENTIALS_FILE)

    private fun sessionFile(context: Context): File =
        authRoot(context).resolve(SESSION_FILE)

    private fun toBackendEmail(identifier: String): String {
        val trimmed = identifier.trim()
        return if (trimmed.contains("@")) trimmed.lowercase() else "${trimmed.lowercase()}@roadguide.local"
    }

    private fun serverFailure(response: HttpResult): AuthResult.Failure {
        val detail = response.body?.optString("error")?.trim().orEmpty().ifEmpty { null }
        return AuthResult.Failure(AuthError.ServerError, detail)
    }

    private fun postJson(path: String, payload: JSONObject): HttpResult {
        val base = BuildConfig.MAIN_BACKEND_BASE_URL.trim().trimEnd('/')
        val request = Request.Builder()
            .url("$base/api/v1$path")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        return runCatching {
            httpClient.newCall(request).execute().use { response ->
                val bodyText = response.body?.string().orEmpty()
                val body = if (bodyText.isNotBlank()) JSONObject(bodyText) else null
                HttpResult(response.isSuccessful, response.code, body)
            }
        }.getOrElse { HttpResult(false, 0, null) }
    }

    private data class HttpResult(
        val ok: Boolean,
        val statusCode: Int,
        val body: JSONObject?,
    )

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

sealed class AuthResult {
    data object Success : AuthResult()
    data class Failure(val error: AuthError, val detail: String? = null) : AuthResult()
}

enum class AuthError {
    NoAccount,
    AccountAlreadyExists,
    InvalidCredentials,
    EmptyIdentifier,
    IdentifierTooShort,
    EmptyPassword,
    PasswordTooShort,
    PasswordMismatch,
    EmptyFields,
    NetworkError,
    ServerError,
    UnsupportedOperation,
}
