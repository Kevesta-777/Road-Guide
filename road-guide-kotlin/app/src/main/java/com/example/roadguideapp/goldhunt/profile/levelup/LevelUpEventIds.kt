package com.example.roadguideapp.goldhunt.profile.levelup

/** Idempotent ledger keys — one persisted event per reached level. */
internal object LevelUpEventIds {
    fun forLevel(newLevel: Int): String = "levelup:$newLevel"
}
