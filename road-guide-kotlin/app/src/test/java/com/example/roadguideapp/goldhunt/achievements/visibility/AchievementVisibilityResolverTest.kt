package com.example.roadguideapp.goldhunt.achievements.visibility

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementDefinition
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementVisibilityResolverTest {
    private fun definition(
        key: String,
        category: AchievementCategory = AchievementCategory.TREASURE,
        targetCount: Int = 100,
        extensionJson: String = AchievementSchema.EMPTY_EXTENSIONS_JSON,
    ) = AchievementDefinition(
        key = key,
        category = category,
        displayName = key,
        targetCount = targetCount,
        extensionJson = extensionJson,
    )

    @Test
    fun forDefinition_marksCount1000AsSecret() {
        val visibility = AchievementVisibilityResolver.forDefinition(
            definition("achievement:treasure:count_1000", targetCount = 1000),
        )
        assertEquals(AchievementVisibility.SECRET, visibility)
    }

    @Test
    fun forDefinition_marksCount500AsHidden() {
        val visibility = AchievementVisibilityResolver.forDefinition(
            definition("achievement:treasure:count_500", targetCount = 500),
        )
        assertEquals(AchievementVisibility.HIDDEN, visibility)
    }

    @Test
    fun forDefinition_marksMasterExplorerAsHidden() {
        val visibility = AchievementVisibilityResolver.forDefinition(
            definition(
                key = AchievementSchema.AchievementKeys.firstUnlock(AchievementCategory.MASTER_EXPLORER),
                category = AchievementCategory.MASTER_EXPLORER,
            ),
        )
        assertEquals(AchievementVisibility.HIDDEN, visibility)
    }

    @Test
    fun forDefinition_marksLegendaryRelic500AsSecret() {
        val visibility = AchievementVisibilityResolver.forDefinition(
            definition(
                key = AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.LEGENDARY_RELIC, 500),
                category = AchievementCategory.LEGENDARY_RELIC,
                targetCount = 500,
            ),
        )
        assertEquals(AchievementVisibility.SECRET, visibility)
    }

    @Test
    fun forDefinition_marksStreak100AsSecret() {
        val visibility = AchievementVisibilityResolver.forDefinition(
            definition(
                key = AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.STREAK, 100),
                category = AchievementCategory.STREAK,
                targetCount = 100,
            ),
        )
        assertEquals(AchievementVisibility.SECRET, visibility)
    }

    @Test
    fun forDefinition_marksStreak30AsHidden() {
        val visibility = AchievementVisibilityResolver.forDefinition(
            definition(
                key = AchievementSchema.MilestoneKeys.forCategory(AchievementCategory.STREAK, 30),
                category = AchievementCategory.STREAK,
                targetCount = 30,
            ),
        )
        assertEquals(AchievementVisibility.HIDDEN, visibility)
    }

    @Test
    fun forDefinition_defaultsToVisible() {
        val visibility = AchievementVisibilityResolver.forDefinition(
            definition("achievement:treasure:count_100"),
        )
        assertEquals(AchievementVisibility.VISIBLE, visibility)
    }

    @Test
    fun forDefinition_respectsExtensionOverride() {
        val visibility = AchievementVisibilityResolver.forDefinition(
            definition(
                key = "achievement:treasure:count_100",
                extensionJson = """{"visibility":"secret"}""",
            ),
        )
        assertEquals(AchievementVisibility.SECRET, visibility)
    }
}
