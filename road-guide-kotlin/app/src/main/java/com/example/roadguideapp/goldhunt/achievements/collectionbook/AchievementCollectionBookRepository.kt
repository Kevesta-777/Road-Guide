package com.example.roadguideapp.goldhunt.achievements.collectionbook

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRepository
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalMapper
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalRepository
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardHookDao
import com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatisticsManager
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRepository
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AchievementCollectionBookRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val database = GoldHuntDatabase.get(appContext)
    private val hookDao: AchievementRewardHookDao = database.achievementRewardHookDao()
    private val journalRepository = AchievementJournalRepository.get(appContext)
    private val badgeRepository = AchievementBadgeRepository.get(appContext)
    private val titleRepository = AchievementTitleRepository.get(appContext)
    private val statisticsManager = AchievementStatisticsManager.get(appContext)
    private val explorerProfileRepository = ExplorerProfileRepository.get(appContext)
    private val writeMutex = Mutex()

    suspend fun load(): AchievementCollectionBook = writeMutex.withLock {
        val journal = journalRepository.load()
        val badges = badgeRepository.load()
        val titles = titleRepository.load()
        val statistics = statisticsManager.load()
        val explorerLevel = explorerProfileRepository.loadProfile().explorerLevel
        val definitions = AchievementRegistry.allDefinitions()
        val entities = database.achievementDao().getAll()
        val achievementsByKey = AchievementJournalMapper.achievementMapFromEntities(
            definitions = definitions,
            entities = entities,
        ).mapValues { (_, achievement) -> achievement }
        val rewardHooks = loadRewardHooks()
        AchievementCollectionBookMapper.build(
            journal = journal,
            badges = badges,
            titles = titles,
            statistics = statistics,
            explorerLevel = explorerLevel,
            achievementsByKey = achievementsByKey,
            rewardHooks = rewardHooks,
        )
    }

    private suspend fun loadRewardHooks() = buildList {
        addAll(hookDao.getAllByHookType(AchievementRewardSchema.HookTypes.STORY_FRAGMENT))
        addAll(hookDao.getAllByHookType(AchievementRewardSchema.HookTypes.LEGENDARY_RELIC))
        addAll(hookDao.getAllByHookType(AchievementRewardSchema.HookTypes.TITLE))
        addAll(hookDao.getAllByHookType(AchievementRewardSchema.HookTypes.BADGE))
    }

    companion object {
        @Volatile
        private var instance: AchievementCollectionBookRepository? = null

        fun get(context: Context): AchievementCollectionBookRepository =
            instance ?: synchronized(this) {
                instance ?: AchievementCollectionBookRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
