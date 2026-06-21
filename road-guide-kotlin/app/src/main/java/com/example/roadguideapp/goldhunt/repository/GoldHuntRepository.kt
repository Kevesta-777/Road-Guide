package com.example.roadguideapp.goldhunt.repository

import android.content.Context
import com.example.roadguideapp.goldhunt.GoldHuntConfig
import com.example.roadguideapp.goldhunt.GoldHuntPreferences
import com.example.roadguideapp.goldhunt.database.CollectedTreasureEntity
import com.example.roadguideapp.goldhunt.database.CreditEventEntity
import com.example.roadguideapp.goldhunt.database.DiscoveredRoadEntity
import com.example.roadguideapp.goldhunt.database.DistrictDiscoveryEntity
import com.example.roadguideapp.goldhunt.database.ExploredCellEntity
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.database.L1ProgressEntity
import com.example.roadguideapp.goldhunt.database.PlayerStatsEntity
import com.example.roadguideapp.goldhunt.database.SecretPlaceDiscoveryEntity
import com.example.roadguideapp.goldhunt.engine.GridCell
import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.engine.PlayRegionResolver
import com.example.roadguideapp.goldhunt.clusters.completion.ClusterCompletionManager
import com.example.roadguideapp.goldhunt.profile.PlayerProfile
import com.example.roadguideapp.goldhunt.profile.xp.GameplayXpAwarder
import com.example.roadguideapp.goldhunt.profile.xp.XpAwardOutcome
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardDispatcher
import com.example.roadguideapp.goldhunt.rewards.SecretPlaceRewardDispatcher
import com.example.roadguideapp.goldhunt.rewards.TreasureRewardDispatcher
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceSpec
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopediaRepository
import com.example.roadguideapp.goldhunt.treasure.stats.TreasureRarityStatisticsManager
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerationTier
import com.example.roadguideapp.goldhunt.treasure.TreasureHoardRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.achievements.progress.AchievementProgressEngine
import com.example.roadguideapp.goldhunt.events.rewards.SeasonalEventRewardGrantManager
import com.example.roadguideapp.goldhunt.treasure.TreasureType
import com.example.roadguideapp.goldhunt.treasure.events.EventTreasureDetector
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedHashMap

internal data class DiscoveryTickResult(
    val fogDirty: Boolean,
    val roadsOverlayDirty: Boolean,
    val treasureOverlayDirty: Boolean,
    val creditsEarned: Int,
    val newlyExplored: Boolean,
    val xpEarned: Long = 0L,
    val collectedTreasureType: TreasureType? = null,
    val clusterCompletionCredits: Int = 0,
    val clusterCompletionXp: Long = 0L,
    val clusterCompleted: Boolean = false,
) {
    companion object {
        fun skipped() = DiscoveryTickResult(
            fogDirty = false,
            roadsOverlayDirty = false,
            treasureOverlayDirty = false,
            creditsEarned = 0,
            newlyExplored = false,
            xpEarned = 0L,
        )
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
    private val collectedTreasureDao = db.collectedTreasureDao()
    private val secretPlaceDao = db.secretPlaceDiscoveryDao()
    private val discoveredRoadDao = db.discoveredRoadDao()
    private val gameplayXp = GameplayXpAwarder.get(appContext)
    private val treasureRarityStats = TreasureRarityStatisticsManager.get(appContext)
    private val treasureEncyclopedia = TreasureEncyclopediaRepository.get(appContext)
    private val clusterCompletionManager = ClusterCompletionManager.get(appContext)
    private val seasonalRewardGrantManager by lazy { SeasonalEventRewardGrantManager.get(appContext) }
    private val achievementProgressEngine by lazy { AchievementProgressEngine.get(appContext) }
    private val writeMutex = Mutex()

    @Volatile
    private var region: PlayRegion? = null

    @Volatile
    private var collectedCacheWarmed = false

    private val collectedCacheWarmLock = Any()

    private val exploredCache = object : LinkedHashMap<String, Boolean>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
            return size > GoldHuntConfig.EXPLORED_CACHE_CAPACITY
        }
    }

    private val collectedTreasureCache = object : LinkedHashMap<String, Boolean>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
            return size > GoldHuntConfig.COLLECTED_TREASURE_CACHE_CAPACITY
        }
    }

    private val secretPlaceCache = object : LinkedHashMap<String, Boolean>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
            return size > GoldHuntConfig.COLLECTED_TREASURE_CACHE_CAPACITY
        }
    }

    suspend fun ensureInitialized(): PlayRegion {
        region?.let {
            warmCollectedTreasureCacheIfNeeded()
            return it
        }
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
                warmCollectedTreasureCacheIfNeeded()
                resolved
            }
        }
    }

    private suspend fun warmCollectedTreasureCacheIfNeeded() {
        if (collectedCacheWarmed) return
        val treasureIds = collectedTreasureDao.allTreasureIds()
        synchronized(collectedCacheWarmLock) {
            if (collectedCacheWarmed) return
            for (treasureId in treasureIds) {
                collectedTreasureCache[treasureId] = true
            }
            collectedCacheWarmed = true
        }
    }

    fun playRegion(): PlayRegion = region ?: throw IllegalStateException("Call ensureInitialized() first")

    fun isInPlayRegion(lat: Double, lng: Double): Boolean {
        val playRegion = region ?: return false
        return GridIndex.encode(lat, lng, level = 0, playRegion) != null
    }

    fun isTreasurePlacementExploredCached(lat: Double, lng: Double): Boolean =
        isInPlayRegion(lat, lng)

    fun isExploredCached(cellId: String): Boolean = exploredCache[cellId] == true

    fun isTreasureCollectedCached(treasureId: String): Boolean =
        collectedTreasureCache[treasureId] == true

    fun isSecretPlaceDiscoveredCached(secretPlaceId: String): Boolean =
        secretPlaceCache[secretPlaceId] == true

    suspend fun collectedTreasureCount(type: TreasureType): Int =
        collectedTreasureDao.countByType(type.id)

    suspend fun collectedSecretPlaceCount(): Int = secretPlaceDao.count()

    suspend fun areSecretPlacesUnlocked(): Boolean {
        ensureInitialized()
        return collectedTreasureCount(TreasureType.GIFT) >= GoldHuntConfig.SECRET_UNLOCK_MIN_GIFTS
    }

    fun activeTreasureHoard(): TreasureHoardRegion? =
        GoldHuntPreferences.getActiveHoard(appContext)

    suspend fun treasureGenerationTier(): TreasureGenerationTier {
        ensureInitialized()
        return TreasureGenerationTier.fromProgress(
            starsCollected = collectedTreasureDao.countByType(TreasureType.STAR.id),
            flowersCollected = collectedTreasureDao.countByType(TreasureType.FLOWER.id),
            crystalsCollected = collectedTreasureDao.countByType(TreasureType.CRYSTAL.id),
        )
    }

    suspend fun collectTreasure(spec: TreasureSpec, timestampMs: Long): DiscoveryTickResult {
        val result = writeMutex.withLock {
            if (isTreasureCollectedCached(spec.treasureId)) {
                return DiscoveryTickResult.skipped()
            }
            val inserted = collectedTreasureDao.insert(
                CollectedTreasureEntity(
                    treasureId = spec.treasureId,
                    type = spec.type.id,
                    lat = spec.lat,
                    lng = spec.lng,
                    creditAmount = spec.creditAmount,
                    placementKind = spec.placementKind,
                    regionL1Id = spec.regionL1Id,
                    collectedAt = timestampMs,
                    generatorVersion = GoldHuntConfig.TREASURE_GENERATOR_VERSION,
                ),
            )
            if (inserted < 0L) {
                collectedTreasureCache[spec.treasureId] = true
                return DiscoveryTickResult.skipped()
            }
            collectedTreasureCache[spec.treasureId] = true
            var credits = 0
            var xpOutcome: XpAwardOutcome = XpAwardOutcome.Duplicate
            if (EventTreasureDetector.isEventTreasure(spec)) {
                seasonalRewardGrantManager.grantForTreasureCollection(spec, timestampMs)?.let { seasonal ->
                    credits += seasonal.creditsGranted
                    xpOutcome = seasonal.xpOutcome
                }
            } else {
                TreasureRewardDispatcher.grant(spec)?.let { grant ->
                    credits += applyGrant(grant, timestampMs)
                }
                xpOutcome = gameplayXp.tryAwardTreasureCollection(spec, timestampMs)
            }
            refreshStats(timestampMs, treasureDelta = 1)
            treasureRarityStats.recordCollection(spec, timestampMs)
            val creditsForEncyclopedia = credits.takeIf { it > 0 } ?: spec.creditAmount
            treasureEncyclopedia.recordCollection(
                treasureId = spec.treasureId,
                type = spec.type,
                creditsEarned = creditsForEncyclopedia,
                collectedAtMs = timestampMs,
            )
            val region = playRegion()
            val completionOutcome = clusterCompletionManager.tryCompleteAfterTreasureCollection(
                spec = spec,
                region = region,
                isTreasureCollected = ::isTreasureCollectedCached,
                applyGrant = { grant -> applyGrant(grant, timestampMs) },
                timestampMs = timestampMs,
            )
            val totalCredits = credits + completionOutcome.creditsGranted
            val totalXp = xpOutcome.xpAwarded + completionOutcome.xpGranted
            DiscoveryTickResult(
                fogDirty = false,
                roadsOverlayDirty = false,
                treasureOverlayDirty = true,
                creditsEarned = totalCredits,
                newlyExplored = true,
                xpEarned = totalXp,
                collectedTreasureType = spec.type,
                clusterCompletionCredits = completionOutcome.creditsGranted,
                clusterCompletionXp = completionOutcome.xpGranted,
                clusterCompleted = completionOutcome.completed,
            )
        }
        if (result.newlyExplored) {
            achievementProgressEngine.onTreasureCollected(timestampMs)
        }
        return result
    }

    suspend fun discoverSecretPlace(spec: SecretPlaceSpec, timestampMs: Long): SecretPlaceDiscoveryResult {
        val result = writeMutex.withLock {
            if (isSecretPlaceDiscoveredCached(spec.secretPlaceId)) {
                return SecretPlaceDiscoveryResult.skipped()
            }
            val inserted = secretPlaceDao.insert(
                SecretPlaceDiscoveryEntity(
                    secretPlaceId = spec.secretPlaceId,
                    type = spec.type.id,
                    rarity = spec.rarity.id,
                    lat = spec.lat,
                    lng = spec.lng,
                    regionL1Id = spec.regionL1Id,
                    regionL2Id = spec.regionL2Id,
                    displayName = spec.displayName,
                    creditsGranted = 0,
                    treasureNestSlots = spec.treasureNestSlots,
                    storyFragmentId = spec.storyFragmentId,
                    placementKind = spec.placementKind,
                    discoveredAt = timestampMs,
                    generatorVersion = spec.generatorVersion,
                ),
            )
            if (inserted < 0L) {
                secretPlaceCache[spec.secretPlaceId] = true
                return SecretPlaceDiscoveryResult.skipped()
            }
            secretPlaceCache[spec.secretPlaceId] = true
            var credits = 0
            for (grant in SecretPlaceRewardDispatcher.grantsForDiscovery(spec)) {
                credits += applyGrant(grant, timestampMs)
            }
            refreshStats(timestampMs, secretDelta = 1)
            val xpOutcome = gameplayXp.tryAwardSecretPlace(spec, timestampMs)
            SecretPlaceDiscoveryResult(
                creditsEarned = credits,
                newlyDiscovered = true,
                overlayDirty = true,
                xpEarned = xpOutcome.xpAwarded,
            )
        }
        if (result.newlyDiscovered) {
            achievementProgressEngine.onSecretPlaceDiscovered(timestampMs)
        }
        return result
    }

    suspend fun applyCreditGrant(
        grant: CreditGrant,
        timestampMs: Long = System.currentTimeMillis(),
    ): Int = applyGrant(grant, timestampMs)

    private suspend fun applyGrant(grant: CreditGrant, timestampMs: Long): Int {
        val row = creditDao.insert(
            CreditEventEntity(
                eventId = grant.eventId,
                ruleType = grant.ruleType,
                amount = grant.amount,
                createdAt = timestampMs,
                payloadJson = grant.label,
            ),
        )
        return if (row >= 0L) grant.amount else 0
    }

    private suspend fun refreshStats(
        timestampMs: Long,
        treasureDelta: Int = 0,
        secretDelta: Int = 0,
    ) {
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
                treasuresCollectedCount = stats.treasuresCollectedCount + treasureDelta,
                secretPlacesFoundCount = stats.secretPlacesFoundCount + secretDelta,
                updatedAt = timestampMs,
            ),
        )
    }

    suspend fun discoverCell(cell: GridCell, timestampMs: Long): DiscoveryTickResult {
        val result = writeMutex.withLock {
            if (isExploredCached(cell.id)) {
                return DiscoveryTickResult.skipped()
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
                return DiscoveryTickResult.skipped()
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
                credits += applyGrant(grant, timestampMs)
            }

            if (l1 != null && grants.any { it.ruleType == com.example.roadguideapp.goldhunt.rewards.RewardRuleType.CLUSTER_L1 }) {
                l1Dao.find(l1.id)?.let { row ->
                    l1Dao.update(row.copy(clusterRewardGranted = true))
                }
            }

            refreshStats(timestampMs)

            var xpEarned = 0L
            val roadXp = gameplayXp.tryAwardRoadDiscovery(
                roadKey = cell.id,
                timestampMs = timestampMs,
                description = "Road discovered",
                metadataJson = """{"cellId":"${cell.id}"}""",
            )
            xpEarned += roadXp.xpAwarded
            if (districtIsNew && districtKey != null) {
                val areaXp = gameplayXp.tryAwardAreaDiscovery(
                    districtKey = districtKey,
                    timestampMs = timestampMs,
                    description = "Area discovered",
                    metadataJson = """{"districtKey":"$districtKey"}""",
                )
                xpEarned += areaXp.xpAwarded
            }

            DiscoveryTickResult(
                fogDirty = true,
                roadsOverlayDirty = false,
                treasureOverlayDirty = false,
                creditsEarned = credits,
                newlyExplored = true,
                xpEarned = xpEarned,
            )
        }
        if (result.newlyExplored) {
            achievementProgressEngine.onCellExplored(timestampMs)
        }
        return result
    }

    /**
     * Records a graph road segment discovery (offline routing network).
     * Fog grid discovery uses [discoverCell] instead.
     */
    suspend fun discoverRoad(
        roadKey: String,
        lat: Double,
        lng: Double,
        timestampMs: Long = System.currentTimeMillis(),
        source: String = "graph",
        graphImportId: String? = null,
        closestNode: Int? = null,
        streetGroupKey: String? = null,
        regionL1Id: String? = null,
        regionL2Id: String? = null,
    ): DiscoveryTickResult = writeMutex.withLock {
        val inserted = discoveredRoadDao.insert(
            DiscoveredRoadEntity(
                roadKey = roadKey,
                source = source,
                graphImportId = graphImportId,
                closestNode = closestNode,
                streetGroupKey = streetGroupKey,
                regionL1Id = regionL1Id,
                regionL2Id = regionL2Id,
                firstDiscoveredAt = timestampMs,
                lastVisitedAt = timestampMs,
                firstLat = lat,
                firstLng = lng,
                endLat = lat,
                endLng = lng,
            ),
        )
        if (inserted < 0L) {
            return@withLock DiscoveryTickResult.skipped()
        }
        val xpOutcome = gameplayXp.tryAwardRoadDiscovery(
            roadKey = roadKey,
            timestampMs = timestampMs,
            description = "Road discovered",
            metadataJson = """{"roadKey":"$roadKey","source":"$source"}""",
        )
        DiscoveryTickResult(
            fogDirty = false,
            roadsOverlayDirty = true,
            treasureOverlayDirty = false,
            creditsEarned = 0,
            newlyExplored = true,
            xpEarned = xpOutcome.xpAwarded,
        )
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
            treasuresCollectedCount = stats.treasuresCollectedCount,
            secretPlacesFoundCount = stats.secretPlacesFoundCount,
        )
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
