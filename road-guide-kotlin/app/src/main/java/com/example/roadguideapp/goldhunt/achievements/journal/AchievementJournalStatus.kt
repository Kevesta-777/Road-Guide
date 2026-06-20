package com.example.roadguideapp.goldhunt.achievements.journal

internal enum class AchievementJournalStatus(val id: String) {
    COMPLETED("completed"),
    INCOMPLETE("incomplete"),
    LOCKED("locked"),
    HIDDEN("hidden"),
    ;

    companion object {
        fun fromId(id: String): AchievementJournalStatus? =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
