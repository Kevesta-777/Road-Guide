package com.example.roadguideapp.goldhunt.profile.xp

/**
 * Domain record for a single XP award (storage-only; no level logic).
 */
internal data class XpTransaction(
    val id: String,
    val timestampMs: Long,
    val source: XpSource,
    val xpAwarded: Long,
    val description: String,
    val metadataJson: String? = null,
    val schemaVersion: Int = XpTransactionSchema.VERSION,
)
