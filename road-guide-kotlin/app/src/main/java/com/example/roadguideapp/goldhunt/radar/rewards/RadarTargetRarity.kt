package com.example.roadguideapp.goldhunt.radar.rewards

/**
 * Rarity tier used to scale radar pulse rewards (offline tuning).
 */
internal enum class RadarTargetRarity(
    val displayName: String,
    val tier: Int,
    val creditMultiplier: Double,
    val xpMultiplier: Double,
) {
    COMMON(
        displayName = "Common",
        tier = 1,
        creditMultiplier = 1.0,
        xpMultiplier = 1.0,
    ),
    UNCOMMON(
        displayName = "Uncommon",
        tier = 2,
        creditMultiplier = 1.5,
        xpMultiplier = 1.5,
    ),
    RARE(
        displayName = "Rare",
        tier = 3,
        creditMultiplier = 2.5,
        xpMultiplier = 2.5,
    ),
    EPIC(
        displayName = "Epic",
        tier = 4,
        creditMultiplier = 4.0,
        xpMultiplier = 4.0,
    ),
    LEGENDARY(
        displayName = "Legendary",
        tier = 5,
        creditMultiplier = 6.0,
        xpMultiplier = 6.0,
    ),
    MYTHIC(
        displayName = "Mythic",
        tier = 6,
        creditMultiplier = 10.0,
        xpMultiplier = 10.0,
    ),
    ;

    val id: String get() = name

    companion object {
        val ALL_ORDERED: List<RadarTargetRarity> = entries.sortedBy { it.tier }

        fun fromTier(tier: Int): RadarTargetRarity =
            ALL_ORDERED.firstOrNull { it.tier == tier } ?: COMMON
    }
}
