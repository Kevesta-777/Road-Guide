package com.example.roadguideapp.goldhunt.radar.direction

/**
 * Eight-point compass sector returned by the directional radar.
 * Targets are reported by bearing only — never by exact coordinates.
 */
internal enum class RadarCompassDirection(
    val displayName: String,
    val shortLabel: String,
    val sectorIndex: Int,
) {
    NORTH(
        displayName = "North",
        shortLabel = "N",
        sectorIndex = 0,
    ),
    NORTH_EAST(
        displayName = "North-East",
        shortLabel = "NE",
        sectorIndex = 1,
    ),
    EAST(
        displayName = "East",
        shortLabel = "E",
        sectorIndex = 2,
    ),
    SOUTH_EAST(
        displayName = "South-East",
        shortLabel = "SE",
        sectorIndex = 3,
    ),
    SOUTH(
        displayName = "South",
        shortLabel = "S",
        sectorIndex = 4,
    ),
    SOUTH_WEST(
        displayName = "South-West",
        shortLabel = "SW",
        sectorIndex = 5,
    ),
    WEST(
        displayName = "West",
        shortLabel = "W",
        sectorIndex = 6,
    ),
    NORTH_WEST(
        displayName = "North-West",
        shortLabel = "NW",
        sectorIndex = 7,
    ),
    ;

    val id: String get() = name

    companion object {
        val ALL_ORDERED: List<RadarCompassDirection> = entries.sortedBy { it.sectorIndex }

        fun fromSectorIndex(index: Int): RadarCompassDirection =
            ALL_ORDERED.firstOrNull { it.sectorIndex == index % 8 } ?: NORTH
    }
}
