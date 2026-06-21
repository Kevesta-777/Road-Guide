package com.example.roadguideapp.goldhunt.clusters

/**
 * Read-only access to [ClusterType] definitions.
 * Placement, discovery, and reward systems consume this catalog; no generation here.
 */
internal object ClusterCatalog {
    val displayOrder: List<ClusterType> = ClusterType.ALL_ORDERED

    fun fromId(id: String): ClusterType? = ClusterType.fromId(id)

    fun totalRarityWeight(): Int = displayOrder.sumOf { it.rarityWeight }

    fun tierOf(type: ClusterType): Int = type.tier
}
