package com.example.roadguideapp.goldhunt.secretplaces

/**
 * Instance rarity tier for a [SecretPlace] (offline tuning).
 *
 * [rarityWeight] drives future weighted rolls (higher = more common).
 * [rewardMultiplier] scales visit and completion payouts for this instance.
 */
internal enum class SecretPlaceRarity(
    val displayName: String,
    val rarityWeight: Int,
    val rewardMultiplier: Double,
    val tier: Int,
) {
    COMMON(
        displayName = "Common",
        rarityWeight = 500,
        rewardMultiplier = 1.0,
        tier = 1,
    ),
    UNCOMMON(
        displayName = "Uncommon",
        rarityWeight = 200,
        rewardMultiplier = 1.35,
        tier = 2,
    ),
    RARE(
        displayName = "Rare",
        rarityWeight = 80,
        rewardMultiplier = 1.75,
        tier = 3,
    ),
    EPIC(
        displayName = "Epic",
        rarityWeight = 25,
        rewardMultiplier = 2.25,
        tier = 4,
    ),
    LEGENDARY(
        displayName = "Legendary",
        rarityWeight = 8,
        rewardMultiplier = 3.0,
        tier = 5,
    ),
    ;

    val id: String get() = name

    companion object {
        val ALL_ORDERED: List<SecretPlaceRarity> = listOf(
            COMMON,
            UNCOMMON,
            RARE,
            EPIC,
            LEGENDARY,
        )

        fun fromId(id: String): SecretPlaceRarity? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }

        fun maxOf(a: SecretPlaceRarity, b: SecretPlaceRarity): SecretPlaceRarity =
            if (a.tier >= b.tier) a else b
    }

    init {
        require(rarityWeight > 0) { "$name rarityWeight must be positive" }
        require(rewardMultiplier > 0.0) { "$name rewardMultiplier must be positive" }
        require(tier > 0) { "$name tier must be positive" }
    }
}
