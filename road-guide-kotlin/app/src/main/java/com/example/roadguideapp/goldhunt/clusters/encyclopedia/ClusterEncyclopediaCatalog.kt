package com.example.roadguideapp.goldhunt.clusters.encyclopedia

import com.example.roadguideapp.goldhunt.clusters.ClusterCatalog
import com.example.roadguideapp.goldhunt.clusters.ClusterType

internal data class ClusterEncyclopediaTemplate(
    val clusterType: ClusterType,
    val displayName: String,
    val achievementKey: String?,
    val storyFragmentKey: String?,
)

internal object ClusterEncyclopediaCatalog {
    val displayTemplates: List<ClusterEncyclopediaTemplate> =
        ClusterCatalog.displayOrder.map { type ->
            ClusterEncyclopediaTemplate(
                clusterType = type,
                displayName = type.displayName,
                achievementKey = type.achievementKey,
                storyFragmentKey = type.storyFragmentKey,
            )
        }

    fun initialStatus(): ClusterEncyclopediaStatus = ClusterEncyclopediaStatus.MISSING

    fun templateForType(type: ClusterType): ClusterEncyclopediaTemplate? =
        displayTemplates.firstOrNull { it.clusterType == type }
}
