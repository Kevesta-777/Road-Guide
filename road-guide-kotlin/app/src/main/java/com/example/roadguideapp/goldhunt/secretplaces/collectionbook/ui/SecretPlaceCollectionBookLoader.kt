package com.example.roadguideapp.goldhunt.secretplaces.collectionbook.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBookRepository

internal object SecretPlaceCollectionBookLoader {
    suspend fun load(context: Context): SecretPlaceCollectionBookUiState {
        val appContext = context.applicationContext
        val book = SecretPlaceCollectionBookRepository.get(appContext).load()
        val formatters = SecretPlaceCollectionBookFormatters(appContext)
        return SecretPlaceCollectionBookUiState.from(book, formatters)
    }
}
