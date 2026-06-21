package com.example.roadguideapp.goldhunt.panorama.statistics

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class PanoramaHuntSessionRecordResult(
    val isNew: Boolean,
) {
    companion object {
        val skipped = PanoramaHuntSessionRecordResult(isNew = false)
    }
}

internal class PanoramaHuntSessionRepository private constructor(context: Context) {
    private val dao = GoldHuntDatabase.get(context.applicationContext).panoramaHuntSessionDao()
    private val writeMutex = Mutex()

    suspend fun recordStarted(
        hunt: PanoramaHunt,
        startedAtMs: Long = System.currentTimeMillis(),
    ): PanoramaHuntSessionRecordResult = writeMutex.withLock {
        val inserted = dao.insert(
            PanoramaHuntSessionEntity(
                huntId = hunt.huntId,
                huntType = hunt.huntType.id,
                secretPlaceId = hunt.secretPlaceId,
                panoramaId = hunt.panoramaId,
                startedAtMs = startedAtMs,
            ),
        )
        PanoramaHuntSessionRecordResult(isNew = inserted >= 0L)
    }

    suspend fun count(): Int = dao.count()

    companion object {
        @Volatile
        private var instance: PanoramaHuntSessionRepository? = null

        fun get(context: Context): PanoramaHuntSessionRepository =
            instance ?: synchronized(this) {
                instance ?: PanoramaHuntSessionRepository(context.applicationContext)
                    .also { instance = it }
            }
    }
}
