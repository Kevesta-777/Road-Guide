package com.example.roadguideapp.goldhunt.achievements.badges

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
internal interface AchievementBadgeDao {
    @Query("SELECT * FROM achievement_badge_entry ORDER BY unlockDateMs DESC, badgeKey ASC")
    suspend fun all(): List<AchievementBadgeEntryEntity>

    @Query("SELECT * FROM achievement_badge_entry WHERE badgeKey = :badgeKey LIMIT 1")
    suspend fun get(badgeKey: String): AchievementBadgeEntryEntity?

    @Upsert
    suspend fun upsert(entity: AchievementBadgeEntryEntity)

    @Upsert
    suspend fun upsertAll(entities: List<AchievementBadgeEntryEntity>)
}
