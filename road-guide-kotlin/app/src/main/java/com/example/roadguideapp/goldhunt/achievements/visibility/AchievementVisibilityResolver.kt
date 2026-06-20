package com.example.roadguideapp.goldhunt.achievements.visibility

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema

internal object AchievementVisibilityResolver {
    fun forDefinition(definition: AchievementDefinition): AchievementVisibility {
        visibilityOverride(definition)?.let { return it }
        return resolveFromRules(definition)
    }

    fun forKey(
        key: String,
        category: AchievementCategory,
        targetCount: Int = 1,
    ): AchievementVisibility = resolveFromRules(
        key = key,
        category = category,
        targetCount = targetCount,
    )

    private fun resolveFromRules(definition: AchievementDefinition): AchievementVisibility =
        resolveFromRules(
            key = definition.key,
            category = definition.category,
            targetCount = definition.targetCount,
        )

    private fun resolveFromRules(
        key: String,
        category: AchievementCategory,
        targetCount: Int,
    ): AchievementVisibility {
        when {
            key.endsWith(AchievementVisibilitySchema.MilestoneSuffixes.TIER_1000) ->
                return AchievementVisibility.SECRET
            category == AchievementCategory.LEGENDARY_RELIC && targetCount >= 500 ->
                return AchievementVisibility.SECRET
            key.endsWith(AchievementVisibilitySchema.MilestoneSuffixes.TIER_500) ->
                return AchievementVisibility.HIDDEN
            key.endsWith(":${AchievementVisibilitySchema.ActionSuffixes.MASTERY}") ->
                return when (category) {
                    AchievementCategory.MASTER_EXPLORER,
                    AchievementCategory.LEGENDARY_RELIC,
                    -> AchievementVisibility.SECRET
                    else -> AchievementVisibility.HIDDEN
                }
            key.endsWith(":${AchievementVisibilitySchema.ActionSuffixes.COLLECTOR}") ->
                return AchievementVisibility.HIDDEN
            category == AchievementCategory.MASTER_EXPLORER ->
                return AchievementVisibility.HIDDEN
            category == AchievementCategory.STREAK && targetCount >= 100 ->
                return AchievementVisibility.SECRET
            category == AchievementCategory.STREAK && targetCount >= 30 ->
                return AchievementVisibility.HIDDEN
            category == AchievementCategory.SEASONAL_EVENT &&
                key.contains(":mastery") ->
                return AchievementVisibility.HIDDEN
        }
        return AchievementVisibility.VISIBLE
    }

    private fun visibilityOverride(definition: AchievementDefinition): AchievementVisibility? {
        val extensionJson = definition.extensionJson
        if (extensionJson.isBlank() || extensionJson == AchievementSchema.EMPTY_EXTENSIONS_JSON) {
            return null
        }
        val visibilityPattern = Regex(
            """"${AchievementSchema.ExtensionKeys.VISIBILITY}"\s*:\s*"([^"]+)"""",
            RegexOption.IGNORE_CASE,
        )
        val match = visibilityPattern.find(extensionJson) ?: return null
        return AchievementVisibility.fromId(match.groupValues[1])
    }
}
