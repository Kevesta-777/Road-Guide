package com.example.roadguideapp.goldhunt.relics.journal.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalRepository

internal object LegendaryRelicJournalLoader {
    suspend fun load(context: Context): LegendaryRelicJournalUiState {
        val appContext = context.applicationContext
        val journal = LegendaryRelicJournalRepository.get(appContext).load()
        val formatters = LegendaryRelicJournalFormatters(appContext)
        return LegendaryRelicJournalUiState.from(journal, formatters)
    }
}
