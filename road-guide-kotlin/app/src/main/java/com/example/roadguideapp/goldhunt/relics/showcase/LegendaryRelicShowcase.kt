package com.example.roadguideapp.goldhunt.relics.showcase

internal data class LegendaryRelicShowcase(
    val entries: List<LegendaryRelicShowcaseEntry>,
    val badgeHighlights: List<LegendaryRelicShowcaseBadgeHighlight>,
    val titleHighlights: List<LegendaryRelicShowcaseTitleHighlight>,
    val progress: LegendaryRelicShowcaseProgress,
)
