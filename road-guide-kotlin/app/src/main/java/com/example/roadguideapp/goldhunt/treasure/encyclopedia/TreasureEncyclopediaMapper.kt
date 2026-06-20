package com.example.roadguideapp.goldhunt.treasure.encyclopedia

import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinitionKey
import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureTypeTemplate
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

internal object TreasureEncyclopediaMapper {
    fun toDomain(
        template: TreasureTypeTemplate,
        entity: TreasureEncyclopediaEntryEntity?,
    ): TreasureEncyclopediaEntry {
        val status = entity?.status?.let(TreasureEncyclopediaStatus::fromId)
            ?: TreasureEncyclopediaCatalog.initialStatus(template)
        val bestRarity = entity?.bestRarityId?.let(TreasureRarity::fromId)
        return TreasureEncyclopediaEntry(
            catalogKey = template.key,
            displayName = template.displayName,
            status = status,
            catalogRarity = template.rarity,
            firstDiscoveredAtMs = entity?.firstDiscoveredAtMs,
            bestRarity = bestRarity,
            totalCreditsEarned = entity?.totalCreditsEarned ?: 0,
            collectionCount = entity?.collectionCount ?: 0,
            enabled = template.enabled,
            schemaVersion = entity?.schemaVersion ?: TreasureEncyclopediaSchema.VERSION,
            extensionJson = entity?.extensionJson ?: TreasureEncyclopediaSchema.EMPTY_EXTENSIONS_JSON,
        )
    }

    fun toEntity(
        catalogKey: TreasureDefinitionKey,
        status: TreasureEncyclopediaStatus,
        firstDiscoveredAtMs: Long?,
        bestRarity: TreasureRarity?,
        totalCreditsEarned: Int,
        collectionCount: Int,
        timestampMs: Long,
    ): TreasureEncyclopediaEntryEntity = TreasureEncyclopediaEntryEntity(
        catalogKey = catalogKey.name,
        status = status.id,
        firstDiscoveredAtMs = firstDiscoveredAtMs,
        bestRarityId = bestRarity?.id,
        totalCreditsEarned = totalCreditsEarned,
        collectionCount = collectionCount,
        updatedAtMs = timestampMs,
    )

    fun buildProgress(entries: List<TreasureEncyclopediaEntry>): TreasureEncyclopediaProgress {
        val trackable = entries.filter { it.enabled }
        val found = trackable.count { it.isFound }
        val locked = entries.count { it.isLocked }
        val credits = entries.sumOf { it.totalCreditsEarned }
        return TreasureEncyclopediaProgress(
            foundCount = found,
            trackableCount = trackable.size,
            lockedCount = locked,
            totalCreditsEarned = credits,
        )
    }
}
