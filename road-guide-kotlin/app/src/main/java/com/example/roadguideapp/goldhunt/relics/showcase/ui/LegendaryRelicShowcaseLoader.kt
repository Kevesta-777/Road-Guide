package com.example.roadguideapp.goldhunt.relics.showcase.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseRepository

internal object LegendaryRelicShowcaseLoader {
    suspend fun load(context: Context): LegendaryRelicShowcaseUiState {
        val appContext = context.applicationContext
        val showcase = LegendaryRelicShowcaseRepository.get(appContext).load()
        val formatters = LegendaryRelicShowcaseFormatters(appContext)
        return LegendaryRelicShowcaseUiState.from(showcase, formatters)
    }
}
