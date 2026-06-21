package com.example.roadguideapp.goldhunt.relics.progress

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface RelicPieceGrantHookDao {
    @Query("SELECT eventId FROM relic_piece_grant_hook WHERE eventId = :eventId LIMIT 1")
    suspend fun findEventId(eventId: String): String?

    @Query(
        """
        SELECT eventId FROM relic_piece_grant_hook
        WHERE hookKey = :hookKey AND hookType = :hookType
        LIMIT 1
        """,
    )
    suspend fun findHook(hookKey: String, hookType: String): String?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: RelicPieceGrantHookEntity): Long

    @Query("SELECT * FROM relic_piece_grant_hook ORDER BY grantedAtMs ASC")
    suspend fun getAll(): List<RelicPieceGrantHookEntity>

    @Query("SELECT COUNT(*) FROM relic_piece_grant_hook")
    suspend fun countAll(): Int
}
