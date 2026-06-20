package com.example.roadguideapp.goldhunt.relics.showcase

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.AchievementMapper
import com.example.roadguideapp.goldhunt.achievements.registry.AchievementRegistry
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalRepository
import com.example.roadguideapp.goldhunt.relics.progress.LegendaryRelicProgressRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class LegendaryRelicShowcaseRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val database = GoldHuntDatabase.get(appContext)
    private val journalRepository = LegendaryRelicJournalRepository.get(appContext)
    private val progressRepository = LegendaryRelicProgressRepository.get(appContext)
    private val explorerProfileRepository = ExplorerProfileRepository.get(appContext)
    private val writeMutex = Mutex()

    suspend fun load(): LegendaryRelicShowcase = writeMutex.withLock {
        val journal = journalRepository.load()
        val progress = progressRepository.loadProgress()
        val profile = explorerProfileRepository.loadProfile()
        val relicsById = progress.relics.associateBy { it.relicId }
        val grants = database.legendaryRelicRewardGrantDao().getAll()
        val badgeEntities = database.achievementBadgeDao().all()
        val titleEntities = database.achievementTitleDao().all()
        val pieceEntities = database.relicPieceDao().getAll()
        val achievementEntities = database.achievementDao().getAll()
        val definitions = AchievementRegistry.allDefinitions()
        val achievementsByKey = achievementEntities.mapNotNull { entity ->
            AchievementMapper.toDomain(entity)?.let { achievement ->
                achievement.achievementId to achievement
            }
        }.toMap()
        LegendaryRelicShowcaseMapper.buildShowcase(
            journal = journal,
            relicsById = relicsById,
            grantsByRelicId = grants.associateBy { it.relicId },
            badgeEntitiesByKey = badgeEntities.associateBy { it.badgeKey },
            titleEntitiesByKey = titleEntities.associateBy { it.titleKey },
            achievementsByKey = achievementsByKey,
            achievementDefinitions = definitions,
            pieceEntitiesByRelicId = pieceEntities.groupBy { it.relicId },
            activeTitleKey = profile.activeAchievementTitleKey,
        )
    }

    companion object {
        @Volatile
        private var instance: LegendaryRelicShowcaseRepository? = null

        fun get(context: Context): LegendaryRelicShowcaseRepository =
            instance ?: synchronized(this) {
                instance ?: LegendaryRelicShowcaseRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
