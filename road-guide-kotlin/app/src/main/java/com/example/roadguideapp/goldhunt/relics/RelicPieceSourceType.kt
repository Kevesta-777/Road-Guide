package com.example.roadguideapp.goldhunt.relics

/**
 * Provenance for how a relic piece is discovered.
 *
 * Maps to cross-domain hook keys in [RelicSchema] so secret places, achievements,
 * story fragments, and panorama hunts can grant individual shards later.
 */
internal enum class RelicPieceSourceType(val id: String) {
    SECRET_PLACE("secretPlace"),
    TREASURE_CLUSTER("treasureCluster"),
    STORY_FRAGMENT("storyFragment"),
    PANORAMA_HUNT("panoramaHunt"),
    SEASONAL_EVENT("seasonalEvent"),
    ACHIEVEMENT("achievement"),
    RADAR_DISCOVERY("radarDiscovery"),
    ;

    companion object {
        val ALL_ORDERED: List<RelicPieceSourceType> = listOf(
            SECRET_PLACE,
            TREASURE_CLUSTER,
            STORY_FRAGMENT,
            PANORAMA_HUNT,
            SEASONAL_EVENT,
            ACHIEVEMENT,
            RADAR_DISCOVERY,
        )

        fun fromId(id: String): RelicPieceSourceType? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
