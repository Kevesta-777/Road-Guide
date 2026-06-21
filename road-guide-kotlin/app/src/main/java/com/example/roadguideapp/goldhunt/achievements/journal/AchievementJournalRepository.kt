package com.example.roadguideapp.goldhunt.achievements.journal

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.progress.AchievementProgressEngine
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class AchievementJournalRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val achievementDao = GoldHuntDatabase.get(appContext).achievementDao()
    private val explorerProfileRepository = ExplorerProfileRepository.get(appContext)
    private val progressEngine = AchievementProgressEngine.get(appContext)
    private val writeMutex = Mutex()

    suspend fun load(): AchievementJournal = writeMutex.withLock {
        progressEngine.ensureSeeded()
        val definitions = AchievementRegistry.allDefinitions()
        val entities = achievementDao.getAll()
        val achievementsByKey = AchievementJournalMapper.achievementMapFromEntities(
            definitions = definitions,
            entities = entities,
        )
        val explorerLevel = explorerProfileRepository.loadProfile().explorerLevel
        AchievementJournalMapper.buildJournal(
            definitions = definitions,
            achievementsByKey = achievementsByKey,
            explorerLevel = explorerLevel,
        )
    }

    companion object {
        @Volatile
        private var instance: AchievementJournalRepository? = null

        fun get(context: Context): AchievementJournalRepository =
            instance ?: synchronized(this) {
                instance ?: AchievementJournalRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
