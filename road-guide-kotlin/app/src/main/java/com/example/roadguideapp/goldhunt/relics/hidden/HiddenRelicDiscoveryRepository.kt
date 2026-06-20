package com.example.roadguideapp.goldhunt.relics.hidden

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDao
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicMapper
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class HiddenRelicDiscoveryRepository private constructor(context: Context) {
    private val database = GoldHuntDatabase.get(context.applicationContext)
    private val discoveryDao = database.hiddenRelicDiscoveryDao()
    private val hookDao = database.hiddenRelicDiscoveryHookDao()
    private val relicDao: LegendaryRelicDao = database.legendaryRelicDao()
    private val writeMutex = Mutex()

    suspend fun ensureSeeded(timestampMs: Long = System.currentTimeMillis()): Unit =
        writeMutex.withLock {
            ensureSeededUnlocked(timestampMs)
        }

    suspend fun loadProgress(): HiddenRelicDiscoveryProgress = writeMutex.withLock {
        ensureSeededUnlocked(System.currentTimeMillis())
        buildProgressUnlocked()
    }

    suspend fun get(relicId: String): HiddenRelicDiscoveryRecord? = writeMutex.withLock {
        discoveryDao.get(relicId)?.let { HiddenRelicDiscoveryMapper.toDomain(it) }
    }

    suspend fun revealOnFirstPiece(
        relicId: String,
        pieceId: String,
        timestampMs: Long,
    ): HiddenRelicDiscoveryRevealResult = writeMutex.withLock {
        ensureSeededUnlocked(timestampMs)
        revealOnFirstPieceUnlocked(relicId, pieceId, timestampMs)
    }

    private suspend fun ensureSeededUnlocked(timestampMs: Long) {
        val existingIds = discoveryDao.getAll().map { it.relicId }.toSet()
        val missing = LegendaryRelicRegistry.allDefinitions()
            .mapNotNull { definition ->
                val visibility = RelicVisibilityResolver.forDefinition(definition)
                if (visibility != RelicVisibility.HIDDEN) return@mapNotNull null
                if (definition.relicId in existingIds) return@mapNotNull null
                HiddenRelicDiscoveryRecord(
                    relicId = definition.relicId,
                    visibility = visibility,
                    revealedAtMs = null,
                    firstPieceId = null,
                    storyFragmentKey = HiddenRelicDiscoverySchema.storyFragmentKeyFor(
                        relicId = definition.relicId,
                        category = definition.category,
                    ),
                    storyFragmentRecorded = false,
                    achievementKey = HiddenRelicDiscoverySchema.achievementKeyFor(definition.category),
                    achievementRecorded = false,
                )
            }
        if (missing.isNotEmpty()) {
            discoveryDao.upsertAll(
                missing.map { record ->
                    HiddenRelicDiscoveryMapper.toEntity(record, updatedAtMs = timestampMs)
                },
            )
        }
    }

    private suspend fun revealOnFirstPieceUnlocked(
        relicId: String,
        pieceId: String,
        timestampMs: Long,
    ): HiddenRelicDiscoveryRevealResult {
        val entity = discoveryDao.get(relicId) ?: return HiddenRelicDiscoveryRevealResult.skipped
        val record = HiddenRelicDiscoveryMapper.toDomain(entity)
            ?: return HiddenRelicDiscoveryRevealResult.skipped
        if (!record.isHidden) return HiddenRelicDiscoveryRevealResult.skipped
        if (record.isRevealed) return HiddenRelicDiscoveryRevealResult.skipped

        val revealInserted = hookDao.insert(
            HiddenRelicDiscoveryHookEntity(
                eventId = HiddenRelicDiscoverySchema.revealEventId(relicId),
                relicId = relicId,
                hookType = HiddenRelicDiscoverySchema.HookTypes.ACHIEVEMENT,
                hookKey = HiddenRelicDiscoverySchema.revealEventId(relicId),
                grantedAtMs = timestampMs,
            ),
        )
        if (revealInserted < 0L) {
            return HiddenRelicDiscoveryRevealResult.skipped
        }

        val storyRecorded = recordStoryHook(record, timestampMs)
        val achievementRecorded = recordAchievementHook(record, timestampMs)
        discoveryDao.upsert(
            HiddenRelicDiscoveryMapper.toEntity(
                record = record.copy(
                    revealedAtMs = timestampMs,
                    firstPieceId = pieceId,
                    storyFragmentRecorded = storyRecorded || record.storyFragmentRecorded,
                    achievementRecorded = achievementRecorded || record.achievementRecorded,
                ),
                updatedAtMs = timestampMs,
            ),
        )
        return HiddenRelicDiscoveryRevealResult(
            isNewReveal = true,
            relicId = relicId,
            storyFragmentKey = record.storyFragmentKey,
            achievementKey = record.achievementKey,
            storyFragmentRecorded = storyRecorded,
            achievementRecorded = achievementRecorded,
        )
    }

    private suspend fun recordStoryHook(
        record: HiddenRelicDiscoveryRecord,
        timestampMs: Long,
    ): Boolean {
        val storyKey = record.storyFragmentKey ?: return false
        val inserted = hookDao.insert(
            HiddenRelicDiscoveryHookEntity(
                eventId = HiddenRelicDiscoverySchema.storyFragmentEventId(record.relicId),
                relicId = record.relicId,
                hookType = HiddenRelicDiscoverySchema.HookTypes.STORY_FRAGMENT,
                hookKey = storyKey,
                grantedAtMs = timestampMs,
            ),
        )
        return inserted >= 0L
    }

    private suspend fun recordAchievementHook(
        record: HiddenRelicDiscoveryRecord,
        timestampMs: Long,
    ): Boolean {
        val achievementKey = record.achievementKey ?: return false
        val inserted = hookDao.insert(
            HiddenRelicDiscoveryHookEntity(
                eventId = HiddenRelicDiscoverySchema.achievementEventId(record.relicId),
                relicId = record.relicId,
                hookType = HiddenRelicDiscoverySchema.HookTypes.ACHIEVEMENT,
                hookKey = achievementKey,
                grantedAtMs = timestampMs,
            ),
        )
        return inserted >= 0L
    }

    private suspend fun buildProgressUnlocked(): HiddenRelicDiscoveryProgress {
        val records = discoveryDao.getAll().mapNotNull { HiddenRelicDiscoveryMapper.toDomain(it) }
        val relicsById = relicDao.getAll()
            .mapNotNull { LegendaryRelicMapper.toDomain(it) }
            .associateBy { it.relicId }
        val presentations = records.mapNotNull { record ->
            val relic = relicsById[record.relicId]
                ?: LegendaryRelicCatalog.findById(record.relicId)?.let { definition ->
                    LegendaryRelicCatalog.relicFromDefinition(definition)
                }
                ?: return@mapNotNull null
            HiddenRelicPresentationMapper.present(
                relic = relic,
                visibility = record.visibility,
                revealedAtMs = record.revealedAtMs,
            )
        }
        val hiddenCatalogCount = records.count { it.isHidden }
        val hiddenRevealedCount = records.count { it.isHidden && it.isRevealed }
        return HiddenRelicDiscoveryProgress(
            hiddenCatalogCount = hiddenCatalogCount,
            hiddenRevealedCount = hiddenRevealedCount,
            hiddenUndiscoveredCount = (hiddenCatalogCount - hiddenRevealedCount).coerceAtLeast(0),
            records = records,
            presentations = presentations,
        )
    }

    companion object {
        @Volatile
        private var instance: HiddenRelicDiscoveryRepository? = null

        fun get(context: Context): HiddenRelicDiscoveryRepository =
            instance ?: synchronized(this) {
                instance ?: HiddenRelicDiscoveryRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
