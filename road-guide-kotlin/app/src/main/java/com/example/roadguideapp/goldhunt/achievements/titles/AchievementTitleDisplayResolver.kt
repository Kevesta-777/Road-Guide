package com.example.roadguideapp.goldhunt.achievements.titles

/**
 * Resolves the player-facing title shown on profile and future leaderboard surfaces.
 *
 * Achievement titles override the explorer rank title when one is active.
 */
internal object AchievementTitleDisplayResolver {
    fun resolvePublicDisplayTitle(
        activeTitleKey: String?,
        collection: AchievementTitleCollection,
        explorerRankTitle: String,
    ): String {
        val activeTitle = collection.activeEntry?.displayTitle
            ?: activeTitleKey?.let { key ->
                collection.unlockedEntries.firstOrNull { it.titleKey == key }?.displayTitle
                    ?: AchievementTitleCatalog.definitionForKey(key)?.displayTitle
            }
        return activeTitle?.takeIf { it.isNotBlank() } ?: explorerRankTitle
    }
}
