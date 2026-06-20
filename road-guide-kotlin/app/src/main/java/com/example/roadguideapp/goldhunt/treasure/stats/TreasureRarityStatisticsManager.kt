package com.example.roadguideapp.goldhunt.treasure.stats

import android.content.Context
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileMapper
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

/**
 * Persists lifetime treasure rarity counts on the explorer profile singleton.
 */
internal class TreasureRarityStatisticsManager private constructor(context: Context) {
    private val repository = ExplorerProfileRepository.get(context.applicationContext)

    suspend fun recordCollection(
        spec: TreasureSpec,
        timestampMs: Long = System.currentTimeMillis(),
    ): TreasureRarityStatistics = recordFound(
        rarity = TreasureRarityResolver.resolve(spec),
        timestampMs = timestampMs,
    )

    suspend fun recordFound(
        rarity: TreasureRarity,
        timestampMs: Long = System.currentTimeMillis(),
    ): TreasureRarityStatistics {
        val updated = repository.updateProfile(timestampMs) { profile ->
            profile.copy(
                treasureRarityStats = profile.treasureRarityStats.withIncrement(rarity),
            )
        }
        return updated.treasureRarityStats
    }

    suspend fun load(): TreasureRarityStatistics =
        repository.loadProfile().treasureRarityStats

    companion object {
        @Volatile
        private var instance: TreasureRarityStatisticsManager? = null

        fun get(context: Context): TreasureRarityStatisticsManager =
            instance ?: synchronized(this) {
                instance ?: TreasureRarityStatisticsManager(context.applicationContext)
                    .also { instance = it }
            }

        fun fromEntity(entity: ExplorerProfileEntity): TreasureRarityStatistics =
            ExplorerProfileMapper.treasureRarityStatsFromEntity(entity)

        fun applyToEntity(
            entity: ExplorerProfileEntity,
            stats: TreasureRarityStatistics,
        ): ExplorerProfileEntity = entity.copy(
            commonTreasuresFound = stats.commonFound,
            uncommonTreasuresFound = stats.uncommonFound,
            rareTreasuresFound = stats.rareFound,
            epicTreasuresFound = stats.epicFound,
            legendaryTreasuresFound = stats.legendaryFound,
            mythicTreasuresFound = stats.mythicFound,
        )
    }
}
