package com.example.roadguideapp.goldhunt.treasure.encyclopedia.ui

import android.content.Context
import com.example.roadguideapp.R
import com.example.roadguideapp.goldhunt.profile.ui.ExplorerProfileFormatters
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopediaEntry
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopediaProgress
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopediaStatus
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import java.text.DateFormat
import java.util.Date
import java.util.Locale

internal class TreasureEncyclopediaFormatters(private val context: Context) {
    private val dateFormat: DateFormat =
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    fun formatProgress(progress: TreasureEncyclopediaProgress): String =
        context.getString(
            R.string.treasure_encyclopedia_progress,
            progress.foundCount,
            progress.trackableCount,
        )

    fun formatCredits(value: Int): String = ExplorerProfileFormatters.formatCredits(value)

    fun toEntryUiState(entry: TreasureEncyclopediaEntry): TreasureEncyclopediaEntryUiState =
        TreasureEncyclopediaEntryUiState(
            catalogKey = entry.catalogKey.name,
            displayName = entry.displayName,
            statusLabel = formatStatus(entry.status),
            rarityLabel = formatRarity(entry.displayRarity),
            firstDiscoveryLabel = formatFirstDiscovery(entry.firstDiscoveredAtMs, entry.isFound),
            creditsLabel = formatCredits(entry.totalCreditsEarned),
            collectionCountLabel = formatCollectionCount(entry.collectionCount, entry.isFound),
            isFound = entry.isFound,
            isMissing = entry.isMissing,
            isLocked = entry.isLocked,
        )

    private fun formatStatus(status: TreasureEncyclopediaStatus): String = when (status) {
        TreasureEncyclopediaStatus.FOUND ->
            context.getString(R.string.treasure_encyclopedia_status_found)
        TreasureEncyclopediaStatus.MISSING ->
            context.getString(R.string.treasure_encyclopedia_status_missing)
        TreasureEncyclopediaStatus.LOCKED ->
            context.getString(R.string.treasure_encyclopedia_status_locked)
    }

    private fun formatRarity(rarity: TreasureRarity): String = when (rarity) {
        TreasureRarity.COMMON -> context.getString(R.string.explorer_profile_rarity_common)
        TreasureRarity.UNCOMMON -> context.getString(R.string.explorer_profile_rarity_uncommon)
        TreasureRarity.RARE -> context.getString(R.string.explorer_profile_rarity_rare)
        TreasureRarity.EPIC -> context.getString(R.string.explorer_profile_rarity_epic)
        TreasureRarity.LEGENDARY -> context.getString(R.string.explorer_profile_rarity_legendary)
        TreasureRarity.MYTHIC -> context.getString(R.string.explorer_profile_rarity_mythic)
    }

    private fun formatFirstDiscovery(firstDiscoveredAtMs: Long?, isFound: Boolean): String {
        if (!isFound || firstDiscoveredAtMs == null || firstDiscoveredAtMs <= 0L) {
            return context.getString(R.string.treasure_encyclopedia_not_discovered)
        }
        return dateFormat.format(Date(firstDiscoveredAtMs))
    }

    private fun formatCollectionCount(count: Int, isFound: Boolean): String {
        if (!isFound) return context.getString(R.string.treasure_encyclopedia_not_discovered)
        return context.getString(R.string.treasure_encyclopedia_collection_count, count)
    }
}
