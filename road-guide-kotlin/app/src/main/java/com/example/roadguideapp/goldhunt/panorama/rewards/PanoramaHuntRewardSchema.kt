package com.example.roadguideapp.goldhunt.panorama.rewards

internal object PanoramaHuntRewardSchema {
    const val VERSION = 1
    const val EMPTY_EXTENSIONS_JSON = "{}"

    object HookTypes {
        const val STORY_FRAGMENT = "storyFragment"
        const val ACHIEVEMENT = "achievement"
        const val LEGENDARY_RELIC = "legendaryRelic"
    }

    object EventPrefixes {
        const val COMPLETION = "panorama_hunt_reward:completion"
        const val STORY = "panorama_hunt_reward:story"
        const val ACHIEVEMENT = "panorama_hunt_reward:achievement"
        const val RELIC = "panorama_hunt_reward:relic"
    }
}
