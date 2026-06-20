package com.example.roadguideapp.goldhunt.achievements.titles

import com.example.roadguideapp.goldhunt.achievements.AchievementCategory
import com.example.roadguideapp.goldhunt.achievements.AchievementSchema
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementTitleCatalogTest {
    @Test
    fun definitions_includeCategoryTitleAchievements() {
        val definitions = AchievementTitleCatalog.definitions
        assertTrue(definitions.isNotEmpty())
        val masterExplorer = definitions.firstOrNull {
            it.titleKey == AchievementSchema.TitleKeys.forCategory(AchievementCategory.MASTER_EXPLORER)
        }
        assertNotNull(masterExplorer)
        assertTrue(masterExplorer!!.displayTitle.isNotBlank())
    }

    @Test
    fun definitionForKey_returnsCatalogEntry() {
        val key = AchievementSchema.TitleKeys.forCategory(AchievementCategory.STREAK)
        val definition = AchievementTitleCatalog.definitionForKey(key!!)
        assertNotNull(definition)
        assertEquals(key, definition!!.titleKey)
    }
}
