package com.example.roadguideapp.goldhunt.panorama.journal.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalRepository

internal object PanoramaHuntJournalLoader {
    suspend fun load(context: Context): PanoramaHuntJournalUiState {
        val appContext = context.applicationContext
        val journal = PanoramaHuntJournalRepository.get(appContext).load()
        val formatters = PanoramaHuntJournalFormatters(appContext)
        return PanoramaHuntJournalUiState.from(journal, formatters)
    }
}
