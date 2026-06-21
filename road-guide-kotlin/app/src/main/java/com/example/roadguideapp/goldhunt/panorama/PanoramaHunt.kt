package com.example.roadguideapp.goldhunt.panorama

/**
 * Runtime model for a panorama hunt instance linked to a secret place.
 *
 * Tracks completion progress. Nullable hooks wire explorer-level gates,
 * story fragments, achievements, and legendary relics without requiring
 * schema changes on [PanoramaHuntType].
 */
internal data class PanoramaHunt(
    val huntId: String,
    val huntType: PanoramaHuntType,
    val secretPlaceId: String,
    val panoramaId: String,
    val difficulty: Int,
    val rewardSeed: Long,
    val completed: Boolean = false,
    val completionDate: Long? = null,
    val minimumExplorerLevel: Int? = null,
    val storyFragmentId: String? = null,
    val legendaryRelicKey: String? = null,
    val achievementKey: String? = null,
    val schemaVersion: Int = PanoramaHuntSchema.VERSION,
    val extensionJson: String = PanoramaHuntSchema.EMPTY_EXTENSIONS_JSON,
) {
    /** Instance override, then hunt-type difficulty gate. */
    val resolvedMinimumExplorerLevel: Int
        get() = minimumExplorerLevel
            ?: PanoramaHuntSchema.defaultMinimumExplorerLevel(huntType)

    /** Instance override, then hunt-type default. */
    val resolvedStoryFragmentId: String?
        get() = storyFragmentId ?: huntType.storyFragmentKey

    /** Instance override, then hunt-type default. */
    val resolvedLegendaryRelicKey: String?
        get() = legendaryRelicKey ?: huntType.legendaryRelicKey

    /** Instance override, then hunt-type default. */
    val resolvedAchievementKey: String?
        get() = achievementKey ?: huntType.achievementKey

    val hasStoryFragmentHook: Boolean
        get() = !resolvedStoryFragmentId.isNullOrBlank()

    val hasLegendaryRelicHook: Boolean
        get() = !resolvedLegendaryRelicKey.isNullOrBlank()

    val hasAchievementHook: Boolean
        get() = !resolvedAchievementKey.isNullOrBlank()

    val effectiveRewardMultiplier: Double
        get() = huntType.rewardMultiplier

    val instanceKey: String
        get() = PanoramaHuntTypeSchema.HuntInstanceKeys.forSecretPlace(secretPlaceId, huntType)

    init {
        require(huntId.isNotBlank()) { "huntId must not be blank" }
        require(secretPlaceId.isNotBlank()) { "secretPlaceId must not be blank" }
        require(panoramaId.isNotBlank()) { "panoramaId must not be blank" }
        require(difficulty > 0) { "difficulty must be positive" }
        require(minimumExplorerLevel == null || minimumExplorerLevel > 0) {
            "minimumExplorerLevel must be positive when set"
        }
        if (completionDate != null) {
            require(completionDate > 0L) { "completionDate must be positive when set" }
        }
        if (completed) {
            require(completionDate != null && completionDate > 0L) {
                "completed hunts must have a positive completionDate"
            }
        }
    }

    fun isAccessibleAtLevel(explorerLevel: Int): Boolean =
        PanoramaHuntExplorerLevelGate.isAccessible(this, explorerLevel)

    fun withCompleted(
        completed: Boolean = true,
        completionDate: Long? = this.completionDate,
    ): PanoramaHunt = copy(
        completed = completed,
        completionDate = if (completed) completionDate else null,
    )

    companion object {
        fun fromIdParts(
            huntId: String,
            huntTypeId: String,
            secretPlaceId: String,
            panoramaId: String,
            difficulty: Int? = null,
            rewardSeed: Long,
            completed: Boolean = false,
            completionDate: Long? = null,
            minimumExplorerLevel: Int? = null,
            storyFragmentId: String? = null,
            legendaryRelicKey: String? = null,
            achievementKey: String? = null,
        ): PanoramaHunt? {
            val huntType = PanoramaHuntTypeCatalog.fromId(huntTypeId) ?: return null
            return PanoramaHunt(
                huntId = huntId,
                huntType = huntType,
                secretPlaceId = secretPlaceId,
                panoramaId = panoramaId,
                difficulty = difficulty ?: PanoramaHuntSchema.defaultDifficulty(huntType),
                rewardSeed = rewardSeed,
                completed = completed,
                completionDate = completionDate,
                minimumExplorerLevel = minimumExplorerLevel,
                storyFragmentId = storyFragmentId,
                legendaryRelicKey = legendaryRelicKey,
                achievementKey = achievementKey,
            )
        }

        fun forSecretPlace(
            secretPlaceId: String,
            huntType: PanoramaHuntType,
            panoramaId: String,
            rewardSeed: Long,
            difficulty: Int? = null,
            minimumExplorerLevel: Int? = null,
            storyFragmentId: String? = null,
            legendaryRelicKey: String? = null,
            achievementKey: String? = null,
        ): PanoramaHunt = PanoramaHunt(
            huntId = PanoramaHuntId.forSecretPlace(
                secretPlaceId = secretPlaceId,
                huntType = huntType,
                panoramaId = panoramaId,
                rewardSeed = rewardSeed,
            ),
            huntType = huntType,
            secretPlaceId = secretPlaceId,
            panoramaId = panoramaId,
            difficulty = difficulty ?: PanoramaHuntSchema.defaultDifficulty(huntType),
            rewardSeed = rewardSeed,
            minimumExplorerLevel = minimumExplorerLevel,
            storyFragmentId = storyFragmentId,
            legendaryRelicKey = legendaryRelicKey,
            achievementKey = achievementKey,
        )
    }
}
