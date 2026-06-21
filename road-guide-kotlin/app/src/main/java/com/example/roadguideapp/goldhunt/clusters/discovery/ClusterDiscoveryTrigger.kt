package com.example.roadguideapp.goldhunt.clusters.discovery

internal enum class ClusterDiscoveryTrigger {
    DISTANCE,
    EXPLORATION,
    ;

    val id: String get() = name

    companion object {
        fun fromId(id: String): ClusterDiscoveryTrigger? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }
}
