package com.example.roadguideapp.goldhunt.achievements.chain

internal enum class AchievementChainUnlockType(val id: String) {
    ACHIEVEMENT("achievement"),
    BADGE("badge"),
    TITLE("title"),
    LEGENDARY_RELIC("legendaryRelic"),
    STORY_FRAGMENT("storyFragment"),
    ;

    companion object {
        fun fromId(id: String): AchievementChainUnlockType? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
