package com.example.roadguideapp.goldhunt.achievements

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface AchievementDao {
    @Query("SELECT * FROM achievement ORDER BY category ASC, achievementId ASC")
    suspend fun getAll(): List<AchievementEntity>

    @Query("SELECT * FROM achievement WHERE achievementId = :achievementId LIMIT 1")
    suspend fun get(achievementId: String): AchievementEntity?

    @Query(
        """
        SELECT * FROM achievement
        WHERE category = :category
        ORDER BY achievementId ASC
        """,
    )
    suspend fun getByCategory(category: String): List<AchievementEntity>

    @Query("SELECT COUNT(*) FROM achievement WHERE completed = 1")
    suspend fun countCompleted(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<AchievementEntity>)
}
