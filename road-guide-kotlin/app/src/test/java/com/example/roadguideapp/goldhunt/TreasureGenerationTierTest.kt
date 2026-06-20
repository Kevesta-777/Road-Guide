package com.example.roadguideapp.goldhunt

import com.example.roadguideapp.goldhunt.treasure.TreasureGenerationTier
import org.junit.Assert.assertEquals
import org.junit.Test

class TreasureGenerationTierTest {
    @Test
    fun tierFromProgress_followsUnlockThresholds() {
        assertEquals(TreasureGenerationTier.STAR_ONLY, TreasureGenerationTier.fromProgress(0, 0, 0))
        assertEquals(TreasureGenerationTier.STAR_ONLY, TreasureGenerationTier.fromProgress(9, 0, 0))
        assertEquals(
            TreasureGenerationTier.STAR_AND_FLOWER,
            TreasureGenerationTier.fromProgress(10, 0, 0),
        )
        assertEquals(
            TreasureGenerationTier.STAR_AND_FLOWER,
            TreasureGenerationTier.fromProgress(20, 9, 0),
        )
        assertEquals(
            TreasureGenerationTier.STAR_FLOWER_AND_CRYSTAL,
            TreasureGenerationTier.fromProgress(15, 10, 0),
        )
        assertEquals(
            TreasureGenerationTier.STAR_FLOWER_AND_CRYSTAL,
            TreasureGenerationTier.fromProgress(15, 10, 9),
        )
        assertEquals(
            TreasureGenerationTier.STAR_FLOWER_CRYSTAL_AND_GIFT,
            TreasureGenerationTier.fromProgress(15, 12, 10),
        )
    }
}
