package com.example.roadguideapp.goldhunt.relics.chain

internal enum class RelicHuntChainUnlockType(val id: String) {
    ACHIEVEMENT("achievement"),
    BADGE("badge"),
    TITLE("title"),
    LEGENDARY_RELIC("legendaryRelic"),
    STORY_FRAGMENT("storyFragment"),
    ;

    companion object {
        fun fromId(id: String): RelicHuntChainUnlockType? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
