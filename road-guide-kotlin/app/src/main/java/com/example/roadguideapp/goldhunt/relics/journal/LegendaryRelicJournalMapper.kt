package com.example.roadguideapp.goldhunt.relics.journal

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicPiece
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicDiscoveryRecord
import com.example.roadguideapp.goldhunt.relics.hidden.HiddenRelicPresentationMapper
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibility
import com.example.roadguideapp.goldhunt.relics.hidden.RelicVisibilityResolver
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantEntity
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardHookEntity
import com.example.roadguideapp.goldhunt.relics.statistics.LegendaryRelicStatistics
import com.example.roadguideapp.goldhunt.relics.chain.RelicHuntChainResolver
import com.example.roadguideapp.goldhunt.relics.chain.RelicHuntChainRewardPreviewMapper
import com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryCatalog
import com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryUnlockResolver

internal object LegendaryRelicJournalMapper {
    fun buildJournal(
        definitions: List<LegendaryRelicDefinition>,
        relicsById: Map<String, LegendaryRelic>,
        piecesByRelicId: Map<String, List<RelicPiece>>,
        hiddenRecordsByRelicId: Map<String, HiddenRelicDiscoveryRecord>,
        grantedRelicIds: Set<String>,
        grantsByRelicId: Map<String, LegendaryRelicRewardGrantEntity>,
        rewardHooksByRelicId: Map<String, List<LegendaryRelicRewardHookEntity>>,
        hiddenStoryHookKeysByRelicId: Map<String, Set<String>>,
        explorerLevel: Int,
        statistics: LegendaryRelicStatistics,
    ): LegendaryRelicJournal {
        val chainContext = RelicHuntChainResolver.buildContext(
            relicsById = relicsById,
            chainHooks = rewardHooksByRelicId.values.flatten()
                .filter { it.hookType == com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardSchema.HookTypes.CHAIN_UNLOCK },
        )
        val entries = definitions.map { definition ->
            val relic = relicsById[definition.relicId]
                ?: LegendaryRelicCatalog.relicFromDefinition(definition)
            val hiddenRecord = hiddenRecordsByRelicId[definition.relicId]
            val visibility = hiddenRecord?.visibility
                ?: RelicVisibilityResolver.forDefinition(definition)
            val revealedAtMs = hiddenRecord?.revealedAtMs
            val presentation = HiddenRelicPresentationMapper.present(
                relic = relic,
                visibility = visibility,
                revealedAtMs = revealedAtMs,
            )
            val status = LegendaryRelicJournalStatusResolver.resolve(
                relic = relic,
                visibility = visibility,
                revealed = presentation.revealed,
                explorerLevel = explorerLevel,
                chainContext = chainContext,
            )
            val relicPieces = piecesByRelicId[definition.relicId].orEmpty()
                .sortedBy { it.pieceNumber }
            toEntry(
                definition = definition,
                relic = relic,
                presentationName = presentation.name,
                presentationDescription = presentation.description,
                presentationCategoryLabel = presentation.categoryLabel,
                visibility = visibility,
                revealed = presentation.revealed,
                status = status,
                pieces = relicPieces,
                masked = presentation.isMasked,
                rewardPreview = buildRewardPreview(
                    relic = relic,
                    definition = definition,
                    rewardsGranted = relic.relicId in grantedRelicIds,
                    masked = presentation.isMasked,
                ),
                storyPreview = buildStoryPreview(
                    definition = definition,
                    relic = relic,
                    visibility = visibility,
                    revealed = presentation.revealed,
                    pieces = relicPieces,
                    masked = presentation.isMasked,
                    hiddenRecord = hiddenRecord,
                    grant = grantsByRelicId[definition.relicId],
                    rewardHooks = rewardHooksByRelicId[definition.relicId].orEmpty(),
                    hiddenStoryHookKeys = hiddenStoryHookKeysByRelicId[definition.relicId].orEmpty(),
                ),
            )
        }.sortedWith(
            compareBy<LegendaryRelicJournalEntry> { categoryOrder(it.category) }
                .thenBy { statusOrder(it.status) }
                .thenBy { it.displayName },
        )
        return LegendaryRelicJournal(
            entries = entries,
            progress = buildProgress(entries, statistics),
        )
    }

    private fun toEntry(
        definition: LegendaryRelicDefinition,
        relic: LegendaryRelic,
        presentationName: String,
        presentationDescription: String,
        presentationCategoryLabel: String,
        visibility: RelicVisibility,
        revealed: Boolean,
        status: LegendaryRelicJournalStatus,
        pieces: List<RelicPiece>,
        masked: Boolean,
        rewardPreview: LegendaryRelicJournalRewardPreview,
        storyPreview: com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryPreview,
    ): LegendaryRelicJournalEntry {
        val piecesCollected = if (masked) 0 else relic.piecesCollected
        val pieceCount = relic.pieceCount
        return LegendaryRelicJournalEntry(
            relicId = relic.relicId,
            displayName = presentationName,
            description = presentationDescription,
            category = relic.category,
            categoryLabel = presentationCategoryLabel,
            rarity = relic.rarity,
            status = status,
            visibility = visibility,
            revealed = revealed,
            piecesCollected = piecesCollected,
            pieceCount = pieceCount,
            piecesMissing = (pieceCount - piecesCollected).coerceAtLeast(0),
            progressFraction = if (pieceCount <= 0) {
                0f
            } else {
                (piecesCollected.toFloat() / pieceCount.toFloat()).coerceIn(0f, 1f)
            },
            completionDateMs = relic.completionDate,
            minimumExplorerLevel = RelicSchema.CategoryGates.minExplorerLevel(relic.category),
            rewardPreview = rewardPreview,
            storyPreview = storyPreview,
            pieces = pieces.map { piece ->
                LegendaryRelicJournalPieceEntry(
                    pieceId = piece.pieceId,
                    pieceNumber = piece.pieceNumber,
                    discovered = if (masked) false else piece.discovered,
                    discoveryDateMs = if (masked) null else piece.discoveryDate,
                    sourceType = piece.sourceType,
                    masked = masked,
                )
            },
        )
    }

    private fun buildRewardPreview(
        relic: LegendaryRelic,
        definition: LegendaryRelicDefinition,
        rewardsGranted: Boolean,
        masked: Boolean,
    ): LegendaryRelicJournalRewardPreview {
        if (masked) {
            return LegendaryRelicJournalRewardPreview()
        }
        val preview = LegendaryRelicJournalRewardPreview(
            credits = relic.rewardCredits,
            xp = relic.rewardXp,
            badgeKey = relic.badgeKey,
            titleKey = relic.titleKey,
            powerKey = relic.powerKey,
            storyFragmentKey = LegendaryRelicStoryCatalog.completionStoryFragmentKey(relic, definition),
            rewardsGranted = rewardsGranted,
        )
        return RelicHuntChainRewardPreviewMapper.appendTo(preview, relic.relicId)
    }

    private fun buildStoryPreview(
        definition: LegendaryRelicDefinition,
        relic: LegendaryRelic,
        visibility: RelicVisibility,
        revealed: Boolean,
        pieces: List<RelicPiece>,
        masked: Boolean,
        hiddenRecord: HiddenRelicDiscoveryRecord?,
        grant: LegendaryRelicRewardGrantEntity?,
        rewardHooks: List<LegendaryRelicRewardHookEntity>,
        hiddenStoryHookKeys: Set<String>,
    ): com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryPreview {
        val template = LegendaryRelicStoryCatalog.buildTemplate(
            relic = relic,
            definition = definition,
            visibility = visibility,
        )
        return LegendaryRelicStoryUnlockResolver.resolve(
            template = template,
            pieces = pieces,
            masked = masked,
            revealed = revealed,
            visibility = visibility,
            hiddenRecord = hiddenRecord,
            grant = grant,
            rewardHooks = rewardHooks,
            hiddenStoryHookKeys = hiddenStoryHookKeys,
        )
    }

    fun buildProgress(
        entries: List<LegendaryRelicJournalEntry>,
        statistics: LegendaryRelicStatistics,
    ): LegendaryRelicJournalProgress = LegendaryRelicJournalProgress(
        trackableCount = entries.size,
        completedCount = entries.count { it.isCompleted },
        inProgressCount = entries.count { it.isInProgress },
        missingCount = entries.count { it.isMissing },
        lockedCount = entries.count { it.isLocked },
        hiddenMaskedCount = entries.count { it.isHiddenMasked },
        piecesFound = statistics.piecesCollected,
        piecesMissing = (statistics.totalPieces - statistics.piecesCollected).coerceAtLeast(0),
        totalPieces = statistics.totalPieces,
        hiddenRelicsDiscovered = statistics.hiddenRelicsDiscovered,
        hiddenRelicsCatalogCount = statistics.hiddenRelicsCatalogCount,
        totalCreditsEarned = statistics.creditsEarned,
        totalXpEarned = statistics.xpEarned,
    )

    fun grantedRelicIds(grants: List<LegendaryRelicRewardGrantEntity>): Set<String> =
        grants.map { it.relicId }.toSet()

    private fun categoryOrder(category: RelicCategory): Int =
        RelicCategory.ALL_ORDERED.indexOf(category).takeIf { it >= 0 } ?: Int.MAX_VALUE

    private fun statusOrder(status: LegendaryRelicJournalStatus): Int = when (status) {
        LegendaryRelicJournalStatus.COMPLETED -> 0
        LegendaryRelicJournalStatus.IN_PROGRESS -> 1
        LegendaryRelicJournalStatus.MISSING -> 2
        LegendaryRelicJournalStatus.HIDDEN_MASKED -> 3
        LegendaryRelicJournalStatus.LOCKED -> 4
    }
}
