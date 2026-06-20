package com.example.roadguideapp.goldhunt.achievements.titles.leaderboard

/**
 * Stable read model for future leaderboard and social profile integrations.
 */
internal data class AchievementTitleLeaderboardProfile(
    val activeTitleKey: String?,
    val activeTitleDisplay: String?,
    val explorerRankTitle: String,
    val publicDisplayTitle: String,
) {
    val usesAchievementTitle: Boolean get() = !activeTitleKey.isNullOrBlank()
}
