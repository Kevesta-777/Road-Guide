package com.example.roadguideapp.goldhunt.radar

import android.content.Context
import com.example.roadguideapp.goldhunt.clusters.TreasureClusterGenerator
import com.example.roadguideapp.goldhunt.clusters.completion.ClusterCompletionManager
import com.example.roadguideapp.goldhunt.discovery.GoldHuntFogVisibility
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceGenerator
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator
import com.example.roadguideapp.goldhunt.treasure.TreasureHoardGenerator
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.events.EventTreasureGenerator
import java.util.LinkedHashMap

/**
 * Offline radar pulse engine.
 *
 * Input: player latitude/longitude.
 * Output: [RadarScanResult] with prioritized [RadarSignal] entries.
 *
 * Uses deterministic procedural generators (treasures, clusters, secret places) and
 * derives story-fragment / legendary-relic hooks from cluster and secret-place metadata.
 */
internal object RadarScanEngine {
    suspend fun scan(
        context: Context,
        latitude: Double,
        longitude: Double,
        scannedAtMs: Long = System.currentTimeMillis(),
    ): RadarScanResult {
        val appContext = context.applicationContext
        val gameRepository = GoldHuntRepository.get(appContext)
        gameRepository.ensureInitialized()
        val profile = RadarProfileManager.get(appContext).loadProfile()
        val clusterCompletionManager = ClusterCompletionManager.get(appContext)
        clusterCompletionManager.ensureCacheLoaded()
        val scanContext = RadarScanContext(
            playRegion = gameRepository.playRegion(),
            playRegionId = TreasureGenerator.playRegionId(gameRepository.playRegion()),
            detectionRangeMeters = profile.detectionProfile().detectionRangeMeters,
            radarType = profile.currentRadarType,
            isTreasureCollected = gameRepository::isTreasureCollectedCached,
            isTreasurePlacementExplored = gameRepository::isTreasurePlacementExploredCached,
            isSecretPlaceDiscovered = gameRepository::isSecretPlaceDiscoveredCached,
            areSecretPlacesUnlocked = gameRepository.areSecretPlacesUnlocked(),
            isClusterCompleted = clusterCompletionManager::isCompletedCached,
            treasureGenerationTier = gameRepository.treasureGenerationTier(),
            activeHoard = gameRepository.activeTreasureHoard(),
        )
        return scan(
            latitude = latitude,
            longitude = longitude,
            profile = profile,
            context = scanContext,
            scannedAtMs = scannedAtMs,
        )
    }

    fun scan(
        latitude: Double,
        longitude: Double,
        profile: RadarProfile,
        context: RadarScanContext,
        scannedAtMs: Long = System.currentTimeMillis(),
    ): RadarScanResult {
        require(latitude in -90.0..90.0) { "latitude out of range" }
        require(longitude in -180.0..180.0) { "longitude out of range" }

        val detectionRangeMeters = context.detectionRangeMeters.coerceAtLeast(1)
        val radarType = profile.currentRadarType
        val rawSignals = buildList {
            addAll(probeTreasures(latitude, longitude, detectionRangeMeters, context, scannedAtMs, radarType))
            addAll(probeClusters(latitude, longitude, detectionRangeMeters, context, scannedAtMs, radarType))
            addAll(probeSecretPlaces(latitude, longitude, detectionRangeMeters, context, scannedAtMs, radarType))
            addAll(probeStoryFragments(latitude, longitude, detectionRangeMeters, context, scannedAtMs, radarType))
            addAll(probeLegendaryRelics(latitude, longitude, detectionRangeMeters, context, scannedAtMs, radarType))
        }
        val signals = finalizeSignals(rawSignals, radarType)
        return RadarScanResult(
            latitude = latitude,
            longitude = longitude,
            scannedAtMs = scannedAtMs,
            radarType = radarType,
            detectionRangeMeters = detectionRangeMeters,
            signals = signals,
        )
    }

    private fun probeTreasures(
        latitude: Double,
        longitude: Double,
        detectionRangeMeters: Int,
        context: RadarScanContext,
        scannedAtMs: Long,
        radarType: RadarType,
    ): List<RadarSignal> {
        if (!RadarDetectionScope.isDetectable(RadarTargetCategory.TREASURE, radarType)) {
            return emptyList()
        }
        val candidates = LinkedHashMap<String, TreasureSpec>()
        TreasureGenerator.nearLatLng(
            lat = latitude,
            lng = longitude,
            radiusM = detectionRangeMeters.toDouble(),
            region = context.playRegion,
            isCollected = context.isTreasureCollected,
            tier = context.treasureGenerationTier,
            isInPlayRegion = context.isTreasurePlacementExplored,
        ).forEach { candidates[it.treasureId] = it }

        val bounds = RadarScanBounds.fromCenter(latitude, longitude, detectionRangeMeters.toDouble())
        TreasureGenerator.clusterTreasuresInBounds(
            bounds = bounds,
            region = context.playRegion,
            tier = context.treasureGenerationTier,
            isCollected = context.isTreasureCollected,
            isInPlayRegion = context.isTreasurePlacementExplored,
            isClusterCompleted = context.isClusterCompleted,
        ).forEach { candidates[it.treasureId] = it }

        context.activeHoard?.let { hoard ->
            TreasureHoardGenerator.treasuresForViewport(
                hoard = hoard,
                bounds = bounds,
                region = context.playRegion,
                tier = context.treasureGenerationTier,
                isCollected = context.isTreasureCollected,
            ).forEach { candidates[it.treasureId] = it }
        }

        EventTreasureGenerator.nearLatLng(
            lat = latitude,
            lng = longitude,
            radiusM = detectionRangeMeters.toDouble(),
            region = context.playRegion,
            isCollected = context.isTreasureCollected,
            tier = context.treasureGenerationTier,
            isInPlayRegion = context.isTreasurePlacementExplored,
        ).forEach { candidates[it.treasureId] = it }

        return candidates.values.mapNotNull { spec ->
            signalForCoordinate(
                targetCategory = RadarTargetCategory.TREASURE,
                targetId = spec.treasureId,
                targetLat = spec.lat,
                targetLng = spec.lng,
                playerLat = latitude,
                playerLng = longitude,
                detectionRangeMeters = detectionRangeMeters,
                scannedAtMs = scannedAtMs,
                radarType = radarType,
            )
        }
    }

    private fun probeClusters(
        latitude: Double,
        longitude: Double,
        detectionRangeMeters: Int,
        context: RadarScanContext,
        scannedAtMs: Long,
        radarType: RadarType,
    ): List<RadarSignal> {
        if (!RadarDetectionScope.isDetectable(RadarTargetCategory.CLUSTER, radarType)) {
            return emptyList()
        }
        return TreasureClusterGenerator.clustersNear(
            lat = latitude,
            lng = longitude,
            scanRadiusM = detectionRangeMeters.toDouble(),
            region = context.playRegion,
            isDiscovered = { false },
        )
            .asSequence()
            .filter { !context.isClusterCompleted(it.clusterId) }
            .mapNotNull { cluster ->
                val reachM = detectionRangeMeters + cluster.radiusMeters
                val distance = RadarScanBounds.distanceMeters(
                    latitude,
                    longitude,
                    cluster.centerLatitude,
                    cluster.centerLongitude,
                )
                if (distance > reachM) return@mapNotNull null
                signalForCoordinate(
                    targetCategory = RadarTargetCategory.CLUSTER,
                    targetId = cluster.clusterId,
                    targetLat = cluster.centerLatitude,
                    targetLng = cluster.centerLongitude,
                    playerLat = latitude,
                    playerLng = longitude,
                    detectionRangeMeters = detectionRangeMeters,
                    scannedAtMs = scannedAtMs,
                    radarType = radarType,
                    distanceMeters = distance,
                )
            }
            .toList()
    }

    private fun probeSecretPlaces(
        latitude: Double,
        longitude: Double,
        detectionRangeMeters: Int,
        context: RadarScanContext,
        scannedAtMs: Long,
        radarType: RadarType,
    ): List<RadarSignal> {
        if (!RadarDetectionScope.isDetectable(RadarTargetCategory.SECRET_PLACE, radarType)) {
            return emptyList()
        }
        if (!context.areSecretPlacesUnlocked) return emptyList()
        return SecretPlaceGenerator.secretsNear(
            lat = latitude,
            lng = longitude,
            scanRadiusM = detectionRangeMeters.toDouble(),
            region = context.playRegion,
            isDiscovered = context.isSecretPlaceDiscovered,
            additionalSecretGeneration = true,
        )
            .asSequence()
            .filter { spec ->
                GoldHuntFogVisibility.isInPlayRegion(spec.lat, spec.lng, context.playRegion)
            }
            .mapNotNull { spec ->
                signalForSecretPlace(
                    spec = spec,
                    playerLat = latitude,
                    playerLng = longitude,
                    detectionRangeMeters = detectionRangeMeters,
                    scannedAtMs = scannedAtMs,
                    radarType = radarType,
                )
            }
            .toList()
    }

    private fun probeStoryFragments(
        latitude: Double,
        longitude: Double,
        detectionRangeMeters: Int,
        context: RadarScanContext,
        scannedAtMs: Long,
        radarType: RadarType,
    ): List<RadarSignal> {
        if (!RadarDetectionScope.isDetectable(RadarTargetCategory.STORY_FRAGMENT, radarType)) {
            return emptyList()
        }
        val signals = ArrayList<RadarSignal>()
        TreasureClusterGenerator.clustersNear(
            lat = latitude,
            lng = longitude,
            scanRadiusM = detectionRangeMeters.toDouble(),
            region = context.playRegion,
            isDiscovered = { false },
        )
            .asSequence()
            .filter { !context.isClusterCompleted(it.clusterId) }
            .forEach { cluster ->
                val fragmentId = RadarHookResolver.storyFragmentForCluster(cluster) ?: return@forEach
                signalForCoordinate(
                    targetCategory = RadarTargetCategory.STORY_FRAGMENT,
                    targetId = fragmentId,
                    targetLat = cluster.centerLatitude,
                    targetLng = cluster.centerLongitude,
                    playerLat = latitude,
                    playerLng = longitude,
                    detectionRangeMeters = detectionRangeMeters,
                    scannedAtMs = scannedAtMs,
                    radarType = radarType,
                )?.let { signals += it }
            }

        if (context.areSecretPlacesUnlocked) {
            SecretPlaceGenerator.secretsNear(
                lat = latitude,
                lng = longitude,
                scanRadiusM = detectionRangeMeters.toDouble(),
                region = context.playRegion,
                isDiscovered = context.isSecretPlaceDiscovered,
                additionalSecretGeneration = true,
            )
                .asSequence()
                .filter { spec ->
                    GoldHuntFogVisibility.isInPlayRegion(spec.lat, spec.lng, context.playRegion)
                }
                .forEach { spec ->
                    val fragmentId = RadarHookResolver.storyFragmentForSecretPlace(
                        spec = spec,
                        playRegionId = context.playRegionId,
                    ) ?: return@forEach
                    signalForCoordinate(
                        targetCategory = RadarTargetCategory.STORY_FRAGMENT,
                        targetId = fragmentId,
                        targetLat = spec.lat,
                        targetLng = spec.lng,
                        playerLat = latitude,
                        playerLng = longitude,
                        detectionRangeMeters = detectionRangeMeters,
                        scannedAtMs = scannedAtMs,
                        radarType = radarType,
                    )?.let { signals += it }
                }
        }
        return signals
    }

    private fun probeLegendaryRelics(
        latitude: Double,
        longitude: Double,
        detectionRangeMeters: Int,
        context: RadarScanContext,
        scannedAtMs: Long,
        radarType: RadarType,
    ): List<RadarSignal> {
        if (!RadarDetectionScope.isDetectable(RadarTargetCategory.LEGENDARY_RELIC, radarType)) {
            return emptyList()
        }
        val signals = ArrayList<RadarSignal>()
        TreasureClusterGenerator.clustersNear(
            lat = latitude,
            lng = longitude,
            scanRadiusM = detectionRangeMeters.toDouble(),
            region = context.playRegion,
            isDiscovered = { false },
        )
            .asSequence()
            .filter { !context.isClusterCompleted(it.clusterId) }
            .forEach { cluster ->
                val relicKey = RadarHookResolver.legendaryRelicForCluster(cluster) ?: return@forEach
                signalForCoordinate(
                    targetCategory = RadarTargetCategory.LEGENDARY_RELIC,
                    targetId = relicKey,
                    targetLat = cluster.centerLatitude,
                    targetLng = cluster.centerLongitude,
                    playerLat = latitude,
                    playerLng = longitude,
                    detectionRangeMeters = detectionRangeMeters,
                    scannedAtMs = scannedAtMs,
                    radarType = radarType,
                )?.let { signals += it }
            }

        if (context.areSecretPlacesUnlocked) {
            SecretPlaceGenerator.secretsNear(
                lat = latitude,
                lng = longitude,
                scanRadiusM = detectionRangeMeters.toDouble(),
                region = context.playRegion,
                isDiscovered = context.isSecretPlaceDiscovered,
                additionalSecretGeneration = true,
            )
                .asSequence()
                .filter { spec ->
                    GoldHuntFogVisibility.isInPlayRegion(spec.lat, spec.lng, context.playRegion)
                }
                .forEach { spec ->
                    val relicKey = RadarHookResolver.legendaryRelicForSecretPlace(
                        spec = spec,
                        playRegionId = context.playRegionId,
                    ) ?: return@forEach
                    signalForCoordinate(
                        targetCategory = RadarTargetCategory.LEGENDARY_RELIC,
                        targetId = relicKey,
                        targetLat = spec.lat,
                        targetLng = spec.lng,
                        playerLat = latitude,
                        playerLng = longitude,
                        detectionRangeMeters = detectionRangeMeters,
                        scannedAtMs = scannedAtMs,
                        radarType = radarType,
                    )?.let { signals += it }
                }
        }
        return signals
    }

    private fun signalForSecretPlace(
        spec: SecretPlaceSpec,
        playerLat: Double,
        playerLng: Double,
        detectionRangeMeters: Int,
        scannedAtMs: Long,
        radarType: RadarType,
    ): RadarSignal? = signalForCoordinate(
        targetCategory = RadarTargetCategory.SECRET_PLACE,
        targetId = spec.secretPlaceId,
        targetLat = spec.lat,
        targetLng = spec.lng,
        playerLat = playerLat,
        playerLng = playerLng,
        detectionRangeMeters = detectionRangeMeters,
        scannedAtMs = scannedAtMs,
        radarType = radarType,
    )

    private fun signalForCoordinate(
        targetCategory: RadarTargetCategory,
        targetId: String,
        targetLat: Double,
        targetLng: Double,
        playerLat: Double,
        playerLng: Double,
        detectionRangeMeters: Int,
        scannedAtMs: Long,
        radarType: RadarType,
        distanceMeters: Double? = null,
    ): RadarSignal? {
        val distance = distanceMeters ?: RadarScanBounds.distanceMeters(
            playerLat,
            playerLng,
            targetLat,
            targetLng,
        )
        return RadarSignalCalculator.buildWithBearing(
            targetCategory = targetCategory,
            targetId = targetId,
            distanceMeters = distance,
            maxRangeMeters = detectionRangeMeters,
            playerLat = playerLat,
            playerLng = playerLng,
            targetLat = targetLat,
            targetLng = targetLng,
            detectedTime = scannedAtMs,
            radarType = radarType,
        )
    }

    private fun finalizeSignals(
        rawSignals: List<RadarSignal>,
        radarType: RadarType,
    ): List<RadarSignal> {
        val deduped = rawSignals
            .filter { it.signalStrength >= RadarScanConfig.MIN_REPORTED_STRENGTH }
            .groupBy { it.targetCategory to it.targetId }
            .map { (_, group) -> group.maxBy { it.signalStrength } }
        val sorted = RadarSignalSorter.sortByPriority(deduped, radarType)
        val capped = LinkedHashMap<RadarTargetCategory, Int>()
        val limited = ArrayList<RadarSignal>(RadarScanConfig.MAX_TOTAL_SIGNALS)
        for (signal in sorted) {
            if (limited.size >= RadarScanConfig.MAX_TOTAL_SIGNALS) break
            val count = capped.getOrDefault(signal.targetCategory, 0)
            if (count >= RadarScanConfig.MAX_SIGNALS_PER_CATEGORY) continue
            capped[signal.targetCategory] = count + 1
            limited += signal
        }
        return limited
    }
}
