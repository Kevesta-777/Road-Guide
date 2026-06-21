package com.example.roadguideapp.goldhunt

import com.example.roadguideapp.goldhunt.storage.DiscoveryStore

internal object ProgressCalculator {

    fun regionPercent(store: DiscoveryStore, region: DiscoveryRegion): Float {
        val total = region.totalCellCount
        if (total <= 0) return 0f
        val discovered = store.discoveredCount().coerceAtMost(total)
        return (discovered.toFloat() / total.toFloat() * 100f).coerceIn(0f, 100f)
    }

    fun formatPercent(percent: Float): String {
        return when {
            percent >= 99.95f -> "100%"
            percent < 0.05f -> "0%"
            else -> "%.1f%%".format(percent)
        }
    }
}
