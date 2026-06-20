package com.example.roadguideapp.goldhunt.achievements.titles

internal sealed class AchievementTitleSelectionResult {
    data class Active(val entry: AchievementTitleEntry) : AchievementTitleSelectionResult()
    data object Cleared : AchievementTitleSelectionResult()
    data object NotUnlocked : AchievementTitleSelectionResult()
    data object UnknownTitle : AchievementTitleSelectionResult()
}
