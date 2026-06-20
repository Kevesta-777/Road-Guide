package com.example.roadguideapp.goldhunt.achievements

/**
 * Top-level achievement families for the Gold Hunt progression system.
 *
 * Each category defines presentation metadata ([displayName], [iconKey]) and a
 * [difficultyMultiplier] used to scale XP/credit rewards for achievements in
 * that family. Domain-specific achievement keys (treasure rarity, seasonal events,
 * secret places, etc.) remain in their feature packages; this enum is the unified
 * catalog layer for UI grouping, reward scaling, and future cross-domain progress.
 *
 * Explorer level gates, story-fragment hooks, and legendary-relic hooks are
 * resolved in [AchievementSchema] so categories stay lightweight.
 */
internal enum class AchievementCategory(
    val displayName: String,
    val iconKey: String,
    val difficultyMultiplier: Double,
) {
    EXPLORATION(
        displayName = "Exploration",
        iconKey = "achievement_exploration",
        difficultyMultiplier = 1.0,
    ),
    TREASURE(
        displayName = "Treasure",
        iconKey = "achievement_treasure",
        difficultyMultiplier = 1.0,
    ),
    RARITY(
        displayName = "Rarity",
        iconKey = "achievement_rarity",
        difficultyMultiplier = 1.15,
    ),
    SECRET_PLACE(
        displayName = "Secret Place",
        iconKey = "achievement_secret_place",
        difficultyMultiplier = 1.2,
    ),
    TREASURE_CLUSTER(
        displayName = "Treasure Cluster",
        iconKey = "achievement_treasure_cluster",
        difficultyMultiplier = 1.25,
    ),
    STORY(
        displayName = "Story",
        iconKey = "achievement_story",
        difficultyMultiplier = 1.1,
    ),
    RADAR(
        displayName = "Radar",
        iconKey = "achievement_radar",
        difficultyMultiplier = 1.15,
    ),
    PANORAMA(
        displayName = "Panorama",
        iconKey = "achievement_panorama",
        difficultyMultiplier = 1.3,
    ),
    SEASONAL_EVENT(
        displayName = "Seasonal Event",
        iconKey = "achievement_seasonal_event",
        difficultyMultiplier = 1.35,
    ),
    LEGENDARY_RELIC(
        displayName = "Legendary Relic",
        iconKey = "achievement_legendary_relic",
        difficultyMultiplier = 1.5,
    ),
    STREAK(
        displayName = "Streak",
        iconKey = "achievement_streak",
        difficultyMultiplier = 1.4,
    ),
    MASTER_EXPLORER(
        displayName = "Master Explorer",
        iconKey = "achievement_master_explorer",
        difficultyMultiplier = 2.0,
    ),
    ;

    val id: String get() = name

    companion object {
        val ALL_ORDERED: List<AchievementCategory> = listOf(
            EXPLORATION,
            TREASURE,
            RARITY,
            SECRET_PLACE,
            TREASURE_CLUSTER,
            STORY,
            RADAR,
            PANORAMA,
            SEASONAL_EVENT,
            LEGENDARY_RELIC,
            STREAK,
            MASTER_EXPLORER,
        )

        fun fromId(id: String): AchievementCategory? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }

    init {
        require(displayName.isNotBlank()) { "$name displayName must not be blank" }
        require(iconKey.isNotBlank()) { "$name iconKey must not be blank" }
        require(difficultyMultiplier > 0.0) { "$name difficultyMultiplier must be positive" }
    }
}
