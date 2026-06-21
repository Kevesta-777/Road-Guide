package com.example.roadguideapp.goldhunt.achievements.titles

import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementTitleDisplayResolverTest {
    private val definition = AchievementTitleDefinition(
        titleKey = "title_achievement_streak",
        achievementId = "achievement:streak:first_unlock",
        displayTitle = "Streak Champion",
        rarity = AchievementTitleRarity.RARE,
    )

    @Test
    fun resolvePublicDisplayTitle_usesActiveAchievementTitle() {
        val entry = AchievementTitleEntry(
            titleKey = definition.titleKey,
            achievementId = definition.achievementId,
            displayTitle = definition.displayTitle,
            rarity = definition.rarity,
            unlockDateMs = 1_000L,
            isActive = true,
        )
        val collection = AchievementTitleCollection(
            entries = listOf(entry),
            progress = AchievementTitleProgress(
                totalCount = 1,
                unlockedCount = 1,
                lockedCount = 0,
                activeCount = 1,
            ),
            activeTitleKey = definition.titleKey,
        )
        val resolved = AchievementTitleDisplayResolver.resolvePublicDisplayTitle(
            activeTitleKey = definition.titleKey,
            collection = collection,
            explorerRankTitle = "Trail Finder",
        )
        assertEquals("Streak Champion", resolved)
    }

    @Test
    fun resolvePublicDisplayTitle_fallsBackToExplorerRank() {
        val collection = AchievementTitleCollection(
            entries = emptyList(),
            progress = AchievementTitleProgress(
                totalCount = 0,
                unlockedCount = 0,
                lockedCount = 0,
                activeCount = 0,
            ),
            activeTitleKey = null,
        )
        val resolved = AchievementTitleDisplayResolver.resolvePublicDisplayTitle(
            activeTitleKey = null,
            collection = collection,
            explorerRankTitle = "Trail Finder",
        )
        assertEquals("Trail Finder", resolved)
    }
}
