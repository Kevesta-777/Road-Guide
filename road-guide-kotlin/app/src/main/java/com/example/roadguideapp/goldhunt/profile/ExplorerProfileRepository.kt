package com.example.roadguideapp.goldhunt.profile

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Offline persistence for the Gold Hunt explorer profile.
 * Thread-safe writes; does not compute XP, levels, or rewards.
 */
internal class ExplorerProfileRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val dao = GoldHuntDatabase.get(appContext).explorerProfileDao()
    private val writeMutex = Mutex()

    suspend fun ensureProfile(timestampMs: Long = System.currentTimeMillis()): ExplorerProfile =
        writeMutex.withLock {
            val existing = dao.get()
            if (existing != null) {
                return@withLock ExplorerProfileMapper.toDomain(existing)
            }
            val created = ExplorerProfileEntity(
                createdAtMs = timestampMs,
                updatedAtMs = timestampMs,
            )
            dao.upsert(created)
            ExplorerProfileMapper.toDomain(created)
        }

    suspend fun loadProfile(): ExplorerProfile =
        writeMutex.withLock {
            val entity = dao.get() ?: return@withLock ensureProfileUnlocked()
            ExplorerProfileMapper.toDomain(entity)
        }

    suspend fun saveProfile(
        profile: ExplorerProfile,
        timestampMs: Long = System.currentTimeMillis(),
    ): ExplorerProfile = writeMutex.withLock {
        val existing = dao.get()
        val entity = ExplorerProfileMapper.toEntity(
            profile = profile.copy(
                createdAtMs = existing?.createdAtMs?.takeIf { it > 0L } ?: profile.createdAtMs,
                updatedAtMs = timestampMs,
            ),
        )
        dao.upsert(entity)
        ExplorerProfileMapper.toDomain(entity)
    }

    /**
     * Atomically read-modify-write the singleton profile row.
     * The [transform] receives the current domain profile (or defaults if missing).
     */
    suspend fun updateProfile(
        timestampMs: Long = System.currentTimeMillis(),
        transform: (ExplorerProfile) -> ExplorerProfile,
    ): ExplorerProfile = writeMutex.withLock {
        val current = dao.get()?.let { ExplorerProfileMapper.toDomain(it) }
            ?: ExplorerProfile.default(timestampMs)
        val next = transform(current).copy(
            createdAtMs = current.createdAtMs.takeIf { it > 0L } ?: timestampMs,
            updatedAtMs = timestampMs,
        )
        val entity = ExplorerProfileMapper.toEntity(next)
        dao.upsert(entity)
        ExplorerProfileMapper.toDomain(entity)
    }

    suspend fun addExploredDistance(
        distanceM: Double,
        timestampMs: Long = System.currentTimeMillis(),
    ): ExplorerProfile {
        if (distanceM <= 0.0) return loadProfile()
        return updateProfile(timestampMs) { profile ->
            profile.copy(
                totalDistanceExploredM = profile.totalDistanceExploredM + distanceM,
            )
        }
    }

    suspend fun resetProfile(timestampMs: Long = System.currentTimeMillis()): ExplorerProfile =
        writeMutex.withLock {
            val fresh = ExplorerProfile.default(timestampMs)
            val entity = ExplorerProfileMapper.toEntity(fresh)
            dao.upsert(entity)
            ExplorerProfileMapper.toDomain(entity)
        }

    private suspend fun ensureProfileUnlocked(): ExplorerProfile {
        val created = ExplorerProfileEntity(
            createdAtMs = System.currentTimeMillis(),
            updatedAtMs = System.currentTimeMillis(),
        )
        dao.upsert(created)
        return ExplorerProfileMapper.toDomain(created)
    }

    companion object {
        @Volatile
        private var instance: ExplorerProfileRepository? = null

        fun get(context: Context): ExplorerProfileRepository =
            instance ?: synchronized(this) {
                instance ?: ExplorerProfileRepository(context.applicationContext).also { instance = it }
            }
    }
}
