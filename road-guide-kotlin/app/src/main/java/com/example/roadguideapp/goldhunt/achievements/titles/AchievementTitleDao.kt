package com.example.roadguideapp.goldhunt.achievements.titles

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
internal interface AchievementTitleDao {
    @Query("SELECT * FROM achievement_title_entry ORDER BY unlockDateMs DESC, titleKey ASC")
    suspend fun all(): List<AchievementTitleEntryEntity>

    @Query("SELECT * FROM achievement_title_entry WHERE titleKey = :titleKey LIMIT 1")
    suspend fun get(titleKey: String): AchievementTitleEntryEntity?

    @Upsert
    suspend fun upsert(entity: AchievementTitleEntryEntity)

    @Upsert
    suspend fun upsertAll(entities: List<AchievementTitleEntryEntity>)
}
