package com.example.roadguideapp.goldhunt.radar

/**
 * Gameplay families a radar pulse can surface.
 * [preferredRadarType] is the radar tuned for this category.
 */
internal enum class RadarTargetCategory(
    val displayName: String,
    val preferredRadarType: RadarType,
) {
    TREASURE(
        displayName = "Treasure",
        preferredRadarType = RadarType.TREASURE_RADAR,
    ),
    CLUSTER(
        displayName = "Cluster",
        preferredRadarType = RadarType.CLUSTER_RADAR,
    ),
    SECRET_PLACE(
        displayName = "Secret Place",
        preferredRadarType = RadarType.SECRET_PLACE_RADAR,
    ),
    STORY_FRAGMENT(
        displayName = "Story Fragment",
        preferredRadarType = RadarType.STORY_RADAR,
    ),
    LEGENDARY_RELIC(
        displayName = "Legendary Relic",
        preferredRadarType = RadarType.LEGENDARY_RADAR,
    ),
    ;

    val id: String get() = name

    companion object {
        val ALL_ORDERED: List<RadarTargetCategory> = listOf(
            TREASURE,
            CLUSTER,
            SECRET_PLACE,
            STORY_FRAGMENT,
            LEGENDARY_RELIC,
        )

        fun fromId(id: String): RadarTargetCategory? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }
}
