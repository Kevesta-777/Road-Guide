package com.example.roadguideapp.goldhunt.achievements.registry

import com.example.roadguideapp.goldhunt.achievements.AchievementCatalog
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaSchema
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventAchievementCatalog
import com.example.roadguideapp.goldhunt.events.achievements.SeasonalEventCategory
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntTypeSchema
import com.example.roadguideapp.goldhunt.radar.RadarSchema
import com.example.roadguideapp.goldhunt.radar.RadarType
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategorySchema
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import com.example.roadguideapp.goldhunt.treasure.stats.TreasureRarityStatisticsSchema

internal object FoundationAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "foundation"

    override fun definitions(): List<AchievementDefinition> =
        AchievementCatalog.foundationDefinitions()
}

internal object SeasonalEventAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "seasonal_event"

    override fun definitions(): List<AchievementDefinition> {
        val eventDefinitions = SeasonalEventType.ALL_ORDERED.flatMap { type ->
            SeasonalEventAchievementCatalog.eventAchievements(type)
        }
        val categoryDefinitions = SeasonalEventCategory.ALL_ORDERED.flatMap { category ->
            SeasonalEventAchievementCatalog.categoryAchievements(category)
        }
        return (eventDefinitions + categoryDefinitions).map { definition ->
            AchievementRegistryAdapters.fromSeasonal(definition)
        }
    }
}

internal object PanoramaAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "panorama"

    override fun definitions(): List<AchievementDefinition> =
        PanoramaHuntType.ALL_ORDERED.map { type ->
            AchievementRegistryAdapters.simple(
                key = PanoramaHuntTypeSchema.AchievementKeys.firstCompletion(type),
                category = AchievementCategory.PANORAMA,
                displayName = "${type.displayName} — first completion",
                storyFragmentKey = type.storyFragmentKey,
                legendaryRelicKey = type.legendaryRelicKey,
            )
        } + AchievementRegistryAdapters.simple(
            key = PanoramaHuntTypeSchema.AchievementKeys.completeAllTypes(),
            category = AchievementCategory.PANORAMA,
            displayName = "All panorama hunt types completed",
            targetCount = PanoramaHuntType.ALL_ORDERED.size,
            storyFragmentKey = AchievementSchema.StoryFragmentKeys.forCategory(AchievementCategory.PANORAMA),
            legendaryRelicKey = AchievementSchema.LegendaryRelicKeys.forCategory(AchievementCategory.PANORAMA),
        )
}

internal object SecretPlaceAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "secret_place"

    override fun definitions(): List<AchievementDefinition> =
        SecretPlaceCategory.ALL_ORDERED.flatMap { category ->
            listOf(
                AchievementRegistryAdapters.simple(
                    key = SecretPlaceCategorySchema.AchievementKeys.firstDiscovery(category),
                    category = AchievementCategory.SECRET_PLACE,
                    displayName = "${category.displayName} — first discovery",
                    storyFragmentKey = category.storyFragmentKey,
                    legendaryRelicKey = category.legendaryRelicKey,
                ),
                AchievementRegistryAdapters.simple(
                    key = SecretPlaceCategorySchema.AchievementKeys.firstVisit(category),
                    category = AchievementCategory.SECRET_PLACE,
                    displayName = "${category.displayName} — first visit",
                    storyFragmentKey = category.storyFragmentKey,
                    legendaryRelicKey = category.legendaryRelicKey,
                ),
            )
        } + AchievementRegistryAdapters.simple(
            key = SecretPlaceCategorySchema.AchievementKeys.completeAllCategories(),
            category = AchievementCategory.SECRET_PLACE,
            displayName = "All secret place categories completed",
            targetCount = SecretPlaceCategory.ALL_ORDERED.size,
            storyFragmentKey = AchievementSchema.StoryFragmentKeys.forCategory(AchievementCategory.SECRET_PLACE),
            legendaryRelicKey = AchievementSchema.LegendaryRelicKeys.forCategory(AchievementCategory.SECRET_PLACE),
        )
}

internal object TreasureClusterAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "treasure_cluster"

    override fun definitions(): List<AchievementDefinition> =
        ClusterType.ALL_ORDERED.flatMap { type ->
            listOf(
                AchievementRegistryAdapters.simple(
                    key = ClusterEncyclopediaSchema.AchievementKeys.firstDiscovery(type),
                    category = AchievementCategory.TREASURE_CLUSTER,
                    displayName = "${type.displayName} — first discovery",
                    storyFragmentKey = type.storyFragmentKey,
                ),
                AchievementRegistryAdapters.simple(
                    key = ClusterEncyclopediaSchema.AchievementKeys.firstCompletion(type),
                    category = AchievementCategory.TREASURE_CLUSTER,
                    displayName = "${type.displayName} — first completion",
                    storyFragmentKey = type.storyFragmentKey,
                ),
            )
        } + AchievementRegistryAdapters.simple(
            key = ClusterEncyclopediaSchema.AchievementKeys.completeAllFamilies(),
            category = AchievementCategory.TREASURE_CLUSTER,
            displayName = "All treasure cluster families completed",
            targetCount = ClusterType.ALL_ORDERED.size,
            storyFragmentKey = AchievementSchema.StoryFragmentKeys.forCategory(AchievementCategory.TREASURE_CLUSTER),
            legendaryRelicKey = AchievementSchema.LegendaryRelicKeys.forCategory(AchievementCategory.TREASURE_CLUSTER),
        )
}

internal object RadarAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "radar"

    override fun definitions(): List<AchievementDefinition> =
        RadarType.ALL_ORDERED.map { type ->
            AchievementRegistryAdapters.simple(
                key = RadarSchema.AchievementKeys.firstUnlock(type),
                category = AchievementCategory.RADAR,
                displayName = "${type.displayName} — first pulse",
                minExplorerLevel = type.unlockLevel,
            )
        } + AchievementRegistryAdapters.simple(
            key = RadarSchema.AchievementKeys.unlockAll(),
            category = AchievementCategory.RADAR,
            displayName = "All radar types unlocked",
            targetCount = RadarType.ALL_ORDERED.size,
        )
}

internal object TreasureRarityAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "treasure_rarity"

    override fun definitions(): List<AchievementDefinition> =
        TreasureRarity.ALL_ORDERED.flatMap { rarity ->
            buildList {
                add(
                    AchievementRegistryAdapters.simple(
                        key = TreasureRarityStatisticsSchema.AchievementKeys.firstOf(rarity.id.lowercase()),
                        category = AchievementCategory.RARITY,
                        displayName = "${rarity.displayName} — first treasure",
                        legendaryRelicKey = rarity.achievementKey,
                    ),
                )
                AchievementMilestoneScale.STANDARD.forEach { count ->
                    add(
                        AchievementRegistryAdapters.simple(
                            key = TreasureRarityStatisticsSchema.AchievementKeys.countMilestone(
                                rarity.id.lowercase(),
                                count,
                            ),
                            category = AchievementCategory.RARITY,
                            displayName = "${rarity.displayName} — $count collected",
                            targetCount = count,
                        ),
                    )
                }
            }
        }
}

internal class MilestoneAchievementRegistryProvider(
    private val category: AchievementCategory,
    private val displayLabel: String,
    private val milestones: List<Int> = AchievementMilestoneScale.STANDARD,
) : AchievementRegistryProvider {
    override val providerId: String = "milestone_${category.id.lowercase()}"

    override fun definitions(): List<AchievementDefinition> =
        milestones.map { count ->
            AchievementRegistryAdapters.milestone(
                category = category,
                displayLabel = displayLabel,
                count = count,
            )
        }
}

internal object StoryAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "story"

    override fun definitions(): List<AchievementDefinition> =
        AchievementMilestoneScale.STANDARD.map { count ->
            AchievementRegistryAdapters.milestone(
                category = AchievementCategory.STORY,
                displayLabel = "Story fragments discovered",
                count = count,
            )
        } + AchievementRegistryAdapters.simple(
            key = AchievementSchema.AchievementKeys.collector(AchievementCategory.STORY),
            category = AchievementCategory.STORY,
            displayName = "Story — collector",
            targetCount = AchievementMilestoneScale.STANDARD.max(),
            storyFragmentKey = AchievementSchema.StoryFragmentKeys.forCategory(AchievementCategory.STORY),
        )
}

internal object LegendaryRelicAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "legendary_relic"

    override fun definitions(): List<AchievementDefinition> =
        AchievementMilestoneScale.STANDARD.map { count ->
            AchievementRegistryAdapters.milestone(
                category = AchievementCategory.LEGENDARY_RELIC,
                displayLabel = "Legendary relics earned",
                count = count,
            )
        } + AchievementRegistryAdapters.simple(
            key = AchievementSchema.AchievementKeys.mastery(AchievementCategory.LEGENDARY_RELIC),
            category = AchievementCategory.LEGENDARY_RELIC,
            displayName = "Legendary Relic — mastery",
            targetCount = AchievementMilestoneScale.STANDARD.max(),
            legendaryRelicKey = AchievementSchema.LegendaryRelicKeys.forCategory(
                AchievementCategory.LEGENDARY_RELIC,
            ),
        )
}

internal object StreakAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "streak"

    override fun definitions(): List<AchievementDefinition> =
        listOf(7, 30, 100).map { days ->
            AchievementRegistryAdapters.simple(
                key = AchievementSchema.AchievementKeys.forCategory(AchievementCategory.STREAK, "days_$days"),
                category = AchievementCategory.STREAK,
                displayName = "Exploration streak — $days days",
                targetCount = days,
                minExplorerLevel = AchievementSchema.CategoryGates.minExplorerLevel(AchievementCategory.STREAK),
            )
        }
}

internal object MasterExplorerAchievementRegistryProvider : AchievementRegistryProvider {
    override val providerId: String = "master_explorer"

    override fun definitions(): List<AchievementDefinition> =
        listOf(10, 20, 30).map { level ->
            AchievementRegistryAdapters.simple(
                key = AchievementSchema.AchievementKeys.forCategory(
                    AchievementCategory.MASTER_EXPLORER,
                    "level_$level",
                ),
                category = AchievementCategory.MASTER_EXPLORER,
                displayName = "Reach explorer level $level",
                targetCount = level,
                minExplorerLevel = level,
                storyFragmentKey = AchievementSchema.StoryFragmentKeys.forCategory(
                    AchievementCategory.MASTER_EXPLORER,
                ),
                legendaryRelicKey = AchievementSchema.LegendaryRelicKeys.forCategory(
                    AchievementCategory.MASTER_EXPLORER,
                ),
            )
        } + AchievementRegistryAdapters.simple(
            key = AchievementSchema.AchievementKeys.mastery(AchievementCategory.MASTER_EXPLORER),
            category = AchievementCategory.MASTER_EXPLORER,
            displayName = "Master Explorer — capstone",
            targetCount = 1,
            minExplorerLevel = AchievementSchema.CategoryGates.minExplorerLevel(
                AchievementCategory.MASTER_EXPLORER,
            ),
            storyFragmentKey = AchievementSchema.StoryFragmentKeys.forCategory(AchievementCategory.MASTER_EXPLORER),
            legendaryRelicKey = AchievementSchema.LegendaryRelicKeys.forCategory(AchievementCategory.MASTER_EXPLORER),
        )
}
