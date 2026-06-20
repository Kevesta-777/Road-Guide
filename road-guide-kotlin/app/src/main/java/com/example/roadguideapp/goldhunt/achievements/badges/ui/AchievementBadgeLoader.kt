package com.example.roadguideapp.goldhunt.achievements.badges.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRepository

internal object AchievementBadgeLoader {
    suspend fun load(context: Context): AchievementBadgeUiState {
        val appContext = context.applicationContext
        val collection = AchievementBadgeRepository.get(appContext).load()
        val formatters = AchievementBadgeFormatters(appContext)
        return AchievementBadgeUiState.from(collection, formatters)
    }
}
