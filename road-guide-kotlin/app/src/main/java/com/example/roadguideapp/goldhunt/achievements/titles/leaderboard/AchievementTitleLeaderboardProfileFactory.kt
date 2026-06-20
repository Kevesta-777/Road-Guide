package com.example.roadguideapp.goldhunt.achievements.titles.leaderboard

import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleCollection
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleDisplayResolver

internal object AchievementTitleLeaderboardProfileFactory {
    fun from(
        collection: AchievementTitleCollection,
        explorerRankTitle: String,
    ): AchievementTitleLeaderboardProfile {
        val activeTitleDisplay = collection.activeEntry?.displayTitle
        val publicDisplayTitle = AchievementTitleDisplayResolver.resolvePublicDisplayTitle(
            activeTitleKey = collection.activeTitleKey,
            collection = collection,
            explorerRankTitle = explorerRankTitle,
        )
        return AchievementTitleLeaderboardProfile(
            activeTitleKey = collection.activeTitleKey,
            activeTitleDisplay = activeTitleDisplay,
            explorerRankTitle = explorerRankTitle,
            publicDisplayTitle = publicDisplayTitle,
        )
    }
}
