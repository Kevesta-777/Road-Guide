package com.example.roadguideapp.goldhunt.secretplaces.discovery

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

/**
 * Offline discovery requirement set for a [SecretPlaceCategory].
 * All [requirements] must be satisfied (AND semantics).
 */
internal data class SecretPlaceDiscoveryRequirementProfile(
    val category: SecretPlaceCategory,
    val requirements: List<SecretPlaceDiscoveryRequirement>,
) {
    val requirementTypes: Set<SecretPlaceDiscoveryRequirementType>
        get() = requirements.map { it.type }.toSet()

    init {
        require(requirements.isNotEmpty()) { "requirements must not be empty" }
    }
}
