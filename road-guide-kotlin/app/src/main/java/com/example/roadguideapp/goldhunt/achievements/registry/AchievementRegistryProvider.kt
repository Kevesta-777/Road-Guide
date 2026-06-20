package com.example.roadguideapp.goldhunt.achievements.registry

import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition

/**
 * Contributes achievement definitions to [AchievementRegistry].
 *
 * Add a new provider when introducing a gameplay domain so the central registry
 * scales past 100 / 500 / 1000 entries without modifying registry internals.
 */
internal interface AchievementRegistryProvider {
    val providerId: String

    fun definitions(): List<AchievementDefinition>
}
