package com.example.roadguideapp.goldhunt.treasure.rarity

/**
 * Canonical treasure rarity tiers (offline tuning).
 *
 * [rarityWeight] drives future weighted rolls (higher = more common).
 * [creditMultiplier] and [xpMultiplier] mirror [com.example.roadguideapp.goldhunt.treasure.rewards.TreasureRewardMultipliers].
 *
 * Nullable visual keys are reserved for future icon effects, animations, radar highlights, and achievements.
 */
internal enum class TreasureRarity(
    val displayName: String,
    val rarityWeight: Int,
    val creditMultiplier: Double,
    val xpMultiplier: Double,
    val tier: Int,
    val iconEffectKey: String? = null,
    val animationKey: String? = null,
    val radarHighlightKey: String? = null,
    val achievementKey: String? = null,
) {
    COMMON(
        displayName = "Common",
        rarityWeight = 500,
        creditMultiplier = 1.0,
        xpMultiplier = 1.0,
        tier = 1,
    ),
    UNCOMMON(
        displayName = "Uncommon",
        rarityWeight = 200,
        creditMultiplier = 1.5,
        xpMultiplier = 1.5,
        tier = 2,
    ),
    RARE(
        displayName = "Rare",
        rarityWeight = 80,
        creditMultiplier = 3.0,
        xpMultiplier = 3.0,
        tier = 3,
    ),
    EPIC(
        displayName = "Epic",
        rarityWeight = 25,
        creditMultiplier = 5.0,
        xpMultiplier = 5.0,
        tier = 4,
    ),
    LEGENDARY(
        displayName = "Legendary",
        rarityWeight = 8,
        creditMultiplier = 10.0,
        xpMultiplier = 10.0,
        tier = 5,
    ),
    MYTHIC(
        displayName = "Mythic",
        rarityWeight = 2,
        creditMultiplier = 25.0,
        xpMultiplier = 25.0,
        tier = 6,
    ),
    ;

    val id: String get() = name

    companion object {
        /** Lowest to highest [tier]. */
        val ALL_ORDERED: List<TreasureRarity> = entries.sortedBy { it.tier }

        val TOTAL_RARITY_WEIGHT: Int = entries.sumOf { it.rarityWeight }

        fun fromId(id: String): TreasureRarity? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }

        fun maxOf(a: TreasureRarity, b: TreasureRarity): TreasureRarity =
            if (a.tier >= b.tier) a else b
    }
}
