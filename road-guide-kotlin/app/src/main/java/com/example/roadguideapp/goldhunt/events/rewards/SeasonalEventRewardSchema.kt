package com.example.roadguideapp.goldhunt.events.rewards

import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity

internal object SeasonalEventRewardSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    const val ACHIEVEMENT_PROGRESS_INCREMENT = 1

    val STORY_FRAGMENT_MIN_RARITY: TreasureRarity = TreasureRarity.UNCOMMON
    val LEGENDARY_RELIC_MIN_RARITY: TreasureRarity = TreasureRarity.EPIC

    object HookTypes {
        const val STORY_FRAGMENT = "storyFragment"
        const val ACHIEVEMENT = "achievement"
        const val LEGENDARY_RELIC = "legendaryRelic"
    }

    object EventPrefixes {
        const val TREASURE = "seasonal_event_reward:treasure"
        const val STORY = "seasonal_event_reward:story"
        const val ACHIEVEMENT = "seasonal_event_reward:achievement"
        const val RELIC = "seasonal_event_reward:relic"
        const val PARTICIPATION = "seasonal_event_reward:participation"
    }

    object GrantKeys {
        const val TREASURE_COLLECTION = "treasure_collection"
        const val PARTICIPATION = "participation"
        const val COMPLETION = "completion"
    }
}
