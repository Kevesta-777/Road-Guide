package com.example.roadguideapp.goldhunt.achievements.statistics

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementMapper
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantDao
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardGrantResult
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository

/**
 * Loads, backfills, and incrementally updates lifetime achievement statistics
 * on the explorer profile.
 */
internal class AchievementStatisticsManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val database = GoldHuntDatabase.get(appContext)
    private val explorerProfileRepository = ExplorerProfileRepository.get(appContext)
    private val achievementDao = database.achievementDao()
    private val grantDao: AchievementRewardGrantDao = database.achievementRewardGrantDao()

    suspend fun load(): AchievementStatistics {
        val catalogSize = AchievementRegistry.count()
        val profile = explorerProfileRepository.ensureProfile()
        val persisted = profile.achievementStatistics.withCatalogSize(catalogSize)
        val computed = backfill(catalogSize)
        if (needsBackfill(persisted, computed)) {
            explorerProfileRepository.updateProfile { current ->
                current.copy(achievementStatistics = computed)
            }
            return computed
        }
        return persisted
    }

    suspend fun recordGrant(
        achievement: Achievement,
        grantResult: AchievementRewardGrantResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): AchievementStatistics {
        if (!grantResult.isNewGrant) {
            return load()
        }
        val catalogSize = AchievementRegistry.count()
        val updated = explorerProfileRepository.updateProfile(timestampMs) { profile ->
            profile.copy(
                achievementStatistics = profile.achievementStatistics
                    .withCatalogSize(catalogSize)
                    .withGrant(achievement, grantResult),
            )
        }
        return updated.achievementStatistics.withCatalogSize(catalogSize)
    }

    suspend fun reconcile(timestampMs: Long = System.currentTimeMillis()): AchievementStatistics {
        val catalogSize = AchievementRegistry.count()
        val stats = backfill(catalogSize)
        explorerProfileRepository.updateProfile(timestampMs) { profile ->
            profile.copy(achievementStatistics = stats)
        }
        return stats
    }

    private suspend fun backfill(catalogSize: Int): AchievementStatistics {
        val achievements = achievementDao.getAll().mapNotNull { AchievementMapper.toDomain(it) }
        val grants = grantDao.getAll()
        return AchievementStatisticsCalculator.compute(
            achievements = achievements,
            grants = grants,
            catalogSize = catalogSize,
        )
    }

    private fun needsBackfill(
        persisted: AchievementStatistics,
        computed: AchievementStatistics,
    ): Boolean =
        persisted.completedCount != computed.completedCount ||
            persisted.rareCompletedCount != computed.rareCompletedCount ||
            persisted.legendaryCompletedCount != computed.legendaryCompletedCount ||
            persisted.creditsEarned != computed.creditsEarned ||
            persisted.xpEarned != computed.xpEarned

    companion object {
        @Volatile
        private var instance: AchievementStatisticsManager? = null

        fun get(context: Context): AchievementStatisticsManager =
            instance ?: synchronized(this) {
                instance ?: AchievementStatisticsManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
