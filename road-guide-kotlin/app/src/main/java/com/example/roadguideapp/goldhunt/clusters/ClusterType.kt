package com.example.roadguideapp.goldhunt.clusters

/**
 * Canonical treasure cluster families (offline tuning).
 *
 * [rarityWeight] drives future weighted spawn rolls (higher = more common).
 * [minimumTreasureCount] and [maximumTreasureCount] bound procedural fill for a cluster instance.
 * [rewardMultiplier] scales base rewards for treasures collected inside the cluster.
 *
 * Nullable keys reserve hooks for achievements, story fragments, radar, and seasonal events.
 * Generation, UI, and persistence are implemented elsewhere.
 */
internal enum class ClusterType(
    val displayName: String,
    val rarityWeight: Int,
    val minimumTreasureCount: Int,
    val maximumTreasureCount: Int,
    val rewardMultiplier: Double,
    val tier: Int,
    val achievementKey: String? = null,
    val storyFragmentKey: String? = null,
    val radarHighlightKey: String? = null,
    val seasonalEventKey: String? = null,
) {
    ROADSIDE_CACHE(
        displayName = "Roadside Cache",
        rarityWeight = 420,
        minimumTreasureCount = 1,
        maximumTreasureCount = 2,
        rewardMultiplier = 1.0,
        tier = 1,
    ),
    EXPLORER_NEST(
        displayName = "Explorer Nest",
        rarityWeight = 220,
        minimumTreasureCount = 2,
        maximumTreasureCount = 4,
        rewardMultiplier = 1.25,
        tier = 2,
    ),
    TREASURE_GARDEN(
        displayName = "Treasure Garden",
        rarityWeight = 110,
        minimumTreasureCount = 3,
        maximumTreasureCount = 6,
        rewardMultiplier = 1.5,
        tier = 3,
    ),
    ANCIENT_VAULT(
        displayName = "Ancient Vault",
        rarityWeight = 45,
        minimumTreasureCount = 4,
        maximumTreasureCount = 8,
        rewardMultiplier = 1.75,
        tier = 4,
        achievementKey = "cluster_ancient_vault",
        storyFragmentKey = "story_fragment_ancient_vault",
    ),
    LEGENDARY_HOARD(
        displayName = "Legendary Hoard",
        rarityWeight = 15,
        minimumTreasureCount = 6,
        maximumTreasureCount = 12,
        rewardMultiplier = 2.5,
        tier = 5,
        achievementKey = "cluster_legendary_hoard",
        storyFragmentKey = "story_fragment_legendary_hoard",
        radarHighlightKey = "radar_legendary_hoard",
    ),
    ;

    /** Stable persistence / catalog identifier (enum name). */
    val id: String get() = name

    companion object {
        /** Display and catalog order from common to legendary. */
        val ALL_ORDERED: List<ClusterType> = listOf(
            ROADSIDE_CACHE,
            EXPLORER_NEST,
            TREASURE_GARDEN,
            ANCIENT_VAULT,
            LEGENDARY_HOARD,
        )

        fun fromId(id: String): ClusterType? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }

    init {
        require(minimumTreasureCount > 0) {
            "$name minimumTreasureCount must be positive"
        }
        require(maximumTreasureCount >= minimumTreasureCount) {
            "$name maximumTreasureCount must be >= minimumTreasureCount"
        }
        require(rarityWeight > 0) {
            "$name rarityWeight must be positive"
        }
        require(rewardMultiplier > 0.0) {
            "$name rewardMultiplier must be positive"
        }
        require(tier > 0) {
            "$name tier must be positive"
        }
    }
}
