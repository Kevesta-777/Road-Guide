package com.example.roadguideapp.goldhunt.achievements.registry

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition

/**
 * Immutable, indexed view of all registered achievement definitions.
 */
internal data class AchievementRegistrySnapshot(
    val definitions: List<AchievementDefinition>,
    val byKey: Map<String, AchievementDefinition>,
    val byCategory: Map<AchievementCategory, List<AchievementDefinition>>,
    val providerContributions: Map<String, Int>,
    val builtAtMs: Long,
) {
    val size: Int get() = definitions.size

    fun findByKey(key: String): AchievementDefinition? = byKey[key]

    fun definitionsFor(category: AchievementCategory): List<AchievementDefinition> =
        byCategory[category].orEmpty()

    fun providerIds(): Set<String> = providerContributions.keys
}
