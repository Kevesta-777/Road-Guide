package com.example.roadguideapp.goldhunt.achievements.titles.leaderboard

import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleCollection
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleEntry
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleProgress
import com.example.roadguideapp.goldhunt.achievements.titles.AchievementTitleRarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementTitleLeaderboardProfileFactoryTest {
    @Test
    fun from_buildsLeaderboardReadyProfile() {
        val collection = AchievementTitleCollection(
            entries = listOf(
                AchievementTitleEntry(
                    titleKey = "title_achievement_master_explorer",
                    achievementId = "achievement:master_explorer:first_unlock",
                    displayTitle = "Master Explorer",
                    rarity = AchievementTitleRarity.LEGENDARY,
                    unlockDateMs = 1_000L,
                    isActive = true,
                ),
            ),
            progress = AchievementTitleProgress(
                totalCount = 1,
                unlockedCount = 1,
                lockedCount = 0,
                activeCount = 1,
            ),
            activeTitleKey = "title_achievement_master_explorer",
        )
        val profile = AchievementTitleLeaderboardProfileFactory.from(
            collection = collection,
            explorerRankTitle = "Gold Hunt Champion",
        )
        assertTrue(profile.usesAchievementTitle)
        assertEquals("Master Explorer", profile.publicDisplayTitle)
        assertEquals("Gold Hunt Champion", profile.explorerRankTitle)
    }
}
