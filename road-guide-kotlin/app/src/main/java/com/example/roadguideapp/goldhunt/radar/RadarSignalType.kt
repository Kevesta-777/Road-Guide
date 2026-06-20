package com.example.roadguideapp.goldhunt.radar

/**
 * Distance-band classification for a radar blip.
 * [signalStrength] on [RadarSignal] is the continuous value; this type is the UX tier.
 */
internal enum class RadarSignalType(
    val displayName: String,
    val minimumStrength: Double,
) {
    DISTANT(
        displayName = "Distant",
        minimumStrength = 0.0,
    ),
    APPROACHING(
        displayName = "Approaching",
        minimumStrength = 0.30,
    ),
    NEARBY(
        displayName = "Nearby",
        minimumStrength = 0.55,
    ),
    IMMEDIATE(
        displayName = "Immediate",
        minimumStrength = 0.80,
    ),
    ;

    val id: String get() = name

    companion object {
        val ALL_ORDERED: List<RadarSignalType> = listOf(
            DISTANT,
            APPROACHING,
            NEARBY,
            IMMEDIATE,
        )

        fun fromId(id: String): RadarSignalType? =
            entries.firstOrNull { it.name.equals(id, ignoreCase = true) }

        fun fromStrength(strength: Double): RadarSignalType {
            val clamped = strength.coerceIn(
                RadarSchema.MIN_SIGNAL_STRENGTH,
                RadarSchema.MAX_SIGNAL_STRENGTH,
            )
            return ALL_ORDERED.lastOrNull { clamped >= it.minimumStrength } ?: DISTANT
        }
    }
}
