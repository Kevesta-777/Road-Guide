package com.example.roadguideapp.goldhunt.relics.journal

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicPiece
import com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryRecord
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibility
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantEntity
import com.example.roadguideapp.goldhunt.relics.statistics.LegendaryRelicStatistics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegendaryRelicJournalMapperTest {
    @Test
    fun buildJournal_tracksProgressPiecesAndRewards() {
        val explorerDefinition = definition(RelicCategory.EXPLORER, pieceCount = 2)
        val hiddenDefinition = definition(RelicCategory.HIDDEN, pieceCount = 3)
        val explorerRelic = relic(
            definition = explorerDefinition,
            piecesCollected = 2,
            completed = true,
            completionDate = 2_000L,
        )
        val hiddenRelic = relic(definition = hiddenDefinition)
        val explorerPieces = listOf(
            piece(explorerDefinition.relicId, 1, discovered = true),
            piece(explorerDefinition.relicId, 2, discovered = true),
        )
        val hiddenPieces = listOf(
            piece(hiddenDefinition.relicId, 1, discovered = false),
            piece(hiddenDefinition.relicId, 2, discovered = false),
            piece(hiddenDefinition.relicId, 3, discovered = false),
        )
        val journal = LegendaryRelicJournalMapper.buildJournal(
            definitions = listOf(explorerDefinition, hiddenDefinition),
            relicsById = mapOf(
                explorerDefinition.relicId to explorerRelic,
                hiddenDefinition.relicId to hiddenRelic,
            ),
            piecesByRelicId = mapOf(
                explorerDefinition.relicId to explorerPieces,
                hiddenDefinition.relicId to hiddenPieces,
            ),
            hiddenRecordsByRelicId = mapOf(
                hiddenDefinition.relicId to HiddenRelicDiscoveryRecord(
                    relicId = hiddenDefinition.relicId,
                    visibility = RelicVisibility.HIDDEN,
                    revealedAtMs = null,
                    firstPieceId = null,
                    storyFragmentKey = "story_fragment_relic_hidden",
                    storyFragmentRecorded = false,
                    achievementKey = "relic_category:hidden:first_discovery",
                    achievementRecorded = false,
                ),
            ),
            grantedRelicIds = setOf(explorerDefinition.relicId),
            grantsByRelicId = mapOf(
                explorerDefinition.relicId to com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantEntity(
                    relicId = explorerDefinition.relicId,
                    creditsGranted = 50,
                    xpGranted = 80L,
                    storyFragmentKey = RelicSchema.StoryFragmentKeys.completionForRelic(
                        explorerDefinition.relicId,
                    ),
                    storyFragmentRecorded = true,
                    titleKey = explorerDefinition.titleKey,
                    titleRecorded = true,
                    badgeKey = explorerDefinition.badgeKey,
                    badgeRecorded = true,
                    cosmeticKey = null,
                    cosmeticRecorded = false,
                    grantedAtMs = 2_500L,
                ),
            ),
            rewardHooksByRelicId = emptyMap(),
            hiddenStoryHookKeysByRelicId = emptyMap(),
            explorerLevel = 35,
            statistics = LegendaryRelicStatistics(
                catalogCount = 2,
                completedCount = 1,
                piecesCollected = 2,
                totalPieces = 5,
                hiddenRelicsDiscovered = 0,
                hiddenRelicsCatalogCount = 1,
                creditsEarned = 50,
                xpEarned = 80L,
            ),
        )

        assertEquals(2, journal.progress.trackableCount)
        assertEquals(1, journal.progress.completedCount)
        assertEquals(2, journal.progress.piecesFound)
        assertEquals(3, journal.progress.piecesMissing)
        assertEquals(50, journal.progress.totalCreditsEarned)

        val explorerEntry = journal.entries.first { it.relicId == explorerDefinition.relicId }
        assertEquals(LegendaryRelicJournalStatus.COMPLETED, explorerEntry.status)
        assertEquals(2, explorerEntry.piecesCollected)
        assertEquals(0, explorerEntry.piecesMissing)
        assertTrue(explorerEntry.rewardPreview.rewardsGranted)
        assertEquals(
            RelicSchema.StoryFragmentKeys.completionForRelic(explorerDefinition.relicId),
            explorerEntry.rewardPreview.storyFragmentKey,
        )
        assertTrue(explorerEntry.storyPreview.hasStoryContent)
        assertTrue(explorerEntry.storyPreview.completionStory?.unlocked == true)
        assertEquals(2, explorerEntry.pieces.count { it.discovered })

        val hiddenEntry = journal.entries.first { it.relicId == hiddenDefinition.relicId }
        assertEquals(LegendaryRelicJournalStatus.LOCKED, hiddenEntry.status)
        assertEquals("???", hiddenEntry.displayName)
        assertEquals(0, hiddenEntry.piecesCollected)
        assertTrue(hiddenEntry.pieces.all { !it.discovered })
    }

    private fun definition(category: RelicCategory, pieceCount: Int): LegendaryRelicDefinition =
        LegendaryRelicDefinition(
            relicId = RelicSchema.RelicKeys.forCategory(category),
            name = "${category.displayName} Relic",
            description = "Description",
            category = category,
            rarity = RelicRarity.LEGENDARY,
            pieceCount = pieceCount,
            badgeKey = "badge_relic:${category.id.lowercase()}",
            titleKey = "title_relic:${category.id.lowercase()}",
            powerKey = "relic_power:${category.id.lowercase()}",
        )

    private fun relic(
        definition: LegendaryRelicDefinition,
        piecesCollected: Int = 0,
        completed: Boolean = false,
        completionDate: Long? = null,
    ): LegendaryRelic = LegendaryRelic(
        relicId = definition.relicId,
        name = definition.name,
        description = definition.description,
        category = definition.category,
        rarity = definition.rarity,
        pieceCount = definition.pieceCount,
        piecesCollected = piecesCollected,
        completed = completed,
        completionDate = completionDate,
        rewardCredits = 50,
        rewardXp = 80L,
        badgeKey = definition.badgeKey,
        titleKey = definition.titleKey,
        powerKey = definition.powerKey,
    )

    private fun piece(
        relicId: String,
        pieceNumber: Int,
        discovered: Boolean,
    ): RelicPiece = RelicPiece(
        pieceId = RelicSchema.PieceKeys.forPiece(relicId, pieceNumber),
        relicId = relicId,
        pieceNumber = pieceNumber,
        totalPieces = 3,
        sourceType = RelicPieceSourceType.ACHIEVEMENT,
        discovered = discovered,
        discoveryDate = if (discovered) 1_000L else null,
    )
}
