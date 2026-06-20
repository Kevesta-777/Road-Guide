package com.example.roadguideapp.goldhunt.events

/**
 * Offline custom seasonal event definition (registered at runtime or from bundled config).
 */
internal data class SeasonalCustomEventDefinition(
    val eventKey: String,
    val displayName: String,
    val schedule: SeasonalCustomEventSchedule,
    val rewardMultiplier: Double,
    val minExplorerLevel: Int = 1,
    val achievementKey: String? = null,
    val storyFragmentKey: String? = null,
    val legendaryRelicKey: String? = null,
    val enabled: Boolean = true,
    val extensionJson: String = SeasonalEventSchema.EMPTY_EXTENSIONS_JSON,
) {
    val resolvedAchievementKey: String
        get() = achievementKey ?: SeasonalEventSchema.CustomAchievementKeys.firstParticipation(eventKey)

    val resolvedStoryFragmentKey: String
        get() = storyFragmentKey ?: SeasonalEventSchema.CustomStoryFragmentKeys.forKey(eventKey)

    val resolvedLegendaryRelicKey: String
        get() = legendaryRelicKey ?: SeasonalEventSchema.CustomLegendaryRelicKeys.forKey(eventKey)

    init {
        require(eventKey.isNotBlank()) { "eventKey must not be blank" }
        require(displayName.isNotBlank()) { "displayName must not be blank" }
        require(rewardMultiplier >= SeasonalEventSchema.BASE_MULTIPLIER) {
            "rewardMultiplier must be >= ${SeasonalEventSchema.BASE_MULTIPLIER}"
        }
        require(minExplorerLevel >= 1) { "minExplorerLevel must be at least 1" }
    }
}
