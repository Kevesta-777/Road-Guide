package com.example.roadguideapp.goldhunt.treasure.metadata

/**
 * Schema metadata for [TreasureDefinition].
 * Bump [VERSION] when adding fields; use [TreasureDefinition.extensionJson] for experiments first.
 */
internal object TreasureDefinitionSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object ExtensionKeys {
        const val LEGENDARY_RELIC = "legendaryRelic"
        const val STORY_FRAGMENT = "storyFragment"
        const val SECRET_PLACE_REWARD = "secretPlaceReward"
        const val ROLLED_CREDIT_AMOUNT = "rolledCreditAmount"
    }
}
