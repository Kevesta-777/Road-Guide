package com.example.roadguideapp.goldhunt.secretplaces.discovery

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategoryCatalog

/**
 * Read-only discovery requirement profiles per [SecretPlaceCategory].
 */
internal object SecretPlaceDiscoveryRequirementCatalog {
    private val profiles: Map<SecretPlaceCategory, SecretPlaceDiscoveryRequirementProfile> =
        SecretPlaceCategoryCatalog.displayOrder.associateWith { category ->
            SecretPlaceDiscoveryRequirementProfile(
                category = category,
                requirements = requirementsFor(category),
            )
        }

    fun profileFor(category: SecretPlaceCategory): SecretPlaceDiscoveryRequirementProfile =
        profiles.getValue(category)

    private fun requirementsFor(
        category: SecretPlaceCategory,
    ): List<SecretPlaceDiscoveryRequirement> = when (category) {
        SecretPlaceCategory.NATURE_SANCTUARY -> listOf(
            SecretPlaceDiscoveryRequirement.VisitOnce(minVisits = 1),
        )
        SecretPlaceCategory.SCENIC_VIEWPOINT -> listOf(
            SecretPlaceDiscoveryRequirement.ReachLocation(),
        )
        SecretPlaceCategory.HISTORIC_LANDMARK -> listOf(
            SecretPlaceDiscoveryRequirement.CollectNearbyTreasures(
                minCount = 2,
                radiusM = SecretPlaceDiscoveryConfig.NEARBY_TREASURE_RADIUS_M,
            ),
        )
        SecretPlaceCategory.MYSTERY_ZONE -> listOf(
            SecretPlaceDiscoveryRequirement.SolveClueChain(
                clueChainKey = SecretPlaceDiscoveryRequirementSchema.ClueChainKeys.forCategory(category),
                requiredSteps = SecretPlaceDiscoveryConfig.MYSTERY_ZONE_CLUE_STEPS,
            ),
        )
        SecretPlaceCategory.ANCIENT_RELIC_SITE -> listOf(
            SecretPlaceDiscoveryRequirement.ReachLocation(),
            SecretPlaceDiscoveryRequirement.CollectNearbyTreasures(minCount = 1),
        )
        SecretPlaceCategory.EXPLORER_HIDEOUT -> listOf(
            SecretPlaceDiscoveryRequirement.VisitOnce(minVisits = 2),
            SecretPlaceDiscoveryRequirement.ReachLocation(),
        )
        SecretPlaceCategory.MYTHICAL_PLACE -> listOf(
            SecretPlaceDiscoveryRequirement.ExplorerLevel(),
            SecretPlaceDiscoveryRequirement.SolveClueChain(
                clueChainKey = SecretPlaceDiscoveryRequirementSchema.ClueChainKeys.forCategory(category),
                requiredSteps = SecretPlaceDiscoveryConfig.MYTHICAL_PLACE_CLUE_STEPS,
            ),
        )
        SecretPlaceCategory.LEGENDARY_SITE -> listOf(
            SecretPlaceDiscoveryRequirement.ExplorerLevel(),
            SecretPlaceDiscoveryRequirement.ReachLocation(
                distanceThresholdM = 50.0,
            ),
        )
    }
}
