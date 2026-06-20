package com.example.roadguideapp.goldhunt.relics.rewards

/**
 * Schema metadata for legendary relic completion rewards.
 *
 * Credits and XP use dedicated ledger event IDs; story fragments, titles, badges,
 * and future cosmetics use [HookTypes] with idempotent hook rows.
 */
internal object LegendaryRelicRewardSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object HookTypes {
        const val STORY_FRAGMENT = "storyFragment"
        const val TITLE = "title"
        const val BADGE = "badge"
        const val COSMETIC = "cosmetic"
        const val CHAIN_UNLOCK = "chainUnlock"
    }

    object EventPrefixes {
        const val GRANT = "legendary_relic_reward:grant"
        const val CREDITS = "legendary_relic_reward:credits"
        const val STORY_FRAGMENT = "legendary_relic_reward:story"
        const val TITLE = "legendary_relic_reward:title"
        const val BADGE = "legendary_relic_reward:badge"
        const val COSMETIC = "legendary_relic_reward:cosmetic"
    }

    /** Reserved JSON keys for future cosmetic reward payloads on grant rows. */
    object ExtensionKeys {
        const val COSMETIC_KEY = "cosmeticKey"
        const val COSMETIC_TYPE = "cosmeticType"
        const val COSMETIC_RECORDED = "cosmeticRecorded"
    }

    fun grantEventId(relicId: String): String = "$GRANT:$relicId"

    fun creditsEventId(relicId: String): String = "$CREDITS:$relicId"

    fun storyFragmentEventId(relicId: String): String = "$STORY_FRAGMENT:$relicId"

    fun titleEventId(relicId: String): String = "$TITLE:$relicId"

    fun badgeEventId(relicId: String): String = "$BADGE:$relicId"

    fun cosmeticEventId(relicId: String, cosmeticKey: String): String =
        "$COSMETIC:$relicId:$cosmeticKey"

    private const val GRANT = EventPrefixes.GRANT
    private const val CREDITS = EventPrefixes.CREDITS
    private const val STORY_FRAGMENT = EventPrefixes.STORY_FRAGMENT
    private const val TITLE = EventPrefixes.TITLE
    private const val BADGE = EventPrefixes.BADGE
    private const val COSMETIC = EventPrefixes.COSMETIC
}
