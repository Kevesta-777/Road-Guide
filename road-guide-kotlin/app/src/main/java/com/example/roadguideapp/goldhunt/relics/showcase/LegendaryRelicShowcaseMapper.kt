package com.example.roadguideapp.goldhunt.relics.showcase

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeEntryEntity
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleEntryEntity
import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.RelicPieceEntity
import com.example.roadguideapp.goldhunt.relics.RelicPieceSourceType
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournal
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalEntry
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardBadgeCatalog
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardGrantEntity
import com.example.roadguideapp.goldhunt.relics.rewards.LegendaryRelicRewardTitleCatalog

internal object LegendaryRelicShowcaseMapper {
    fun buildShowcase(
        journal: LegendaryRelicJournal,
        relicsById: Map<String, LegendaryRelic>,
        grantsByRelicId: Map<String, LegendaryRelicRewardGrantEntity>,
        badgeEntitiesByKey: Map<String, AchievementBadgeEntryEntity>,
        titleEntitiesByKey: Map<String, AchievementTitleEntryEntity>,
        achievementsByKey: Map<String, Achievement>,
        achievementDefinitions: List<AchievementDefinition>,
        pieceEntitiesByRelicId: Map<String, List<RelicPieceEntity>>,
        activeTitleKey: String?,
    ): LegendaryRelicShowcase {
        val completedEntries = journal.entries.filter { it.isCompleted }
        val entries = completedEntries.map { journalEntry ->
            val relic = relicsById[journalEntry.relicId]
                ?: LegendaryRelicCatalog.findById(journalEntry.relicId)?.let { definition ->
                    LegendaryRelicCatalog.relicFromDefinition(
                        definition = definition,
                        piecesCollected = journalEntry.piecesCollected,
                        completed = true,
                        completionDate = journalEntry.completionDateMs,
                    )
                }
                ?: return@map null
            val grant = grantsByRelicId[journalEntry.relicId]
            toEntry(
                journalEntry = journalEntry,
                relic = relic,
                grant = grant,
                badgeEntitiesByKey = badgeEntitiesByKey,
                titleEntitiesByKey = titleEntitiesByKey,
                achievementsByKey = achievementsByKey,
                achievementDefinitions = achievementDefinitions,
                pieceEntities = pieceEntitiesByRelicId[journalEntry.relicId].orEmpty(),
                activeTitleKey = activeTitleKey,
            )
        }.filterNotNull().sortedWith(
            compareBy<LegendaryRelicShowcaseEntry> { it.rarity.ordinal }.reversed()
                .thenByDescending { it.completionDateMs ?: 0L }
                .thenBy { it.displayName },
        )
        val badgeHighlights = entries.mapNotNull { it.badge }.distinctBy { it.badgeKey }
        val titleHighlights = entries.mapNotNull { it.title }.distinctBy { it.titleKey }
        val allLinks = entries.flatMap { it.achievementLinks }.distinctBy { it.achievementId }
        return LegendaryRelicShowcase(
            entries = entries,
            badgeHighlights = badgeHighlights,
            titleHighlights = titleHighlights,
            progress = LegendaryRelicShowcaseProgress(
                completedCount = entries.size,
                trackableCount = journal.progress.trackableCount,
                badgesEarned = badgeHighlights.count { it.earned },
                titlesEarned = titleHighlights.count { it.earned },
                totalCreditsEarned = entries.sumOf { it.creditsEarned },
                totalXpEarned = entries.sumOf { it.xpEarned },
                linkedAchievementsCompleted = allLinks.count { it.completed },
                linkedAchievementsTotal = allLinks.size,
            ),
        )
    }

    private fun toEntry(
        journalEntry: LegendaryRelicJournalEntry,
        relic: LegendaryRelic,
        grant: LegendaryRelicRewardGrantEntity?,
        badgeEntitiesByKey: Map<String, AchievementBadgeEntryEntity>,
        titleEntitiesByKey: Map<String, AchievementTitleEntryEntity>,
        achievementsByKey: Map<String, Achievement>,
        achievementDefinitions: List<AchievementDefinition>,
        pieceEntities: List<RelicPieceEntity>,
        activeTitleKey: String?,
    ): LegendaryRelicShowcaseEntry {
        val badgeDefinition = LegendaryRelicRewardBadgeCatalog.definitionForRelic(relic)
        val titleDefinition = LegendaryRelicRewardTitleCatalog.definitionForRelic(relic)
        val badgeEntity = badgeDefinition?.badgeKey?.let { badgeEntitiesByKey[it] }
        val titleEntity = titleDefinition?.titleKey?.let { titleEntitiesByKey[it] }
        val badgeEarned = grant?.badgeRecorded == true ||
            (badgeEntity?.unlockDateMs != null && badgeEntity.unlockDateMs > 0L)
        val titleEarned = grant?.titleRecorded == true ||
            (titleEntity?.unlockDateMs != null && titleEntity.unlockDateMs > 0L)
        return LegendaryRelicShowcaseEntry(
            relicId = journalEntry.relicId,
            displayName = journalEntry.displayName,
            description = journalEntry.description,
            category = journalEntry.category,
            categoryLabel = journalEntry.categoryLabel,
            rarity = journalEntry.rarity,
            completionDateMs = journalEntry.completionDateMs ?: grant?.grantedAtMs,
            creditsEarned = grant?.creditsGranted ?: journalEntry.rewardPreview.credits,
            xpEarned = grant?.xpGranted ?: journalEntry.rewardPreview.xp,
            badge = badgeDefinition?.let { definition ->
                LegendaryRelicShowcaseBadgeHighlight(
                    badgeKey = definition.badgeKey,
                    title = definition.title,
                    iconKey = definition.iconKey,
                    rarity = definition.rarity,
                    unlockDateMs = badgeEntity?.unlockDateMs?.takeIf { it > 0L }
                        ?: grant?.grantedAtMs?.takeIf { badgeEarned },
                    earned = badgeEarned,
                )
            },
            title = titleDefinition?.let { definition ->
                LegendaryRelicShowcaseTitleHighlight(
                    titleKey = definition.titleKey,
                    displayTitle = definition.displayTitle,
                    rarity = definition.rarity,
                    unlockDateMs = titleEntity?.unlockDateMs?.takeIf { it > 0L }
                        ?: grant?.grantedAtMs?.takeIf { titleEarned },
                    earned = titleEarned,
                    isActive = definition.titleKey == activeTitleKey,
                )
            },
            achievementLinks = resolveAchievementLinks(
                relic = relic,
                achievementDefinitions = achievementDefinitions,
                achievementsByKey = achievementsByKey,
                pieceEntities = pieceEntities,
            ),
            story = toStoryHighlight(journalEntry),
            rewardsGranted = grant != null || journalEntry.rewardPreview.rewardsGranted,
        )
    }

    private fun resolveAchievementLinks(
        relic: LegendaryRelic,
        achievementDefinitions: List<AchievementDefinition>,
        achievementsByKey: Map<String, Achievement>,
        pieceEntities: List<RelicPieceEntity>,
    ): List<LegendaryRelicShowcaseAchievementLink> {
        val links = linkedMapOf<String, LegendaryRelicShowcaseAchievementLink>()
        achievementDefinitions
            .filter { it.legendaryRelicKey == relic.relicId }
            .forEach { definition ->
                val achievement = achievementsByKey[definition.key]
                links[definition.key] = toAchievementLink(
                    achievementId = definition.key,
                    title = achievement?.title ?: definition.displayName,
                    linkType = LegendaryRelicShowcaseSchema.AchievementLinkTypes.GRANTS_RELIC,
                    achievement = achievement,
                )
            }
        val categoryDiscoveryKey = RelicSchema.AchievementKeys.firstDiscovery(relic.category)
        achievementsByKey[categoryDiscoveryKey]?.let { achievement ->
            links[categoryDiscoveryKey] = toAchievementLink(
                achievementId = categoryDiscoveryKey,
                title = achievement.title,
                linkType = LegendaryRelicShowcaseSchema.AchievementLinkTypes.CATEGORY_DISCOVERY,
                achievement = achievement,
            )
        }
        pieceEntities
            .filter { it.discovered && it.sourceType == RelicPieceSourceType.ACHIEVEMENT.id }
            .map { it.sourceKey }
            .distinct()
            .forEach { sourceKey ->
                val achievement = achievementsByKey[sourceKey]
                if (achievement != null || achievementDefinitions.any { it.key == sourceKey }) {
                    val definition = achievementDefinitions.firstOrNull { it.key == sourceKey }
                    links[sourceKey] = toAchievementLink(
                        achievementId = sourceKey,
                        title = achievement?.title ?: definition?.displayName ?: sourceKey,
                        linkType = LegendaryRelicShowcaseSchema.AchievementLinkTypes.PIECE_SOURCE,
                        achievement = achievement,
                    )
                }
            }
        return links.values.toList()
    }

    private fun toStoryHighlight(
        journalEntry: LegendaryRelicJournalEntry,
    ): LegendaryRelicShowcaseStoryHighlight? {
        val story = journalEntry.storyPreview
        if (!story.hasStoryContent) return null
        val completion = story.completionStory
        return LegendaryRelicShowcaseStoryHighlight(
            storyFragmentKey = completion?.storyFragmentKey
                ?: journalEntry.rewardPreview.storyFragmentKey
                ?: "",
            title = completion?.title ?: journalEntry.displayName,
            body = completion?.body ?: journalEntry.description,
            chaptersUnlocked = story.chapters.count { it.unlocked },
            chaptersTotal = story.chapters.size,
            loreUnlocked = story.loreEntries.count { it.unlocked },
            loreTotal = story.loreEntries.size,
            completionUnlocked = completion?.unlocked == true,
            unlockDateMs = completion?.unlockDateMs,
        )
    }

    private fun toAchievementLink(
        achievementId: String,
        title: String,
        linkType: String,
        achievement: Achievement?,
    ): LegendaryRelicShowcaseAchievementLink = LegendaryRelicShowcaseAchievementLink(
        achievementId = achievementId,
        title = title,
        linkType = linkType,
        completed = achievement?.completed == true,
        completionDateMs = achievement?.completionDate,
    )
}
