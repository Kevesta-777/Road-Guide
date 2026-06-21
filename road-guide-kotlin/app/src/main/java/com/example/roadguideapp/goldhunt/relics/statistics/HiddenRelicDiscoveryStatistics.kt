package com.example.roadguideapp.goldhunt.relics.statistics

internal data class HiddenRelicDiscoveryStatistics(
    val hiddenCatalogCount: Int = 0,
    val hiddenRevealedCount: Int = 0,
    val hiddenUndiscoveredCount: Int = 0,
) {
    val revealPercentage: Float
        get() = if (hiddenCatalogCount <= 0) {
            0f
        } else {
            (hiddenRevealedCount.toFloat() / hiddenCatalogCount.toFloat()).coerceIn(0f, 1f)
        }

    companion object {
        val EMPTY = HiddenRelicDiscoveryStatistics()
    }
}
