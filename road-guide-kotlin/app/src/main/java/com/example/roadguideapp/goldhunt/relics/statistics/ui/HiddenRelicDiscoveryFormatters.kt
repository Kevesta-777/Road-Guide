package com.example.roadguideapp.goldhunt.relics.statistics.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import com.example.roadguideapp.goldhunt.relics.statistics.HiddenRelicDiscoveryStatistics

internal class HiddenRelicDiscoveryFormatters(
    private val context: Context,
) {
    fun formatRevealedProgress(stats: HiddenRelicDiscoveryStatistics): String =
        ExplorerProfileFormatters.formatCompletionProgress(
            completed = stats.hiddenRevealedCount,
            total = stats.hiddenCatalogCount,
        )

    fun formatRevealPercentage(stats: HiddenRelicDiscoveryStatistics): String =
        ExplorerProfileFormatters.formatCompletionPercentage(stats.revealPercentage)

    fun formatUndiscoveredCount(stats: HiddenRelicDiscoveryStatistics): String =
        context.getString(
            R.string.explorer_profile_hidden_relics_undiscovered,
            ExplorerProfileFormatters.formatCount(stats.hiddenUndiscoveredCount),
        )
}
