package com.example.roadguideapp.goldhunt.relics.chain

internal data class RelicHuntChainReward(
    val sourceRelicId: String,
    val unlocks: List<RelicHuntChainUnlock>,
) {
    init {
        require(sourceRelicId.isNotBlank()) { "sourceRelicId must not be blank" }
        require(unlocks.isNotEmpty()) { "unlocks must not be empty" }
    }
}
