package com.example.roadguideapp.goldhunt.profile.xp

internal sealed class XpAwardOutcome {
    data class Awarded(val transaction: XpTransaction) : XpAwardOutcome()
    data object Duplicate : XpAwardOutcome()

    val xpAwarded: Long
        get() = when (this) {
            is Awarded -> transaction.xpAwarded
            Duplicate -> 0L
        }

    val wasAwarded: Boolean get() = this is Awarded
}
