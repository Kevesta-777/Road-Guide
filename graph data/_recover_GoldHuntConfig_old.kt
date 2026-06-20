package com.example.roadguideapp.goldhunt

/**
 * Tunable Gold Hunt gameplay constants (offline, no remote config).
 */
internal object GoldHuntConfig {
    const val MAX_LOCATION_ACCURACY_M = 50f
    const val MIN_LOCATION_INTERVAL_MS = 3_000L
    const val MIN_LOCATION_DISTANCE_M = 12.0

    // Treasure workflow — unlock tiers by collection progress
    const val FLOWER_MISSION_MIN_STARS = 50
    /** Extra flower spawns once this many flowers are collected (mission must already be met). */
    const val FLOWER_MISSION_BOOST_MIN_FLOWERS = 10
    const val CRYSTAL_UNLOCK_MIN_FLOWERS = 5

    /** Minimum map zoom to collect treasures (tap or GPS). */
    const val TREASURE_REVEAL_MIN_ZOOM = 17.0

    // Hidden treasures
    const val TREASURE_GENERATOR_VERSION = 1
    const val SLOTS_PER_L1 = 4
    const val MAX_TREASURE_L1_CELLS_SCAN = 80
    const val MAX_TREASURE_FEATURES = 200
    const val TREASURE_COLLECT_RADIUS_WALK_M = 25.0
    const val TREASURE_COLLECT_RADIUS_DRIVE_M = 40.0
    const val TREASURE_DRIVE_SPEED_MPS = 4.0
    const val TREASURE_COLLECT_COOLDOWN_MS = 2_000L
    const val TREASURE_MIN_MOVEMENT_M = 8.0
    const val TREASURE_MAX_SPEED_MPS = 55.0
    const val COLLECTED_TREASURE_CACHE_CAPACITY = 8_000

    const val TREASURE_STAR_CREDIT_MIN = 1
    const val TREASURE_STAR_CREDIT_MAX = 3
    const val TREASURE_FLOWER_CREDIT_MIN = 3
    const val TREASURE_FLOWER_CREDIT_MAX = 5
    const val TREASURE_CRYSTAL_CREDIT_MIN = 5
    const val TREASURE_CRYSTAL_CREDIT_MAX = 20
    const val TREASURE_GIFT_CREDIT_MIN = 10
    const val TREASURE_GIFT_CREDIT_MAX = 100

    // Secret places
    const val SECRET_GENERATOR_VERSION = 1
    const val SECRET_SLOTS_PER_L2 = 1
    const val SECRET_PROCEDURAL_SPAWN_CHANCE = 0.35
    const val MAX_SECRET_L2_CELLS_SCAN = 40
    const val MAX_SECRET_FEATURES = 60
    const val SECRET_REVEAL_MIN_ZOOM = 16.0
    const val SECRET_COLLECT_RADIUS_M = 40.0
    const val SECRET_CREDIT_COMMON = 50
    const val SECRET_CREDIT_RARE = 100
    const val SECRET_CREDIT_LEGENDARY = 500
    const val SECRET_CREDIT_REGION_L1 = 150
    const val SECRET_NEST_BONUS_PER_SLOT = 10
    const val SECRET_L1_COMPLETION_THRESHOLD = 3
    const val DISCOVERED_SECRET_CACHE_CAPACITY = 4_000
}
