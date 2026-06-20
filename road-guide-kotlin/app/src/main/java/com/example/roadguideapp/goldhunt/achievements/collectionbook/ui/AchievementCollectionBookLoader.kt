package com.example.roadguideapp.goldhunt.achievements.collectionbook.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.collectionbook.AchievementCollectionBookRepository

internal object AchievementCollectionBookLoader {
    suspend fun load(context: Context): AchievementCollectionBookUiState {
        val appContext = context.applicationContext
        val book = AchievementCollectionBookRepository.get(appContext).load()
        val formatters = AchievementCollectionBookFormatters(appContext)
        return AchievementCollectionBookUiState.from(book, formatters)
    }
}
