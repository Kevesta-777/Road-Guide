package com.example.roadguideapp.goldhunt.relics

/**
 * Rarity tier for individual legendary relic instances.
 *
 * Distinct from [RelicCategory] family gates; two relics in the same category
 * may differ in rarity. [rewardMultiplier] scales completion payouts.
 */
internal enum class RelicRarity(
    val id: String,
    val displayName: String,
    val rewardMultiplier: Double,
) {
    COMMON(
        id = "common",
        displayName = "Common",
        rewardMultiplier = 1.0,
    ),
    RARE(
        id = "rare",
        displayName = "Rare",
        rewardMultiplier = 1.35,
    ),
    EPIC(
        id = "epic",
        displayName = "Epic",
        rewardMultiplier = 1.75,
    ),
    LEGENDARY(
        id = "legendary",
        displayName = "Legendary",
        rewardMultiplier = 2.25,
    ),
    ;

    companion object {
        val ALL_ORDERED: List<RelicRarity> = listOf(COMMON, RARE, EPIC, LEGENDARY)

        fun fromId(id: String): RelicRarity? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }

        fun forCategory(category: RelicCategory): RelicRarity = when (category) {
            RelicCategory.EXPLORER,
            RelicCategory.STORY,
            -> COMMON
            RelicCategory.SEASONAL_EVENT,
            RelicCategory.WARRIOR,
            -> RARE
            RelicCategory.MYTHICAL,
            RelicCategory.ANCIENT_CIVILIZATION,
            -> EPIC
            RelicCategory.ROYAL,
            RelicCategory.HIDDEN,
            -> LEGENDARY
        }
    }

    init {
        require(rewardMultiplier > 0.0) { "$name rewardMultiplier must be positive" }
    }
}
