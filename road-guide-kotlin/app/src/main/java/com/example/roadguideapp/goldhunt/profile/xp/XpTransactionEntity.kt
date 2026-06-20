package com.example.roadguideapp.goldhunt.profile.xp

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Append-only XP award ledger persisted in `goldhunt.db`.
 * [metadataJson] holds optional future payloads (achievement id, treasure id, etc.).
 */
@Entity(
    tableName = XpTransactionSchema.TABLE_NAME,
    indices = [
        Index(value = ["timestampMs"]),
        Index(value = ["source"]),
    ],
)
internal data class XpTransactionEntity(
    @PrimaryKey val id: String,
    val timestampMs: Long,
    /** [XpSource.name] */
    val source: String,
    val xpAwarded: Long,
    val description: String,
    val metadataJson: String? = null,
    val schemaVersion: Int = XpTransactionSchema.VERSION,
)
