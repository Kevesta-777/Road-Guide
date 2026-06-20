package com.example.roadguideapp.goldhunt.profile.xp

/**
 * Canonical XP award sources. Persisted as [XpTransactionEntity.source] enum names.
 */
internal enum class XpSource {
    ROAD_DISCOVERY,
    AREA_DISCOVERY,
    TREASURE_COLLECTION,
    SECRET_PLACE,
    CLUSTER_COMPLETION,
    PANORAMA_HUNT_COMPLETION,
    RADAR_SCAN,
    SEASONAL_EVENT,
    ACHIEVEMENT,
    LEGENDARY_RELIC,
}
