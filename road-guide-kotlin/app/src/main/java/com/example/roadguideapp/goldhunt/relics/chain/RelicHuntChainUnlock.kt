package com.example.roadguideapp.goldhunt.relics.chain

/**
 * Extensible unlock payload granted when a relic hunt chain source completes.
 */
internal sealed class RelicHuntChainUnlock {
    abstract val type: RelicHuntChainUnlockType

    data class Achievement(
        val achievementKey: String,
    ) : RelicHuntChainUnlock() {
        override val type: RelicHuntChainUnlockType = RelicHuntChainUnlockType.ACHIEVEMENT

        init {
            require(achievementKey.isNotBlank()) { "achievementKey must not be blank" }
        }
    }

    data class Badge(
        val badgeKey: String,
    ) : RelicHuntChainUnlock() {
        override val type: RelicHuntChainUnlockType = RelicHuntChainUnlockType.BADGE

        init {
            require(badgeKey.isNotBlank()) { "badgeKey must not be blank" }
        }
    }

    data class Title(
        val titleKey: String,
    ) : RelicHuntChainUnlock() {
        override val type: RelicHuntChainUnlockType = RelicHuntChainUnlockType.TITLE

        init {
            require(titleKey.isNotBlank()) { "titleKey must not be blank" }
        }
    }

    data class LegendaryRelic(
        val relicKey: String,
    ) : RelicHuntChainUnlock() {
        override val type: RelicHuntChainUnlockType = RelicHuntChainUnlockType.LEGENDARY_RELIC

        init {
            require(relicKey.isNotBlank()) { "relicKey must not be blank" }
        }
    }

    data class StoryFragment(
        val storyFragmentKey: String,
    ) : RelicHuntChainUnlock() {
        override val type: RelicHuntChainUnlockType = RelicHuntChainUnlockType.STORY_FRAGMENT

        init {
            require(storyFragmentKey.isNotBlank()) { "storyFragmentKey must not be blank" }
        }
    }
}
