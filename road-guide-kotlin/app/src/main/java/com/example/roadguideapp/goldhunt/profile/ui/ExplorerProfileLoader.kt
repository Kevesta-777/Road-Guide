package com.example.roadguideapp.goldhunt.profile.ui

import android.content.Context
import com.example.roadguideapp.goldhunt.profile.ExplorerProfileRepository
import com.example.roadguideapp.goldhunt.profile.level.ExplorerLevelCalculator
import com.example.roadguideapp.goldhunt.profile.rank.rankForLevel
import com.example.roadguideapp.goldhunt.profile.xp.XpManager
import com.example.roadguideapp.goldhunt.repository.GoldHuntRepository
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ClusterEncyclopediaRepository
import com.example.roadguideapp.goldhunt.clusters.encyclopedia.ui.ClusterEncyclopediaFormatters
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.SecretPlaceCollectionBookRepository
import com.example.roadguideapp.goldhunt.secretplaces.collectionbook.ui.SecretPlaceCollectionBookFormatters
import com.example.roadguideapp.goldhunt.panorama.journal.PanoramaHuntJournalRepository
import com.example.roadguideapp.goldhunt.panorama.journal.ui.PanoramaHuntJournalFormatters
import com.example.roadguideapp.goldhunt.events.collectionbook.SeasonalEventCollectionBookRepository
import com.example.roadguideapp.goldhunt.events.collectionbook.ui.SeasonalEventCollectionBookFormatters
import com.example.roadguideapp.goldhunt.achievements.badges.AchievementBadgeRepository
import com.example.roadguideapp.goldhunt.achievements.badges.ui.AchievementBadgeFormatters
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleDisplayResolver
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRepository
import com.example.roadguideapp.goldhunt.achievements.collectionbook.AchievementCollectionBookRepository
import com.example.roadguideapp.goldhunt.achievements.collectionbook.ui.AchievementCollectionBookFormatters
import com.example.roadguideapp.goldhunt.achievements.titles.ui.AchievementTitleFormatters
import com.example.roadguideapp.goldhunt.achievements.journal.AchievementJournalRepository
import com.example.roadguideapp.goldhunt.achievements.journal.ui.AchievementJournalFormatters
import com.example.roadguideapp.goldhunt.achievements.statistics.AchievementStatisticsManager
import com.example.roadguideapp.goldhunt.relics.journal.LegendaryRelicJournalRepository
import com.example.roadguideapp.goldhunt.relics.journal.ui.LegendaryRelicJournalFormatters
import com.example.roadguideapp.goldhunt.relics.showcase.LegendaryRelicShowcaseRepository
import com.example.roadguideapp.goldhunt.relics.showcase.ui.LegendaryRelicShowcaseFormatters
import com.example.roadguideapp.goldhunt.relics.statistics.HiddenRelicDiscoveryStatisticsManager
import com.example.roadguideapp.goldhunt.relics.statistics.LegendaryRelicStatisticsManager
import com.example.roadguideapp.goldhunt.relics.statistics.ui.HiddenRelicDiscoveryFormatters
import com.example.roadguideapp.goldhunt.relics.statistics.ui.LegendaryRelicStatisticsFormatters
import com.example.roadguideapp.goldhunt.events.statistics.SeasonalEventStatisticsManager
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.TreasureEncyclopediaRepository
import com.example.roadguideapp.goldhunt.treasure.encyclopedia.ui.TreasureEncyclopediaFormatters

internal object ExplorerProfileLoader {
    suspend fun load(context: Context): ExplorerProfileUiState {
        val appContext = context.applicationContext
        val explorer = ExplorerProfileRepository.get(appContext).ensureProfile()
        val gameRepo = GoldHuntRepository.get(appContext)
        gameRepo.ensureInitialized()
        val credits = gameRepo.loadProfile().totalCredits
        val lifetimeXp = XpManager.get(appContext).getLifetimeXp()
        val progress = ExplorerLevelCalculator.xpProgressToNextLevel(lifetimeXp)
        val rank = rankForLevel(progress.currentLevel)
        val encyclopedia = TreasureEncyclopediaRepository.get(appContext).load()
        val encyclopediaFormatters = TreasureEncyclopediaFormatters(appContext)
        val clusterEncyclopedia = ClusterEncyclopediaRepository.get(appContext).load()
        val clusterEncyclopediaFormatters = ClusterEncyclopediaFormatters(appContext)
        val collectionBook = SecretPlaceCollectionBookRepository.get(appContext).load()
        val collectionBookFormatters = SecretPlaceCollectionBookFormatters(appContext)
        val panoramaHuntJournal = PanoramaHuntJournalRepository.get(appContext).load()
        val panoramaHuntJournalFormatters = PanoramaHuntJournalFormatters(appContext)
        val seasonalEventStatistics = SeasonalEventStatisticsManager.get(appContext).load()
        val achievementStatistics = AchievementStatisticsManager.get(appContext).load()
        val legendaryRelicStatistics = LegendaryRelicStatisticsManager.get(appContext).load()
        val hiddenRelicDiscoveryStatistics = HiddenRelicDiscoveryStatisticsManager.get(appContext).load()
        val legendaryRelicFormatters = LegendaryRelicStatisticsFormatters(appContext)
        val hiddenRelicFormatters = HiddenRelicDiscoveryFormatters(appContext)
        val legendaryRelicJournal = LegendaryRelicJournalRepository.get(appContext).load()
        val legendaryRelicJournalFormatters = LegendaryRelicJournalFormatters(appContext)
        val legendaryRelicShowcase = LegendaryRelicShowcaseRepository.get(appContext).load()
        val legendaryRelicShowcaseFormatters = LegendaryRelicShowcaseFormatters(appContext)
        val achievementJournal = AchievementJournalRepository.get(appContext).load()
        val achievementJournalFormatters = AchievementJournalFormatters(appContext)
        val achievementBadges = AchievementBadgeRepository.get(appContext).load()
        val achievementBadgeFormatters = AchievementBadgeFormatters(appContext)
        val achievementTitles = AchievementTitleRepository.get(appContext).load()
        val achievementTitleFormatters = AchievementTitleFormatters(appContext)
        val achievementCollectionBook = AchievementCollectionBookRepository.get(appContext).load()
        val achievementCollectionBookFormatters = AchievementCollectionBookFormatters(appContext)
        val displayTitle = AchievementTitleDisplayResolver.resolvePublicDisplayTitle(
            activeTitleKey = achievementTitles.activeTitleKey,
            collection = achievementTitles,
            explorerRankTitle = rank.title,
        )
        val seasonalEventCollectionBook = SeasonalEventCollectionBookRepository.get(appContext).load()
        val seasonalEventCollectionBookFormatters = SeasonalEventCollectionBookFormatters(appContext)
        return ExplorerProfileUiState(
            level = progress.currentLevel,
            rankTitle = rank.title,
            displayTitle = displayTitle,
            usesAchievementTitle = achievementTitles.activeTitleKey != null,
            lifetimeXp = lifetimeXp,
            xpIntoLevel = progress.xpIntoLevel,
            xpToNextLevel = progress.xpToNextLevel,
            progressFraction = progress.progressFraction.toFloat(),
            totalCredits = credits,
            treasuresCollected = explorer.totalTreasuresCollected,
            secretPlacesFound = explorer.totalSecretPlacesFound,
            clusterStatistics = explorer.clusterStatistics,
            panoramaHuntStatistics = explorer.panoramaHuntStatistics,
            radarStatistics = explorer.radarStatistics,
            seasonalEventStatistics = seasonalEventStatistics,
            achievementStatistics = achievementStatistics,
            legendaryRelicStatistics = legendaryRelicStatistics,
            hiddenRelicDiscoveryStatistics = hiddenRelicDiscoveryStatistics,
            secretPlaceCategoryStatistics = explorer.secretPlaceCategoryStatistics,
            distanceExploredM = explorer.totalDistanceExploredM,
            treasureRarityStats = explorer.treasureRarityStats,
            encyclopediaProgressLabel = encyclopediaFormatters.formatProgress(encyclopedia.progress),
            encyclopediaProgressFraction = encyclopedia.progress.completionFraction,
            clusterEncyclopediaProgressLabel =
                clusterEncyclopediaFormatters.formatCompletedProgress(clusterEncyclopedia.progress),
            clusterEncyclopediaProgressFraction = clusterEncyclopedia.progress.completionFraction,
            collectionBookProgressLabel =
                collectionBookFormatters.formatCompletedProgress(collectionBook.progress),
            collectionBookProgressFraction = collectionBook.progress.completionFraction,
            panoramaHuntJournalProgressLabel =
                panoramaHuntJournalFormatters.formatCompletedProgress(panoramaHuntJournal.progress),
            panoramaHuntJournalDiscoveredProgressLabel =
                panoramaHuntJournalFormatters.formatDiscoveredProgress(panoramaHuntJournal.progress),
            panoramaHuntJournalMissingProgressLabel =
                panoramaHuntJournalFormatters.formatMissingProgress(panoramaHuntJournal.progress),
            panoramaHuntJournalProgressFraction = panoramaHuntJournal.progress.completionFraction,
            seasonalEventCollectionBookProgressLabel =
                seasonalEventCollectionBookFormatters.formatParticipatedProgress(
                    seasonalEventCollectionBook.progress,
                ),
            seasonalEventCollectionBookProgressFraction =
                seasonalEventCollectionBook.progress.participatedFraction,
            achievementJournalProgressLabel =
                achievementJournalFormatters.formatCompletedProgress(achievementJournal.progress),
            achievementJournalCompletionPercentageLabel =
                achievementJournalFormatters.formatCompletionPercentage(achievementJournal.progress),
            achievementJournalProgressFraction = achievementJournal.progress.completionFraction,
            achievementBadgeProgressLabel =
                achievementBadgeFormatters.formatUnlockedProgress(achievementBadges.progress),
            achievementBadgeCompletionPercentageLabel =
                achievementBadgeFormatters.formatCompletionPercentage(achievementBadges.progress),
            achievementBadgeProgressFraction = achievementBadges.progress.completionFraction,
            achievementTitleProgressLabel =
                achievementTitleFormatters.formatUnlockedProgress(achievementTitles.progress),
            achievementTitleCompletionPercentageLabel =
                achievementTitleFormatters.formatCompletionPercentage(achievementTitles.progress),
            achievementTitleProgressFraction = achievementTitles.progress.completionFraction,
            activeAchievementTitleLabel =
                achievementTitleFormatters.formatActiveTitleLabel(achievementTitles.activeEntry?.displayTitle),
            achievementCollectionBookProgressLabel =
                achievementCollectionBookFormatters.formatCompletedProgress(achievementCollectionBook.progress),
            achievementCollectionBookCompletionPercentageLabel =
                achievementCollectionBookFormatters.formatCompletionPercentage(achievementCollectionBook.progress),
            achievementCollectionBookRareLegendaryLabel =
                achievementCollectionBookFormatters.formatRareLegendary(achievementCollectionBook.progress),
            achievementCollectionBookProgressFraction = achievementCollectionBook.progress.completionFraction,
            legendaryRelicProgressLabel = legendaryRelicFormatters.formatCompletedProgress(
                legendaryRelicStatistics,
            ),
            legendaryRelicCompletionPercentageLabel = legendaryRelicFormatters.formatCompletionPercentage(
                legendaryRelicStatistics,
            ),
            legendaryRelicPieceProgressLabel = legendaryRelicFormatters.formatPieceProgress(
                legendaryRelicStatistics,
            ),
            legendaryRelicPieceCompletionPercentageLabel =
                legendaryRelicFormatters.formatPieceCompletionPercentage(legendaryRelicStatistics),
            legendaryRelicProgressFraction = legendaryRelicStatistics.completionPercentage,
            legendaryRelicPieceProgressFraction = legendaryRelicStatistics.pieceCompletionPercentage,
            legendaryRelicHiddenDiscoveredLabel = legendaryRelicFormatters.formatHiddenRelicsDiscovered(
                legendaryRelicStatistics,
            ),
            legendaryRelicHiddenDiscoveryPercentageLabel =
                legendaryRelicFormatters.formatHiddenDiscoveryPercentage(legendaryRelicStatistics),
            hiddenRelicRevealProgressLabel = hiddenRelicFormatters.formatRevealedProgress(
                hiddenRelicDiscoveryStatistics,
            ),
            hiddenRelicRevealPercentageLabel = hiddenRelicFormatters.formatRevealPercentage(
                hiddenRelicDiscoveryStatistics,
            ),
            hiddenRelicUndiscoveredLabel = hiddenRelicFormatters.formatUndiscoveredCount(
                hiddenRelicDiscoveryStatistics,
            ),
            hiddenRelicRevealProgressFraction = hiddenRelicDiscoveryStatistics.revealPercentage,
            legendaryRelicJournalCompletedProgressLabel =
                legendaryRelicJournalFormatters.formatCompletedProgress(legendaryRelicJournal.progress),
            legendaryRelicJournalPiecesFoundProgressLabel =
                legendaryRelicJournalFormatters.formatPiecesFoundProgress(legendaryRelicJournal.progress),
            legendaryRelicJournalPiecesMissingProgressLabel =
                legendaryRelicJournalFormatters.formatPiecesMissingProgress(legendaryRelicJournal.progress),
            legendaryRelicJournalProgressFraction = legendaryRelicJournal.progress.completionFraction,
            legendaryRelicShowcaseCompletedProgressLabel =
                legendaryRelicShowcaseFormatters.formatCompletedProgress(legendaryRelicShowcase.progress),
            legendaryRelicShowcaseBadgesEarnedLabel =
                legendaryRelicShowcaseFormatters.formatBadgesEarned(legendaryRelicShowcase.progress),
            legendaryRelicShowcaseTitlesEarnedLabel =
                legendaryRelicShowcaseFormatters.formatTitlesEarned(legendaryRelicShowcase.progress),
            legendaryRelicShowcaseProgressFraction = legendaryRelicShowcase.progress.completionFraction,
        )
    }
}
