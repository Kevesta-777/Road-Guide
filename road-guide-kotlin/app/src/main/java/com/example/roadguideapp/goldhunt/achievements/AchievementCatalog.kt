package com.example.roadguideapp.goldhunt.achievements

/**
 * Read-only facade over [AchievementCategory] and foundation achievement definitions.
 *
 * Domain catalogs (seasonal events, secret places, panorama hunts) remain authoritative
 * for gameplay-specific achievements. This catalog seeds cross-cutting foundation
 * achievements and exposes lookup helpers for UI and reward scaling.
 */
internal object AchievementCatalog {
    val displayOrder: List<AchievementCategory> = AchievementCategory.ALL_ORDERED

    private val foundationDefinitions: List<AchievementDefinition> =
        AchievementCategory.ALL_ORDERED.map { category ->
            foundationDefinition(category)
        }

    fun fromId(id: String): AchievementCategory? = AchievementCategory.fromId(id)

    fun foundationDefinitions(): List<AchievementDefinition> = foundationDefinitions

    fun foundationDefinition(category: AchievementCategory): AchievementDefinition {
        val minLevel = AchievementSchema.CategoryGates.minExplorerLevel(category)
        val storyFragmentKey = if (AchievementSchema.CategoryGates.supportsStoryFragments(category)) {
            AchievementSchema.StoryFragmentKeys.forCategory(category)
        } else {
            null
        }
        val legendaryRelicKey = if (AchievementSchema.CategoryGates.supportsLegendaryRelics(category)) {
            AchievementSchema.LegendaryRelicKeys.forCategory(category)
        } else {
            null
        }
        return AchievementDefinition(
            key = AchievementSchema.AchievementKeys.firstUnlock(category),
            category = category,
            displayName = "${category.displayName} — first unlock",
            targetCount = 1,
            minExplorerLevel = minLevel,
            storyFragmentKey = storyFragmentKey,
            legendaryRelicKey = legendaryRelicKey,
        )
    }

    fun findByKey(key: String): AchievementDefinition? =
        foundationDefinitions.firstOrNull { it.key == key }

    fun definitionsFor(category: AchievementCategory): List<AchievementDefinition> =
        foundationDefinitions.filter { it.category == category }

    fun domainKeyPrefix(category: AchievementCategory): String =
        AchievementSchema.DomainPrefixes.forCategory(category)

    fun scaledCompletionCredits(
        category: AchievementCategory,
        baseCredits: Int = BASE_COMPLETION_CREDITS,
    ): Int = AchievementSchema.RewardScaling.scaledCredits(category, baseCredits)

    fun scaledCompletionXp(
        category: AchievementCategory,
        baseXp: Long = BASE_COMPLETION_XP,
    ): Long = AchievementSchema.RewardScaling.scaledXp(category, baseXp)

    fun foundationAchievements(): List<Achievement> =
        foundationDefinitions.map { definition ->
            achievementFromDefinition(definition)
        }

    fun achievementFromDefinition(
        definition: AchievementDefinition,
        currentValue: Int = 0,
        completed: Boolean = false,
        completionDate: Long? = null,
    ): Achievement = AchievementMapper.fromDefinition(
        definition = definition,
        currentValue = currentValue,
        completed = completed,
        completionDate = completionDate,
    )

    internal const val BASE_COMPLETION_CREDITS = 25
    internal const val BASE_COMPLETION_XP = 60L
}
