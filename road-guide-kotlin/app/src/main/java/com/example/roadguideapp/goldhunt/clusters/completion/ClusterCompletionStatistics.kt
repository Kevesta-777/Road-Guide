package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.ClusterType

internal data class ClusterCompletionStatistics(
    val countsByType: Map<ClusterType, Int> = emptyMap(),
) {
    val totalCompleted: Int
        get() = countsByType.values.sum()

    fun countFor(type: ClusterType): Int = countsByType[type] ?: 0

    fun withIncrement(type: ClusterType): ClusterCompletionStatistics {
        val next = countsByType.toMutableMap()
        next[type] = countFor(type) + 1
        return copy(countsByType = next)
    }

    fun entriesOrdered(): List<Pair<ClusterType, Int>> =
        ClusterType.ALL_ORDERED.map { type -> type to countFor(type) }

    companion object {
        val EMPTY = ClusterCompletionStatistics()
    }
}
