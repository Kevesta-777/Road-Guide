package com.example.roadguideapp.goldhunt.relics.registry

/**
 * Standard count milestones for scalable relic families.
 *
 * Additional tiers can be supplied by custom [LegendaryRelicRegistryProvider]
 * implementations without changing the registry core.
 */
internal object RelicMilestoneScale {
    val TIER_50: List<Int> = listOf(50)
    val STANDARD: List<Int> = listOf(50, 100, 500)

    fun isStandard(milestone: Int): Boolean = milestone in STANDARD
}
