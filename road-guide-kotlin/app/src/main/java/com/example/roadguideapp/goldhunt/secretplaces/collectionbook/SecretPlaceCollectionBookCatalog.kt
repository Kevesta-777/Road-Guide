package com.example.roadguideapp.goldhunt.secretplaces.collectionbook

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategoryCatalog

internal data class SecretPlaceCollectionBookTemplate(
    val category: SecretPlaceCategory,
    val displayName: String,
    val minimumExplorerLevel: Int,
    val achievementKey: String?,
    val storyFragmentKey: String?,
)

internal object SecretPlaceCollectionBookCatalog {
    val displayTemplates: List<SecretPlaceCollectionBookTemplate> =
        SecretPlaceCategoryCatalog.displayOrder.map { category ->
            SecretPlaceCollectionBookTemplate(
                category = category,
                displayName = category.displayName,
                minimumExplorerLevel = SecretPlaceCollectionBookSchema.ExplorerLevelKeys
                    .minimumForCategory(category),
                achievementKey = category.achievementKey,
                storyFragmentKey = category.storyFragmentKey,
            )
        }

    fun initialStatus(): SecretPlaceCollectionBookStatus = SecretPlaceCollectionBookStatus.MISSING

    fun templateForCategory(category: SecretPlaceCategory): SecretPlaceCollectionBookTemplate? =
        displayTemplates.firstOrNull { it.category == category }
}
