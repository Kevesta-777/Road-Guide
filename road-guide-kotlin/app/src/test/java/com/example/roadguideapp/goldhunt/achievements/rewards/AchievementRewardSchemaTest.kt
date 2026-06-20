package com.example.roadguideapp.goldhunt.achievements.rewards

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementRewardSchemaTest {
    @Test
    fun eventIds_areStablePerAchievement() {
        val achievementId = "achievement:treasure:count_100"
        assertEquals(
            "achievement_reward:credits:achievement:treasure:count_100",
            AchievementRewardSchema.creditsEventId(achievementId),
        )
        assertEquals(
            "achievement_reward:story:achievement:treasure:count_100",
            AchievementRewardSchema.storyFragmentEventId(achievementId),
        )
        assertEquals(
            "achievement_reward:relic:achievement:treasure:count_100",
            AchievementRewardSchema.legendaryRelicEventId(achievementId),
        )
        assertTrue(
            AchievementRewardSchema.cosmeticEventId(achievementId, "trail_glow")
                .contains("trail_glow"),
        )
    }
}
