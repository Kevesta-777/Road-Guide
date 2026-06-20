package com.example.roadguideapp.goldhunt.achievements.rewards

/**
 * Schema metadata for unified achievement completion rewards.
 *
 * Credits and XP use dedicated ledger event IDs; story fragments, relic progress,
 * titles, badges, and future cosmetics use [HookTypes] with idempotent hook rows.
 */
internal object AchievementRewardSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object HookTypes {
        const val STORY_FRAGMENT = "storyFragment"
        const val LEGENDARY_RELIC = "legendaryRelic"
        const val TITLE = "title"
        const val BADGE = "badge"
        const val COSMETIC = "cosmetic"
        const val CHAIN_UNLOCK = "chainUnlock"
    }

    object EventPrefixes {
        const val GRANT = "achievement_reward:grant"
        const val CREDITS = "achievement_reward:credits"
        const val STORY_FRAGMENT = "achievement_reward:story"
        const val LEGENDARY_RELIC = "achievement_reward:relic"
        const val TITLE = "achievement_reward:title"
        const val BADGE = "achievement_reward:badge"
        const val COSMETIC = "achievement_reward:cosmetic"
    }

    /** Reserved JSON keys for future cosmetic reward payloads on grant rows. */
    object ExtensionKeys {
        const val COSMETIC_KEY = "cosmeticKey"
        const val COSMETIC_TYPE = "cosmeticType"
        const val COSMETIC_RECORDED = "cosmeticRecorded"
    }

    fun grantEventId(achievementId: String): String =
        "$GRANT:$achievementId"

    fun creditsEventId(achievementId: String): String =
        "$CREDITS:$achievementId"

    fun storyFragmentEventId(achievementId: String): String =
        "$STORY_FRAGMENT:$achievementId"

    fun legendaryRelicEventId(achievementId: String): String =
        "$LEGENDARY_RELIC:$achievementId"

    fun titleEventId(achievementId: String): String =
        "$TITLE:$achievementId"

    fun badgeEventId(achievementId: String): String =
        "$BADGE:$achievementId"

    fun cosmeticEventId(achievementId: String, cosmeticKey: String): String =
        "$COSMETIC:$achievementId:$cosmeticKey"

    private const val GRANT = EventPrefixes.GRANT
    private const val CREDITS = EventPrefixes.CREDITS
    private const val STORY_FRAGMENT = EventPrefixes.STORY_FRAGMENT
    private const val LEGENDARY_RELIC = EventPrefixes.LEGENDARY_RELIC
    private const val TITLE = EventPrefixes.TITLE
    private const val BADGE = EventPrefixes.BADGE
    private const val COSMETIC = EventPrefixes.COSMETIC
}
