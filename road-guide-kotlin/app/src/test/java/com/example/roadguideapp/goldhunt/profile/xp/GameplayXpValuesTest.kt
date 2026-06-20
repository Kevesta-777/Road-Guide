package com.example.roadguideapp.goldhunt.profile.xp

import com.example.roadguideapp.goldhunt.treasure.TreasureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameplayXpValuesTest {
    @Test
    fun treasureXp_matchesDesign() {
        assertEquals(3L, GameplayXpValues.forTreasureType(TreasureType.STAR))
        assertEquals(5L, GameplayXpValues.forTreasureType(TreasureType.FLOWER))
        assertEquals(10L, GameplayXpValues.forTreasureType(TreasureType.CRYSTAL))
        assertEquals(25L, GameplayXpValues.forTreasureType(TreasureType.GIFT))
        assertNull(GameplayXpValues.forTreasureType(TreasureType.HINT))
    }

    @Test
    fun transactionIds_areStable() {
        assertEquals("xp:road:L0:1:2", XpTransactionIds.roadDiscovery("L0:1:2"))
        assertEquals("xp:area:L2:0:1", XpTransactionIds.areaDiscovery("L2:0:1"))
        assertEquals("xp:treasure:abc", XpTransactionIds.treasureCollection("abc"))
        assertEquals("xp:secret:sp-1", XpTransactionIds.secretPlace("sp-1"))
    }
}
