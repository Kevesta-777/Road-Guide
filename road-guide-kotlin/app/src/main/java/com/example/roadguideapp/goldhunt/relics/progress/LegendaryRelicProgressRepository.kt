package com.example.roadguideapp.goldhunt.relics.progress

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDao
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicMapper
import com.example.roadguideapp.goldhunt.relics.RelicPieceCatalog
import com.example.roadguideapp.goldhunt.relics.RelicPieceDao
import com.example.roadguideapp.goldhunt.relics.RelicPieceMapper
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry
import com.example.roadguideapp.goldhunt.relics.sources.RelicPieceSourceGrantOperation
import com.example.roadguideapp.goldhunt.relics.chain.RelicHuntChainResolver
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardSchema
import com.example.roadguideapp.goldhunt.relics.sources.RelicPieceSourceSchema
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class LegendaryRelicProgressRepository private constructor(context: Context) {
    private val database = GoldHuntDatabase.get(context.applicationContext)
    private val relicDao: LegendaryRelicDao = database.legendaryRelicDao()
    private val pieceDao: RelicPieceDao = database.relicPieceDao()
    private val hookDao: RelicPieceGrantHookDao = database.relicPieceGrantHookDao()
    private val writeMutex = Mutex()

    suspend fun ensureSeeded(timestampMs: Long = System.currentTimeMillis()): Unit =
        writeMutex.withLock {
            ensureSeededUnlocked(timestampMs)
        }

    suspend fun loadProgress(): LegendaryRelicProgress = writeMutex.withLock {
        ensureSeededUnlocked(System.currentTimeMillis())
        buildProgressUnlocked()
    }

    suspend fun loadDiscoveredPieceIds(): Set<String> = writeMutex.withLock {
        pieceDao.getDiscoveredPieceIds().toSet()
    }

    suspend fun applyGrant(
        grant: RelicPieceSourceGrantOperation,
    ): LegendaryRelicProgressGrantResult = writeMutex.withLock {
        ensureSeededUnlocked(grant.grantedAtMs)
        applyGrantUnlocked(grant)
    }

    suspend fun applyGrants(
        grants: List<RelicPieceSourceGrantOperation>,
    ): List<LegendaryRelicProgressGrantResult> = writeMutex.withLock {
        if (grants.isEmpty()) return@withLock emptyList()
        ensureSeededUnlocked(grants.first().grantedAtMs)
        grants.map { applyGrantUnlocked(it) }
    }

    private suspend fun ensureSeededUnlocked(timestampMs: Long) {
        val existingRelicIds = relicDao.getAll().map { it.relicId }.toSet()
        val missingRelicDefinitions = LegendaryRelicRegistry.allDefinitions()
            .filter { it.relicId !in existingRelicIds }
        if (missingRelicDefinitions.isNotEmpty()) {
            relicDao.upsertAll(
                missingRelicDefinitions.map { definition ->
                    LegendaryRelicMapper.toEntity(
                        relic = LegendaryRelicCatalog.relicFromDefinition(definition),
                        updatedAtMs = timestampMs,
                    )
                },
            )
        }

        val existingPieceIds = pieceDao.getAll().map { it.pieceId }.toSet()
        val missingPieceDefinitions = LegendaryRelicRegistry.allPieceDefinitions()
            .filter { it.pieceId !in existingPieceIds }
        if (missingPieceDefinitions.isNotEmpty()) {
            pieceDao.upsertAll(
                missingPieceDefinitions.map { definition ->
                    RelicPieceMapper.toEntity(
                        piece = RelicPieceCatalog.pieceFromDefinition(definition),
                        sourceKey = definition.sourceKey,
                        updatedAtMs = timestampMs,
                    )
                },
            )
        }
    }

    private suspend fun applyGrantUnlocked(
        grant: RelicPieceSourceGrantOperation,
    ): LegendaryRelicProgressGrantResult {
        if (hookDao.findEventId(grant.eventId) != null) {
            return LegendaryRelicProgressGrantResult.skipped
        }
        if (hookDao.findHook(grant.hookKey, RelicPieceSourceSchema.HookTypes.RELIC_PIECE) != null) {
            return LegendaryRelicProgressGrantResult.skipped
        }

        val pieceEntity = pieceDao.get(grant.pieceId) ?: return LegendaryRelicProgressGrantResult.skipped
        if (pieceEntity.discovered) {
            return LegendaryRelicProgressGrantResult.skipped
        }
        if (!isChainProgressAllowedUnlocked(grant.relicId)) {
            return LegendaryRelicProgressGrantResult.skipped
        }

        val inserted = hookDao.insert(
            RelicPieceGrantHookEntity(
                eventId = grant.eventId,
                pieceId = grant.pieceId,
                relicId = grant.relicId,
                hookKey = grant.hookKey,
                grantedAtMs = grant.grantedAtMs,
            ),
        )
        if (inserted < 0L) {
            return LegendaryRelicProgressGrantResult.skipped
        }

        val discoveredPiece = RelicPieceMapper.toDomain(
            pieceEntity.copy(
                discovered = true,
                discoveryDateMs = grant.grantedAtMs,
                updatedAtMs = grant.grantedAtMs,
            ),
        ) ?: return LegendaryRelicProgressGrantResult.skipped
        pieceDao.upsert(
            RelicPieceMapper.toEntity(
                piece = discoveredPiece,
                sourceKey = pieceEntity.sourceKey,
                updatedAtMs = grant.grantedAtMs,
            ),
        )

        val relicEntity = relicDao.get(grant.relicId) ?: return LegendaryRelicProgressGrantResult.skipped
        val relic = LegendaryRelicMapper.toDomain(relicEntity) ?: return LegendaryRelicProgressGrantResult.skipped
        val wasCompleted = relic.completed
        val discoveredCount = pieceDao.countDiscoveredForRelic(grant.relicId)
        val updatedRelic = LegendaryRelicProgressUpdater.applyPieceDiscovery(
            relic = relic,
            discoveredCount = discoveredCount,
            timestampMs = grant.grantedAtMs,
        )
        relicDao.upsert(
            LegendaryRelicMapper.toEntity(
                relic = updatedRelic,
                updatedAtMs = grant.grantedAtMs,
            ),
        )

        val relicJustCompleted = updatedRelic.completed && !wasCompleted
        return LegendaryRelicProgressGrantResult(
            isNewGrant = true,
            pieceId = grant.pieceId,
            relicId = grant.relicId,
            relicCompleted = relicJustCompleted,
            piecesCollected = updatedRelic.piecesCollected,
            pieceCount = updatedRelic.pieceCount,
            creditsGranted = if (relicJustCompleted) updatedRelic.rewardCredits else 0,
            xpGranted = if (relicJustCompleted) updatedRelic.rewardXp else 0L,
        )
    }

    private suspend fun buildProgressUnlocked(): LegendaryRelicProgress {
        val relics = relicDao.getAll().mapNotNull { LegendaryRelicMapper.toDomain(it) }
        val pieces = pieceDao.getAll().mapNotNull { RelicPieceMapper.toDomain(it) }
        return LegendaryRelicProgress(
            catalogCount = LegendaryRelicRegistry.count(),
            completedCount = relics.count { it.completed },
            piecesCollected = relics.sumOf { it.piecesCollected },
            totalPieces = relics.sumOf { it.pieceCount },
            relics = relics,
            pieces = pieces,
        )
    }

    private suspend fun isChainProgressAllowedUnlocked(relicId: String): Boolean {
        val relics = relicDao.getAll().mapNotNull { LegendaryRelicMapper.toDomain(it) }
        val relicsById = relics.associateBy { it.relicId }
        val chainHooks = database.legendaryRelicRewardHookDao()
            .getAllByHookType(LegendaryRelicRewardSchema.HookTypes.CHAIN_UNLOCK)
        val context = RelicHuntChainResolver.buildContext(
            relicsById = relicsById,
            chainHooks = chainHooks,
        )
        return RelicHuntChainResolver.isProgressAllowed(relicId, context)
    }

    suspend fun loadRelics(): List<LegendaryRelic> = writeMutex.withLock {
        relicDao.getAll().mapNotNull { LegendaryRelicMapper.toDomain(it) }
    }

    companion object {
        @Volatile
        private var instance: LegendaryRelicProgressRepository? = null

        fun get(context: Context): LegendaryRelicProgressRepository =
            instance ?: synchronized(this) {
                instance ?: LegendaryRelicProgressRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
