package com.example.roadguideapp.goldhunt.achievements.collectionbook

internal data class AchievementCollectionBookProgress(
    val explorerLevel: Int,
    val catalogCount: Int,
    val completedCount: Int,
    val rareCompletedCount: Int,
    val legendaryCompletedCount: Int,
    val lockedCount: Int,
    val hiddenCount: Int,
    val badgesUnlockedCount: Int,
    val badgesTotalCount: Int,
    val titlesUnlockedCount: Int,
    val titlesTotalCount: Int,
    val activeTitleKey: String?,
    val storyFragmentsEarned: Int,
    val legendaryRelicsEarned: Int,
    val creditsEarned: Int,
    val xpEarned: Long,
) {
    val completionFraction: Float
        get() = if (catalogCount <= 0) {
            0f
        } else {
            (completedCount.toFloat() / catalogCount.toFloat()).coerceIn(0f, 1f)
        }

    val badgeCompletionFraction: Float
        get() = if (badgesTotalCount <= 0) {
            0f
        } else {
            (badgesUnlockedCount.toFloat() / badgesTotalCount.toFloat()).coerceIn(0f, 1f)
        }

    val titleCompletionFraction: Float
        get() = if (titlesTotalCount <= 0) {
            0f
        } else {
            (titlesUnlockedCount.toFloat() / titlesTotalCount.toFloat()).coerceIn(0f, 1f)
        }
}
