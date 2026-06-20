package com.example.roadguideapp.goldhunt.profile.levelup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelUpManagerTest {
    @Test
    fun levelUpSteps_singleLevel() {
        assertEquals(listOf(1 to 2), LevelUpManager.levelUpSteps(1, 2))
    }

    @Test
    fun levelUpSteps_multiLevelJump() {
        assertEquals(
            listOf(1 to 2, 2 to 3, 3 to 4),
            LevelUpManager.levelUpSteps(1, 4),
        )
    }

    @Test
    fun levelUpSteps_noChange_isEmpty() {
        assertTrue(LevelUpManager.levelUpSteps(5, 5).isEmpty())
        assertTrue(LevelUpManager.levelUpSteps(5, 3).isEmpty())
    }

    @Test
    fun eventIds_areStablePerLevel() {
        assertEquals("levelup:10", LevelUpEventIds.forLevel(10))
    }
}
