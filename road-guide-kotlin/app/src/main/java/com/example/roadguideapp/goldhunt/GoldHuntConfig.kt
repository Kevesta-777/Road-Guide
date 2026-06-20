package com.example.roadguideapp.goldhunt

/** Tunable Gold Hunt gameplay constants (offline, no remote config). */
internal object GoldHuntConfig {
    const val MAX_LOCATION_ACCURACY_M = 50f
    const val MIN_LOCATION_INTERVAL_MS = 3_000L
    const val MIN_LOCATION_DISTANCE_M = 12.0

    const val L0_CELL_DEG = 0.00025
    const val L1_CELL_DEG = 0.002
    const val L2_CELL_DEG = 0.01

    const val EXPLORED_CACHE_CAPACITY = 8_000

    /** Procedural type weights (sum = 0.96); secret places use [SPAWN_RATIO_SECRET_PLACE] separately. */
    const val SPAWN_RATIO_STAR = 0.43
    const val SPAWN_RATIO_FLOWER = 0.27
    const val SPAWN_RATIO_CRYSTAL = 0.17
    const val SPAWN_RATIO_GIFT = 0.09
    const val SPAWN_RATIO_SECRET_PLACE = 0.04

    // Treasure workflow — additional types spawn after collecting 10 of the previous tier
    const val FLOWER_UNLOCK_MIN_STARS = 10
    const val CRYSTAL_UNLOCK_MIN_FLOWERS = 10
    const val GIFT_UNLOCK_MIN_CRYSTALS = 10
    /** Secret places unlock after collecting this many gifts (10+). */
    const val SECRET_UNLOCK_MIN_GIFTS = 10

    /** Minimum map zoom to show and collect treasures (overlay, tap, GPS). */
    const val TREASURE_DISPLAY_MIN_ZOOM = 17.0
    const val TREASURE_COLLECT_MIN_ZOOM = TREASURE_DISPLAY_MIN_ZOOM

    /** Dense treasure pile after a secret place is discovered. */
    const val HOARD_TREASURE_COUNT = 50
    /** First N secret discoveries spawn the hoard at the secret place; later ones use a random region. */
    const val HOARD_AT_SECRET_PLACE_MAX_DISCOVERIES = 2
    const val HOARD_RADIUS_M = 100.0
    const val HOARD_VISIBILITY_RADIUS_M = 350.0

    // Hidden treasures
    const val TREASURE_GENERATOR_VERSION = 1
    /** Procedural spawn chance per L1 slot (was 0.72; halved to reduce map density). */
    const val TREASURE_PROCEDURAL_SPAWN_CHANCE = 0.2
    const val SLOTS_PER_L1 = 4
    const val MAX_TREASURE_L1_CELLS_SCAN = 80
    const val MAX_TREASURE_FEATURES = 100
    const val TREASURE_COLLECT_RADIUS_WALK_M = 100.0
    const val TREASURE_COLLECT_RADIUS_DRIVE_M = 200.0
    const val TREASURE_DRIVE_SPEED_MPS = 10.0
    const val TREASURE_COLLECT_COOLDOWN_MS = 2_000L
    const val TREASURE_MIN_MOVEMENT_M = 8.0
    const val TREASURE_MAX_SPEED_MPS = 55.0
    const val COLLECTED_TREASURE_CACHE_CAPACITY = 5_000

    const val TREASURE_STAR_CREDIT_MIN = 1
    const val TREASURE_STAR_CREDIT_MAX = 2
    const val TREASURE_FLOWER_CREDIT_MIN = 2
    const val TREASURE_FLOWER_CREDIT_MAX = 4
    const val TREASURE_CRYSTAL_CREDIT_MIN = 4
    const val TREASURE_CRYSTAL_CREDIT_MAX = 6
    const val TREASURE_GIFT_CREDIT_MIN = 10
    const val TREASURE_GIFT_CREDIT_MAX = 20

    // Secret places
    const val SECRET_GENERATOR_VERSION = 1
    const val SECRET_SLOTS_PER_L2 = 1
    const val SECRET_SLOTS_PER_L2_UNLOCKED = 2
    const val SECRET_PROCEDURAL_SPAWN_CHANCE = SPAWN_RATIO_SECRET_PLACE
    const val MAX_SECRET_L2_CELLS_SCAN = 40
    const val MAX_SECRET_L2_CELLS_SCAN_UNLOCKED = 64
    const val MAX_SECRET_FEATURES = 60
    /** Minimum map zoom to show and discover secret places (overlay and tap). */
    const val SECRET_DISPLAY_MIN_ZOOM = 17.0
    const val SECRET_COLLECT_MIN_ZOOM = SECRET_DISPLAY_MIN_ZOOM
    const val SECRET_COLLECT_RADIUS_M = 40.0
    const val SECRET_CREDIT_COMMON = 0
    const val SECRET_CREDIT_RARE = 0
    const val SECRET_CREDIT_LEGENDARY = 0
    const val SECRET_CREDIT_REGION_L1 = 0
    const val SECRET_NEST_BONUS_PER_SLOT = 0
    const val SECRET_L1_COMPLETION_THRESHOLD = 3
    const val SECRET_UNLOCKED_MIN_IN_VIEWPORT = 4
    const val DISCOVERED_SECRET_CACHE_CAPACITY = 4_000
}
