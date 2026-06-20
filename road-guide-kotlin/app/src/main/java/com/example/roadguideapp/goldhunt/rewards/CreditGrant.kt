package com.example.roadguideapp.goldhunt.rewards

internal data class CreditGrant(
    val eventId: String,
    val ruleType: String,
    val amount: Int,
    val label: String? = null,
)

internal object RewardRuleType {
    const val CELL_L0 = "CELL_L0"
    const val CLUSTER_L1 = "CLUSTER_L1"
    const val DISTRICT_L2 = "DISTRICT_L2"
    const val ROAD_SEGMENT = "ROAD_SEGMENT"
    const val STREET = "STREET"
    const val NEIGHBORHOOD = "NEIGHBORHOOD"
    const val MILESTONE = "MILESTONE"
    const val TREASURE_STAR = "TREASURE_STAR"
    const val TREASURE_FLOWER = "TREASURE_FLOWER"
    const val TREASURE_CRYSTAL = "TREASURE_CRYSTAL"
    const val TREASURE_GIFT = "TREASURE_GIFT"
    const val TREASURE_HINT = "TREASURE_HINT"
    const val SECRET_COMMON = "SECRET_COMMON"
    const val SECRET_RARE = "SECRET_RARE"
    const val SECRET_LEGENDARY = "SECRET_LEGENDARY"
    const val SECRET_REGION_L1 = "SECRET_REGION_L1"
    const val SECRET_NEST_BONUS = "SECRET_NEST_BONUS"
    const val CLUSTER_COMPLETION = "CLUSTER_COMPLETION"
    const val SECRET_PLACE_CATEGORY = "SECRET_PLACE_CATEGORY"
    const val PANORAMA_HUNT_COMPLETION = "PANORAMA_HUNT_COMPLETION"
    const val RADAR_DETECTION = "RADAR_DETECTION"
    const val RADAR_PULSE = "RADAR_PULSE"
    const val SEASONAL_EVENT_TREASURE = "SEASONAL_EVENT_TREASURE"
    const val SEASONAL_EVENT_COMPLETION = "SEASONAL_EVENT_COMPLETION"
    const val ACHIEVEMENT_COMPLETION = "ACHIEVEMENT_COMPLETION"
    const val LEGENDARY_RELIC_COMPLETION = "LEGENDARY_RELIC_COMPLETION"
}
