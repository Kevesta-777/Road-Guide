package com.example.roadguideapp.goldhunt.relics.statistics

import android.content.Context

/**
 * Read facade over hidden relic discovery counters stored on [LegendaryRelicStatistics].
 */
internal class HiddenRelicDiscoveryStatisticsManager private constructor(context: Context) {
    private val legendaryRelicStatisticsManager = LegendaryRelicStatisticsManager.get(
        context.applicationContext,
    )

    suspend fun load(): HiddenRelicDiscoveryStatistics =
        legendaryRelicStatisticsManager.load().toHiddenDiscoveryStatistics()

    suspend fun reconcile(timestampMs: Long = System.currentTimeMillis()): HiddenRelicDiscoveryStatistics =
        legendaryRelicStatisticsManager.reconcile(timestampMs).toHiddenDiscoveryStatistics()

    companion object {
        @Volatile
        private var instance: HiddenRelicDiscoveryStatisticsManager? = null

        fun get(context: Context): HiddenRelicDiscoveryStatisticsManager =
            instance ?: synchronized(this) {
                instance ?: HiddenRelicDiscoveryStatisticsManager(context.applicationContext)
                    .also { instance = it }
            }
    }
}
