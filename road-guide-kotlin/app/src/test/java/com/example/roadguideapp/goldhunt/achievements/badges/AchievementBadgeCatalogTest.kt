package com.example.roadguideapp.goldhunt.achievements.badges

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementBadgeCatalogTest {
    @Test
    fun definitions_includeCategoryBadgeAchievements() {
        val definitions = AchievementBadgeCatalog.definitions
        assertTrue(definitions.isNotEmpty())
        val panorama = definitions.firstOrNull {
            it.badgeKey == AchievementSchema.BadgeKeys.forCategory(AchievementCategory.PANORAMA)
        }
        assertNotNull(panorama)
        assertEquals("achievement_panorama", panorama!!.iconKey)
    }

    @Test
    fun definitionForKey_returnsCatalogEntry() {
        val key = AchievementSchema.BadgeKeys.forCategory(AchievementCategory.TREASURE_CLUSTER)
        val definition = AchievementBadgeCatalog.definitionForKey(key!!)
        assertNotNull(definition)
        assertEquals(key, definition!!.badgeKey)
        assertTrue(definition.title.isNotBlank())
    }
}
