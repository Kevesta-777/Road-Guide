package com.example.roadguideapp.goldhunt.achievements.visibility

internal object AchievementVisibilitySchema {
    const val VERSION = 1

    object ExtensionKeys {
        const val VISIBILITY = "visibility"
        const val REVEALED_AT_MS = "revealedAtMs"
    }

    object Placeholders {
        const val HIDDEN_TITLE = "???"
        const val SECRET_TITLE = "Secret achievement"
        const val HIDDEN_DESCRIPTION = "Complete this achievement to reveal its details."
    }

    object MilestoneSuffixes {
        const val TIER_500 = "count_500"
        const val TIER_1000 = "count_1000"
    }

    object ActionSuffixes {
        const val MASTERY = "mastery"
        const val COLLECTOR = "collector"
    }
}
