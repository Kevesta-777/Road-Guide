package com.example.roadguideapp.goldhunt.secretplaces.categories.assignment

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategoryCatalog
import com.example.roadguideapp.goldhunt.treasure.TreasureHash

/**
 * Deterministic secret-place category roller.
 *
 * Same [SecretPlaceCategoryAssignmentInput] always yields the same [SecretPlaceCategory]
 * (SHA-256 via [TreasureHash], no randomness, no persistence).
 *
 * Inputs bias category weights before the location-stable roll:
 * - [SecretPlacePoiMetadata] and [SecretPlaceEnvironmentType] — map/POI context
 * - [SecretPlaceTreasureDensity] — local treasure saturation
 * - [SecretPlaceExplorationProgress] — grid-stable difficulty signal
 *
 * Does not generate placements, persist discoveries, or render UI.
 */
internal object SecretPlaceCategoryAssignmentEngine {
    fun assign(input: SecretPlaceCategoryAssignmentInput): SecretPlaceCategory {
        val roll = TreasureHash.unitFraction(rollSeed(input))
        val weights = buildAdjustedWeights(input)
        return pickWeighted(roll, weights)
    }

    /** Exposed for tests — canonical seed string passed to [TreasureHash]. */
    fun rollSeed(input: SecretPlaceCategoryAssignmentInput): String {
        val lat = "%.6f".format(input.latitude)
        val lng = "%.6f".format(input.longitude)
        return listOf(
            input.playRegionId,
            "secretPlaceCategory",
            input.anchorId,
            lat,
            lng,
        ).joinToString("|")
    }

    private fun buildAdjustedWeights(
        input: SecretPlaceCategoryAssignmentInput,
    ): List<Pair<SecretPlaceCategory, Double>> {
        val environment = resolveEnvironment(input)
        return SecretPlaceCategoryCatalog.displayOrder.map { category ->
            var weight = category.rarityWeight.toDouble()
            weight *= environmentBias(environment, category)
            weight *= input.poiMetadata.categoryAffinityMultiplier(category.id)
            weight *= densityBias(input.treasureDensity, category)
            weight *= explorationBias(input.explorationProgress, category)
            category to weight.coerceAtLeast(0.01)
        }
    }

    private fun resolveEnvironment(input: SecretPlaceCategoryAssignmentInput): SecretPlaceEnvironmentType {
        if (input.environmentType != SecretPlaceEnvironmentType.UNKNOWN) {
            return input.environmentType
        }
        return input.poiMetadata.resolveEnvironmentType()
    }

    private fun environmentBias(
        environment: SecretPlaceEnvironmentType,
        category: SecretPlaceCategory,
    ): Double = ENVIRONMENT_CATEGORY_BIAS[environment to category] ?: 1.0

    private fun densityBias(
        density: SecretPlaceTreasureDensity,
        category: SecretPlaceCategory,
    ): Double {
        val fraction = density.fraction
        return when (category) {
            SecretPlaceCategory.NATURE_SANCTUARY,
            SecretPlaceCategory.SCENIC_VIEWPOINT,
            -> 1.0 + (0.35 * (1.0 - fraction))
            SecretPlaceCategory.MYSTERY_ZONE,
            SecretPlaceCategory.EXPLORER_HIDEOUT,
            -> 1.0 + (0.45 * fraction)
            SecretPlaceCategory.ANCIENT_RELIC_SITE,
            SecretPlaceCategory.MYTHICAL_PLACE,
            -> 1.0 + (0.25 * fraction)
            else -> 1.0
        }
    }

    private fun explorationBias(
        progress: SecretPlaceExplorationProgress,
        category: SecretPlaceCategory,
    ): Double {
        val signal = progress.combinedSignal
        return when (category) {
            SecretPlaceCategory.NATURE_SANCTUARY,
            SecretPlaceCategory.SCENIC_VIEWPOINT,
            SecretPlaceCategory.HISTORIC_LANDMARK,
            -> 1.0 + (0.15 * (1.0 - signal))
            SecretPlaceCategory.MYSTERY_ZONE,
            SecretPlaceCategory.ANCIENT_RELIC_SITE,
            -> 1.0 + (0.2 * signal)
            SecretPlaceCategory.EXPLORER_HIDEOUT,
            SecretPlaceCategory.MYTHICAL_PLACE,
            -> 1.0 + (0.35 * signal)
            SecretPlaceCategory.LEGENDARY_SITE,
            -> 1.0 + (0.5 * signal)
        }
    }

    private fun pickWeighted(
        roll: Double,
        weights: List<Pair<SecretPlaceCategory, Double>>,
    ): SecretPlaceCategory {
        val total = weights.sumOf { it.second }
        if (total <= 0.0) return SecretPlaceCategory.NATURE_SANCTUARY
        var cumulative = 0.0
        for ((category, weight) in weights) {
            cumulative += weight / total
            if (roll < cumulative) return category
        }
        return weights.last().first
    }

    private val ENVIRONMENT_CATEGORY_BIAS: Map<Pair<SecretPlaceEnvironmentType, SecretPlaceCategory>, Double> =
        buildMap {
            put(SecretPlaceEnvironmentType.NATURAL to SecretPlaceCategory.NATURE_SANCTUARY, 2.5)
            put(SecretPlaceEnvironmentType.WILDERNESS to SecretPlaceCategory.NATURE_SANCTUARY, 2.0)
            put(SecretPlaceEnvironmentType.WATERFRONT to SecretPlaceCategory.SCENIC_VIEWPOINT, 2.2)
            put(SecretPlaceEnvironmentType.NATURAL to SecretPlaceCategory.SCENIC_VIEWPOINT, 1.6)
            put(SecretPlaceEnvironmentType.HISTORIC to SecretPlaceCategory.HISTORIC_LANDMARK, 3.0)
            put(SecretPlaceEnvironmentType.URBAN to SecretPlaceCategory.HISTORIC_LANDMARK, 1.2)
            put(SecretPlaceEnvironmentType.URBAN to SecretPlaceCategory.MYSTERY_ZONE, 1.35)
            put(SecretPlaceEnvironmentType.SUBURBAN to SecretPlaceCategory.MYSTERY_ZONE, 1.25)
            put(SecretPlaceEnvironmentType.MYSTICAL to SecretPlaceCategory.MYSTERY_ZONE, 2.0)
            put(SecretPlaceEnvironmentType.HISTORIC to SecretPlaceCategory.ANCIENT_RELIC_SITE, 2.1)
            put(SecretPlaceEnvironmentType.MYSTICAL to SecretPlaceCategory.ANCIENT_RELIC_SITE, 2.4)
            put(SecretPlaceEnvironmentType.WILDERNESS to SecretPlaceCategory.ANCIENT_RELIC_SITE, 1.7)
            put(SecretPlaceEnvironmentType.NATURAL to SecretPlaceCategory.EXPLORER_HIDEOUT, 1.5)
            put(SecretPlaceEnvironmentType.WILDERNESS to SecretPlaceCategory.EXPLORER_HIDEOUT, 1.9)
            put(SecretPlaceEnvironmentType.SUBURBAN to SecretPlaceCategory.EXPLORER_HIDEOUT, 1.2)
            put(SecretPlaceEnvironmentType.MYSTICAL to SecretPlaceCategory.MYTHICAL_PLACE, 3.0)
            put(SecretPlaceEnvironmentType.WILDERNESS to SecretPlaceCategory.MYTHICAL_PLACE, 1.6)
            put(SecretPlaceEnvironmentType.MYSTICAL to SecretPlaceCategory.LEGENDARY_SITE, 2.5)
            put(SecretPlaceEnvironmentType.HISTORIC to SecretPlaceCategory.LEGENDARY_SITE, 1.5)
        }
}
