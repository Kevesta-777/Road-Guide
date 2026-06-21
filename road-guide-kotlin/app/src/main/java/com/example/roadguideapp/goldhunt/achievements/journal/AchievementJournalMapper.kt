package com.example.roadguideapp.goldhunt.achievements.journal

import com.example.roadguideapp.goldhunt.achievements.Achievement
import com.example.roadguideapp.goldhunt.achievements.AchievementCatalog
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.AchievementMapper
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import com.example.roadguideapp.goldhunt.achievements.visibility.AchievementRevealEvaluator
import com.example.roadguideapp.goldhunt.achievements.visibility.AchievementVisibility
import com.example.roadguideapp.goldhunt.achievements.visibility.AchievementVisibilityResolver
import com.example.roadguideapp.goldhunt.achievements.chain.AchievementChainResolver
import com.example.roadguideapp.goldhunt.achievements.chain.AchievementChainRewardPreviewMapper
import com.example.roadguideapp.goldhunt.achievements.visibility.AchievementVisibilitySchema

internal object AchievementJournalMapper {
    fun toEntry(
        definition: AchievementDefinition,
        achievement: Achievement,
        explorerLevel: Int,
        achievementsByKey: Map<String, Achievement> = emptyMap(),
    ): AchievementJournalEntry {
        val visibility = AchievementVisibilityResolver.forDefinition(definition)
        val revealed = AchievementRevealEvaluator.isRevealed(visibility, achievement)
        val chainContext = AchievementChainResolver.buildContext(achievementsByKey)
        val status = AchievementJournalStatusResolver.resolve(
            definition = definition,
            achievement = achievement,
            explorerLevel = explorerLevel,
            visibility = visibility,
            chainContext = chainContext,
        )
        return AchievementJournalEntry(
            achievementId = definition.key,
            title = if (revealed) {
                achievement.title
            } else {
                AchievementVisibilitySchema.Placeholders.HIDDEN_TITLE
            },
            description = if (revealed) {
                achievement.description
            } else {
                AchievementVisibilitySchema.Placeholders.HIDDEN_DESCRIPTION
            },
            category = definition.category,
            status = status,
            visibility = visibility,
            currentValue = achievement.currentValue,
            targetValue = achievement.targetValue,
            progressFraction = achievement.progressFraction,
            completionDateMs = achievement.completionDate,
            minimumExplorerLevel = definition.minExplorerLevel,
            rewardPreview = if (revealed) {
                buildRewardPreview(definition, achievement)
            } else {
                AchievementJournalRewardPreview(credits = 0, xp = 0L)
            },
        )
    }

    fun buildRewardPreview(
        definition: AchievementDefinition,
        achievement: Achievement,
    ): AchievementJournalRewardPreview = AchievementChainRewardPreviewMapper.appendTo(
        preview = AchievementJournalRewardPreview(
            credits = achievement.rewardCredits,
            xp = achievement.rewardXp,
            titleKey = achievement.titleKey ?: AchievementSchema.TitleKeys.forCategory(definition.category),
            legendaryRelicKey = achievement.legendaryRelicKey ?: definition.legendaryRelicKey,
            badgeKey = achievement.badgeKey ?: AchievementSchema.BadgeKeys.forCategory(definition.category),
            storyFragmentKey = definition.storyFragmentKey,
        ),
        sourceAchievementKey = definition.key,
    )

    fun buildJournal(
        definitions: List<AchievementDefinition>,
        achievementsByKey: Map<String, Achievement>,
        explorerLevel: Int,
    ): AchievementJournal {
        var undiscoveredSecretCount = 0
        val entries = definitions.mapNotNull { definition ->
            val achievement = achievementsByKey[definition.key]
                ?: AchievementCatalog.achievementFromDefinition(definition)
            val visibility = AchievementVisibilityResolver.forDefinition(definition)
            if (!AchievementRevealEvaluator.shouldIncludeInJournal(visibility, achievement)) {
                undiscoveredSecretCount++
                return@mapNotNull null
            }
            toEntry(
                definition = definition,
                achievement = achievement,
                explorerLevel = explorerLevel,
                achievementsByKey = achievementsByKey,
            )
        }.sortedWith(
            compareBy<AchievementJournalEntry> { it.category.displayName }
                .thenBy { statusOrder(it.status) }
                .thenBy { it.title },
        )
        return AchievementJournal(
            entries = entries,
            progress = buildProgress(
                entries = entries,
                catalogCount = definitions.size,
                undiscoveredSecretCount = undiscoveredSecretCount,
            ),
        )
    }

    fun achievementMapFromEntities(
        definitions: List<AchievementDefinition>,
        entities: List<com.example.roadguideapp.goldhunt.achievements.AchievementEntity>,
    ): Map<String, Achievement> {
        val entityMap = entities.associate { it.achievementId to it }
        return definitions.associate { definition ->
            val achievement = entityMap[definition.key]?.let { AchievementMapper.toDomain(it) }
                ?: AchievementCatalog.achievementFromDefinition(definition)
            definition.key to achievement
        }
    }

    private fun buildProgress(
        entries: List<AchievementJournalEntry>,
        catalogCount: Int,
        undiscoveredSecretCount: Int,
    ): AchievementJournalProgress = AchievementJournalProgress(
        trackableCount = entries.size,
        catalogCount = catalogCount,
        completedCount = entries.count { it.isCompleted },
        incompleteCount = entries.count { it.isIncomplete },
        lockedCount = entries.count { it.isLocked },
        hiddenCount = entries.count { it.isHidden },
        undiscoveredSecretCount = undiscoveredSecretCount,
    )

    private fun statusOrder(status: AchievementJournalStatus): Int = when (status) {
        AchievementJournalStatus.COMPLETED -> 0
        AchievementJournalStatus.INCOMPLETE -> 1
        AchievementJournalStatus.LOCKED -> 2
        AchievementJournalStatus.HIDDEN -> 3
    }
}
