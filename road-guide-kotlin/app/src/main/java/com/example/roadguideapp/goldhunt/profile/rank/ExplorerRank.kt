package com.example.roadguideapp.goldhunt.profile.rank

import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator

/**
 * Display metadata for an explorer rank tier.
 *
 * [iconResName], [primaryColorArgb], and [badgeResName] are reserved for future UI theming;
 * they are null until assets and palette entries are wired.
 */
internal data class ExplorerRank(
    val id: String,
    val title: String,
    val minLevel: Int,
    val tier: Int,
    val iconResName: String? = null,
    val primaryColorArgb: Int? = null,
    val badgeResName: String? = null,
) {
    companion object {
        val RoadWanderer = ExplorerRank(
            id = "road_wanderer",
            title = "Road Wanderer",
            minLevel = 1,
            tier = 1,
        )
        val TrailFinder = ExplorerRank(
            id = "trail_finder",
            title = "Trail Finder",
            minLevel = 5,
            tier = 2,
        )
        val Explorer = ExplorerRank(
            id = "explorer",
            title = "Explorer",
            minLevel = 10,
            tier = 3,
        )
        val TreasureHunter = ExplorerRank(
            id = "treasure_hunter",
            title = "Treasure Hunter",
            minLevel = 15,
            tier = 4,
        )
        val SecretSeeker = ExplorerRank(
            id = "secret_seeker",
            title = "Secret Seeker",
            minLevel = 20,
            tier = 5,
        )
        val MasterExplorer = ExplorerRank(
            id = "master_explorer",
            title = "Master Explorer",
            minLevel = 30,
            tier = 6,
        )
        val LegendHunter = ExplorerRank(
            id = "legend_hunter",
            title = "Legend Hunter",
            minLevel = 40,
            tier = 7,
        )
        val GoldHuntChampion = ExplorerRank(
            id = "gold_hunt_champion",
            title = "Gold Hunt Champion",
            minLevel = 50,
            tier = 8,
        )

        /** Ordered from lowest to highest [minLevel]. */
        val ALL: List<ExplorerRank> = listOf(
            RoadWanderer,
            TrailFinder,
            Explorer,
            TreasureHunter,
            SecretSeeker,
            MasterExplorer,
            LegendHunter,
            GoldHuntChampion,
        )
    }
}

/**
 * Returns the highest rank whose [ExplorerRank.minLevel] is less than or equal to [level].
 * Levels below 1 clamp to [ExplorerRank.RoadWanderer]; levels above the top tier keep
 * [ExplorerRank.GoldHuntChampion].
 */
internal fun rankForLevel(level: Int): ExplorerRank {
    val clamped = level.coerceAtLeast(ExplorerLevelCalculator.MIN_LEVEL)
    return ExplorerRank.ALL.lastOrNull { it.minLevel <= clamped } ?: ExplorerRank.RoadWanderer
}

/**
 * Rank immediately above [rank], or null when [rank] is already the highest tier.
 */
internal fun nextRankAfter(rank: ExplorerRank): ExplorerRank? =
    ExplorerRank.ALL.firstOrNull { it.tier == rank.tier + 1 }
