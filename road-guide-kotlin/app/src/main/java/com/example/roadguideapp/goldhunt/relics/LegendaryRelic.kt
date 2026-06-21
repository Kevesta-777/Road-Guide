package com.example.roadguideapp.goldhunt.relics

/**
 * Player-facing legendary relic progress model for the unified Gold Hunt catalog.
 *
 * Tracks shard collection toward [pieceCount] and stores completion rewards.
 * Nullable badge, title, and power keys reserve future reward dispatch without
 * requiring schema migrations.
 */
internal data class LegendaryRelic(
    val relicId: String,
    val name: String,
    val description: String,
    val category: RelicCategory,
    val rarity: RelicRarity,
    val pieceCount: Int,
    val piecesCollected: Int = 0,
    val completed: Boolean,
    val completionDate: Long?,
    val rewardCredits: Int,
    val rewardXp: Long,
    val badgeKey: String? = null,
    val titleKey: String? = null,
    val powerKey: String? = null,
    val schemaVersion: Int = RelicSchema.VERSION,
    val extensionJson: String = RelicSchema.EMPTY_EXTENSIONS_JSON,
) {
    init {
        require(relicId.isNotBlank()) { "relicId must not be blank" }
        require(name.isNotBlank()) { "name must not be blank" }
        require(description.isNotBlank()) { "description must not be blank" }
        require(pieceCount >= 1) { "pieceCount must be at least 1" }
        require(piecesCollected >= 0) { "piecesCollected must not be negative" }
        require(piecesCollected <= pieceCount) {
            "piecesCollected must not exceed pieceCount"
        }
        require(rewardCredits >= 0) { "rewardCredits must not be negative" }
        require(rewardXp >= 0L) { "rewardXp must not be negative" }
        require(!completed || piecesCollected >= pieceCount) {
            "completed relics must collect all pieces"
        }
        require(!completed || completionDate != null) {
            "completed relics must record completionDate"
        }
        require(completed || completionDate == null) {
            "incomplete relics must not record completionDate"
        }
    }

    val iconKey: String get() = category.iconKey

    val progressFraction: Float
        get() = if (pieceCount <= 0) {
            0f
        } else {
            (piecesCollected.toFloat() / pieceCount.toFloat()).coerceIn(0f, 1f)
        }

    val isComplete: Boolean get() = completed || piecesCollected >= pieceCount

    val hasBadgeReward: Boolean get() = !badgeKey.isNullOrBlank()

    val hasTitleReward: Boolean get() = !titleKey.isNullOrBlank()

    val hasPowerReward: Boolean get() = !powerKey.isNullOrBlank()

    fun withProgress(
        piecesCollected: Int,
        completed: Boolean = false,
        completionDate: Long? = null,
    ): LegendaryRelic {
        val resolvedPieces = piecesCollected.coerceIn(0, pieceCount)
        val resolvedCompleted = completed || resolvedPieces >= pieceCount
        val resolvedCompletionDate = when {
            resolvedCompleted -> completionDate ?: this.completionDate ?: System.currentTimeMillis()
            else -> null
        }
        return copy(
            piecesCollected = if (resolvedCompleted) pieceCount else resolvedPieces,
            completed = resolvedCompleted,
            completionDate = resolvedCompletionDate,
        )
    }
}
