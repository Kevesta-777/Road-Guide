package com.example.roadguideapp.goldhunt.radar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RadarSchemaTest {
    @Test
    fun achievementKeys_areStablePerType() {
        assertEquals(
            "radar_type:basic_radar:first_pulse",
            RadarSchema.AchievementKeys.firstUnlock(RadarType.BASIC_RADAR),
        )
        assertEquals(
            "radar_type:all_radars_unlocked",
            RadarSchema.AchievementKeys.unlockAll(),
        )
    }

    @Test
    fun seasonalAndRelicKeys_reservedForAdvancedRadars() {
        assertNull(RadarSchema.SeasonalEventKeys.forType(RadarType.TREASURE_RADAR))
        assertNotNull(RadarSchema.SeasonalEventKeys.forType(RadarType.STORY_RADAR))
        assertNotNull(RadarSchema.LegendaryRelicKeys.forType(RadarType.LEGENDARY_RADAR))
    }

    @Test
    fun profileExtensionKeys_reservedForUpgrades() {
        assertEquals("upgrades", RadarSchema.ProfileExtensionKeys.UPGRADES)
        assertEquals("rangeBonusM", RadarSchema.UpgradeKeys.RANGE_BONUS_M)
    }

    @Test
    fun eventIds_areDeterministic() {
        assertEquals(
            "radar:unlock:story_radar",
            RadarSchema.unlockEventId(RadarType.STORY_RADAR),
        )
        assertEquals(
            "radar:pulse:cluster_radar:1700000000000",
            RadarSchema.pulseEventId(RadarType.CLUSTER_RADAR, 1_700_000_000_000L),
        )
        assertEquals(
            "radar:detection:treasure:treasure-42:1700000000000",
            RadarSchema.detectionEventId(
                RadarTargetCategory.TREASURE,
                "treasure-42",
                1_700_000_000_000L,
            ),
        )
    }
}
