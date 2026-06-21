package com.example.roadguideapp.goldhunt.achievements.visibility

/**
 * Catalog visibility tier for unified achievements.
 *
 * [VISIBLE] achievements are listed with full details once level requirements are met.
 * [HIDDEN] achievements appear as placeholders until completed.
 * [SECRET] achievements are omitted from the journal until completed.
 */
internal enum class AchievementVisibility(val id: String) {
    VISIBLE("visible"),
    HIDDEN("hidden"),
    SECRET("secret"),
    ;

    companion object {
        fun fromId(id: String): AchievementVisibility? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
