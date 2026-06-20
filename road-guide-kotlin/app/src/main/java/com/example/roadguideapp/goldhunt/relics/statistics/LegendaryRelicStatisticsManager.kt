package com.example.roadguideapp.goldhunt.relics.statistics

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicMapper
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryRevealResult
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryRepository
import com.example.roadguideapp.goldhunt.relics.progress.LegendaryRelicProgressRepository
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantResult

/**
 * Loads, backfills, and incrementally updates lifetime legendary relic statistics
 * on the explorer profile.
 */
internal class LegendaryRelicStatisticsManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val database = GoldHuntDatabase.get(appContext)
    private val explorerProfileRepository = ExplorerProfileRepository.get(appContext)
    private val relicDao = database.legendaryRelicDao()
    private val grantDao = database.legendaryRelicRewardGrantDao()
    private val progressRepository = LegendaryRelicProgressRepository.get(appContext)
    private val hiddenDiscoveryRepository = HiddenRelicDiscoveryRepository.get(appContext)

    suspend fun load(): LegendaryRelicStatistics {
        progressRepository.ensureSeeded()
        hiddenDiscoveryRepository.ensureSeeded()
        val catalogSize = LegendaryRelicRegistry.count()
        val totalPieces = LegendaryRelicRegistry.pieceCount()
        val profile = explorerProfileRepository.ensureProfile()
        val persisted = profile.legendaryRelicStatistics.withCatalogSize(catalogSize, totalPieces)
        val computed = backfill(catalogSize, totalPieces)
        if (needsBackfill(persisted, computed)) {
            explorerProfileRepository.updateProfile { current ->
                current.copy(legendaryRelicStatistics = computed)
            }
            return computed
        }
        return persisted
    }

    suspend fun recordGrant(
        relic: LegendaryRelic,
        grantResult: LegendaryRelicRewardGrantResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): LegendaryRelicStatistics {
        if (!grantResult.isNewGrant) {
            return load()
        }
        val catalogSize = LegendaryRelicRegistry.count()
        val totalPieces = LegendaryRelicRegistry.pieceCount()
        val updated = explorerProfileRepository.updateProfile(timestampMs) { profile ->
            profile.copy(
                legendaryRelicStatistics = profile.legendaryRelicStatistics
                    .withCatalogSize(catalogSize, totalPieces)
                    .withGrant(relic, grantResult),
            )
        }
        return updated.legendaryRelicStatistics.withCatalogSize(catalogSize, totalPieces)
    }

    suspend fun recordHiddenReveal(
        revealResult: HiddenRelicDiscoveryRevealResult,
        timestampMs: Long = System.currentTimeMillis(),
    ): LegendaryRelicStatistics {
        if (!revealResult.isNewReveal) {
            return load()
        }
        val catalogSize = LegendaryRelicRegistry.count()
        val totalPieces = LegendaryRelicRegistry.pieceCount()
        val updated = explorerProfileRepository.updateProfile(timestampMs) { profile ->
            profile.copy(
                legendaryRelicStatistics = profile.legendaryRelicStatistics
                    .withCatalogSize(catalogSize, totalPieces)
                    .withHiddenReveal(revealResult),
            )
        }
        return updated.legendaryRelicStatistics.withCatalogSize(catalogSize, totalPieces)
    }

    suspend fun reconcile(timestampMs: Long = System.currentTimeMillis()): LegendaryRelicStatistics {
        progressRepository.ensureSeeded(timestampMs)
        hiddenDiscoveryRepository.ensureSeeded(timestampMs)
        val catalogSize = LegendaryRelicRegistry.count()
        val totalPieces = LegendaryRelicRegistry.pieceCount()
        val stats = backfill(catalogSize, totalPieces)
        explorerProfileRepository.updateProfile(timestampMs) { profile ->
            profile.copy(legendaryRelicStatistics = stats)
        }
        return stats
    }

    private suspend fun backfill(
        catalogSize: Int,
        totalPieces: Int,
    ): LegendaryRelicStatistics {
        val relics = relicDao.getAll().mapNotNull { LegendaryRelicMapper.toDomain(it) }
        val grants = grantDao.getAll()
        val hiddenProgress = hiddenDiscoveryRepository.loadProgress()
        return LegendaryRelicStatisticsCalculator.compute(
            relics = relics,
            grants = grants,
            catalogSize = catalogSize,
            totalPieces = totalPieces,
            hiddenRelicsDiscovered = hiddenProgress.hiddenRevealedCount,
            hiddenRelicsCatalogCount = hiddenProgress.hiddenCatalogCount,
        )
    }

    private fun needsBackfill(
        persisted: LegendaryRelicStatistics,
        computed: LegendaryRelicStatistics,
    ): Boolean =
        persisted.completedCount != computed.completedCount ||
            persisted.piecesCollected != computed.piecesCollected ||
            persisted.totalPieces != computed.totalPieces ||
            persisted.catalogCount != computed.catalogCount ||
            persisted.hiddenRelicsDiscovered != computed.hiddenRelicsDiscovered ||
            persisted.hiddenRelicsCatalogCount != computed.hiddenRelicsCatalogCount ||
            persisted.epicCompletedCount != computed.epicCompletedCount ||
            persisted.legendaryCompletedCount != computed.legendaryCompletedCount ||
            persisted.creditsEarned != computed.creditsEarned ||
            persisted.xpEarned != computed.xpEarned

    companion object {
        @Volatile
        private var instance: LegendaryRelicStatisticsManager? = null

        fun get(context: Context): LegendaryRelicStatisticsManager =
            instance ?: synchronized(this) {
                instance ?: LegendaryRelicStatisticsManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
