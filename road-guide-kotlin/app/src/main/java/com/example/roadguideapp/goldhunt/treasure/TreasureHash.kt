package com.example.roadguideapp.goldhunt.treasure

import java.security.MessageDigest

/** Deterministic 32-bit mixer for offline treasure slots. */
internal object TreasureHash {
    fun digest32(seed: String): Int {
        val bytes = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
        return ((bytes[0].toInt() and 0xFF) shl 24) or
            ((bytes[1].toInt() and 0xFF) shl 16) or
            ((bytes[2].toInt() and 0xFF) shl 8) or
            (bytes[3].toInt() and 0xFF)
    }

    fun mix(vararg parts: Any): Int = digest32(parts.joinToString("|"))

    fun unitFraction(seed: String): Double {
        val h = digest32(seed)
        return (h and 0x7FFFFFFF) / Int.MAX_VALUE.toDouble()
    }

    fun intInRange(seed: String, min: Int, max: Int): Int {
        if (max <= min) return min
        val span = max - min + 1
        val h = digest32(seed)
        return min + (h and 0x7FFFFFFF) % span
    }
}
