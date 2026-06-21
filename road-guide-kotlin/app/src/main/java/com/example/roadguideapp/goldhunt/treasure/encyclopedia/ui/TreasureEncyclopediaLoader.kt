package com.example.roadguideapp.goldhunt.treasure.encyclopedia.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopediaRepository

internal object TreasureEncyclopediaLoader {
    suspend fun load(context: Context): TreasureEncyclopediaUiState {
        val appContext = context.applicationContext
        val encyclopedia = TreasureEncyclopediaRepository.get(appContext).load()
        val formatters = TreasureEncyclopediaFormatters(appContext)
        return TreasureEncyclopediaUiState.from(encyclopedia, formatters)
    }
}
