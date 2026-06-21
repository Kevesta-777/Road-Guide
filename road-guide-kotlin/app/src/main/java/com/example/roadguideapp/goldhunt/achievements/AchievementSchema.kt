package com.example.roadguideapp.goldhunt.achievements

import com.example.roadguideapp.goldhunt.profile.ExplorerProfileSchema
import kotlin.math.roundToLong

/**
 * Schema metadata for the unified achievement foundation.
 *
 * Bump [VERSION] when adding columns or changing key formats. Use [extensionJson]
 * payloads on future achievement progress entities before adding Room columns.
 */
internal object AchievementSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    /** Reserved JSON keys for achievement progress extension payloads. */
    object ExtensionKeys {
        const val CATEGORY = "category"
        const val TIER = "tier"
        const val EXPLORER_LEVEL_AT_UNLOCK = "explorerLevelAtUnlock"
        const val STORY_FRAGMENT_KEY = "storyFragmentKey"
        const val LEGENDARY_RELIC_KEY = "legendaryRelicKey"
        const val RELIC_REWARD_KEY = "relicRewardKey"
        const val TITLE_KEY = "titleKey"
        const val BADGE_KEY = "badgeKey"
        const val DOMAIN_KEY = "domainKey"
        const val VISIBILITY = "visibility"
        const val REQUIRED_ACHIEVEMENT_KEYS = "requiredAchievementKeys"
        const val CHAIN_UNLOCK_KEYS = "chainUnlockKeys"
    }

    /**
     * Cross-profile extension buckets that achievement grants may update.
     * Mirrors [ExplorerProfileSchema.ExtensionKeys] for future wiring.
     */
    object ProfileExtensionBuckets {
        const val ACHIEVEMENTS = ExplorerProfileSchema.ExtensionKeys.ACHIEVEMENTS
        const val STORY_FRAGMENTS = ExplorerProfileSchema.ExtensionKeys.STORY_FRAGMENTS
        const val LEGENDARY_RELICS = ExplorerProfileSchema.ExtensionKeys.LEGENDARY_RELICS
    }

    /** Colon-delimited achievement key namespace for the unified catalog layer. */
    object AchievementKeys {
        const val PREFIX = "achievement"

        fun forCategory(category: AchievementCategory, action: String): String =
            "$PREFIX:${category.id.lowercase()}:$action"

        fun firstUnlock(category: AchievementCategory): String =
            forCategory(category, "first_unlock")

        fun collector(category: AchievementCategory): String =
            forCategory(category, "collector")

        fun mastery(category: AchievementCategory): String =
            forCategory(category, "mastery")
    }

    /** Count-milestone keys for scalable 100 / 500 / 1000+ achievement families. */
    object MilestoneKeys {
        fun forCategory(category: AchievementCategory, count: Int): String =
            "${AchievementKeys.PREFIX}:${category.id.lowercase()}:count_$count"
    }

    /**
     * Story-fragment hook namespace reserved for achievement completion rewards.
     * Domain packages keep their own fragment keys; this prefix unifies catalog lookup.
     */
    object StoryFragmentKeys {
        const val PREFIX = "story_fragment_achievement"

        fun forCategory(category: AchievementCategory): String =
            "${PREFIX}_${category.id.lowercase()}"
    }

    /**
     * Legendary-relic hook namespace reserved for achievement completion rewards.
     */
    object LegendaryRelicKeys {
        const val PREFIX = "legendary_relic_achievement"

        fun forCategory(category: AchievementCategory): String =
            "${PREFIX}_${category.id.lowercase()}"
    }

    /** Explorer title hook namespace reserved for achievement completion rewards. */
    object TitleKeys {
        const val PREFIX = "title_achievement"

        fun forCategory(category: AchievementCategory): String? = when (category) {
            AchievementCategory.MASTER_EXPLORER,
            AchievementCategory.LEGENDARY_RELIC,
            AchievementCategory.SEASONAL_EVENT,
            AchievementCategory.STREAK,
            -> "${PREFIX}_${category.id.lowercase()}"
            else -> null
        }
    }

    /** Badge hook namespace reserved for achievement completion rewards. */
    object BadgeKeys {
        const val PREFIX = "badge_achievement"

        fun forCategory(category: AchievementCategory): String? = when (category) {
            AchievementCategory.MASTER_EXPLORER,
            AchievementCategory.LEGENDARY_RELIC,
            AchievementCategory.PANORAMA,
            AchievementCategory.TREASURE_CLUSTER,
            -> "${PREFIX}_${category.id.lowercase()}"
            else -> null
        }
    }

    /** Maps each category to its existing domain achievement key prefix. */
    object DomainPrefixes {
        fun forCategory(category: AchievementCategory): String = when (category) {
            AchievementCategory.EXPLORATION -> "exploration"
            AchievementCategory.TREASURE -> "treasure"
            AchievementCategory.RARITY -> "treasure_rarity"
            AchievementCategory.SECRET_PLACE -> "secret_place_category"
            AchievementCategory.TREASURE_CLUSTER -> "cluster_encyclopedia"
            AchievementCategory.STORY -> "story_fragment"
            AchievementCategory.RADAR -> "radar_type"
            AchievementCategory.PANORAMA -> "panorama_hunt_type"
            AchievementCategory.SEASONAL_EVENT -> "seasonal_event"
            AchievementCategory.LEGENDARY_RELIC -> "legendary_relic"
            AchievementCategory.STREAK -> "streak"
            AchievementCategory.MASTER_EXPLORER -> "master_explorer"
        }
    }

    /** Per-category explorer level gates and reward-hook eligibility. */
    object CategoryGates {
        fun minExplorerLevel(category: AchievementCategory): Int = when (category) {
            AchievementCategory.EXPLORATION,
            AchievementCategory.TREASURE,
            -> 1
            AchievementCategory.RARITY,
            AchievementCategory.STORY,
            -> 3
            AchievementCategory.SECRET_PLACE,
            AchievementCategory.RADAR,
            -> 5
            AchievementCategory.TREASURE_CLUSTER,
            AchievementCategory.PANORAMA,
            -> 7
            AchievementCategory.SEASONAL_EVENT,
            AchievementCategory.STREAK,
            -> 10
            AchievementCategory.LEGENDARY_RELIC,
            -> 15
            AchievementCategory.MASTER_EXPLORER,
            -> 20
        }

        fun supportsStoryFragments(category: AchievementCategory): Boolean = when (category) {
            AchievementCategory.STORY,
            AchievementCategory.SECRET_PLACE,
            AchievementCategory.PANORAMA,
            AchievementCategory.SEASONAL_EVENT,
            AchievementCategory.TREASURE_CLUSTER,
            AchievementCategory.LEGENDARY_RELIC,
            AchievementCategory.MASTER_EXPLORER,
            -> true
            else -> false
        }

        fun supportsLegendaryRelics(category: AchievementCategory): Boolean = when (category) {
            AchievementCategory.LEGENDARY_RELIC,
            AchievementCategory.RARITY,
            AchievementCategory.SECRET_PLACE,
            AchievementCategory.TREASURE_CLUSTER,
            AchievementCategory.PANORAMA,
            AchievementCategory.SEASONAL_EVENT,
            AchievementCategory.MASTER_EXPLORER,
            -> true
            else -> false
        }
    }

    object RewardScaling {
        fun scaledCredits(category: AchievementCategory, baseCredits: Int): Int =
            (baseCredits * category.difficultyMultiplier).roundToLong().toInt().coerceAtLeast(0)

        fun scaledXp(category: AchievementCategory, baseXp: Long): Long =
            (baseXp * category.difficultyMultiplier).roundToLong().coerceAtLeast(0L)
    }
}
