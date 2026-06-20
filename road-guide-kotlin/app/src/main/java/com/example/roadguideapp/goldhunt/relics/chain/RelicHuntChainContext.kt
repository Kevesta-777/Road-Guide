package com.example.roadguideapp.goldhunt.relics.chain

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic

internal data class RelicHuntChainContext(
    val relicsById: Map<String, LegendaryRelic>,
    val chainUnlockedRelicIds: Set<String> = emptySet(),
    val completedAchievementKeys: Set<String> = emptySet(),
) {
    fun isRelicCompleted(relicId: String): Boolean =
        relicsById[relicId]?.completed == true

    fun isRelicAvailable(relicId: String): Boolean =
        isRelicCompleted(relicId) || relicId in chainUnlockedRelicIds
}
