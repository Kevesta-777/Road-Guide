package com.example.roadguideapp.goldhunt.relics.registry

import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegendaryRelicRegistryTest {
    @Test
    fun defaultRegistry_registersMoreThanFiftyRelics() {
        val snapshot = LegendaryRelicRegistry.rebuild()
        assertTrue("expected >= 50 relics, got ${snapshot.size}", snapshot.size >= 50)
    }

    @Test
    fun defaultRegistry_includesStandardMilestones() {
        val keys = setOf(
            RelicSchema.MilestoneKeys.relicId(RelicCategory.EXPLORER, 50),
            RelicSchema.MilestoneKeys.relicId(RelicCategory.EXPLORER, 100),
            RelicSchema.MilestoneKeys.relicId(RelicCategory.EXPLORER, 500),
            RelicSchema.MilestoneKeys.relicId(RelicCategory.WARRIOR, 50),
            RelicSchema.MilestoneKeys.relicId(RelicCategory.HIDDEN, 500),
        )
        keys.forEach { relicId ->
            assertNotNull("missing milestone $relicId", LegendaryRelicRegistry.findById(relicId))
        }
    }

    @Test
    fun defaultRegistry_includesFoundationAndDomainEntries() {
        assertNotNull(
            LegendaryRelicRegistry.findById(
                RelicSchema.RelicKeys.forCategory(RelicCategory.STORY),
            ),
        )
        assertNotNull(
            LegendaryRelicRegistry.findById(
                RelicSchema.DomainKeys.SecretPlace.forCategory(
                    com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory.LEGENDARY_SITE,
                ),
            ),
        )
        assertNotNull(
            LegendaryRelicRegistry.findById(
                RelicSchema.DomainKeys.Panorama.forType(
                    com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType.HIDDEN_SYMBOL,
                ),
            ),
        )
    }

    @Test
    fun definitionsFor_groupsByCategory() {
        val explorerDefinitions = LegendaryRelicRegistry.definitionsFor(RelicCategory.EXPLORER)
        assertTrue(explorerDefinitions.size >= 4)
        assertTrue(explorerDefinitions.all { it.category == RelicCategory.EXPLORER })
    }

    @Test
    fun snapshot_expandsPieceDefinitionsForAllRelics() {
        val snapshot = LegendaryRelicRegistry.rebuild()
        assertTrue(snapshot.pieceCount > snapshot.size)
        snapshot.definitions.forEach { definition ->
            val pieces = snapshot.pieceDefinitionsFor(definition.relicId)
            assertEquals(definition.pieceCount, pieces.size)
        }
    }

    @Test
    fun builder_rejectsDuplicateRelicIdsAcrossProviders() {
        val duplicateProvider = object : LegendaryRelicRegistryProvider {
            override val providerId: String = "duplicate_test"
            override fun definitions(): List<LegendaryRelicDefinition> = listOf(
                LegendaryRelicRegistry.findById(
                    RelicSchema.RelicKeys.forCategory(RelicCategory.EXPLORER),
                )!!,
            )
        }
        try {
            LegendaryRelicRegistry.builder(extraProviders = listOf(duplicateProvider)).build()
            throw AssertionError("expected duplicate relicId failure")
        } catch (error: IllegalStateException) {
            assertTrue(error.message!!.contains("duplicate relicIds"))
        }
    }

    @Test
    fun builder_supportsCustomFutureProvider() {
        val futureProvider = object : LegendaryRelicRegistryProvider {
            override val providerId: String = "future_content"
            override fun definitions(): List<LegendaryRelicDefinition> = listOf(
                LegendaryRelicRegistryAdapters.catalogIndexed(RelicCategory.HIDDEN, index = 9_999),
            )
        }
        val snapshot = LegendaryRelicRegistry.rebuild(extraProviders = listOf(futureProvider))
        assertNotNull(snapshot.findById(RelicSchema.CatalogKeys.forCategory(RelicCategory.HIDDEN, 9_999)))
        assertEquals(1, snapshot.providerContributions["future_content"])
        assertTrue(snapshot.size >= 51)
    }

    @Test
    fun catalogIndexedProvider_supportsHundredPlusScale() {
        val indexedProviders = RelicCategory.ALL_ORDERED.map { category ->
            CatalogIndexedRelicRegistryProvider(category = category, indices = 1..12)
        }
        val snapshot = LegendaryRelicRegistryBuilder()
            .registerAll(indexedProviders)
            .build()
        assertTrue(snapshot.size >= 96)
    }

    @Test
    fun catalogIndexedProvider_supportsFiveHundredPlusScale() {
        val indexedProviders = RelicCategory.ALL_ORDERED.map { category ->
            CatalogIndexedRelicRegistryProvider(category = category, indices = 1..64)
        }
        val snapshot = LegendaryRelicRegistryBuilder()
            .registerAll(indexedProviders)
            .build()
        assertTrue("expected >= 500 relics, got ${snapshot.size}", snapshot.size >= 500)
    }

    @Test
    fun allRelics_mapsDefinitionsToPlayerModel() {
        val relics = LegendaryRelicRegistry.allRelics()
        assertEquals(LegendaryRelicRegistry.count(), relics.size)
        relics.forEach { relic ->
            assertNotNull(LegendaryRelicRegistry.findById(relic.relicId))
            assertEquals(relic.relicId, relic.relicId)
        }
    }

    @Test
    fun findById_returnsNullForUnknownRelic() {
        assertNull(LegendaryRelicRegistry.findById("legendary_relic:unknown:entry"))
    }

    @Test
    fun catalog_delegatesToRegistryWithoutHardCodedLists() {
        assertEquals(LegendaryRelicRegistry.count(), LegendaryRelicCatalog.allDefinitions().size)
    }
}
