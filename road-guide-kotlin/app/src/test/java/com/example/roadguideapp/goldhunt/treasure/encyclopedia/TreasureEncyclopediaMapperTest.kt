package com.example.roadguideapp.goldhunt.treasure.encyclopedia

import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinitionCatalog
import com.example.roadguideapp.goldhunt.treasure.metadata.TreasureDefinitionKey
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import org.junit.Assert.assertEquals
import org.junit.Test

class TreasureEncyclopediaMapperTest {
    @Test
    fun toDomain_foundEntry_mapsFields() {
        val entity = TreasureEncyclopediaEntryEntity(
            catalogKey = TreasureDefinitionKey.GOLD_STAR.name,
            status = TreasureEncyclopediaStatus.FOUND.id,
            firstDiscoveredAtMs = 1_000L,
            bestRarityId = TreasureRarity.RARE.id,
            totalCreditsEarned = 42,
            collectionCount = 3,
        )
        val entry = TreasureEncyclopediaMapper.toDomain(TreasureDefinitionCatalog.GoldStar, entity)
        assertEquals(TreasureEncyclopediaStatus.FOUND, entry.status)
        assertEquals(1_000L, entry.firstDiscoveredAtMs)
        assertEquals(TreasureRarity.RARE, entry.bestRarity)
        assertEquals(42, entry.totalCreditsEarned)
        assertEquals(3, entry.collectionCount)
    }

    @Test
    fun buildProgress_countsFoundAndTrackable() {
        val entries = listOf(
            TreasureEncyclopediaMapper.toDomain(
                TreasureDefinitionCatalog.GoldStar,
                TreasureEncyclopediaMapper.toEntity(
                    catalogKey = TreasureDefinitionKey.GOLD_STAR,
                    status = TreasureEncyclopediaStatus.FOUND,
                    firstDiscoveredAtMs = 1L,
                    bestRarity = TreasureRarity.COMMON,
                    totalCreditsEarned = 5,
                    collectionCount = 1,
                    timestampMs = 1L,
                ),
            ),
            TreasureEncyclopediaMapper.toDomain(TreasureDefinitionCatalog.Flower, null),
            TreasureEncyclopediaMapper.toDomain(TreasureDefinitionCatalog.LegendaryRelic, null),
        )
        val progress = TreasureEncyclopediaMapper.buildProgress(entries)
        assertEquals(1, progress.foundCount)
        assertEquals(2, progress.trackableCount)
        assertEquals(1, progress.lockedCount)
        assertEquals(5, progress.totalCreditsEarned)
    }
}
