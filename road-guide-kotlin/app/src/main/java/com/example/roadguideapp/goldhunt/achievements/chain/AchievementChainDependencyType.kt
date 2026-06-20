package com.example.roadguideapp.goldhunt.achievements.chain

internal enum class AchievementChainDependencyType(val id: String) {
    ACHIEVEMENT_COMPLETED("achievementCompleted"),
    ;

    companion object {
        fun fromId(id: String): AchievementChainDependencyType? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
