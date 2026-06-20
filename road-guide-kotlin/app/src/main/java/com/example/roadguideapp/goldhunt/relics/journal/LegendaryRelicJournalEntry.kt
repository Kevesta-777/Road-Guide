package com.example.roadguideapp.goldhunt.relics.journal

import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibility
import com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryPreview

internal data class LegendaryRelicJournalEntry(
    val relicId: String,
    val displayName: String,
    val description: String,
    val category: RelicCategory,
    val categoryLabel: String,
    val rarity: RelicRarity,
    val status: LegendaryRelicJournalStatus,
    val visibility: RelicVisibility,
    val revealed: Boolean,
    val piecesCollected: Int,
    val pieceCount: Int,
    val piecesMissing: Int,
    val progressFraction: Float,
    val completionDateMs: Long?,
    val minimumExplorerLevel: Int,
    val rewardPreview: LegendaryRelicJournalRewardPreview,
    val storyPreview: LegendaryRelicStoryPreview = LegendaryRelicStoryPreview(),
    val pieces: List<LegendaryRelicJournalPieceEntry>,
) {
    val isCompleted: Boolean get() = status == LegendaryRelicJournalStatus.COMPLETED
    val isInProgress: Boolean get() = status == LegendaryRelicJournalStatus.IN_PROGRESS
    val isMissing: Boolean get() = status == LegendaryRelicJournalStatus.MISSING
    val isLocked: Boolean get() = status == LegendaryRelicJournalStatus.LOCKED
    val isHiddenMasked: Boolean get() = status == LegendaryRelicJournalStatus.HIDDEN_MASKED
}
