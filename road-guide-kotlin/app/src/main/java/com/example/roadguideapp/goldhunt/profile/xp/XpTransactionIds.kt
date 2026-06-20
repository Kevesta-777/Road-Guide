package com.example.roadguideapp.goldhunt.profile.xp

/** Stable, idempotent XP ledger keys (one award per discovery target). */
internal object XpTransactionIds {
    fun roadDiscovery(roadKey: String): String = "xp:road:$roadKey"
    fun areaDiscovery(districtKey: String): String = "xp:area:$districtKey"
    fun treasureCollection(treasureId: String): String = "xp:treasure:$treasureId"
    fun secretPlace(secretPlaceId: String): String = "xp:secret:$secretPlaceId"
    fun clusterCompletion(clusterId: String): String = "xp:cluster_complete:$clusterId"
    fun panoramaHuntCompletion(huntId: String): String = "xp:panorama_hunt_complete:$huntId"
    fun seasonalEventTreasure(treasureId: String): String = "xp:seasonal_event_treasure:$treasureId"
    fun seasonalEventCompletion(eventId: String): String = "xp:seasonal_event_completion:$eventId"
    fun achievementCompletion(achievementId: String): String = "xp:achievement:$achievementId"
    fun legendaryRelicCompletion(relicId: String): String = "xp:legendary_relic:$relicId"
}
