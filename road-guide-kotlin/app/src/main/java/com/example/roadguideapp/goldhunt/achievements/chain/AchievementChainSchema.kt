package com.example.roadguideapp.goldhunt.achievements.chain

/**
 * Schema metadata for achievement chain dependencies and completion unlocks.
 *
 * Chain gates block progress until prerequisites are met. Chain rewards grant
 * additional unlocks (achievements, badges, titles, relic progress, story
 * fragments) when a source achievement completes. Persist unlock state via
 * [com.example.roadguideapp.goldhunt.achievements.rewards.AchievementRewardSchema.HookTypes.CHAIN_UNLOCK].
 */
internal object AchievementChainSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object HookSubtypes {
        const val ACHIEVEMENT = "achievement"
        const val BADGE = "badge"
        const val TITLE = "title"
        const val LEGENDARY_RELIC = "legendaryRelic"
        const val STORY_FRAGMENT = "storyFragment"
    }

    object ExtensionKeys {
        const val REQUIRED_ACHIEVEMENT_KEYS = "requiredAchievementKeys"
        const val CHAIN_UNLOCK_KEYS = "chainUnlockKeys"
    }

    object KeyPrefixes {
        const val STORY_FRAGMENT = "story_fragment_chain"
        const val LEGENDARY_RELIC = "legendary_relic_chain"
        const val BADGE = "badge_chain"
        const val TITLE = "title_chain"
    }

    fun chainUnlockEventId(
        sourceAchievementId: String,
        unlockSubtype: String,
        hookKey: String,
    ): String = "achievement_chain:unlock:$sourceAchievementId:$unlockSubtype:$hookKey"
}
