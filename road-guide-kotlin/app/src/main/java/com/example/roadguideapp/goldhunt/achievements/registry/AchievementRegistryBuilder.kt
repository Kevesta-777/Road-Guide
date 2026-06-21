package com.example.roadguideapp.goldhunt.achievements.registry

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition

internal class AchievementRegistryBuilder {
    private val providers = linkedSetOf<AchievementRegistryProvider>()

    fun register(provider: AchievementRegistryProvider): AchievementRegistryBuilder {
        providers.add(provider)
        return this
    }

    fun registerAll(providers: Collection<AchievementRegistryProvider>): AchievementRegistryBuilder {
        providers.forEach { register(it) }
        return this
    }

    fun build(): AchievementRegistrySnapshot {
        val merged = linkedMapOf<String, AchievementDefinition>()
        val contributions = linkedMapOf<String, Int>()
        val keyOwners = linkedMapOf<String, String>()
        val duplicateKeys = mutableListOf<String>()

        for (provider in providers) {
            var contributed = 0
            for (definition in provider.definitions()) {
                val previousOwner = keyOwners.put(definition.key, provider.providerId)
                if (previousOwner != null) {
                    duplicateKeys += "${definition.key} ($previousOwner, ${provider.providerId})"
                    continue
                }
                merged[definition.key] = definition
                contributed++
            }
            contributions[provider.providerId] = contributed
        }

        if (duplicateKeys.isNotEmpty()) {
            error("AchievementRegistry duplicate keys detected: ${duplicateKeys.joinToString("; ")}")
        }

        val definitions = merged.values.toList()
        val byCategory = definitions.groupBy { it.category }
            .mapValues { (_, values) -> values.sortedBy { it.key } }
            .toSortedMap(compareBy { categoryOrder(it) })

        return AchievementRegistrySnapshot(
            definitions = definitions,
            byKey = merged,
            byCategory = byCategory,
            providerContributions = contributions,
            builtAtMs = System.currentTimeMillis(),
        )
    }

    private fun categoryOrder(category: AchievementCategory): Int =
        AchievementCategory.ALL_ORDERED.indexOf(category).coerceAtLeast(0)
}
