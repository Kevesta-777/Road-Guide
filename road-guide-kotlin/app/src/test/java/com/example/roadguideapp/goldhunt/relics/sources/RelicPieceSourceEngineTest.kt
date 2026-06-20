package com.example.roadguideapp.goldhunt.relics.sources

import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.events.SeasonalEventType
import com.example.roadguideapp.goldhunt.panorama.PanoramaHuntType
import com.example.roadguideapp.goldhunt.radar.RadarType
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicPieceCatalog
import com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.registry.CatalogIndexedRelicRegistryProvider
import com.example.roadguideapp.goldhunt.relics.registry.LegendaryRelicRegistry
import com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelicPieceSourceEngineTest {
    private val engine = RelicPieceSourceEngine.create()

    @Test
    fun index_registersAllSevenSourceTypes() {
        val sourceTypes = RelicPieceSourceIndex.fromRegistry()
            .pieceDefinitions
            .map { it.sourceType }
            .toSet()
        RelicPieceSourceType.ALL_ORDERED.forEach { sourceType ->
            assertTrue("missing source type $sourceType", sourceType in sourceTypes)
        }
    }

    @Test
    fun secretPlaceDiscovery_grantsDomainAndFoundationPieces() {
        val category = SecretPlaceCategory.LEGENDARY_SITE
        val result = engine.resolveAndDispatch(
            RelicPieceSourceEvent.SecretPlaceDiscovered(
                category = category,
                secretPlaceId = "sp_legendary_1",
                timestampMs = 1_000L,
            ),
        )
        assertTrue(result.hasGrants)
        val relicIds = result.grants.map { it.relicId }.toSet()
        assertTrue(
            RelicSchema.DomainKeys.SecretPlace.forCategory(category) in relicIds,
        )
        assertTrue(
            RelicSchema.RelicKeys.forCategory(RelicCategory.ROYAL) in relicIds ||
                RelicSchema.RelicKeys.forCategory(RelicCategory.HIDDEN) in relicIds,
        )
    }

    @Test
    fun treasureClusterCompletion_grantsNextClusterRelicPiece() {
        val clusterType = ClusterType.ANCIENT_VAULT
        val relicId = RelicSchema.DomainKeys.Cluster.forType(clusterType)
        val firstPiece = RelicPieceCatalog.pieceDefinitionsFor(relicId).first()

        val result = engine.resolveAndDispatch(
            RelicPieceSourceEvent.TreasureClusterCompleted(
                clusterType = clusterType,
                clusterId = "cluster_1",
                timestampMs = 2_000L,
            ),
        )

        assertTrue(result.hasGrants)
        val domainGrant = result.grants.first { it.relicId == relicId }
        assertEquals(firstPiece.pieceId, domainGrant.pieceId)
        assertEquals(RelicPieceSourceType.TREASURE_CLUSTER, domainGrant.sourceType)
    }

    @Test
    fun treasureClusterCompletion_progressesToNextUndiscoveredPiece() {
        val clusterType = ClusterType.ANCIENT_VAULT
        val relicId = RelicSchema.DomainKeys.Cluster.forType(clusterType)
        val pieces = RelicPieceCatalog.pieceDefinitionsFor(relicId)
        val discovered = setOf(pieces[0].pieceId, pieces[1].pieceId)

        val result = engine.resolveAndDispatch(
            RelicPieceSourceEvent.TreasureClusterCompleted(
                clusterType = clusterType,
                clusterId = "cluster_2",
                timestampMs = 3_000L,
            ),
            discoveredPieceIds = discovered,
        )

        val domainGrant = result.grants.first { it.relicId == relicId }
        assertEquals(pieces[2].pieceId, domainGrant.pieceId)
    }

    @Test
    fun storyFragmentUnlock_grantsStoryRelicPiece() {
        val fragmentKey = RelicSchema.StoryFragmentKeys.forCategory(RelicCategory.STORY)
        val result = engine.resolveAndDispatch(
            RelicPieceSourceEvent.StoryFragmentUnlocked(
                storyFragmentKey = fragmentKey,
                timestampMs = 4_000L,
            ),
        )
        assertTrue(result.hasGrants)
        assertTrue(
            result.grants.any {
                it.relicId == RelicSchema.RelicKeys.forCategory(RelicCategory.STORY)
            },
        )
    }

    @Test
    fun panoramaHuntCompletion_grantsDomainAndFoundationPieces() {
        val huntType = PanoramaHuntType.HIDDEN_SYMBOL
        val result = engine.resolveAndDispatch(
            RelicPieceSourceEvent.PanoramaHuntCompleted(
                huntType = huntType,
                huntId = "hunt_1",
                timestampMs = 5_000L,
            ),
        )
        val relicIds = result.grants.map { it.relicId }.toSet()
        assertTrue(RelicSchema.DomainKeys.Panorama.forType(huntType) in relicIds)
    }

    @Test
    fun seasonalEventGrant_grantsSeasonalRelicPiece() {
        val eventType = SeasonalEventType.SPRING_BLOSSOM
        val result = engine.resolveAndDispatch(
            RelicPieceSourceEvent.SeasonalEventGranted(
                eventType = eventType,
                grantKey = RelicSchema.SourceDomains.SEASONAL_EVENT,
                timestampMs = 6_000L,
            ),
        )
        assertEquals(1, result.grants.size)
        assertEquals(
            RelicSchema.DomainKeys.SeasonalEvent.forType(eventType),
            result.grants.single().relicId,
        )
    }

    @Test
    fun achievementCompletion_grantsExplorerRelicPiece() {
        val achievementKey = RelicSchema.AchievementKeys.firstDiscovery(RelicCategory.EXPLORER)
        val result = engine.resolveAndDispatch(
            RelicPieceSourceEvent.AchievementCompleted(
                achievementKey = achievementKey,
                timestampMs = 7_000L,
            ),
        )
        assertTrue(result.hasGrants)
        assertTrue(
            result.grants.any {
                it.relicId == RelicSchema.RelicKeys.forCategory(RelicCategory.EXPLORER)
            },
        )
    }

    @Test
    fun radarDiscovery_grantsRadarRelicPiece() {
        val radarType = RadarType.TREASURE_RADAR
        val result = engine.resolveAndDispatch(
            RelicPieceSourceEvent.RadarDiscovery(
                radarType = radarType,
                targetId = "target_1",
                timestampMs = 8_000L,
            ),
        )
        assertEquals(1, result.grants.size)
        assertEquals(
            RelicSchema.DomainKeys.Radar.forType(radarType),
            result.grants.single().relicId,
        )
    }

    @Test
    fun dispatch_producesIdempotentGrantOperations() {
        val radarType = RadarType.BASIC_RADAR
        val relicId = RelicSchema.DomainKeys.Radar.forType(radarType)
        val result = engine.resolveAndDispatch(
            RelicPieceSourceEvent.RadarDiscovery(
                radarType = radarType,
                targetId = "target_basic",
                timestampMs = 9_000L,
            ),
        )
        val grant = result.grants.first { it.relicId == relicId }
        assertNotNull(grant.eventId)
        assertEquals(grant.pieceId, grant.hookKey)
        assertTrue(grant.eventId.startsWith(RelicPieceSourceSchema.EventPrefixes.GRANT))
    }

    @Test
    fun skippedPieces_areNotGrantedAgain() {
        val clusterType = ClusterType.ROADSIDE_CACHE
        val relicId = RelicSchema.DomainKeys.Cluster.forType(clusterType)
        val allPieceIds = RelicPieceCatalog.pieceDefinitionsFor(relicId).map { it.pieceId }.toSet()

        val result = engine.resolveAndDispatch(
            RelicPieceSourceEvent.TreasureClusterCompleted(
                clusterType = clusterType,
                clusterId = "cluster_complete",
                timestampMs = 10_000L,
            ),
            discoveredPieceIds = allPieceIds,
        )

        assertTrue(result.grants.isEmpty())
        assertTrue(result.skippedAlreadyDiscovered > 0)
    }

    @Test
    fun registrySnapshot_supportsLargeCatalogIndexing() {
        val indexedProvider = CatalogIndexedRelicRegistryProvider(
            category = RelicCategory.MYTHICAL,
            indices = 1..120,
        )
        val snapshot = LegendaryRelicRegistry.rebuild(extraProviders = listOf(indexedProvider))
        val index = RelicPieceSourceIndex.fromSnapshot(snapshot)
        assertTrue(index.size() >= snapshot.pieceCount)
    }
}
