package com.example.roadguideapp.goldhunt.relics.hidden

internal data class HiddenRelicDiscoveryRecord(
    val relicId: String,
    val visibility: RelicVisibility,
    val revealedAtMs: Long?,
    val firstPieceId: String?,
    val storyFragmentKey: String?,
    val storyFragmentRecorded: Boolean,
    val achievementKey: String?,
    val achievementRecorded: Boolean,
) {
    val isHidden: Boolean get() = visibility == RelicVisibility.HIDDEN
    val isRevealed: Boolean get() = revealedAtMs != null && revealedAtMs > 0L
}

internal data class HiddenRelicDiscoveryProgress(
    val hiddenCatalogCount: Int,
    val hiddenRevealedCount: Int,
    val hiddenUndiscoveredCount: Int,
    val records: List<HiddenRelicDiscoveryRecord>,
    val presentations: List<HiddenRelicPresentation>,
) {
    val revealPercentage: Float
        get() = if (hiddenCatalogCount <= 0) {
            0f
        } else {
            (hiddenRevealedCount.toFloat() / hiddenCatalogCount.toFloat()).coerceIn(0f, 1f)
        }
}

internal data class HiddenRelicDiscoveryRevealResult(
    val isNewReveal: Boolean,
    val relicId: String? = null,
    val storyFragmentKey: String? = null,
    val achievementKey: String? = null,
    val storyFragmentRecorded: Boolean = false,
    val achievementRecorded: Boolean = false,
) {
    companion object {
        val skipped = HiddenRelicDiscoveryRevealResult(isNewReveal = false)
    }
}
