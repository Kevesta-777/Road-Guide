package com.example.roadguideapp.goldhunt.relics

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface RelicPieceDao {
    @Query("SELECT * FROM relic_piece ORDER BY relicId ASC, pieceNumber ASC")
    suspend fun getAll(): List<RelicPieceEntity>

    @Query("SELECT * FROM relic_piece WHERE pieceId = :pieceId LIMIT 1")
    suspend fun get(pieceId: String): RelicPieceEntity?

    @Query("SELECT * FROM relic_piece WHERE relicId = :relicId ORDER BY pieceNumber ASC")
    suspend fun getByRelicId(relicId: String): List<RelicPieceEntity>

    @Query("SELECT pieceId FROM relic_piece WHERE discovered = 1")
    suspend fun getDiscoveredPieceIds(): List<String>

    @Query("SELECT COUNT(*) FROM relic_piece WHERE relicId = :relicId AND discovered = 1")
    suspend fun countDiscoveredForRelic(relicId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RelicPieceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<RelicPieceEntity>)
}
