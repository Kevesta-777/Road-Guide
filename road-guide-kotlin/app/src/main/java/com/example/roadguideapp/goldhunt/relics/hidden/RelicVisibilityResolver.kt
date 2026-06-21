package com.example.roadguideapp.goldhunt.relics.hidden

import com.example.roadguideapp.goldhunt.relics.LegendaryRelicDefinition
import com.example.roadguideapp.goldhunt.relics.RelicCategory
import com.example.roadguideapp.goldhunt.relics.RelicSchema
import org.json.JSONObject

internal object RelicVisibilityResolver {
    fun forDefinition(definition: LegendaryRelicDefinition): RelicVisibility {
        visibilityOverride(definition)?.let { return it }
        return resolveFromRules(
            relicId = definition.relicId,
            category = definition.category,
        )
    }

    fun forRelic(
        relicId: String,
        category: RelicCategory,
        extensionJson: String = RelicSchema.EMPTY_EXTENSIONS_JSON,
    ): RelicVisibility {
        visibilityOverride(extensionJson)?.let { return it }
        return resolveFromRules(relicId = relicId, category = category)
    }

    private fun resolveFromRules(
        relicId: String,
        category: RelicCategory,
    ): RelicVisibility = when {
        category == RelicCategory.HIDDEN -> RelicVisibility.HIDDEN
        relicId.contains(":milestone_500") -> RelicVisibility.HIDDEN
        else -> RelicVisibility.VISIBLE
    }

    private fun visibilityOverride(definition: LegendaryRelicDefinition): RelicVisibility? =
        visibilityOverride(definition.extensionJson)

    private fun visibilityOverride(extensionJson: String): RelicVisibility? {
        if (extensionJson.isBlank() || extensionJson == RelicSchema.EMPTY_EXTENSIONS_JSON) {
            return null
        }
        return runCatching {
            JSONObject(extensionJson)
                .optString(HiddenRelicDiscoverySchema.ExtensionKeys.VISIBILITY)
                .trim()
                .takeIf { it.isNotEmpty() }
                ?.let { RelicVisibility.fromId(it) }
        }.getOrNull()
    }
}
