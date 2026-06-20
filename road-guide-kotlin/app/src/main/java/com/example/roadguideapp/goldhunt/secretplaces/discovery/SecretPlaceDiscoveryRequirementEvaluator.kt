package com.example.roadguideapp.goldhunt.secretplaces.discovery

import com.example.roadguideapp.goldhunt.clusters.distribution.ClusterGeoMath
import com.example.roadguideapp.goldhunt.secretplaces.SecretPlace
import com.example.roadguideapp.goldhunt.secretplaces.SecretPlaceExplorerLevelGate

/**
 * Evaluates category discovery requirements against live player context.
 * Does not persist discoveries or grant rewards.
 */
internal object SecretPlaceDiscoveryRequirementEvaluator {
    fun evaluate(
        place: SecretPlace,
        context: SecretPlaceDiscoveryContext,
    ): SecretPlaceDiscoveryEvaluation {
        if (!context.secretPlacesUnlocked) {
            return blockedEvaluation(place, "Secret places locked")
        }
        val profile = SecretPlaceDiscoveryRequirementCatalog.profileFor(place.category)
        val statuses = profile.requirements.map { requirement ->
            evaluateRequirement(place, context, requirement)
        }
        return SecretPlaceDiscoveryEvaluation(
            place = place,
            statuses = statuses,
        )
    }

    fun isDiscoverable(
        place: SecretPlace,
        context: SecretPlaceDiscoveryContext,
    ): Boolean = evaluate(place, context).isDiscoverable

    private fun blockedEvaluation(
        place: SecretPlace,
        detail: String,
    ): SecretPlaceDiscoveryEvaluation {
        val profile = SecretPlaceDiscoveryRequirementCatalog.profileFor(place.category)
        val statuses = profile.requirements.map { requirement ->
            SecretPlaceDiscoveryRequirementStatus(
                requirement = requirement,
                satisfied = false,
                progressFraction = 0f,
                detail = detail,
            )
        }
        return SecretPlaceDiscoveryEvaluation(place = place, statuses = statuses)
    }

    private fun evaluateRequirement(
        place: SecretPlace,
        context: SecretPlaceDiscoveryContext,
        requirement: SecretPlaceDiscoveryRequirement,
    ): SecretPlaceDiscoveryRequirementStatus = when (requirement) {
        is SecretPlaceDiscoveryRequirement.VisitOnce ->
            evaluateVisitOnce(requirement, context)
        is SecretPlaceDiscoveryRequirement.ReachLocation ->
            evaluateReachLocation(place, requirement, context)
        is SecretPlaceDiscoveryRequirement.CollectNearbyTreasures ->
            evaluateCollectNearbyTreasures(requirement, context)
        is SecretPlaceDiscoveryRequirement.SolveClueChain ->
            evaluateSolveClueChain(requirement, context)
        is SecretPlaceDiscoveryRequirement.ExplorerLevel ->
            evaluateExplorerLevel(place, requirement, context)
    }

    private fun evaluateVisitOnce(
        requirement: SecretPlaceDiscoveryRequirement.VisitOnce,
        context: SecretPlaceDiscoveryContext,
    ): SecretPlaceDiscoveryRequirementStatus {
        val satisfied = context.visitCount >= requirement.minVisits
        val progress = (context.visitCount.toFloat() / requirement.minVisits.toFloat())
            .coerceIn(0f, 1f)
        return SecretPlaceDiscoveryRequirementStatus(
            requirement = requirement,
            satisfied = satisfied,
            progressFraction = progress,
            detail = "${context.visitCount}/${requirement.minVisits} visits",
        )
    }

    private fun evaluateReachLocation(
        place: SecretPlace,
        requirement: SecretPlaceDiscoveryRequirement.ReachLocation,
        context: SecretPlaceDiscoveryContext,
    ): SecretPlaceDiscoveryRequirementStatus {
        val thresholdM = requirement.distanceThresholdM
            ?: minOf(SecretPlaceDiscoveryConfig.DEFAULT_DISTANCE_THRESHOLD_M, place.radiusMeters)
        val distanceM = ClusterGeoMath.distanceMeters(
            context.playerLat,
            context.playerLng,
            place.latitude,
            place.longitude,
        )
        val satisfied = distanceM <= thresholdM
        val progress = if (thresholdM <= 0.0) {
            1f
        } else {
            (1f - (distanceM / thresholdM).toFloat()).coerceIn(0f, 1f)
        }
        return SecretPlaceDiscoveryRequirementStatus(
            requirement = requirement,
            satisfied = satisfied,
            progressFraction = if (satisfied) 1f else progress,
            detail = "${distanceM.toInt()}m / ${thresholdM.toInt()}m",
        )
    }

    private fun evaluateCollectNearbyTreasures(
        requirement: SecretPlaceDiscoveryRequirement.CollectNearbyTreasures,
        context: SecretPlaceDiscoveryContext,
    ): SecretPlaceDiscoveryRequirementStatus {
        val satisfied = context.nearbyTreasuresCollected >= requirement.minCount
        val progress = (context.nearbyTreasuresCollected.toFloat() / requirement.minCount.toFloat())
            .coerceIn(0f, 1f)
        return SecretPlaceDiscoveryRequirementStatus(
            requirement = requirement,
            satisfied = satisfied,
            progressFraction = progress,
            detail = "${context.nearbyTreasuresCollected}/${requirement.minCount} treasures",
        )
    }

    private fun evaluateSolveClueChain(
        requirement: SecretPlaceDiscoveryRequirement.SolveClueChain,
        context: SecretPlaceDiscoveryContext,
    ): SecretPlaceDiscoveryRequirementStatus {
        val completed = context.clueProgressFor(requirement.clueChainKey)
        val satisfied = completed >= requirement.requiredSteps
        val progress = (completed.toFloat() / requirement.requiredSteps.toFloat()).coerceIn(0f, 1f)
        return SecretPlaceDiscoveryRequirementStatus(
            requirement = requirement,
            satisfied = satisfied,
            progressFraction = progress,
            detail = "$completed/${requirement.requiredSteps} clues",
        )
    }

    private fun evaluateExplorerLevel(
        place: SecretPlace,
        requirement: SecretPlaceDiscoveryRequirement.ExplorerLevel,
        context: SecretPlaceDiscoveryContext,
    ): SecretPlaceDiscoveryRequirementStatus {
        val required = requirement.minimumLevel ?: place.resolvedMinimumExplorerLevel
        val satisfied = SecretPlaceExplorerLevelGate.isAccessible(place, context.explorerLevel) &&
            context.explorerLevel >= required
        val progress = (context.explorerLevel.toFloat() / required.toFloat()).coerceIn(0f, 1f)
        return SecretPlaceDiscoveryRequirementStatus(
            requirement = requirement,
            satisfied = satisfied,
            progressFraction = if (satisfied) 1f else progress,
            detail = "level ${context.explorerLevel}/$required",
        )
    }
}
