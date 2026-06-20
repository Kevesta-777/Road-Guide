package com.example.roadguideapp.goldhunt.secretplaces.discovery

/**
 * Flexible discovery requirement definitions.
 * Profiles combine multiple requirements with AND semantics.
 */
internal sealed class SecretPlaceDiscoveryRequirement {
    abstract val type: SecretPlaceDiscoveryRequirementType

    /** Player must enter the place radius at least [minVisits] times. */
    data class VisitOnce(
        val minVisits: Int = 1,
    ) : SecretPlaceDiscoveryRequirement() {
        override val type: SecretPlaceDiscoveryRequirementType =
            SecretPlaceDiscoveryRequirementType.VISIT_ONCE

        init {
            require(minVisits > 0) { "minVisits must be positive" }
        }
    }

    /** Player must be within discovery distance of the anchor. */
    data class ReachLocation(
        val distanceThresholdM: Double? = null,
    ) : SecretPlaceDiscoveryRequirement() {
        override val type: SecretPlaceDiscoveryRequirementType =
            SecretPlaceDiscoveryRequirementType.REACH_LOCATION

        init {
            require(distanceThresholdM == null || distanceThresholdM > 0.0) {
                "distanceThresholdM must be positive when set"
            }
        }
    }

    /** Player must collect treasures near the place before discovery unlocks. */
    data class CollectNearbyTreasures(
        val minCount: Int,
        val radiusM: Double? = null,
    ) : SecretPlaceDiscoveryRequirement() {
        override val type: SecretPlaceDiscoveryRequirementType =
            SecretPlaceDiscoveryRequirementType.COLLECT_NEARBY_TREASURES

        init {
            require(minCount > 0) { "minCount must be positive" }
            require(radiusM == null || radiusM > 0.0) { "radiusM must be positive when set" }
        }
    }

    /** Player must complete a clue chain (future narrative system hook). */
    data class SolveClueChain(
        val clueChainKey: String,
        val requiredSteps: Int,
    ) : SecretPlaceDiscoveryRequirement() {
        override val type: SecretPlaceDiscoveryRequirementType =
            SecretPlaceDiscoveryRequirementType.SOLVE_CLUE_CHAIN

        init {
            require(clueChainKey.isNotBlank()) { "clueChainKey must not be blank" }
            require(requiredSteps > 0) { "requiredSteps must be positive" }
        }
    }

    /** Player must meet a minimum explorer level. */
    data class ExplorerLevel(
        val minimumLevel: Int? = null,
    ) : SecretPlaceDiscoveryRequirement() {
        override val type: SecretPlaceDiscoveryRequirementType =
            SecretPlaceDiscoveryRequirementType.EXPLORER_LEVEL

        init {
            require(minimumLevel == null || minimumLevel > 0) {
                "minimumLevel must be positive when set"
            }
        }
    }
}
