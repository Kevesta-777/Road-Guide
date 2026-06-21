package com.example.roadguideapp.goldhunt.radar

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Offline persistence for the singleton [RadarProfile] row.
 * Scan rules and achievement grants are applied in [RadarProfileManager].
 */
internal class RadarProfileRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val dao = GoldHuntDatabase.get(appContext).radarProfileDao()
    private val writeMutex = Mutex()

    suspend fun ensureProfile(timestampMs: Long = System.currentTimeMillis()): RadarProfile =
        writeMutex.withLock {
            val existing = dao.get()
            if (existing != null) {
                return@withLock RadarProfileMapper.toDomain(existing)
            }
            val created = RadarProfileEntity(
                createdAtMs = timestampMs,
                updatedAtMs = timestampMs,
            )
            dao.upsert(created)
            RadarProfileMapper.toDomain(created)
        }

    suspend fun loadProfile(): RadarProfile =
        writeMutex.withLock {
            val entity = dao.get() ?: return@withLock ensureProfileUnlocked()
            RadarProfileMapper.toDomain(entity)
        }

    suspend fun saveProfile(
        profile: RadarProfile,
        timestampMs: Long = System.currentTimeMillis(),
    ): RadarProfile = writeMutex.withLock {
        val existing = dao.get()
        val entity = RadarProfileMapper.toEntity(
            profile = profile.copy(
                createdAtMs = existing?.createdAtMs?.takeIf { it > 0L } ?: profile.createdAtMs,
                updatedAtMs = timestampMs,
            ),
        )
        dao.upsert(entity)
        RadarProfileMapper.toDomain(entity)
    }

    suspend fun updateProfile(
        timestampMs: Long = System.currentTimeMillis(),
        transform: (RadarProfile) -> RadarProfile,
    ): RadarProfile = writeMutex.withLock {
        val current = dao.get()?.let { RadarProfileMapper.toDomain(it) }
            ?: RadarProfile.default(timestampMs)
        val next = transform(current).copy(
            createdAtMs = current.createdAtMs.takeIf { it > 0L } ?: timestampMs,
            updatedAtMs = timestampMs,
        )
        val entity = RadarProfileMapper.toEntity(next)
        dao.upsert(entity)
        RadarProfileMapper.toDomain(entity)
    }

    suspend fun resetProfile(timestampMs: Long = System.currentTimeMillis()): RadarProfile =
        writeMutex.withLock {
            val fresh = RadarProfile.default(timestampMs)
            val entity = RadarProfileMapper.toEntity(fresh)
            dao.upsert(entity)
            RadarProfileMapper.toDomain(entity)
        }

    private suspend fun ensureProfileUnlocked(): RadarProfile {
        val now = System.currentTimeMillis()
        val created = RadarProfileEntity(
            createdAtMs = now,
            updatedAtMs = now,
        )
        dao.upsert(created)
        return RadarProfileMapper.toDomain(created)
    }

    companion object {
        @Volatile
        private var instance: RadarProfileRepository? = null

        fun get(context: Context): RadarProfileRepository =
            instance ?: synchronized(this) {
                instance ?: RadarProfileRepository(context.applicationContext).also { instance = it }
            }
    }
}
