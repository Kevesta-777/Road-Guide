package com.example.roadguideapp.goldhunt.radar

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class RadarTypeCooldownStateRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).radarTypeCooldownStateDao()
    private val writeMutex = Mutex()

    suspend fun ensureAllTypes(timestampMs: Long = System.currentTimeMillis()): Map<RadarType, RadarTypeCooldownState> =
        writeMutex.withLock {
            val existing = dao.getAll().associateBy { it.radarTypeId }
            val merged = RadarType.ALL_ORDERED.associateWith { type ->
                val entity = existing[type.id]
                if (entity != null) {
                    RadarTypeCooldownStateMapper.toDomain(entity)
                } else {
                    val created = RadarTypeCooldownState.default(type, timestampMs)
                    dao.upsert(RadarTypeCooldownStateMapper.toEntity(created))
                    created
                }
            }
            merged
        }

    suspend fun loadAll(): Map<RadarType, RadarTypeCooldownState> =
        writeMutex.withLock {
            val entities = dao.getAll()
            if (entities.isEmpty()) {
                return@withLock ensureAllTypesUnlocked()
            }
            mergeWithDefaults(entities)
        }

    suspend fun save(state: RadarTypeCooldownState): RadarTypeCooldownState =
        writeMutex.withLock {
            val entity = RadarTypeCooldownStateMapper.toEntity(state)
            dao.upsert(entity)
            RadarTypeCooldownStateMapper.toDomain(entity)
        }

    suspend fun saveAll(states: Collection<RadarTypeCooldownState>): Map<RadarType, RadarTypeCooldownState> =
        writeMutex.withLock {
            if (states.isEmpty()) return@withLock ensureAllTypesUnlocked()
            val entities = states.map { RadarTypeCooldownStateMapper.toEntity(it) }
            dao.upsertAll(entities)
            mergeWithDefaults(dao.getAll())
        }

    suspend fun resetAll(timestampMs: Long = System.currentTimeMillis()): Map<RadarType, RadarTypeCooldownState> =
        writeMutex.withLock {
            dao.deleteAll()
            val defaults = RadarTypeCooldownState.defaults(timestampMs)
            dao.upsertAll(defaults.values.map { RadarTypeCooldownStateMapper.toEntity(it) })
            defaults
        }

    private suspend fun ensureAllTypesUnlocked(): Map<RadarType, RadarTypeCooldownState> {
        val defaults = RadarTypeCooldownState.defaults()
        dao.upsertAll(defaults.values.map { RadarTypeCooldownStateMapper.toEntity(it) })
        return defaults
    }

    private fun mergeWithDefaults(
        entities: List<RadarTypeCooldownStateEntity>,
    ): Map<RadarType, RadarTypeCooldownState> {
        val byId = entities.associate { it.radarTypeId to RadarTypeCooldownStateMapper.toDomain(it) }
        return RadarType.ALL_ORDERED.associateWith { type ->
            byId[type.id] ?: RadarTypeCooldownState.default(type)
        }
    }

    companion object {
        @Volatile
        private var instance: RadarTypeCooldownStateRepository? = null

        fun get(context: Context): RadarTypeCooldownStateRepository =
            instance ?: synchronized(this) {
                instance ?: RadarTypeCooldownStateRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
