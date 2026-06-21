package com.example.roadguideapp.goldhunt.profile.xp

import android.content.Context
import com.example.roadguideapp.goldhunt.database.GoldHuntDatabase
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileEntity
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileSchema
import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator
import com.example.roadguideapp.goldhunt.profile.levelup.LevelUpManager
import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.clusters.completion.ClusterCompletionRewards
import com.example.roadguideapp.goldhunt.panorama.PanoramaHunt
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceSpec
import com.example.roadguideapp.goldhunt.radar.RadarTargetCategory
import com.example.roadguideapp.goldhunt.radar.RadarType
import com.example.roadguideapp.goldhunt.radar.rewards.RadarTargetRarity
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec

/**
 * Awards gameplay XP idempotently and syncs [ExplorerProfileEntity] level + discovery counters.
 */
internal class GameplayXpAwarder private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val database = GoldHuntDatabase.get(appContext)
    private val xpManager = XpManager.get(appContext)
    private val xpDao = database.xpTransactionDao()
    private val explorerDao = database.explorerProfileDao()
    private val levelUpManager = LevelUpManager.get(appContext)

    suspend fun tryAwardRoadDiscovery(
        roadKey: String,
        timestampMs: Long = System.currentTimeMillis(),
        description: String = "Road discovered",
        metadataJson: String? = null,
    ): XpAwardOutcome = awardOnce(
        transactionId = XpTransactionIds.roadDiscovery(roadKey),
        source = XpSource.ROAD_DISCOVERY,
        xpAwarded = GameplayXpValues.ROAD_DISCOVERY,
        description = description,
        timestampMs = timestampMs,
        metadataJson = metadataJson,
        roadsDelta = 1,
    )

    suspend fun tryAwardAreaDiscovery(
        districtKey: String,
        timestampMs: Long = System.currentTimeMillis(),
        description: String = "Area discovered",
        metadataJson: String? = null,
    ): XpAwardOutcome = awardOnce(
        transactionId = XpTransactionIds.areaDiscovery(districtKey),
        source = XpSource.AREA_DISCOVERY,
        xpAwarded = GameplayXpValues.AREA_DISCOVERY,
        description = description,
        timestampMs = timestampMs,
        metadataJson = metadataJson,
        areasDelta = 1,
    )

    suspend fun tryAwardTreasureCollection(
        spec: TreasureSpec,
        timestampMs: Long = System.currentTimeMillis(),
    ): XpAwardOutcome {
        val xp = GameplayXpValues.forTreasureType(spec.type) ?: return XpAwardOutcome.Duplicate
        return awardOnce(
            transactionId = XpTransactionIds.treasureCollection(spec.treasureId),
            source = XpSource.TREASURE_COLLECTION,
            xpAwarded = xp,
            description = "Collected ${spec.type.id.lowercase()} treasure",
            timestampMs = timestampMs,
            metadataJson = """{"treasureId":"${spec.treasureId}","type":"${spec.type.id}"}""",
            treasuresDelta = 1,
        )
    }

    suspend fun tryAwardSeasonalEventTreasure(
        treasureId: String,
        eventId: String,
        eventType: SeasonalEventType,
        xpAwarded: Long,
        timestampMs: Long = System.currentTimeMillis(),
    ): XpAwardOutcome {
        if (xpAwarded <= 0L) return XpAwardOutcome.Duplicate
        return awardOnce(
            transactionId = XpTransactionIds.seasonalEventTreasure(treasureId),
            source = XpSource.SEASONAL_EVENT,
            xpAwarded = xpAwarded,
            description = "Seasonal event treasure (${eventType.displayName})",
            timestampMs = timestampMs,
            metadataJson = """{"treasureId":"$treasureId","eventId":"$eventId","eventType":"${eventType.id}"}""",
            treasuresDelta = 1,
        )
    }

    suspend fun tryAwardSeasonalEventCompletion(
        eventId: String,
        eventType: SeasonalEventType,
        xpAwarded: Long,
        timestampMs: Long = System.currentTimeMillis(),
    ): XpAwardOutcome {
        if (xpAwarded <= 0L) return XpAwardOutcome.Duplicate
        return awardOnce(
            transactionId = XpTransactionIds.seasonalEventCompletion(eventId),
            source = XpSource.SEASONAL_EVENT,
            xpAwarded = xpAwarded,
            description = "Seasonal event completed (${eventType.displayName})",
            timestampMs = timestampMs,
            metadataJson = """{"eventId":"$eventId","eventType":"${eventType.id}"}""",
        )
    }

    suspend fun tryAwardClusterCompletion(
        cluster: TreasureCluster,
        timestampMs: Long = System.currentTimeMillis(),
    ): XpAwardOutcome {
        val xp = ClusterCompletionRewards.xpFor(cluster)
        return awardOnce(
            transactionId = XpTransactionIds.clusterCompletion(cluster.clusterId),
            source = XpSource.CLUSTER_COMPLETION,
            xpAwarded = xp,
            description = "Treasure cluster completed",
            timestampMs = timestampMs,
            metadataJson = buildString {
                append("""{"clusterId":"${cluster.clusterId}","type":"${cluster.clusterType.id}"""")
                cluster.resolvedAchievementKey?.let { append(""","achievementKey":"$it"""") }
                cluster.resolvedStoryFragmentId?.let { append(""","storyFragmentId":"$it"""") }
                cluster.legendaryRelicKey?.let { append(""","legendaryRelicKey":"$it"""") }
                append("}")
            },
        )
    }

    suspend fun tryAwardPanoramaHuntCompletion(
        hunt: PanoramaHunt,
        xpAwarded: Long,
        timestampMs: Long = System.currentTimeMillis(),
    ): XpAwardOutcome {
        if (xpAwarded <= 0L) return XpAwardOutcome.Duplicate
        return awardOnce(
            transactionId = XpTransactionIds.panoramaHuntCompletion(hunt.huntId),
            source = XpSource.PANORAMA_HUNT_COMPLETION,
            xpAwarded = xpAwarded,
            description = "Panorama hunt completed",
            timestampMs = timestampMs,
            metadataJson = buildString {
                append("""{"huntId":"${hunt.huntId}","type":"${hunt.huntType.id}"""")
                hunt.resolvedAchievementKey?.let { append(""","achievementKey":"$it"""") }
                hunt.resolvedStoryFragmentId?.let { append(""","storyFragmentId":"$it"""") }
                hunt.resolvedLegendaryRelicKey?.let { append(""","legendaryRelicKey":"$it"""") }
                append("}")
            },
        )
    }

    suspend fun tryAwardRadarDetection(
        eventId: String,
        xpAwarded: Long,
        radarType: RadarType,
        targetCategory: RadarTargetCategory,
        targetId: String,
        targetRarity: RadarTargetRarity,
        signalStrength: Double,
        timestampMs: Long = System.currentTimeMillis(),
    ): XpAwardOutcome {
        if (xpAwarded <= 0L) return XpAwardOutcome.Duplicate
        return awardOnce(
            transactionId = eventId,
            source = XpSource.RADAR_SCAN,
            xpAwarded = xpAwarded,
            description = "Radar detection (${targetCategory.displayName})",
            timestampMs = timestampMs,
            metadataJson = buildString {
                append("""{"radarType":"${radarType.id}","category":"${targetCategory.id}"""")
                append(""","targetId":"$targetId"""")
                append(""","rarity":"${targetRarity.id}"""")
                append(""","signalStrength":$signalStrength""")
                append("}")
            },
        )
    }

    suspend fun tryAwardRadarPulse(
        eventId: String,
        xpAwarded: Long,
        radarType: RadarType,
        hasDetections: Boolean,
        timestampMs: Long = System.currentTimeMillis(),
    ): XpAwardOutcome {
        if (xpAwarded <= 0L) return XpAwardOutcome.Duplicate
        return awardOnce(
            transactionId = eventId,
            source = XpSource.RADAR_SCAN,
            xpAwarded = xpAwarded,
            description = "Radar pulse completed",
            timestampMs = timestampMs,
            metadataJson = """{"radarType":"${radarType.id}","hasDetections":$hasDetections}""",
        )
    }

    suspend fun tryAwardLegendaryRelicCompletion(
        relicId: String,
        category: RelicCategory,
        xpAwarded: Long,
        name: String,
        timestampMs: Long = System.currentTimeMillis(),
    ): XpAwardOutcome {
        if (xpAwarded <= 0L) return XpAwardOutcome.Duplicate
        return awardOnce(
            transactionId = XpTransactionIds.legendaryRelicCompletion(relicId),
            source = XpSource.LEGENDARY_RELIC,
            xpAwarded = xpAwarded,
            description = "Legendary relic completed: $name",
            timestampMs = timestampMs,
            metadataJson = """{"relicId":"$relicId","category":"${category.id}"}""",
        )
    }

    suspend fun tryAwardAchievementCompletion(
        achievementId: String,
        category: AchievementCategory,
        xpAwarded: Long,
        title: String,
        timestampMs: Long = System.currentTimeMillis(),
    ): XpAwardOutcome {
        if (xpAwarded <= 0L) return XpAwardOutcome.Duplicate
        return awardOnce(
            transactionId = XpTransactionIds.achievementCompletion(achievementId),
            source = XpSource.ACHIEVEMENT,
            xpAwarded = xpAwarded,
            description = "Achievement completed: $title",
            timestampMs = timestampMs,
            metadataJson = """{"achievementId":"$achievementId","category":"${category.id}"}""",
        )
    }

    suspend fun tryAwardSecretPlace(
        spec: SecretPlaceSpec,
        timestampMs: Long = System.currentTimeMillis(),
    ): XpAwardOutcome = awardOnce(
        transactionId = XpTransactionIds.secretPlace(spec.secretPlaceId),
        source = XpSource.SECRET_PLACE,
        xpAwarded = GameplayXpValues.SECRET_PLACE,
        description = "Secret place discovered",
        timestampMs = timestampMs,
        metadataJson = """{"secretPlaceId":"${spec.secretPlaceId}","name":"${spec.displayName}"}""",
        secretsDelta = 1,
    )

    private suspend fun awardOnce(
        transactionId: String,
        source: XpSource,
        xpAwarded: Long,
        description: String,
        timestampMs: Long,
        metadataJson: String?,
        roadsDelta: Int = 0,
        areasDelta: Int = 0,
        treasuresDelta: Int = 0,
        secretsDelta: Int = 0,
    ): XpAwardOutcome {
        val outcome = xpManager.tryAwardXp(
            source = source,
            xpAwarded = xpAwarded,
            description = description,
            timestampMs = timestampMs,
            metadataJson = metadataJson,
            transactionId = transactionId,
        )
        if (outcome is XpAwardOutcome.Awarded) {
            syncExplorerProfile(
                timestampMs = timestampMs,
                roadsDelta = roadsDelta,
                areasDelta = areasDelta,
                treasuresDelta = treasuresDelta,
                secretsDelta = secretsDelta,
            )
        }
        return outcome
    }

    private suspend fun syncExplorerProfile(
        timestampMs: Long,
        roadsDelta: Int,
        areasDelta: Int,
        treasuresDelta: Int,
        secretsDelta: Int,
    ) {
        val lifetimeXp = xpDao.sumLifetimeXp()
        val existing = explorerDao.get()
        val previousLevel = existing?.explorerLevel ?: ExplorerProfileSchema.DEFAULT_EXPLORER_LEVEL
        val level = ExplorerLevelCalculator.calculateLevel(lifetimeXp)

        levelUpManager.checkLevelChangeAfterXp(
            previousLevel = previousLevel,
            lifetimeXp = lifetimeXp,
            timestampMs = timestampMs,
        )

        val createdAtMs = existing?.createdAtMs?.takeIf { it > 0L } ?: timestampMs
        val profile = existing ?: ExplorerProfileEntity(
            createdAtMs = timestampMs,
            updatedAtMs = timestampMs,
        )
        explorerDao.upsert(
            profile.copy(
                explorerLevel = level,
                currentXp = lifetimeXp,
                totalRoadsDiscovered = profile.totalRoadsDiscovered + roadsDelta,
                totalAreasDiscovered = profile.totalAreasDiscovered + areasDelta,
                totalTreasuresCollected = profile.totalTreasuresCollected + treasuresDelta,
                totalSecretPlacesFound = profile.totalSecretPlacesFound + secretsDelta,
                createdAtMs = createdAtMs,
                updatedAtMs = timestampMs,
            ),
        )
    }

    companion object {
        @Volatile
        private var instance: GameplayXpAwarder? = null

        fun get(context: Context): GameplayXpAwarder =
            instance ?: synchronized(this) {
                instance ?: GameplayXpAwarder(context.applicationContext).also { instance = it }
            }
    }
}
