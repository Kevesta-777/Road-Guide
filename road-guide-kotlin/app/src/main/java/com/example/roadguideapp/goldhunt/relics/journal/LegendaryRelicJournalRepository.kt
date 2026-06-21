package com.example.roadguideapp.goldhunt.relics.journal

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryRepository
import com.example.roadguideapp.goldhunt.relics.progress.LegendaryRelicProgressRepository
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry
import com.example.roadguideapp.goldhunt.relics.statistics.LegendaryRelicStatisticsManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class LegendaryRelicJournalRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val database = GoldHuntDatabase.get(appContext)
    private val progressRepository = LegendaryRelicProgressRepository.get(appContext)
    private val hiddenDiscoveryRepository = HiddenRelicDiscoveryRepository.get(appContext)
    private val statisticsManager = LegendaryRelicStatisticsManager.get(appContext)
    private val explorerProfileRepository = ExplorerProfileRepository.get(appContext)
    private val grantDao = database.legendaryRelicRewardGrantDao()
    private val writeMutex = Mutex()

    suspend fun load(): LegendaryRelicJournal = writeMutex.withLock {
        progressRepository.ensureSeeded()
        hiddenDiscoveryRepository.ensureSeeded()
        val progress = progressRepository.loadProgress()
        val hiddenProgress = hiddenDiscoveryRepository.loadProgress()
        val statistics = statisticsManager.load()
        val explorerLevel = explorerProfileRepository.loadProfile().explorerLevel
        val grants = grantDao.getAll()
        val rewardHooks = database.legendaryRelicRewardHookDao().getAll()
        val hiddenStoryHooks = database.hiddenRelicDiscoveryHookDao().getAll()
            .filter { it.hookType == com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoverySchema.HookTypes.STORY_FRAGMENT }
        LegendaryRelicJournalMapper.buildJournal(
            definitions = LegendaryRelicRegistry.allDefinitions(),
            relicsById = progress.relics.associateBy { it.relicId },
            piecesByRelicId = progress.pieces.groupBy { it.relicId },
            hiddenRecordsByRelicId = hiddenProgress.records.associateBy { it.relicId },
            grantedRelicIds = LegendaryRelicJournalMapper.grantedRelicIds(grants),
            grantsByRelicId = grants.associateBy { it.relicId },
            rewardHooksByRelicId = rewardHooks.groupBy { it.relicId },
            hiddenStoryHookKeysByRelicId = hiddenStoryHooks
                .groupBy { it.relicId }
                .mapValues { (_, hooks) -> hooks.map { it.hookKey }.toSet() },
            explorerLevel = explorerLevel,
            statistics = statistics,
        )
    }

    companion object {
        @Volatile
        private var instance: LegendaryRelicJournalRepository? = null

        fun get(context: Context): LegendaryRelicJournalRepository =
            instance ?: synchronized(this) {
                instance ?: LegendaryRelicJournalRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
