package com.example.roadguideapp.goldhunt.achievements.progress

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardSchema
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity

internal class AchievementProgressCounterSource private constructor(context: Context) {
    private val database = GoldHuntDatabase.get(context.applicationContext)
    private val explorerDao = database.explorerProfileDao()
    private val exploredDao = database.exploredCellDao()
    private val legacyDao = database.panoramaHuntAchievementProgressDao()
    private val rewardHookDao = database.panoramaHuntRewardHookDao()

    suspend fun load(): AchievementProgressCounters {
        val profile = explorerDao.get() ?: ExplorerProfileEntity()
        val exploredCells = exploredDao.countL0()
        val storyFragmentsEarned = rewardHookDao.countByHookType(
            SeasonalEventRewardSchema.HookTypes.STORY_FRAGMENT,
        ) + profile.seasonalEventStatsFragmentsEarned
        val legendaryRelicsEarned = rewardHookDao.countByHookType(
            SeasonalEventRewardSchema.HookTypes.LEGENDARY_RELIC,
        )
        val legacyCompletionCounts = legacyDao.getAll().associate { row ->
            row.achievementKey to row.completionCount
        }
        return AchievementProgressCounters.fromProfile(
            profile = profile,
            exploredCells = exploredCells,
            storyFragmentsEarned = storyFragmentsEarned,
            legendaryRelicsEarned = legendaryRelicsEarned,
            legacyCompletionCounts = legacyCompletionCounts,
        )
    }

    companion object {
        @Volatile
        private var instance: AchievementProgressCounterSource? = null

        fun get(context: Context): AchievementProgressCounterSource =
            instance ?: synchronized(this) {
                instance ?: AchievementProgressCounterSource(context.applicationContext)
                    .also { instance = it }
            }
    }
}
