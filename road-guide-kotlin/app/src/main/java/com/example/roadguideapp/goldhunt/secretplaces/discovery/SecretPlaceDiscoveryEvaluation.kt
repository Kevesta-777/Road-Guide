package com.example.roadguideapp.goldhunt.secretplaces.discovery

import com.example.roadguideapp.goldhunt.secretplaces.SecretPlace

internal data class SecretPlaceDiscoveryRequirementStatus(
    val requirement: SecretPlaceDiscoveryRequirement,
    val satisfied: Boolean,
    val progressFraction: Float,
    val detail: String? = null,
) {
    init {
        require(progressFraction in 0f..1f) { "progressFraction must be in [0, 1]" }
    }
}

internal data class SecretPlaceDiscoveryEvaluation(
    val place: SecretPlace,
    val statuses: List<SecretPlaceDiscoveryRequirementStatus>,
    val schemaVersion: Int = SecretPlaceDiscoveryRequirementSchema.VERSION,
) {
    val isDiscoverable: Boolean
        get() = statuses.isNotEmpty() && statuses.all { it.satisfied }

    val satisfiedCount: Int
        get() = statuses.count { it.satisfied }

    val progressFraction: Float
        get() = if (statuses.isEmpty()) {
            0f
        } else {
            statuses.sumOf { it.progressFraction.toDouble() }.toFloat() / statuses.size.toFloat()
        }

    fun statusFor(type: SecretPlaceDiscoveryRequirementType): SecretPlaceDiscoveryRequirementStatus? =
        statuses.firstOrNull { it.requirement.type == type }
}
