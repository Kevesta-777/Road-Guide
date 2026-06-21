package com.example.roadguideapp.goldhunt.relics.rewards

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class LegendaryRelicRewardGrantRecord(
    val entity: LegendaryRelicRewardGrantEntity,
    val isNew: Boolean,
)

internal class LegendaryRelicRewardGrantRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).legendaryRelicRewardGrantDao()
    private val writeMutex = Mutex()

    suspend fun isGranted(relicId: String): Boolean = writeMutex.withLock {
        dao.get(relicId) != null
    }

    suspend fun recordGrant(
        entity: LegendaryRelicRewardGrantEntity,
    ): LegendaryRelicRewardGrantRecord = writeMutex.withLock {
        if (dao.get(entity.relicId) != null) {
            return@withLock LegendaryRelicRewardGrantRecord(entity = entity, isNew = false)
        }
        val inserted = dao.insert(entity)
        LegendaryRelicRewardGrantRecord(
            entity = entity,
            isNew = inserted >= 0L,
        )
    }

    companion object {
        @Volatile
        private var instance: LegendaryRelicRewardGrantRepository? = null

        fun get(context: Context): LegendaryRelicRewardGrantRepository =
            instance ?: synchronized(this) {
                instance ?: LegendaryRelicRewardGrantRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
