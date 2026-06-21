package com.example.roadguideapp.goldhunt.radar

/**
 * Orders pulse results for downstream UI and scan success evaluation.
 */
internal object RadarSignalSorter {
    fun sortByPriority(
        signals: List<RadarSignal>,
        radarType: RadarType,
    ): List<RadarSignal> = signals.sortedWith(
        compareByDescending<RadarSignal> { RadarDetectionScope.priorityWeight(it.targetCategory, radarType) }
            .thenByDescending { it.signalStrength }
            .thenBy { it.distanceMeters }
            .thenBy { it.targetId },
    )

    fun strongest(signals: List<RadarSignal>, radarType: RadarType): RadarSignal? =
        sortByPriority(signals, radarType).firstOrNull()

    fun filterDetectable(
        signals: List<RadarSignal>,
        radarType: RadarType,
    ): List<RadarSignal> = signals.filter {
        RadarDetectionScope.isDetectable(it.targetCategory, radarType)
    }
}
