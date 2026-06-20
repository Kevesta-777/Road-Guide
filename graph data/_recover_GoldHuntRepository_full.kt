package com.example.roadguideapp.goldhunt.repository

import android.content.Context
import com.example.roadguideapp.goldhunt.database.CreditEventEntity
import com.example.roadguideapp.goldhunt.database.DistrictDiscoveryEntity
import com.example.roadguideapp.goldhunt.database.ExploredCellEntity
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.database.L1ProgressEntity
import com.example.roadguideapp.goldhunt.database.PlayerStatsEntity
import com.example.roadguideapp.goldhunt.engine.GridCell
import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.engine.PlayRegionResolver
import com.example.roadguideapp.goldhunt.profile.PlayerProfile
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedHashMap

internal data class DiscoveryTickResult(
    val fogDirty: Boolean,
    val creditsEarned: Int,
    val newlyExplored: Boolean,
) {
    companion object {
        fun skipped() = DiscoveryTickResult(fogDirty = false, creditsEarned = 0, newlyExplored = false)
    }
}

internal class GoldHuntRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val db = GoldHuntDatabase.get(appContext)
    private val exploredDao = db.exploredCellDao()
    private val creditDao = db.creditEventDao()
    private val districtDao = db.districtDiscoveryDao()
    private val l1Dao = db.l1ProgressDao()
    private val statsDao = db.playerStatsDao()
    private val writeMutex = Mutex()

    @Volatile
    private var region: PlayRegion? = null

    private val exploredCache = object : LinkedHashMap<String, Boolean>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
            return size > com.example.roadguideapp.goldhunt.GoldHuntConfig.EXPLORED_CACHE_CAPACITY
        }
    }

    suspend fun ensureInitialized(): PlayRegion {
        region?.let { return it }
        return writeMutex.withLock {
            region ?: run {
                val resolved = PlayRegionResolver.resolve(appContext)
                region = resolved
                val existing = statsDao.get()
                if (existing == null) {
                    statsDao.upsert(
                        PlayerStatsEntity(
                            id = 1,
                            playRegionCellCountL0 = resolved.totalCellsL0,
                            updatedAt = System.currentTimeMillis(),
                        ),
                    )
                } else if (existing.playRegionCellCountL0 <= 0L) {
                    statsDao.upsert(
                        existing.copy(
                            playRegionCellCountL0 = resolved.totalCellsL0,
                            updatedAt = System.currentTimeMillis(),
                        ),
                    )
                }
                resolved
            }
        }
    }

    fun playRegion(): PlayRegion = region ?: throw IllegalStateException("Call ensureInitialized() first")

    fun isExploredCached(cellId: String): Boolean = exploredCache[cellId] == true

    suspend fun discoverCell(cell: GridCell, timestampMs: Long): DiscoveryTickResult =
        writeMutex.withLock {
            if (isExploredCached(cell.id)) {
                return DiscoveryTickResult(fogDirty = false, creditsEarned = 0, newlyExplored = false)
            }
            val region = playRegion()
            val inserted = exploredDao.insert(
                ExploredCellEntity(
                    cellId = cell.id,
                    level = 0,
                    discoveredAt = timestampMs,
                    lat = cell.center.latitude,
                    lng = cell.center.longitude,
                ),
            )
            if (inserted < 0L) {
                exploredCache[cell.id] = true
                return DiscoveryTickResult(fogDirty = false, creditsEarned = 0, newlyExplored = false)
            }
            exploredCache[cell.id] = true

            val l1 = GridIndex.parentL1(cell, region)
            var l1Count = 0
            var l1ClusterGranted = true
            if (l1 != null) {
                val progress = l1Dao.find(l1.id)
                l1Count = (progress?.exploredL0Count ?: 0) + 1
                l1ClusterGranted = progress?.clusterRewardGranted == true
                if (progress == null) {
                    l1Dao.insert(
                        L1ProgressEntity(
                            l1CellId = l1.id,
                            exploredL0Count = l1Count,
                            clusterRewardGranted = false,
                        ),
                    )
                } else {
                    l1Dao.update(
                        progress.copy(
                            exploredL0Count = l1Count,
                            clusterRewardGranted = progress.clusterRewardGranted,
                        ),
                    )
                }
            }

            val l2 = GridIndex.parentL2(cell, region)
            val districtKey = l2?.id
            var districtIsNew = false
            if (districtKey != null && districtDao.findKey(districtKey) == null) {
                districtDao.insert(
                    DistrictDiscoveryEntity(
                        districtKey = districtKey,
                        firstDiscoveredAt = timestampMs,
                        displayName = districtKey,
                    ),
                )
                districtIsNew = true
            }

            val grants = RewardDispatcher.grantsForNewL0(
                cell = cell,
                l1CellId = l1?.id,
                l1ExploredCount = l1Count,
                l1ClusterAlreadyGranted = l1ClusterGranted,
                districtIsNew = districtIsNew,
                districtKey = districtKey,
            )

            var credits = 0
            for (grant in grants) {
                val row = creditDao.insert(
                    CreditEventEntity(
                        eventId = grant.eventId,
                        ruleType = grant.ruleType,
                        amount = grant.amount,
                        createdAt = timestampMs,
                        payloadJson = grant.label,
                    ),
                )
                if (row >= 0L) credits += grant.amount
            }

            if (l1 != null && grants.any { it.ruleType == com.example.roadguideapp.goldhunt.rewards.RewardRuleType.CLUSTER_L1 }) {
                l1Dao.find(l1.id)?.let { row ->
                    l1Dao.update(row.copy(clusterRewardGranted = true))
                }
            }

            val exploredCount = exploredDao.countL0()
            val totalCredits = creditDao.sumCredits()
            val stats = statsDao.get() ?: PlayerStatsEntity()
            val percent = if (stats.playRegionCellCountL0 > 0L) {
                (exploredCount.toDouble() / stats.playRegionCellCountL0.toDouble() * 100.0)
                    .coerceIn(0.0, 100.0)
            } else {
                0.0
            }
            statsDao.upsert(
                stats.copy(
                    totalCredits = totalCredits,
                    totalExploredCellsL0 = exploredCount,
                    discoveryPercent = percent,
                    updatedAt = timestampMs,
                ),
            )

            DiscoveryTickResult(
                fogDirty = true,
                creditsEarned = credits,
                newlyExplored = true,
            )
        }

    suspend fun addDistance(deltaM: Double) {
        if (deltaM <= 0.0) return
        writeMutex.withLock {
            val stats = statsDao.get() ?: return
            statsDao.upsert(
                stats.copy(
                    totalDistanceM = stats.totalDistanceM + deltaM,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun loadProfile(): PlayerProfile {
        ensureInitialized()
        val stats = statsDao.get() ?: PlayerStatsEntity(playRegionCellCountL0 = playRegion().totalCellsL0)
        return PlayerProfile(
            totalCredits = stats.totalCredits,
            totalExploredCells = stats.totalExploredCellsL0,
            totalDistanceM = stats.totalDistanceM,
            discoveryPercent = stats.discoveryPercent,
            playRegionCellCount = stats.playRegionCellCountL0,
        )
    }

    suspend fun warmExploredCache() {
        // Room has no bulk export in MVP; cache fills on discovery.
    }

    companion object {
        @Volatile
        private var instance: GoldHuntRepository? = null

        fun get(context: Context): GoldHuntRepository {
            return instance ?: synchronized(this) {
                instance ?: GoldHuntRepository(context).also { instance = it }
            }
        }
    }
}
