package com.example.roadguideapp.goldhunt.clusters.distribution

import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.engine.GridIndex
import com.example.roadguideapp.goldhunt.engine.PlayRegion
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerationTier
import com.example.roadguideapp.goldhunt.treasure.TreasureGenerator
import com.example.roadguideapp.goldhunt.treasure.TreasureHash
import com.example.roadguideapp.goldhunt.treasure.TreasureId
import com.example.roadguideapp.goldhunt.treasure.TreasureSpec
import com.example.roadguideapp.goldhunt.treasure.TreasureTypePicker
import kotlin.math.PI

/**
 * Places cluster treasures deterministically around [TreasureCluster.centerLatLng].
 *
 * All candidate positions are validated for radius containment and minimum separation.
 * Road/POI modes use [ClusterDistributionHints]; when hints are empty they fall back to
 * [ClusterTreasureDistributionMode.NATURAL_SCATTER] without breaking determinism.
 */
internal object ClusterTreasureDistributionEngine {
    fun distribute(
        cluster: TreasureCluster,
        region: PlayRegion,
        tier: TreasureGenerationTier,
        hints: ClusterDistributionHints = ClusterDistributionHints.EMPTY,
        mode: ClusterTreasureDistributionMode =
            ClusterDistributionModeResolver.resolveMode(cluster.clusterType, cluster.clusterId),
        isCollected: (String) -> Boolean = { false },
        isInPlayRegion: (Double, Double) -> Boolean = { _, _ -> true },
    ): List<TreasureSpec> {
        if (cluster.treasureCount <= 0) return emptyList()
        val effectiveRadius = cluster.radiusMeters * (1.0 - ClusterDistributionConfig.EDGE_MARGIN_FRACTION)
        val resolvedMode = resolveModeWithFallback(mode, cluster, hints, effectiveRadius)
        val regionL1Id = regionL1IdFor(cluster, region)
        val placed = ArrayList<PlacedPoint>(cluster.treasureCount)
        val out = ArrayList<TreasureSpec>(cluster.treasureCount)

        for (index in 0 until cluster.treasureCount) {
            val treasureId = TreasureId.forClusterSlot(cluster.clusterId, index)
            if (isCollected(treasureId)) continue
            val seed = "$treasureId:gen"
            val position = placeTreasure(
                cluster = cluster,
                index = index,
                treasureId = treasureId,
                mode = resolvedMode,
                hints = hints,
                effectiveRadiusM = effectiveRadius,
                placed = placed,
            ) ?: continue
            if (!isInPlayRegion(position.latitude, position.longitude)) continue
            placed += position
            val type = TreasureTypePicker.pick(seed, tier)
            out += TreasureSpec(
                treasureId = treasureId,
                type = type,
                lat = position.latitude,
                lng = position.longitude,
                creditAmount = TreasureGenerator.rollCredits(type, seed),
                placementKind = placementKindFor(resolvedMode),
                regionL1Id = regionL1Id,
            )
        }
        return out
    }

    private fun resolveModeWithFallback(
        mode: ClusterTreasureDistributionMode,
        cluster: TreasureCluster,
        hints: ClusterDistributionHints,
        effectiveRadiusM: Double,
    ): ClusterTreasureDistributionMode = when (mode) {
        ClusterTreasureDistributionMode.ROAD_BIASED -> {
            if (anchorsWithinRadius(cluster, hints.roadAnchors, effectiveRadiusM).isEmpty()) {
                ClusterTreasureDistributionMode.NATURAL_SCATTER
            } else {
                mode
            }
        }
        ClusterTreasureDistributionMode.POI_BIASED -> {
            if (anchorsWithinRadius(cluster, hints.poiAnchors, effectiveRadiusM).isEmpty()) {
                ClusterTreasureDistributionMode.NATURAL_SCATTER
            } else {
                mode
            }
        }
        else -> mode
    }

    private fun placeTreasure(
        cluster: TreasureCluster,
        index: Int,
        treasureId: String,
        mode: ClusterTreasureDistributionMode,
        hints: ClusterDistributionHints,
        effectiveRadiusM: Double,
        placed: List<PlacedPoint>,
    ): PlacedPoint? {
        for (attempt in 0 until ClusterDistributionConfig.MAX_PLACEMENT_ATTEMPTS) {
            val seed = placementSeed(treasureId, attempt)
            val candidate = candidatePosition(
                cluster = cluster,
                index = index,
                seed = seed,
                mode = mode,
                hints = hints,
                effectiveRadiusM = effectiveRadiusM,
                treasureCount = cluster.treasureCount,
            ) ?: continue
            if (!ClusterGeoMath.isWithinRadius(
                    cluster.centerLatitude,
                    cluster.centerLongitude,
                    candidate.latitude,
                    candidate.longitude,
                    effectiveRadiusM,
                )
            ) {
                continue
            }
            if (hasOverlap(candidate, placed)) continue
            return candidate
        }
        return null
    }

    private fun candidatePosition(
        cluster: TreasureCluster,
        index: Int,
        seed: String,
        mode: ClusterTreasureDistributionMode,
        hints: ClusterDistributionHints,
        effectiveRadiusM: Double,
        treasureCount: Int,
    ): PlacedPoint? = when (mode) {
        ClusterTreasureDistributionMode.CIRCLE ->
            circlePosition(cluster, index, treasureCount, seed, effectiveRadiusM)
        ClusterTreasureDistributionMode.NATURAL_SCATTER ->
            scatterPosition(cluster, seed, effectiveRadiusM)
        ClusterTreasureDistributionMode.ROAD_BIASED ->
            anchorBiasedPosition(cluster, seed, effectiveRadiusM, hints.roadAnchors)
        ClusterTreasureDistributionMode.POI_BIASED ->
            anchorBiasedPosition(cluster, seed, effectiveRadiusM, hints.poiAnchors)
    }

    private fun circlePosition(
        cluster: TreasureCluster,
        index: Int,
        treasureCount: Int,
        seed: String,
        effectiveRadiusM: Double,
    ): PlacedPoint {
        val baseAngle = (2.0 * PI * index) / treasureCount.coerceAtLeast(1)
        val jitter = (TreasureHash.unitFraction("$seed:angleJitter") - 0.5) * 0.35
        val angle = baseAngle + jitter
        val radiusSpan = ClusterDistributionConfig.CIRCLE_MAX_RADIUS_FRACTION -
            ClusterDistributionConfig.CIRCLE_MIN_RADIUS_FRACTION
        val radiusFraction = ClusterDistributionConfig.CIRCLE_MIN_RADIUS_FRACTION +
            TreasureHash.unitFraction("$seed:radiusFrac") * radiusSpan
        val distanceM = effectiveRadiusM * radiusFraction
        val (lat, lng) = ClusterGeoMath.offsetMeters(
            cluster.centerLatitude,
            cluster.centerLongitude,
            angle,
            distanceM,
        )
        return PlacedPoint(lat, lng)
    }

    private fun scatterPosition(
        cluster: TreasureCluster,
        seed: String,
        effectiveRadiusM: Double,
    ): PlacedPoint {
        val angle = TreasureHash.unitFraction("$seed:angle") * 2.0 * PI
        val radiusFrac = TreasureHash.unitFraction("$seed:radius")
        val distanceM = effectiveRadiusM * radiusFrac
        val (lat, lng) = ClusterGeoMath.offsetMeters(
            cluster.centerLatitude,
            cluster.centerLongitude,
            angle,
            distanceM,
        )
        return PlacedPoint(lat, lng)
    }

    private fun anchorBiasedPosition(
        cluster: TreasureCluster,
        seed: String,
        effectiveRadiusM: Double,
        anchors: List<ClusterAnchorPoint>,
    ): PlacedPoint? {
        val eligible = anchorsWithinRadius(cluster, anchors, effectiveRadiusM)
        if (eligible.isEmpty()) return scatterPosition(cluster, seed, effectiveRadiusM)
        val pick = TreasureHash.intInRange("$seed:anchor", 0, eligible.lastIndex)
        val anchor = eligible[pick]
        val jitterAngle = TreasureHash.unitFraction("$seed:anchorAngle") * 2.0 * PI
        val jitterDistance = TreasureHash.unitFraction("$seed:anchorDist") *
            ClusterDistributionConfig.ANCHOR_JITTER_M
        val (lat, lng) = ClusterGeoMath.offsetMeters(
            anchor.latitude,
            anchor.longitude,
            jitterAngle,
            jitterDistance,
        )
        return PlacedPoint(lat, lng)
    }

    private fun anchorsWithinRadius(
        cluster: TreasureCluster,
        anchors: List<ClusterAnchorPoint>,
        effectiveRadiusM: Double,
    ): List<ClusterAnchorPoint> = anchors.filter { anchor ->
        ClusterGeoMath.isWithinRadius(
            cluster.centerLatitude,
            cluster.centerLongitude,
            anchor.latitude,
            anchor.longitude,
            effectiveRadiusM,
        )
    }

    private fun hasOverlap(candidate: PlacedPoint, placed: List<PlacedPoint>): Boolean {
        for (existing in placed) {
            val distance = ClusterGeoMath.distanceMeters(
                candidate.latitude,
                candidate.longitude,
                existing.latitude,
                existing.longitude,
            )
            if (distance < ClusterDistributionConfig.MIN_SEPARATION_M) {
                return true
            }
        }
        return false
    }

    private fun regionL1IdFor(cluster: TreasureCluster, region: PlayRegion): String {
        val cell = GridIndex.encode(
            cluster.centerLatitude,
            cluster.centerLongitude,
            level = 1,
            region = region,
        )
        return cell?.id ?: "cluster:${cluster.clusterId}"
    }

    private fun placementSeed(treasureId: String, attempt: Int): String =
        if (attempt == 0) treasureId else "$treasureId:attempt$attempt"

    private fun placementKindFor(mode: ClusterTreasureDistributionMode): String =
        "CLUSTER_${mode.id}"

    private data class PlacedPoint(
        val latitude: Double,
        val longitude: Double,
    )
}
