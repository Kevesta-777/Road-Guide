package com.example.roadguideapp.goldhunt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_event")
internal data class CreditEventEntity(
    @PrimaryKey val eventId: String,
    val ruleType: String,
    val amount: Int,
    val createdAt: Long,
    val payloadJson: String? = null,
)
