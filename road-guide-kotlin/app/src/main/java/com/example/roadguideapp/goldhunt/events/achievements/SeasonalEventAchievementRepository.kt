package com.example.roadguideapp.goldhunt.events.achievements

import android.content.Context
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.panorama.rewards.PanoramaHuntAchievementProgressRepository

internal class SeasonalEventAchievementRepository private constructor(context: Context) {
    private val achievementProgressRepository =
        PanoramaHuntAchievementProgressRepository.get(context.applicationContext)

    suspend fun increment(
        definition: SeasonalEventAchievementDefinition,
        increment: Int = 1,
        timestampMs: Long = System.currentTimeMillis(),
    ): SeasonalEventAchievementProgress? {
        if (!definition.enabled || increment <= 0) return null
        val result = achievementProgressRepository.increment(
            achievementKey = definition.key,
            increment = increment,
            timestampMs = timestampMs,
        )
        val progress = result.progress ?: return null
        return SeasonalEventAchievementProgress(
            definition = definition,
            currentCount = progress.completionCount,
            updatedAtMs = progress.updatedAtMs,
        )
    }

    suspend fun load(definition: SeasonalEventAchievementDefinition): SeasonalEventAchievementProgress {
        val stored = achievementProgressRepository.load(definition.key)
        return SeasonalEventAchievementProgress(
            definition = definition,
            currentCount = stored?.completionCount ?: 0,
            updatedAtMs = stored?.updatedAtMs ?: 0L,
        )
    }

    suspend fun loadEventSnapshot(type: SeasonalEventType): SeasonalEventAchievementProgressSnapshot =
        SeasonalEventAchievementProgressSnapshot(
            eventTypeKey = type.id,
            achievements = SeasonalEventAchievementCatalog.eventAchievements(type).map { load(it) },
        )

    suspend fun loadCategorySnapshot(
        category: SeasonalEventCategory,
    ): List<SeasonalEventAchievementProgress> =
        SeasonalEventAchievementCatalog.categoryAchievements(category).map { load(it) }

    companion object {
        @Volatile
        private var instance: SeasonalEventAchievementRepository? = null

        fun get(context: Context): SeasonalEventAchievementRepository =
            instance ?: synchronized(this) {
                instance ?: SeasonalEventAchievementRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
