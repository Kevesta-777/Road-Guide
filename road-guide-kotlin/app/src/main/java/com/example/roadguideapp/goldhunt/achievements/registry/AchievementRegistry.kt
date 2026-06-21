package com.example.roadguideapp.goldhunt.achievements.registry

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCatalog
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition

/**
 * Central registry for all Gold Hunt achievement definitions.
 *
 * Providers contribute domain and milestone achievements; the registry merges them
 * into indexed snapshots suitable for 100+, 500+, and 1000+ catalog sizes.
 * Register additional [AchievementRegistryProvider] instances via [builder] for
 * future content without modifying existing providers.
 */
internal object AchievementRegistry {
    val defaultProviders: List<AchievementRegistryProvider> = listOf(
        FoundationAchievementRegistryProvider,
        SeasonalEventAchievementRegistryProvider,
        PanoramaAchievementRegistryProvider,
        SecretPlaceAchievementRegistryProvider,
        TreasureClusterAchievementRegistryProvider,
        RadarAchievementRegistryProvider,
        TreasureRarityAchievementRegistryProvider,
        MilestoneAchievementRegistryProvider(
            category = AchievementCategory.EXPLORATION,
            displayLabel = "Cells explored",
        ),
        MilestoneAchievementRegistryProvider(
            category = AchievementCategory.TREASURE,
            displayLabel = "Treasures collected",
        ),
        StoryAchievementRegistryProvider,
        LegendaryRelicAchievementRegistryProvider,
        StreakAchievementRegistryProvider,
        MasterExplorerAchievementRegistryProvider,
    )

    private val cachedSnapshot: AchievementRegistrySnapshot by lazy {
        builder().build()
    }

    fun snapshot(): AchievementRegistrySnapshot = cachedSnapshot

    fun allDefinitions(): List<AchievementDefinition> = snapshot().definitions

    fun findByKey(key: String): AchievementDefinition? = snapshot().findByKey(key)

    fun definitionsFor(category: AchievementCategory): List<AchievementDefinition> =
        snapshot().definitionsFor(category)

    fun count(): Int = snapshot().size

    fun providerIds(): Set<String> = snapshot().providerIds()

    fun providerContributions(): Map<String, Int> = snapshot().providerContributions

    fun allAchievements(): List<Achievement> =
        allDefinitions().map { definition ->
            AchievementCatalog.achievementFromDefinition(definition)
        }

    fun achievementByKey(key: String): Achievement? =
        findByKey(key)?.let { definition ->
            AchievementCatalog.achievementFromDefinition(definition)
        }

    fun builder(
        extraProviders: List<AchievementRegistryProvider> = emptyList(),
    ): AchievementRegistryBuilder =
        AchievementRegistryBuilder()
            .registerAll(defaultProviders)
            .registerAll(extraProviders)

    fun rebuild(
        extraProviders: List<AchievementRegistryProvider> = emptyList(),
    ): AchievementRegistrySnapshot = builder(extraProviders).build()
}
