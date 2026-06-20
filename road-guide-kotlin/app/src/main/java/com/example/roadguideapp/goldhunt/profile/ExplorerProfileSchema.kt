package com.example.roadguideapp.goldhunt.profile

/**
 * Schema metadata for [ExplorerProfileEntity].
 * Bump [VERSION] when adding columns; use [extensionJson] for experimental payloads first.
 */
internal object ExplorerProfileSchema {
    const val VERSION = 14
    const val SINGLETON_ID = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"
    const val DEFAULT_EXPLORER_LEVEL = 1

    /** Reserved JSON keys for future features (stored in [ExplorerProfileEntity.extensionJson]). */
    object ExtensionKeys {
        const val ACHIEVEMENTS = "achievements"
        const val RADAR = "radar"
        const val STORY_FRAGMENTS = "storyFragments"
        const val LEGENDARY_RELICS = "legendaryRelics"
        const val RELIC_CATEGORIES = "relicCategories"
        const val TREASURE_RARITY_STATS = "treasureRarityStats"
        const val TREASURE_ENCYCLOPEDIA = "treasureEncyclopedia"
        const val CLUSTER_ENCYCLOPEDIA = "clusterEncyclopedia"
        const val SECRET_PLACE_CATEGORY_STATS = "secretPlaceCategoryStats"
        const val SECRET_PLACE_COLLECTION_BOOK = "secretPlaceCollectionBook"
        const val PANORAMA_HUNT_STATS = "panoramaHuntStats"
        const val SEASONAL_EVENT_STATS = "seasonalEventStats"
        const val ACHIEVEMENT_STATS = "achievementStats"
        const val ACHIEVEMENT_TITLES = "achievementTitles"
        const val ACTIVE_ACHIEVEMENT_TITLE_KEY = "activeAchievementTitleKey"
        const val ACHIEVEMENT_COLLECTION_BOOK = "achievementCollectionBook"
    }
}
