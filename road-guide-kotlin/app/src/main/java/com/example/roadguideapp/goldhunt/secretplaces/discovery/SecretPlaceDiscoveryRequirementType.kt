package com.example.roadguideapp.goldhunt.secretplaces.discovery

/** Canonical discovery requirement families for secret places. */
internal enum class SecretPlaceDiscoveryRequirementType {
    VISIT_ONCE,
    REACH_LOCATION,
    COLLECT_NEARBY_TREASURES,
    SOLVE_CLUE_CHAIN,
    EXPLORER_LEVEL,
    ;

    val id: String get() = name

    companion object {
        fun fromId(id: String): SecretPlaceDiscoveryRequirementType? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }
}
