package com.example.roadguideapp.goldhunt.secretplaces.categories.assignment

import com.example.roadguideapp.goldhunt.treasure.TreasureHash

/**
 * Normalized local treasure density signal (0 = sparse, 1 = dense).
 *
 * For location-stable category assignment, prefer [fromGridCell] which derives density
 * from grid identifiers rather than live viewport counts.
 */
internal data class SecretPlaceTreasureDensity(
    val fraction: Double,
) {
    init {
        require(fraction in 0.0..1.0) { "fraction must be in [0, 1]" }
    }

    companion object {
        fun fromGridCell(l1CellId: String): SecretPlaceTreasureDensity {
            require(l1CellId.isNotBlank()) { "l1CellId must not be blank" }
            val roll = TreasureHash.unitFraction("$l1CellId|treasureDensity")
            return SecretPlaceTreasureDensity(fraction = roll)
        }

        fun fromFraction(fraction: Double): SecretPlaceTreasureDensity =
            SecretPlaceTreasureDensity(fraction = fraction.coerceIn(0.0, 1.0))
    }
}
