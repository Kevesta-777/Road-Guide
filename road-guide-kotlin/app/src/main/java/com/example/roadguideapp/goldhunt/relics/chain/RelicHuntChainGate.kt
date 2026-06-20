package com.example.roadguideapp.goldhunt.relics.chain

internal data class RelicHuntChainGate(
    val relicId: String,
    val dependencies: List<RelicHuntChainDependency>,
) {
    init {
        require(relicId.isNotBlank()) { "relicId must not be blank" }
        require(dependencies.isNotEmpty()) { "dependencies must not be empty" }
    }
}
