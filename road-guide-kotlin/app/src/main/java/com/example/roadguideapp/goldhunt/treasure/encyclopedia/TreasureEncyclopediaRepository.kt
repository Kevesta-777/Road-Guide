package com.example.roadguideapp.goldhunt.treasure.encyclopedia

import android.content.Context
import com.example.roadguideapp.goldhunt.database.CollectedTreasureEntity
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinitionCatalog
import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinitionKey
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureTypeRarity
import com.example.roadguideapp.goldhunt.treasure.stats.TreasureRarityResolver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Offline treasure encyclopedia persistence and aggregation.
 */
internal class TreasureEncyclopediaRepository private constructor(context: Context) {
    private val database = GoldHuntDatabase.get(context.applicationContext)
    private val encyclopediaDao = database.treasureEncyclopediaDao()
    private val collectedDao = database.collectedTreasureDao()
    private val writeMutex = Mutex()

    suspend fun load(): TreasureEncyclopedia = writeMutex.withLock {
        ensureSeededUnlocked()
        reconcileFromCollectedTreasuresUnlocked()
        buildEncyclopedia()
    }

    suspend fun recordCollection(
        treasureId: String,
        type: TreasureType,
        creditsEarned: Int,
        collectedAtMs: Long,
    ): TreasureEncyclopediaEntry? = writeMutex.withLock {
        ensureSeededUnlocked()
        val catalogKey = TreasureDefinitionCatalog.templateForType(type)?.key ?: return@withLock null
        val rolledRarity = TreasureRarityResolver.resolveFromTreasureId(treasureId)
            ?: TreasureTypeRarity.rarityFor(type)
        val existing = encyclopediaDao.get(catalogKey.name)
        val firstAt = existing?.firstDiscoveredAtMs?.takeIf { it > 0L } ?: collectedAtMs
        val previousBest = existing?.bestRarityId?.let(TreasureRarity::fromId)
        val bestRarity = when {
            previousBest == null -> rolledRarity
            else -> TreasureRarity.maxOf(previousBest, rolledRarity)
        }
        val entity = TreasureEncyclopediaMapper.toEntity(
            catalogKey = catalogKey,
            status = TreasureEncyclopediaStatus.FOUND,
            firstDiscoveredAtMs = firstAt,
            bestRarity = bestRarity,
            totalCreditsEarned = (existing?.totalCreditsEarned ?: 0) + creditsEarned.coerceAtLeast(0),
            collectionCount = (existing?.collectionCount ?: 0) + 1,
            timestampMs = collectedAtMs,
        )
        encyclopediaDao.upsert(entity)
        val template = requireNotNull(TreasureDefinitionCatalog.templateForKey(catalogKey))
        TreasureEncyclopediaMapper.toDomain(template, entity)
    }

    private suspend fun ensureSeededUnlocked() {
        val existingKeys = encyclopediaDao.all().map { it.catalogKey }.toSet()
        val now = System.currentTimeMillis()
        val seeds = TreasureEncyclopediaCatalog.displayTemplates
            .filter { it.key.name !in existingKeys }
            .map { template ->
                TreasureEncyclopediaMapper.toEntity(
                    catalogKey = template.key,
                    status = TreasureEncyclopediaCatalog.initialStatus(template),
                    firstDiscoveredAtMs = null,
                    bestRarity = null,
                    totalCreditsEarned = 0,
                    collectionCount = 0,
                    timestampMs = now,
                )
            }
        if (seeds.isNotEmpty()) {
            encyclopediaDao.upsertAll(seeds)
        }
    }

    private suspend fun reconcileFromCollectedTreasuresUnlocked() {
        val rows = collectedDao.allOrdered()
        if (rows.isEmpty()) return
        val now = System.currentTimeMillis()
        for ((catalogKey, aggregate) in aggregateByCatalogKey(rows)) {
            encyclopediaDao.upsert(
                TreasureEncyclopediaMapper.toEntity(
                    catalogKey = catalogKey,
                    status = TreasureEncyclopediaStatus.FOUND,
                    firstDiscoveredAtMs = aggregate.firstDiscoveredAtMs,
                    bestRarity = aggregate.bestRarity,
                    totalCreditsEarned = aggregate.totalCreditsEarned,
                    collectionCount = aggregate.collectionCount,
                    timestampMs = now,
                ),
            )
        }
    }

    private suspend fun buildEncyclopedia(): TreasureEncyclopedia {
        val entities = encyclopediaDao.all().associateBy { it.catalogKey }
        val entries = TreasureEncyclopediaCatalog.displayTemplates.map { template ->
            TreasureEncyclopediaMapper.toDomain(template, entities[template.key.name])
        }
        return TreasureEncyclopedia(
            entries = entries,
            progress = TreasureEncyclopediaMapper.buildProgress(entries),
        )
    }

    private data class CatalogAggregate(
        val firstDiscoveredAtMs: Long,
        val bestRarity: TreasureRarity,
        val totalCreditsEarned: Int,
        val collectionCount: Int,
    )

    private fun aggregateByCatalogKey(
        rows: List<CollectedTreasureEntity>,
    ): Map<TreasureDefinitionKey, CatalogAggregate> {
        val grouped = linkedMapOf<TreasureDefinitionKey, MutableList<CollectedTreasureEntity>>()
        for (row in rows) {
            val type = TreasureType.fromId(row.type) ?: continue
            val key = TreasureDefinitionCatalog.templateForType(type)?.key ?: continue
            grouped.getOrPut(key) { mutableListOf() }.add(row)
        }
        return grouped.mapValues { (_, items) ->
            val sorted = items.sortedBy { it.collectedAt }
            val best = items.map { row ->
                val treasureType = requireNotNull(TreasureType.fromId(row.type))
                TreasureRarityResolver.resolveFromTreasureId(row.treasureId)
                    ?: TreasureTypeRarity.rarityFor(treasureType)
            }.maxBy { it.tier }
            CatalogAggregate(
                firstDiscoveredAtMs = sorted.first().collectedAt,
                bestRarity = best,
                totalCreditsEarned = items.sumOf { it.creditAmount.coerceAtLeast(0) },
                collectionCount = items.size,
            )
        }
    }

    companion object {
        @Volatile
        private var instance: TreasureEncyclopediaRepository? = null

        fun get(context: Context): TreasureEncyclopediaRepository =
            instance ?: synchronized(this) {
                instance ?: TreasureEncyclopediaRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
