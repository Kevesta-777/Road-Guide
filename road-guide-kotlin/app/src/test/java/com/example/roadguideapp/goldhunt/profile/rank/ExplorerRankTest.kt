package com.example.roadguideapp.goldhunt.profile.rank

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExplorerRankTest {
    @Test
    fun rankForLevel_anchorLevels_matchTitles() {
        assertEquals(ExplorerRank.RoadWanderer, rankForLevel(1))
        assertEquals(ExplorerRank.TrailFinder, rankForLevel(5))
        assertEquals(ExplorerRank.Explorer, rankForLevel(10))
        assertEquals(ExplorerRank.TreasureHunter, rankForLevel(15))
        assertEquals(ExplorerRank.SecretSeeker, rankForLevel(20))
        assertEquals(ExplorerRank.MasterExplorer, rankForLevel(30))
        assertEquals(ExplorerRank.LegendHunter, rankForLevel(40))
        assertEquals(ExplorerRank.GoldHuntChampion, rankForLevel(50))
    }

    @Test
    fun rankForLevel_betweenAnchors_keepsLowerRank() {
        assertEquals(ExplorerRank.RoadWanderer, rankForLevel(4))
        assertEquals(ExplorerRank.TrailFinder, rankForLevel(9))
        assertEquals(ExplorerRank.TreasureHunter, rankForLevel(19))
        assertEquals(ExplorerRank.SecretSeeker, rankForLevel(29))
    }

    @Test
    fun rankForLevel_aboveMax_keepsChampion() {
        assertEquals(ExplorerRank.GoldHuntChampion, rankForLevel(100))
    }

    @Test
    fun rankForLevel_belowMin_clampsToWanderer() {
        assertEquals(ExplorerRank.RoadWanderer, rankForLevel(0))
        assertEquals(ExplorerRank.RoadWanderer, rankForLevel(-5))
    }

    @Test
    fun nextRankAfter_topTier_isNull() {
        assertNull(nextRankAfter(ExplorerRank.GoldHuntChampion))
        assertEquals(ExplorerRank.TrailFinder, nextRankAfter(ExplorerRank.RoadWanderer))
    }
}
