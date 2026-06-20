package com.example.roadguideapp.goldhunt.relics.statistics.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import com.example.roadguideapp.goldhunt.relics.statistics.LegendaryRelicStatistics

internal class LegendaryRelicStatisticsFormatters(
    private val context: Context,
) {
    fun formatCompletedProgress(stats: LegendaryRelicStatistics): String =
        ExplorerProfileFormatters.formatCompletionProgress(
            completed = stats.completedCount,
            total = stats.catalogCount,
        )

    fun formatCompletionPercentage(stats: LegendaryRelicStatistics): String =
        ExplorerProfileFormatters.formatCompletionPercentage(stats.completionPercentage)

    fun formatPieceProgress(stats: LegendaryRelicStatistics): String =
        context.getString(
            R.string.explorer_profile_legendary_relics_pieces_progress,
            ExplorerProfileFormatters.formatCount(stats.piecesCollected),
            ExplorerProfileFormatters.formatCount(stats.totalPieces),
        )

    fun formatPieceCompletionPercentage(stats: LegendaryRelicStatistics): String =
        ExplorerProfileFormatters.formatCompletionPercentage(stats.pieceCompletionPercentage)

    fun formatHiddenRelicsDiscovered(stats: LegendaryRelicStatistics): String =
        ExplorerProfileFormatters.formatCompletionProgress(
            completed = stats.hiddenRelicsDiscovered,
            total = stats.hiddenRelicsCatalogCount,
        )

    fun formatHiddenDiscoveryPercentage(stats: LegendaryRelicStatistics): String =
        ExplorerProfileFormatters.formatCompletionPercentage(stats.hiddenDiscoveryPercentage)
}
