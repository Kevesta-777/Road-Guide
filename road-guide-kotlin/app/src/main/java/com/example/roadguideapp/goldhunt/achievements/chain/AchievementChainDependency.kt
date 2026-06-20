package com.example.roadguideapp.goldhunt.achievements.chain

/**
 * Extensible prerequisite definitions for achievement chain gates.
 * Multiple dependencies on one gate combine with AND semantics.
 */
internal sealed class AchievementChainDependency {
    abstract val type: AchievementChainDependencyType

    data class AchievementCompleted(
        val achievementKey: String,
    ) : AchievementChainDependency() {
        override val type: AchievementChainDependencyType =
            AchievementChainDependencyType.ACHIEVEMENT_COMPLETED

        init {
            require(achievementKey.isNotBlank()) { "achievementKey must not be blank" }
        }
    }
}
