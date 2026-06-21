package com.example.roadguideapp.goldhunt.relics.chain

/**
 * Extensible prerequisite definitions for relic hunt chain gates.
 * Multiple dependencies on one gate combine with AND semantics.
 */
internal sealed class RelicHuntChainDependency {
    abstract val type: RelicHuntChainDependencyType

    data class RelicCompleted(
        val relicId: String,
    ) : RelicHuntChainDependency() {
        override val type: RelicHuntChainDependencyType =
            RelicHuntChainDependencyType.RELIC_COMPLETED

        init {
            require(relicId.isNotBlank()) { "relicId must not be blank" }
        }
    }

    data class AchievementCompleted(
        val achievementKey: String,
    ) : RelicHuntChainDependency() {
        override val type: RelicHuntChainDependencyType =
            RelicHuntChainDependencyType.ACHIEVEMENT_COMPLETED

        init {
            require(achievementKey.isNotBlank()) { "achievementKey must not be blank" }
        }
    }
}
