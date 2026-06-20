package com.example.roadguideapp.goldhunt.achievements.journal.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalRepository

internal object AchievementJournalLoader {
    suspend fun load(context: Context): AchievementJournalUiState {
        val appContext = context.applicationContext
        val journal = AchievementJournalRepository.get(appContext).load()
        val formatters = AchievementJournalFormatters(appContext)
        return AchievementJournalUiState.from(journal, formatters)
    }
}
