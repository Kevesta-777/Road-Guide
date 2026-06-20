package com.example.roadguideapp.goldhunt.secretplaces.categories.assignment

import com.example.roadguideapp.goldhunt.treasure.TreasureHash

/**
 * Grid-stable exploration signals used to bias category difficulty.
 *
 * These values are derived from cell identifiers (not live player DB state) so the
 * same anchor coordinates always receive the same category assignment.
 */
internal data class SecretPlaceExplorationProgress(
    val l1Signal: Double,
    val regionSignal: Double,
) {
    val combinedSignal: Double
        get() = ((l1Signal * 0.6) + (regionSignal * 0.4)).coerceIn(0.0, 1.0)

    init {
        require(l1Signal in 0.0..1.0) { "l1Signal must be in [0, 1]" }
        require(regionSignal in 0.0..1.0) { "regionSignal must be in [0, 1]" }
    }

    companion object {
        fun fromGridCells(
            l1CellId: String,
            l2CellId: String,
            playRegionId: String,
        ): SecretPlaceExplorationProgress {
            require(l1CellId.isNotBlank()) { "l1CellId must not be blank" }
            require(l2CellId.isNotBlank()) { "l2CellId must not be blank" }
            require(playRegionId.isNotBlank()) { "playRegionId must not be blank" }
            val l1Signal = TreasureHash.unitFraction("$playRegionId|$l1CellId|explorationL1")
            val regionSignal = TreasureHash.unitFraction("$playRegionId|$l2CellId|explorationRegion")
            return SecretPlaceExplorationProgress(
                l1Signal = l1Signal,
                regionSignal = regionSignal,
            )
        }

        fun fromSignals(l1Signal: Double, regionSignal: Double): SecretPlaceExplorationProgress =
            SecretPlaceExplorationProgress(
                l1Signal = l1Signal.coerceIn(0.0, 1.0),
                regionSignal = regionSignal.coerceIn(0.0, 1.0),
            )
    }
}
