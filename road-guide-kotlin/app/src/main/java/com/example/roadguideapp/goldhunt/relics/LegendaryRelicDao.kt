package com.example.roadguideapp.goldhunt.relics

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface LegendaryRelicDao {
    @Query("SELECT * FROM legendary_relic ORDER BY category ASC, relicId ASC")
    suspend fun getAll(): List<LegendaryRelicEntity>

    @Query("SELECT * FROM legendary_relic WHERE relicId = :relicId LIMIT 1")
    suspend fun get(relicId: String): LegendaryRelicEntity?

    @Query("SELECT COUNT(*) FROM legendary_relic WHERE completed = 1")
    suspend fun countCompleted(): Int

    @Query("SELECT COALESCE(SUM(piecesCollected), 0) FROM legendary_relic")
    suspend fun sumPiecesCollected(): Int

    @Query("SELECT COALESCE(SUM(pieceCount), 0) FROM legendary_relic")
    suspend fun sumTotalPieces(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LegendaryRelicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<LegendaryRelicEntity>)
}
