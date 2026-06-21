package com.example.roadguideapp.goldhunt.achievements.chain

/**
 * Extensible unlock payload granted when a chain source achievement completes.
 */
internal sealed class AchievementChainUnlock {
    abstract val type: AchievementChainUnlockType

    data class Achievement(
        val achievementKey: String,
    ) : AchievementChainUnlock() {
        override val type: AchievementChainUnlockType = AchievementChainUnlockType.ACHIEVEMENT

        init {
            require(achievementKey.isNotBlank()) { "achievementKey must not be blank" }
        }
    }

    data class Badge(
        val badgeKey: String,
    ) : AchievementChainUnlock() {
        override val type: AchievementChainUnlockType = AchievementChainUnlockType.BADGE

        init {
            require(badgeKey.isNotBlank()) { "badgeKey must not be blank" }
        }
    }

    data class Title(
        val titleKey: String,
    ) : AchievementChainUnlock() {
        override val type: AchievementChainUnlockType = AchievementChainUnlockType.TITLE

        init {
            require(titleKey.isNotBlank()) { "titleKey must not be blank" }
        }
    }

    data class LegendaryRelic(
        val relicKey: String,
    ) : AchievementChainUnlock() {
        override val type: AchievementChainUnlockType = AchievementChainUnlockType.LEGENDARY_RELIC

        init {
            require(relicKey.isNotBlank()) { "relicKey must not be blank" }
        }
    }

    data class StoryFragment(
        val storyFragmentKey: String,
    ) : AchievementChainUnlock() {
        override val type: AchievementChainUnlockType = AchievementChainUnlockType.STORY_FRAGMENT

        init {
            require(storyFragmentKey.isNotBlank()) { "storyFragmentKey must not be blank" }
        }
    }
}
