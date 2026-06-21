package com.example.roadguideapp.goldhunt.relics



import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry

import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistryAdapters

import kotlin.math.roundToLong



/**

 * Read-only facade over [LegendaryRelicRegistry] and reward scaling helpers.

 *

 * Domain catalogs register relics through registry providers. This catalog exposes

 * lookup helpers for UI, reward dispatchers, and player-model mapping without

 * hard-coded relic lists.

 */

internal object LegendaryRelicCatalog {

    val displayOrder: List<RelicCategory> = RelicCategory.ALL_ORDERED



    fun allDefinitions(): List<LegendaryRelicDefinition> =

        LegendaryRelicRegistry.allDefinitions()



    fun foundationDefinitions(): List<LegendaryRelicDefinition> =

        RelicCategory.ALL_ORDERED.map { category ->

            foundationDefinition(category)

        }



    fun foundationDefinition(category: RelicCategory): LegendaryRelicDefinition =

        LegendaryRelicRegistryAdapters.foundation(category)



    fun findById(relicId: String): LegendaryRelicDefinition? =

        LegendaryRelicRegistry.findById(relicId)



    fun definitionsFor(category: RelicCategory): List<LegendaryRelicDefinition> =

        LegendaryRelicRegistry.definitionsFor(category)



    fun foundationRelics(): List<LegendaryRelic> =

        foundationDefinitions().map { definition ->

            relicFromDefinition(definition)

        }



    fun relicFromDefinition(

        definition: LegendaryRelicDefinition,

        piecesCollected: Int = 0,

        completed: Boolean = false,

        completionDate: Long? = null,

    ): LegendaryRelic = LegendaryRelicMapper.fromDefinition(

        definition = definition,

        piecesCollected = piecesCollected,

        completed = completed,

        completionDate = completionDate,

    )



    fun scaledCompletionCredits(

        category: RelicCategory,

        rarity: RelicRarity,

        baseCredits: Int = BASE_COMPLETION_CREDITS,

    ): Int {

        val categoryScaled = RelicSchema.RewardScaling.scaledCredits(category, baseCredits)

        return (categoryScaled * rarity.rewardMultiplier).roundToLong().toInt().coerceAtLeast(0)

    }



    fun scaledCompletionXp(

        category: RelicCategory,

        rarity: RelicRarity,

        baseXp: Long = BASE_COMPLETION_XP,

    ): Long {

        val categoryScaled = RelicSchema.RewardScaling.scaledXp(category, baseXp)

        return (categoryScaled * rarity.rewardMultiplier).roundToLong().coerceAtLeast(0L)

    }



    internal const val BASE_COMPLETION_CREDITS = 40

    internal const val BASE_COMPLETION_XP = 90L

}


