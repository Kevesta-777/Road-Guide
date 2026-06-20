package com.example.roadguideapp.goldhunt.secretplaces.statistics

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

/**
 * Lifetime secret-place category statistics persisted on the explorer profile.
 */
internal data class SecretPlaceCategoryStatistics(
    val discoveredCount: Int = 0,
    val completedCount: Int = 0,
    val creditsEarned: Int = 0,
    val xpEarned: Long = 0L,
    val bestCategory: SecretPlaceCategory? = null,
    val rarestCategory: SecretPlaceCategory? = null,
    val natureSanctuariesFound: Int = 0,
    val scenicViewpointsFound: Int = 0,
    val historicLandmarksFound: Int = 0,
    val mysteryZonesFound: Int = 0,
    val ancientRelicSitesFound: Int = 0,
    val explorerHideoutsFound: Int = 0,
    val mythicalPlacesFound: Int = 0,
    val legendarySitesFound: Int = 0,
    val natureSanctuariesCompleted: Int = 0,
    val scenicViewpointsCompleted: Int = 0,
    val historicLandmarksCompleted: Int = 0,
    val mysteryZonesCompleted: Int = 0,
    val ancientRelicSitesCompleted: Int = 0,
    val explorerHideoutsCompleted: Int = 0,
    val mythicalPlacesCompleted: Int = 0,
    val legendarySitesCompleted: Int = 0,
) {
    fun discoveredCountFor(category: SecretPlaceCategory): Int = when (category) {
        SecretPlaceCategory.NATURE_SANCTUARY -> natureSanctuariesFound
        SecretPlaceCategory.SCENIC_VIEWPOINT -> scenicViewpointsFound
        SecretPlaceCategory.HISTORIC_LANDMARK -> historicLandmarksFound
        SecretPlaceCategory.MYSTERY_ZONE -> mysteryZonesFound
        SecretPlaceCategory.ANCIENT_RELIC_SITE -> ancientRelicSitesFound
        SecretPlaceCategory.EXPLORER_HIDEOUT -> explorerHideoutsFound
        SecretPlaceCategory.MYTHICAL_PLACE -> mythicalPlacesFound
        SecretPlaceCategory.LEGENDARY_SITE -> legendarySitesFound
    }

    fun completedCountFor(category: SecretPlaceCategory): Int = when (category) {
        SecretPlaceCategory.NATURE_SANCTUARY -> natureSanctuariesCompleted
        SecretPlaceCategory.SCENIC_VIEWPOINT -> scenicViewpointsCompleted
        SecretPlaceCategory.HISTORIC_LANDMARK -> historicLandmarksCompleted
        SecretPlaceCategory.MYSTERY_ZONE -> mysteryZonesCompleted
        SecretPlaceCategory.ANCIENT_RELIC_SITE -> ancientRelicSitesCompleted
        SecretPlaceCategory.EXPLORER_HIDEOUT -> explorerHideoutsCompleted
        SecretPlaceCategory.MYTHICAL_PLACE -> mythicalPlacesCompleted
        SecretPlaceCategory.LEGENDARY_SITE -> legendarySitesCompleted
    }

    fun withDiscovery(category: SecretPlaceCategory): SecretPlaceCategoryStatistics {
        val next = copy(
            discoveredCount = discoveredCount + 1,
            natureSanctuariesFound = incrementIf(category, SecretPlaceCategory.NATURE_SANCTUARY, natureSanctuariesFound),
            scenicViewpointsFound = incrementIf(category, SecretPlaceCategory.SCENIC_VIEWPOINT, scenicViewpointsFound),
            historicLandmarksFound = incrementIf(category, SecretPlaceCategory.HISTORIC_LANDMARK, historicLandmarksFound),
            mysteryZonesFound = incrementIf(category, SecretPlaceCategory.MYSTERY_ZONE, mysteryZonesFound),
            ancientRelicSitesFound = incrementIf(category, SecretPlaceCategory.ANCIENT_RELIC_SITE, ancientRelicSitesFound),
            explorerHideoutsFound = incrementIf(category, SecretPlaceCategory.EXPLORER_HIDEOUT, explorerHideoutsFound),
            mythicalPlacesFound = incrementIf(category, SecretPlaceCategory.MYTHICAL_PLACE, mythicalPlacesFound),
            legendarySitesFound = incrementIf(category, SecretPlaceCategory.LEGENDARY_SITE, legendarySitesFound),
        )
        return next.copy(
            bestCategory = resolveBestCategory(next),
            rarestCategory = resolveRarestCategory(next),
        )
    }

    fun withCompletion(
        category: SecretPlaceCategory,
        creditsEarned: Int,
        xpEarned: Long,
    ): SecretPlaceCategoryStatistics {
        val next = copy(
            completedCount = completedCount + 1,
            creditsEarned = this.creditsEarned + creditsEarned.coerceAtLeast(0),
            xpEarned = this.xpEarned + xpEarned.coerceAtLeast(0L),
            natureSanctuariesCompleted = incrementIf(category, SecretPlaceCategory.NATURE_SANCTUARY, natureSanctuariesCompleted),
            scenicViewpointsCompleted = incrementIf(category, SecretPlaceCategory.SCENIC_VIEWPOINT, scenicViewpointsCompleted),
            historicLandmarksCompleted = incrementIf(category, SecretPlaceCategory.HISTORIC_LANDMARK, historicLandmarksCompleted),
            mysteryZonesCompleted = incrementIf(category, SecretPlaceCategory.MYSTERY_ZONE, mysteryZonesCompleted),
            ancientRelicSitesCompleted = incrementIf(category, SecretPlaceCategory.ANCIENT_RELIC_SITE, ancientRelicSitesCompleted),
            explorerHideoutsCompleted = incrementIf(category, SecretPlaceCategory.EXPLORER_HIDEOUT, explorerHideoutsCompleted),
            mythicalPlacesCompleted = incrementIf(category, SecretPlaceCategory.MYTHICAL_PLACE, mythicalPlacesCompleted),
            legendarySitesCompleted = incrementIf(category, SecretPlaceCategory.LEGENDARY_SITE, legendarySitesCompleted),
        )
        return next.copy(
            bestCategory = resolveBestCategory(next),
            rarestCategory = resolveRarestCategory(next),
        )
    }

    fun discoveredEntriesOrdered(): List<Pair<SecretPlaceCategory, Int>> =
        SecretPlaceCategory.ALL_ORDERED.map { it to discoveredCountFor(it) }

    fun completedEntriesOrdered(): List<Pair<SecretPlaceCategory, Int>> =
        SecretPlaceCategory.ALL_ORDERED.map { it to completedCountFor(it) }

    companion object {
        val EMPTY = SecretPlaceCategoryStatistics()

        fun resolveBestCategory(stats: SecretPlaceCategoryStatistics): SecretPlaceCategory? {
            val candidates = SecretPlaceCategory.ALL_ORDERED.map { category ->
                Triple(category, stats.discoveredCountFor(category), stats.completedCountFor(category))
            }.filter { it.second > 0 }
            return candidates.maxWithOrNull(
                compareBy<Triple<SecretPlaceCategory, Int, Int>> { it.second }
                    .thenBy { it.third }
                    .thenBy { it.first.difficultyLevel },
            )?.first
        }

        fun resolveRarestCategory(stats: SecretPlaceCategoryStatistics): SecretPlaceCategory? =
            SecretPlaceCategory.ALL_ORDERED
                .filter { stats.discoveredCountFor(it) > 0 }
                .maxByOrNull { it.difficultyLevel }
    }

    private fun incrementIf(
        actual: SecretPlaceCategory,
        target: SecretPlaceCategory,
        current: Int,
    ): Int = if (actual == target) current + 1 else current
}
