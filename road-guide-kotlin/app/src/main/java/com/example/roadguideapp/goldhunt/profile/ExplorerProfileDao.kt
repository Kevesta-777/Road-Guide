package com.example.roadguideapp.goldhunt.profile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface ExplorerProfileDao {
    @Query("SELECT * FROM explorer_profile WHERE id = :id LIMIT 1")
    suspend fun get(id: Int = ExplorerProfileSchema.SINGLETON_ID): ExplorerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ExplorerProfileEntity)

    @Query("DELETE FROM explorer_profile WHERE id = :id")
    suspend fun delete(id: Int = ExplorerProfileSchema.SINGLETON_ID)

    @Query(
        """
        UPDATE explorer_profile
        SET activeAchievementTitleKey = :titleKey, updatedAtMs = :timestampMs
        WHERE id = ${ExplorerProfileSchema.SINGLETON_ID}
        """,
    )
    suspend fun updateActiveAchievementTitleKey(titleKey: String?, timestampMs: Long)
}
