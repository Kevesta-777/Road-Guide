package com.example.roadguideapp.goldhunt.events.collectionbook.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookRepository

internal object SeasonalEventCollectionBookLoader {
    suspend fun load(context: Context): SeasonalEventCollectionBookUiState {
        val appContext = context.applicationContext
        val book = SeasonalEventCollectionBookRepository.get(appContext).load()
        val formatters = SeasonalEventCollectionBookFormatters(appContext)
        return SeasonalEventCollectionBookUiState.from(book, formatters)
    }
}
