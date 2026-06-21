package com.example.roadguideapp.goldhunt.relics.showcase

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeEntryEntity
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleEntryEntity
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicPieceEntity
import com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType
import com.example.roadguideapp.goldhunt.relics.RelicRarity
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibility
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournal
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalEntry
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalProgress
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalRewardPreview
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalStatus
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantEntity
import com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryCatalog
import com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryUnlockResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegendaryRelicShowcaseMapperTest {
    @Test
    fun buildShowcase_includesCompletedRelicsBadgesTitlesAndAchievementLinks() {
        val explorerDefinition = definition(RelicCategory.EXPLORER)
        val storyDefinition = definition(RelicCategory.STORY)
        val explorerRelic = relic(
            definition = explorerDefinition,
            piecesCollected = 2,
            completed = true,
            completionDate = 2_000L,
        )
        val explorerEntry = journalEntry(
            definition = explorerDefinition,
            status = LegendaryRelicJournalStatus.COMPLETED,
            piecesCollected = 2,
            completionDateMs = 2_000L,
            rewardsGranted = true,
        )
        val storyEntry = journalEntry(
            definition = storyDefinition,
            status = LegendaryRelicJournalStatus.IN_PROGRESS,
            piecesCollected = 1,
        )
        val journal = LegendaryRelicJournal(
            entries = listOf(explorerEntry, storyEntry),
            progress = LegendaryRelicJournalProgress(
                trackableCount = 2,
                completedCount = 1,
            ),
        )
        val grant = LegendaryRelicRewardGrantEntity(
            relicId = explorerDefinition.relicId,
            creditsGranted = 50,
            xpGranted = 80L,
            storyFragmentKey = null,
            storyFragmentRecorded = false,
            titleKey = explorerDefinition.titleKey,
            titleRecorded = true,
            badgeKey = explorerDefinition.badgeKey,
            badgeRecorded = true,
            cosmeticKey = null,
            cosmeticRecorded = false,
            grantedAtMs = 2_500L,
        )
        val badgeKey = requireNotNull(explorerDefinition.badgeKey)
        val titleKey = requireNotNull(explorerDefinition.titleKey)
        val badgeEntity = AchievementBadgeEntryEntity(
            badgeKey = badgeKey,
            achievementId = explorerDefinition.relicId,
            title = "Explorer Relic Badge",
            iconKey = "relic_explorer",
            rarity = "legendary",
            unlockDateMs = 2_400L,
        )
        val titleEntity = AchievementTitleEntryEntity(
            titleKey = titleKey,
            achievementId = explorerDefinition.relicId,
            displayTitle = "Explorer of Legends",
            rarity = "legendary",
            unlockDateMs = 2_400L,
        )
        val categoryDiscoveryKey = RelicSchema.AchievementKeys.firstDiscovery(RelicCategory.EXPLORER)
        val pieceSourceKey = "achievement:explorer_piece"
        val achievementsByKey = mapOf(
            categoryDiscoveryKey to achievement(
                achievementId = categoryDiscoveryKey,
                title = "First Explorer Relic",
                completed = true,
                completionDate = 1_500L,
            ),
            pieceSourceKey to achievement(
                achievementId = pieceSourceKey,
                title = "Piece Hunter",
                completed = true,
                completionDate = 1_800L,
            ),
        )
        val achievementDefinitions = listOf(
            AchievementDefinition(
                key = explorerDefinition.relicId,
                category = AchievementCategory.EXPLORATION,
                displayName = "Complete Explorer Relic",
                legendaryRelicKey = explorerDefinition.relicId,
            ),
        )
        val pieceEntities = listOf(
            RelicPieceEntity(
                pieceId = RelicSchema.PieceKeys.forPiece(explorerDefinition.relicId, 1),
                relicId = explorerDefinition.relicId,
                pieceNumber = 1,
                totalPieces = 2,
                sourceType = RelicPieceSourceType.ACHIEVEMENT.id,
                sourceKey = pieceSourceKey,
                discovered = true,
                discoveryDateMs = 1_000L,
            ),
        )

        val showcase = LegendaryRelicShowcaseMapper.buildShowcase(
            journal = journal,
            relicsById = mapOf(explorerDefinition.relicId to explorerRelic),
            grantsByRelicId = mapOf(explorerDefinition.relicId to grant),
            badgeEntitiesByKey = mapOf(badgeKey to badgeEntity),
            titleEntitiesByKey = mapOf(titleKey to titleEntity),
            achievementsByKey = achievementsByKey,
            achievementDefinitions = achievementDefinitions,
            pieceEntitiesByRelicId = mapOf(explorerDefinition.relicId to pieceEntities),
            activeTitleKey = titleKey,
        )

        assertEquals(1, showcase.entries.size)
        assertEquals(1, showcase.progress.completedCount)
        assertEquals(2, showcase.progress.trackableCount)
        assertEquals(1, showcase.progress.badgesEarned)
        assertEquals(1, showcase.progress.titlesEarned)
        assertEquals(50, showcase.progress.totalCreditsEarned)
        assertEquals(80L, showcase.progress.totalXpEarned)
        assertEquals(2, showcase.progress.linkedAchievementsCompleted)
        assertEquals(3, showcase.progress.linkedAchievementsTotal)

        val entry = showcase.entries.first()
        assertEquals(explorerDefinition.relicId, entry.relicId)
        assertEquals(RelicRarity.LEGENDARY, entry.rarity)
        assertEquals(2_000L, entry.completionDateMs)
        assertTrue(entry.rewardsGranted)
        assertEquals(badgeKey, entry.badge?.badgeKey)
        assertTrue(entry.badge?.earned == true)
        assertEquals(2_400L, entry.badge?.unlockDateMs)
        assertEquals(titleKey, entry.title?.titleKey)
        assertTrue(entry.title?.earned == true)
        assertTrue(entry.title?.isActive == true)

        val linkTypes = entry.achievementLinks.map { it.linkType }.toSet()
        assertTrue(linkTypes.contains(LegendaryRelicShowcaseSchema.AchievementLinkTypes.GRANTS_RELIC))
        assertTrue(linkTypes.contains(LegendaryRelicShowcaseSchema.AchievementLinkTypes.CATEGORY_DISCOVERY))
        assertTrue(linkTypes.contains(LegendaryRelicShowcaseSchema.AchievementLinkTypes.PIECE_SOURCE))

        assertEquals(1, showcase.badgeHighlights.size)
        assertEquals(1, showcase.titleHighlights.size)
        assertNotNull(entry.story)
        assertTrue(entry.story?.completionUnlocked == true)
    }

    private fun definition(category: RelicCategory): LegendaryRelicDefinition =
        LegendaryRelicDefinition(
            relicId = RelicSchema.RelicKeys.forCategory(category),
            name = "${category.displayName} Relic",
            description = "Description",
            category = category,
            rarity = RelicRarity.LEGENDARY,
            pieceCount = 2,
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

    private fun journalEntry(
        definition: LegendaryRelicDefinition,
        status: LegendaryRelicJournalStatus,
        piecesCollected: Int,
        completionDateMs: Long? = null,
        rewardsGranted: Boolean = false,
    ): LegendaryRelicJournalEntry = LegendaryRelicJournalEntry(
        relicId = definition.relicId,
        displayName = definition.name,
        description = definition.description,
        category = definition.category,
        categoryLabel = definition.category.displayName,
        rarity = definition.rarity,
        status = status,
        visibility = RelicVisibility.VISIBLE,
        revealed = true,
        piecesCollected = piecesCollected,
        pieceCount = definition.pieceCount,
        piecesMissing = definition.pieceCount - piecesCollected,
        progressFraction = piecesCollected.toFloat() / definition.pieceCount.toFloat(),
        completionDateMs = completionDateMs,
        minimumExplorerLevel = definition.category.minimumExplorerLevel,
        rewardPreview = LegendaryRelicJournalRewardPreview(
            credits = 50,
            xp = 80L,
            badgeKey = definition.badgeKey,
            titleKey = definition.titleKey,
            powerKey = definition.powerKey,
            storyFragmentKey = RelicSchema.StoryFragmentKeys.completionForRelic(definition.relicId),
            rewardsGranted = rewardsGranted,
        ),
        storyPreview = buildStoryPreview(
            definition = definition,
            piecesCollected = piecesCollected,
            rewardsGranted = rewardsGranted,
            completionDateMs = completionDateMs,
        ),
        pieces = emptyList(),
    )

    private fun buildStoryPreview(
        definition: LegendaryRelicDefinition,
        piecesCollected: Int,
        rewardsGranted: Boolean,
        completionDateMs: Long?,
    ): com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryPreview {
        val relic = relic(
            definition = definition,
            piecesCollected = piecesCollected,
            completed = rewardsGranted,
            completionDate = completionDateMs,
        )
        val template = LegendaryRelicStoryCatalog.buildTemplate(
            relic = relic,
            definition = definition,
            visibility = RelicVisibility.VISIBLE,
        )
        return LegendaryRelicStoryUnlockResolver.resolve(
            template = template,
            pieces = emptyList(),
            masked = false,
            revealed = true,
            visibility = RelicVisibility.VISIBLE,
            hiddenRecord = null,
            grant = if (rewardsGranted) {
                LegendaryRelicRewardGrantEntity(
                    relicId = definition.relicId,
                    creditsGranted = 50,
                    xpGranted = 80L,
                    storyFragmentKey = RelicSchema.StoryFragmentKeys.completionForRelic(definition.relicId),
                    storyFragmentRecorded = true,
                    titleKey = definition.titleKey,
                    titleRecorded = true,
                    badgeKey = definition.badgeKey,
                    badgeRecorded = true,
                    cosmeticKey = null,
                    cosmeticRecorded = false,
                    grantedAtMs = completionDateMs ?: 2_500L,
                )
            } else {
                null
            },
            rewardHooks = emptyList(),
            hiddenStoryHookKeys = emptySet(),
        )
    }

    private fun achievement(
        achievementId: String,
        title: String,
        completed: Boolean,
        completionDate: Long? = null,
    ): Achievement = Achievement(
        achievementId = achievementId,
        title = title,
        description = "Description",
        category = AchievementCategory.EXPLORATION,
        targetValue = 1,
        currentValue = if (completed) 1 else 0,
        completed = completed,
        completionDate = completionDate,
        rewardCredits = 0,
        rewardXp = 0L,
    )
}
