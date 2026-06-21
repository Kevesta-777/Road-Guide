package com.example.roadguideapp.goldhunt.relics.registry

import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition

/**
 * Contributes legendary relic definitions to [LegendaryRelicRegistry].
 *
 * Add a new provider when introducing a gameplay domain so the central registry
 * scales past 50 / 100 / 500 entries without modifying registry internals.
 */
internal interface LegendaryRelicRegistryProvider {
    val providerId: String

    fun definitions(): List<LegendaryRelicDefinition>
}
