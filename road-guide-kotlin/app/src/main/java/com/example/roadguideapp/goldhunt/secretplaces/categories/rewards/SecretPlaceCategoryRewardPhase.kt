package com.example.roadguideapp.goldhunt.secretplaces.categories.rewards

/** Lifecycle phase for secret-place category reward dispatch. */
internal enum class SecretPlaceCategoryRewardPhase {
    DISCOVERY,
    COMPLETION,
    ;

    val id: String get() = name.lowercase()
}
