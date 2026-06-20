package com.example.roadguideapp.goldhunt.achievements

/**
 * Player-facing achievement progress model for the unified Gold Hunt catalog.
 *
 * Tracks progress toward a single milestone ([targetValue]) and stores completion
 * rewards. Nullable relic, title, and badge keys reserve future reward dispatch
 * without requiring schema migrations.
 */
internal data class Achievement(
    val achievementId: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val targetValue: Int,
    val currentValue: Int,
    val completed: Boolean,
    val completionDate: Long?,
    val rewardCredits: Int,
    val rewardXp: Long,
    val legendaryRelicKey: String? = null,
    val titleKey: String? = null,
    val badgeKey: String? = null,
    val schemaVersion: Int = AchievementSchema.VERSION,
    val extensionJson: String = AchievementSchema.EMPTY_EXTENSIONS_JSON,
) {
    init {
        require(achievementId.isNotBlank()) { "achievementId must not be blank" }
        require(title.isNotBlank()) { "title must not be blank" }
        require(description.isNotBlank()) { "description must not be blank" }
        require(targetValue >= 1) { "targetValue must be at least 1" }
        require(currentValue >= 0) { "currentValue must not be negative" }
        require(rewardCredits >= 0) { "rewardCredits must not be negative" }
        require(rewardXp >= 0L) { "rewardXp must not be negative" }
        require(!completed || currentValue >= targetValue) {
            "completed achievements must meet or exceed targetValue"
        }
        require(!completed || completionDate != null) {
            "completed achievements must record completionDate"
        }
        require(completed || completionDate == null) {
            "incomplete achievements must not record completionDate"
        }
    }

    val iconKey: String get() = category.iconKey

    val progressFraction: Float
        get() = if (targetValue <= 0) {
            0f
        } else {
            (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
        }

    val isComplete: Boolean get() = completed || currentValue >= targetValue

    val hasRelicReward: Boolean get() = !legendaryRelicKey.isNullOrBlank()

    val hasTitleReward: Boolean get() = !titleKey.isNullOrBlank()

    val hasBadgeReward: Boolean get() = !badgeKey.isNullOrBlank()

    fun withProgress(
        currentValue: Int,
        completed: Boolean = false,
        completionDate: Long? = null,
    ): Achievement {
        val resolvedCompleted = completed || currentValue >= targetValue
        val resolvedCompletionDate = when {
            resolvedCompleted -> completionDate ?: this.completionDate ?: System.currentTimeMillis()
            else -> null
        }
        return copy(
            currentValue = currentValue.coerceAtLeast(0),
            completed = resolvedCompleted,
            completionDate = resolvedCompletionDate,
        )
    }
}
