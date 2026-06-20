package com.example.roadguideapp.goldhunt.achievements.registry

/**
 * Standard count milestones for scalable achievement families.
 *
 * Additional tiers (2500, 5000, …) can be supplied by custom
 * [AchievementRegistryProvider] implementations without changing the registry core.
 */
internal object AchievementMilestoneScale {
    val STANDARD: List<Int> = listOf(100, 500, 1_000)

    fun isStandard(count: Int): Boolean = count in STANDARD
}
