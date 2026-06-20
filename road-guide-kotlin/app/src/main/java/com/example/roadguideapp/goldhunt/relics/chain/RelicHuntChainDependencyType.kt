package com.example.roadguideapp.goldhunt.relics.chain

internal enum class RelicHuntChainDependencyType(val id: String) {
    RELIC_COMPLETED("relicCompleted"),
    ACHIEVEMENT_COMPLETED("achievementCompleted"),
    ;

    companion object {
        fun fromId(id: String): RelicHuntChainDependencyType? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
