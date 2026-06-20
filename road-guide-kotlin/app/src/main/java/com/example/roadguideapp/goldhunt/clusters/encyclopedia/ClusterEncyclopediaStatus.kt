package com.example.roadguideapp.goldhunt.clusters.encyclopedia

/** Discovery state for a cluster family encyclopedia entry. */
internal enum class ClusterEncyclopediaStatus {
    MISSING,
    DISCOVERED,
    COMPLETED,
    ;

    val id: String get() = name

    companion object {
        fun fromId(id: String): ClusterEncyclopediaStatus? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }
    }
}
