package com.example.roadguideapp.goldhunt.relics.rewards

import com.example.roadguideapp.goldhunt.relics.LegendaryRelic
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicCatalog
import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import com.example.roadguideapp.goldhunt.relics.story.LegendaryRelicStoryCatalog
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType
import org.json.JSONObject

internal object LegendaryRelicRewardDispatcher {
    fun dispatch(
        relic: LegendaryRelic,
        definition: LegendaryRelicDefinition? = LegendaryRelicCatalog.findById(relic.relicId),
    ): LegendaryRelicRewardOutcome = LegendaryRelicRewardOutcome(
        relic = relic,
        creditGrant = CreditGrant(
            eventId = LegendaryRelicRewardSchema.creditsEventId(relic.relicId),
            ruleType = RewardRuleType.LEGENDARY_RELIC_COMPLETION,
            amount = relic.rewardCredits,
            label = relic.relicId,
        ),
        xp = relic.rewardXp,
        storyFragmentKey = LegendaryRelicStoryCatalog.completionStoryFragmentKey(relic, definition),
        titleKey = relic.titleKey ?: definition?.resolvedTitleKey,
        badgeKey = relic.badgeKey ?: definition?.resolvedBadgeKey,
        cosmeticKey = resolveCosmeticKey(relic, definition),
    )

    private fun resolveCosmeticKey(
        relic: LegendaryRelic,
        definition: LegendaryRelicDefinition?,
    ): String? {
        val extensionJson = definition?.extensionJson ?: relic.extensionJson
        if (extensionJson.isBlank() || extensionJson == RelicSchema.EMPTY_EXTENSIONS_JSON) {
            return null
        }
        return runCatching {
            JSONObject(extensionJson)
                .optString(LegendaryRelicRewardSchema.ExtensionKeys.COSMETIC_KEY)
                .trim()
                .takeIf { it.isNotEmpty() }
        }.getOrNull()
    }
}
