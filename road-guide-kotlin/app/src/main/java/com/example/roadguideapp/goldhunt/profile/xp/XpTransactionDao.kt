package com.example.roadguideapp.goldhunt.profile.xp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface XpTransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: XpTransactionEntity): Long

    @Query("SELECT COALESCE(SUM(xpAwarded), 0) FROM xp_transaction")
    suspend fun sumLifetimeXp(): Long

    @Query("SELECT * FROM xp_transaction WHERE id = :id LIMIT 1")
    suspend fun get(id: String): XpTransactionEntity?

    @Query("SELECT * FROM xp_transaction ORDER BY timestampMs DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<XpTransactionEntity>

    @Query("SELECT * FROM xp_transaction WHERE source = :source ORDER BY timestampMs DESC LIMIT :limit")
    suspend fun recentBySource(source: String, limit: Int): List<XpTransactionEntity>
}
