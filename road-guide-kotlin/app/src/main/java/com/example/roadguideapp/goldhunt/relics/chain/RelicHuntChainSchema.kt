package com.example.roadguideapp.goldhunt.relics.chain

/**
 * Schema metadata for relic hunt chain dependencies and completion unlocks.
 *
 * Chain gates block relic progress until prerequisites are met. Chain rewards
 * grant additional unlocks when a source relic completes. Persist unlock state
 * via [com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardSchema.HookTypes.CHAIN_UNLOCK].
 */
internal object RelicHuntChainSchema {
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
        const val REQUIRED_RELIC_KEYS = "requiredRelicKeys"
        const val CHAIN_UNLOCK_KEYS = "chainUnlockKeys"
    }

    object KeyPrefixes {
        const val STORY_FRAGMENT = "story_fragment_relic_hunt_chain"
        const val LEGENDARY_RELIC = "legendary_relic_hunt_chain"
        const val BADGE = "badge_relic_hunt_chain"
        const val TITLE = "title_relic_hunt_chain"
    }

    fun chainUnlockEventId(
        sourceRelicId: String,
        unlockSubtype: String,
        hookKey: String,
    ): String = "relic_hunt_chain:unlock:$sourceRelicId:$unlockSubtype:$hookKey"
}
