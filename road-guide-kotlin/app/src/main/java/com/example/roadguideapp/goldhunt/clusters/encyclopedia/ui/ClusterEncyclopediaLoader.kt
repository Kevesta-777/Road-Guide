package com.example.roadguideapp.goldhunt.clusters.encyclopedia.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaRepository

internal object ClusterEncyclopediaLoader {
    suspend fun load(context: Context): ClusterEncyclopediaUiState {
        val appContext = context.applicationContext
        val encyclopedia = ClusterEncyclopediaRepository.get(appContext).load()
        val formatters = ClusterEncyclopediaFormatters(appContext)
        return ClusterEncyclopediaUiState.from(encyclopedia, formatters)
    }
}
