package com.example.roadguideapp.goldhunt.achievements.titles.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleDisplayResolver
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRepository
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator
import com.example.roadguideapp.goldhunt.profile.rank.rankForLevel
import com.example.roadguideapp.goldhunt.profile.xp.XpManager

internal object AchievementTitleLoader {
    suspend fun load(context: Context): AchievementTitleUiState {
        val appContext = context.applicationContext
        val collection = AchievementTitleRepository.get(appContext).load()
        val lifetimeXp = XpManager.get(appContext).getLifetimeXp()
        val level = ExplorerLevelCalculator.xpProgressToNextLevel(lifetimeXp).currentLevel
        val rankTitle = rankForLevel(level).title
        val publicDisplayTitle = AchievementTitleDisplayResolver.resolvePublicDisplayTitle(
            activeTitleKey = collection.activeTitleKey,
            collection = collection,
            explorerRankTitle = rankTitle,
        )
        val formatters = AchievementTitleFormatters(appContext)
        return AchievementTitleUiState.from(collection, publicDisplayTitle, formatters)
    }

    suspend fun setActive(context: Context, titleKey: String) {
        AchievementTitleRepository.get(context.applicationContext).setActiveTitle(titleKey)
        ExplorerProfileRepository.get(context.applicationContext).ensureProfile()
    }

    suspend fun clearActive(context: Context) {
        AchievementTitleRepository.get(context.applicationContext).clearActiveTitle()
        ExplorerProfileRepository.get(context.applicationContext).ensureProfile()
    }
}
