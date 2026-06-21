package com.example.roadguideapp.goldhunt.secretplaces.discovery

import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory

internal object SecretPlaceDiscoveryRequirementSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val CLUE_CHAIN = "clueChain"
        const val VISIT_COUNT = "visitCount"
        const val NEARBY_TREASURES = "nearbyTreasures"
        const val EXPLORER_LEVEL = "explorerLevel"
    }

    object ClueChainKeys {
        const val PREFIX = "clue_chain_secret_place"

        fun forCategory(category: SecretPlaceCategory): String =
            "$PREFIX:${category.id.lowercase()}"

        fun forPlace(secretPlaceId: String): String =
            "$PREFIX:place:$secretPlaceId"
    }
}
