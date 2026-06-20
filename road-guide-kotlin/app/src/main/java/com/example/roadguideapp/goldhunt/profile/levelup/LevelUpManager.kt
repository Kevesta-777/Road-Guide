package com.example.roadguideapp.goldhunt.profile.levelup

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileSchema
import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Detects explorer level increases after XP awards and persists [LevelUpEvent] history.
 * Does not grant rewards or show UI.
 */
internal class LevelUpManager private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).levelUpEventDao()
    private val writeMutex = Mutex()

    /**
     * Compares [previousLevel] with [newLevel] and records one event per newly reached level.
     * Idempotent per [LevelUpEventIds.forLevel] — replays do not duplicate history.
     */
    suspend fun checkLevelChange(
        previousLevel: Int,
        newLevel: Int,
        timestampMs: Long = System.currentTimeMillis(),
        lifetimeXp: Long = 0L,
        extensionJson: String = LevelUpEventSchema.EMPTY_EXTENSIONS_JSON,
    ): LevelUpCheckResult = writeMutex.withLock {
        val oldLevel = previousLevel.coerceAtLeast(ExplorerProfileSchema.DEFAULT_EXPLORER_LEVEL)
        val resolvedNewLevel = newLevel.coerceAtLeast(oldLevel)
        if (resolvedNewLevel <= oldLevel) {
            return@withLock LevelUpCheckResult(
                previousLevel = oldLevel,
                newLevel = resolvedNewLevel,
                leveledUp = false,
                events = emptyList(),
            )
        }

        val persisted = ArrayList<LevelUpEvent>()
        for ((fromLevel, toLevel) in levelUpSteps(oldLevel, resolvedNewLevel)) {
            val event = LevelUpEvent(
                id = LevelUpEventIds.forLevel(toLevel),
                previousLevel = fromLevel,
                newLevel = toLevel,
                timestampMs = timestampMs,
                lifetimeXpAtLevelUp = lifetimeXp,
                extensionJson = extensionJson,
            )
            val rowId = dao.insert(LevelUpEventMapper.toEntity(event))
            if (rowId >= 0L) {
                persisted += event
            }
        }

        LevelUpCheckResult(
            previousLevel = oldLevel,
            newLevel = resolvedNewLevel,
            leveledUp = persisted.isNotEmpty(),
            events = persisted,
        )
    }

    /**
     * Convenience: derives [newLevel] from [lifetimeXp] then records any increases.
     */
    suspend fun checkLevelChangeAfterXp(
        previousLevel: Int,
        lifetimeXp: Long,
        timestampMs: Long = System.currentTimeMillis(),
        extensionJson: String = LevelUpEventSchema.EMPTY_EXTENSIONS_JSON,
    ): LevelUpCheckResult {
        val newLevel = ExplorerLevelCalculator.calculateLevel(lifetimeXp)
        return checkLevelChange(
            previousLevel = previousLevel,
            newLevel = newLevel,
            timestampMs = timestampMs,
            lifetimeXp = lifetimeXp,
            extensionJson = extensionJson,
        )
    }

    suspend fun recentEvents(limit: Int = 20): List<LevelUpEvent> =
        writeMutex.withLock {
            dao.recent(limit).map { LevelUpEventMapper.toDomain(it) }
        }

    companion object {
        /** Expands a multi-level jump into single-step transitions (e.g. 1→3 → [(1,2), (2,3)]). */
        fun levelUpSteps(previousLevel: Int, newLevel: Int): List<Pair<Int, Int>> {
            if (newLevel <= previousLevel) return emptyList()
            val steps = ArrayList<Pair<Int, Int>>(newLevel - previousLevel)
            for (target in (previousLevel + 1)..newLevel) {
                steps += (target - 1) to target
            }
            return steps
        }

        @Volatile
        private var instance: LevelUpManager? = null

        fun get(context: Context): LevelUpManager =
            instance ?: synchronized(this) {
                instance ?: LevelUpManager(context.applicationContext).also { instance = it }
            }
    }
}
