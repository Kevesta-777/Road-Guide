package com.example.roadguideapp.goldhunt.radar

import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.secretplace.SecretPlaceSpec
import com.example.roadguideapp.goldhunt.secretplaces.categories.assignment.SecretPlaceCategoryAssignmentEngine
import com.example.roadguideapp.goldhunt.secretplaces.categories.assignment.SecretPlaceCategoryAssignmentInput

/**
 * Resolves story-fragment and legendary-relic hook keys for procedural world targets.
 */
internal object RadarHookResolver {
    fun storyFragmentForCluster(cluster: TreasureCluster): String? =
        cluster.resolvedStoryFragmentId?.trim()?.takeIf { it.isNotEmpty() }

    fun legendaryRelicForCluster(cluster: TreasureCluster): String? =
        cluster.legendaryRelicKey?.trim()?.takeIf { it.isNotEmpty() }

    fun storyFragmentForSecretPlace(
        spec: SecretPlaceSpec,
        playRegionId: String,
    ): String? = assignedCategory(spec, playRegionId).storyFragmentKey

    fun legendaryRelicForSecretPlace(
        spec: SecretPlaceSpec,
        playRegionId: String,
    ): String? = assignedCategory(spec, playRegionId).legendaryRelicKey

    private fun assignedCategory(
        spec: SecretPlaceSpec,
        playRegionId: String,
    ) = SecretPlaceCategoryAssignmentEngine.assign(
        SecretPlaceCategoryAssignmentInput.forGridAnchor(
            anchorId = spec.secretPlaceId,
            playRegionId = playRegionId,
            latitude = spec.lat,
            longitude = spec.lng,
            l1CellId = spec.regionL1Id,
            l2CellId = spec.regionL2Id,
        ),
    )
}
