package com.example.roadguideapp.goldhunt.radar

/** Tuning for offline radar pulse scans. */
internal object RadarScanConfig {
    /** Minimum signal strength kept in pulse results (filters noise at range edge). */
    const val MIN_REPORTED_STRENGTH = 0.05

    /** Maximum targets per category in one pulse (keeps UI and logs bounded). */
    const val MAX_SIGNALS_PER_CATEGORY = 24

    /** Maximum total signals returned from one pulse. */
    const val MAX_TOTAL_SIGNALS = 64
}
