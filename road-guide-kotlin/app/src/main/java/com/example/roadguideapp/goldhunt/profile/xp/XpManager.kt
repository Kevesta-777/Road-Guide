package com.example.roadguideapp.goldhunt.profile.xp

import android.content.Context
import androidx.room.withTransaction
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Offline XP ledger and balance manager.
 * Awards XP atomically (transaction row + explorer profile balance). Does not compute levels.
 */
internal class XpManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val database = GoldHuntDatabase.get(appContext)
    private val xpDao = database.xpTransactionDao()
    private val explorerDao = database.explorerProfileDao()
    private val writeMutex = Mutex()

    /**
     * Records an XP award and increments [ExplorerProfileEntity.currentXp].
     *
     * @return The persisted transaction.
     * @throws IllegalArgumentException when [xpAwarded] is not positive.
     */
    suspend fun awardXp(
        source: XpSource,
        xpAwarded: Long,
        description: String = "",
        timestampMs: Long = System.currentTimeMillis(),
        metadataJson: String? = null,
        transactionId: String = UUID.randomUUID().toString(),
    ): XpTransaction = writeMutex.withLock {
        require(xpAwarded > 0L) { "xpAwarded must be positive, got $xpAwarded" }

        val transaction = XpTransaction(
            id = transactionId,
            timestampMs = timestampMs,
            source = source,
            xpAwarded = xpAwarded,
            description = description,
            metadataJson = metadataJson,
        )
        val entity = XpTransactionMapper.toEntity(transaction)

        database.withTransaction {
            xpDao.insert(entity)
            val profile = explorerDao.get() ?: ExplorerProfileEntity(
                createdAtMs = timestampMs,
                updatedAtMs = timestampMs,
            )
            explorerDao.upsert(
                profile.copy(
                    currentXp = profile.currentXp + xpAwarded,
                    updatedAtMs = timestampMs,
                ),
            )
        }

        transaction
    }

    /**
     * Idempotent XP award: returns [XpAwardOutcome.Duplicate] when [transactionId] already exists.
     */
    suspend fun tryAwardXp(
        source: XpSource,
        xpAwarded: Long,
        description: String = "",
        timestampMs: Long = System.currentTimeMillis(),
        metadataJson: String? = null,
        transactionId: String,
    ): XpAwardOutcome = writeMutex.withLock {
        require(xpAwarded > 0L) { "xpAwarded must be positive, got $xpAwarded" }
        if (xpDao.get(transactionId) != null) {
            return@withLock XpAwardOutcome.Duplicate
        }

        val transaction = XpTransaction(
            id = transactionId,
            timestampMs = timestampMs,
            source = source,
            xpAwarded = xpAwarded,
            description = description,
            metadataJson = metadataJson,
        )
        val entity = XpTransactionMapper.toEntity(transaction)

        database.withTransaction {
            xpDao.insert(entity)
            val profile = explorerDao.get() ?: ExplorerProfileEntity(
                createdAtMs = timestampMs,
                updatedAtMs = timestampMs,
            )
            explorerDao.upsert(
                profile.copy(
                    currentXp = profile.currentXp + xpAwarded,
                    updatedAtMs = timestampMs,
                ),
            )
        }

        XpAwardOutcome.Awarded(transaction)
    }

    /** Spendable / current balance stored on the explorer profile singleton row. */
    suspend fun getCurrentXp(): Long = writeMutex.withLock {
        explorerDao.get()?.currentXp ?: 0L
    }

    /** Sum of all recorded XP awards (lifetime earned). */
    suspend fun getLifetimeXp(): Long = writeMutex.withLock {
        xpDao.sumLifetimeXp()
    }

    companion object {
        @Volatile
        private var instance: XpManager? = null

        fun get(context: Context): XpManager =
            instance ?: synchronized(this) {
                instance ?: XpManager(context.applicationContext).also { instance = it }
            }
    }
}
