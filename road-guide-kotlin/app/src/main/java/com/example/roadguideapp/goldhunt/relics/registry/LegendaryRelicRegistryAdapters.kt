package com.example.roadguideapp.goldhunt.relics.registry

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.radar.RadarType
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

internal object LegendaryRelicRegistryAdapters {
    fun foundation(category: RelicCategory): LegendaryRelicDefinition =
        definition(
            relicId = RelicSchema.RelicKeys.forCategory(category),
            name = "${category.displayName} Relic",
            description = LegendaryRelicRegistryDescriptions.foundation(category),
            category = category,
            rarity = RelicRarity.forCategory(category),
            pieceCount = pieceCountFor(category),
        )

    fun milestone(
        category: RelicCategory,
        milestone: Int,
    ): LegendaryRelicDefinition = definition(
        relicId = RelicSchema.MilestoneKeys.relicId(category, milestone),
        name = "${category.displayName} Relic — $milestone",
        description = LegendaryRelicRegistryDescriptions.milestone(category, milestone),
        category = category,
        rarity = rarityForMilestone(category, milestone),
        pieceCount = pieceCountForMilestone(milestone),
    )

    fun catalogIndexed(
        category: RelicCategory,
        index: Int,
    ): LegendaryRelicDefinition = definition(
        relicId = RelicSchema.CatalogKeys.forCategory(category, index),
        name = "${category.displayName} Relic — catalog $index",
        description = LegendaryRelicRegistryDescriptions.catalogIndexed(category, index),
        category = category,
        rarity = rarityForCatalogIndex(category, index),
        pieceCount = pieceCountForCatalogIndex(index),
    )

    fun fromSecretPlace(category: SecretPlaceCategory): LegendaryRelicDefinition {
        val relicCategory = relicCategoryForSecretPlace(category)
        return definition(
            relicId = RelicSchema.DomainKeys.SecretPlace.forCategory(category),
            name = "${category.displayName} Relic",
            description = "A relic uncovered near ${category.displayName.lowercase()} sites.",
            category = relicCategory,
            rarity = RelicRarity.forCategory(relicCategory),
            pieceCount = pieceCountFor(relicCategory),
        )
    }

    fun fromPanorama(type: PanoramaHuntType): LegendaryRelicDefinition = definition(
        relicId = RelicSchema.DomainKeys.Panorama.forType(type),
        name = "${type.displayName} Relic",
        description = "A panorama hunt relic tied to ${type.displayName.lowercase()} challenges.",
        category = RelicCategory.MYTHICAL,
        rarity = RelicRarity.RARE,
        pieceCount = 3,
    )

    fun fromSeasonalEvent(type: SeasonalEventType): LegendaryRelicDefinition = definition(
        relicId = RelicSchema.DomainKeys.SeasonalEvent.forType(type),
        name = "${type.displayName} Relic",
        description = "A seasonal relic earned during ${type.displayName.lowercase()} celebrations.",
        category = RelicCategory.SEASONAL_EVENT,
        rarity = RelicRarity.RARE,
        pieceCount = 3,
    )

    fun fromCluster(type: ClusterType): LegendaryRelicDefinition = definition(
        relicId = RelicSchema.DomainKeys.Cluster.forType(type),
        name = "${type.displayName} Cluster Relic",
        description = "A relic forged from ${type.displayName.lowercase()} treasure clusters.",
        category = RelicCategory.ANCIENT_CIVILIZATION,
        rarity = RelicRarity.EPIC,
        pieceCount = 5,
    )

    fun fromRadar(type: RadarType): LegendaryRelicDefinition = definition(
        relicId = RelicSchema.DomainKeys.Radar.forType(type),
        name = "${type.displayName} Radar Relic",
        description = "A relic revealed by ${type.displayName.lowercase()} radar pulses.",
        category = RelicCategory.MYTHICAL,
        rarity = RelicRarity.EPIC,
        pieceCount = 5,
    )

    fun fromTreasureRarity(rarity: TreasureRarity): LegendaryRelicDefinition = definition(
        relicId = RelicSchema.DomainKeys.TreasureRarityKeys.forRarity(rarity),
        name = "${rarity.displayName} Treasure Relic",
        description = "A relic attuned to ${rarity.displayName.lowercase()} treasure finds.",
        category = relicCategoryForTreasureRarity(rarity),
        rarity = relicRarityForTreasureRarity(rarity),
        pieceCount = pieceCountForTreasureRarity(rarity),
    )

    private fun definition(
        relicId: String,
        name: String,
        description: String,
        category: RelicCategory,
        rarity: RelicRarity,
        pieceCount: Int,
    ): LegendaryRelicDefinition = LegendaryRelicDefinition(
        relicId = relicId,
        name = name,
        description = description,
        category = category,
        rarity = rarity,
        pieceCount = pieceCount,
        badgeKey = RelicSchema.BadgeKeys.forRelic(relicId),
        titleKey = RelicSchema.TitleKeys.forRelic(relicId),
        powerKey = RelicSchema.PowerKeys.forRelic(relicId),
    )

    private fun pieceCountFor(category: RelicCategory): Int = when (category) {
        RelicCategory.EXPLORER,
        RelicCategory.STORY,
        -> 1
        RelicCategory.SEASONAL_EVENT,
        RelicCategory.WARRIOR,
        -> 3
        RelicCategory.MYTHICAL,
        RelicCategory.ANCIENT_CIVILIZATION,
        -> 5
        RelicCategory.ROYAL -> 7
        RelicCategory.HIDDEN -> 9
    }

    private fun pieceCountForMilestone(milestone: Int): Int = when {
        milestone >= 500 -> 7
        milestone >= 100 -> 5
        milestone >= 50 -> 3
        else -> 1
    }

    private fun pieceCountForCatalogIndex(index: Int): Int = when {
        index % 9 == 0 -> 9
        index % 7 == 0 -> 7
        index % 5 == 0 -> 5
        index % 3 == 0 -> 3
        else -> 1
    }

    private fun pieceCountForTreasureRarity(rarity: TreasureRarity): Int = when (rarity.tier) {
        in 0..2 -> 1
        in 3..4 -> 3
        else -> 5
    }

    private fun rarityForMilestone(category: RelicCategory, milestone: Int): RelicRarity = when {
        milestone >= 500 -> RelicRarity.LEGENDARY
        milestone >= 100 -> RelicRarity.EPIC
        milestone >= 50 -> RelicRarity.RARE
        else -> RelicRarity.forCategory(category)
    }

    private fun rarityForCatalogIndex(category: RelicCategory, index: Int): RelicRarity = when {
        index % 9 == 0 -> RelicRarity.LEGENDARY
        index % 5 == 0 -> RelicRarity.EPIC
        index % 3 == 0 -> RelicRarity.RARE
        else -> RelicRarity.forCategory(category)
    }

    private fun relicCategoryForSecretPlace(category: SecretPlaceCategory): RelicCategory = when (category) {
        SecretPlaceCategory.NATURE_SANCTUARY,
        SecretPlaceCategory.SCENIC_VIEWPOINT,
        -> RelicCategory.EXPLORER
        SecretPlaceCategory.HISTORIC_LANDMARK -> RelicCategory.STORY
        SecretPlaceCategory.MYSTERY_ZONE -> RelicCategory.WARRIOR
        SecretPlaceCategory.ANCIENT_RELIC_SITE -> RelicCategory.ANCIENT_CIVILIZATION
        SecretPlaceCategory.EXPLORER_HIDEOUT -> RelicCategory.EXPLORER
        SecretPlaceCategory.MYTHICAL_PLACE -> RelicCategory.MYTHICAL
        SecretPlaceCategory.LEGENDARY_SITE -> RelicCategory.ROYAL
    }

    private fun relicCategoryForTreasureRarity(rarity: TreasureRarity): RelicCategory = when {
        rarity.tier >= 5 -> RelicCategory.ROYAL
        rarity.tier >= 4 -> RelicCategory.MYTHICAL
        rarity.tier >= 3 -> RelicCategory.WARRIOR
        else -> RelicCategory.EXPLORER
    }

    private fun relicRarityForTreasureRarity(rarity: TreasureRarity): RelicRarity = when {
        rarity.tier >= 5 -> RelicRarity.LEGENDARY
        rarity.tier >= 4 -> RelicRarity.EPIC
        rarity.tier >= 3 -> RelicRarity.RARE
        else -> RelicRarity.COMMON
    }
}

internal object LegendaryRelicRegistryDescriptions {
    fun foundation(category: RelicCategory): String = when (category) {
        RelicCategory.EXPLORER ->
            "A trail-worn token earned by explorers who chart new ground."
        RelicCategory.STORY ->
            "A narrative shard that unlocks forgotten chapters of the Gold Hunt saga."
        RelicCategory.SEASONAL_EVENT ->
            "A limited relic forged during a seasonal celebration."
        RelicCategory.WARRIOR ->
            "A battle-marked artifact left by champions of the road."
        RelicCategory.ANCIENT_CIVILIZATION ->
            "A fragment of an empire that vanished before the modern map was drawn."
        RelicCategory.MYTHICAL ->
            "A relic whispered about in campfire tales and radar echoes."
        RelicCategory.ROYAL ->
            "A crown-adjacent treasure reserved for elite explorers."
        RelicCategory.HIDDEN ->
            "A capstone relic revealed only to those who complete the deepest chains."
    }

    fun milestone(category: RelicCategory, milestone: Int): String =
        "A ${category.displayName.lowercase()} relic earned at the $milestone milestone."

    fun catalogIndexed(category: RelicCategory, index: Int): String =
        "Catalog relic $index in the ${category.displayName.lowercase()} family."
}
