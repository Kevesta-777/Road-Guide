package com.example.roadguideapp.goldhunt.treasure

import com.example.roadguideapp.goldhunt.GoldHuntConfig
import java.util.Locale

internal data class TreasureViewportLogState(
    val title: String,
    val summaryLine: String,
    val detailLines: List<String>,
    val footerLine: String?,
    val compactLogLines: List<String> = emptyList(),
)

internal data class TreasureViewportNumberLogState(
    val title: String,
    val countLine: String,
    val typeCountLines: List<String>,
    val footerLine: String?,
    val treasuresInView: Int = 0,
)

internal object TreasureViewportLogBuilder {
    fun fromTreasures(treasures: List<TreasureSpec>, zoom: Double): TreasureViewportLogState {
        if (zoom < GoldHuntConfig.TREASURE_DISPLAY_MIN_ZOOM) {
            return TreasureViewportLogState(
                title = "Treasure log",
                summaryLine = "Hidden until zoom in",
                detailLines = emptyList(),
                compactLogLines = emptyList(),
                footerLine = String.format(
                    Locale.US,
                    "Visible at zoom %.1f+",
                    GoldHuntConfig.TREASURE_DISPLAY_MIN_ZOOM,
                ),
            )
        }
        if (treasures.isEmpty()) {
            return TreasureViewportLogState(
                title = "Treasure log",
                summaryLine = "0 generated in view",
                detailLines = emptyList(),
                compactLogLines = emptyList(),
                footerLine = "Pan map to scan cells",
            )
        }

        val typeCountLines = buildTypeCountLines(treasures, maxLines = 4)
        val hiddenTypes = (treasures.map { it.type }.toSet().size - typeCountLines.size).coerceAtLeast(0)
        val summaryLine = String.format(Locale.US, "%d treasures in view", treasures.size)
        val detailLines = typeCountLines
        val footerLine = when {
            hiddenTypes > 0 -> "+$hiddenTypes more types in view"
            treasures.size >= GoldHuntConfig.MAX_TREASURE_FEATURES ->
                "Capped at ${GoldHuntConfig.MAX_TREASURE_FEATURES} in viewport"
            else -> null
        }
        return TreasureViewportLogState(
            title = "Treasure log",
            summaryLine = summaryLine,
            detailLines = detailLines,
            footerLine = footerLine,
            compactLogLines = compactLogLines(treasures),
        )
    }

    private fun compactLogLines(treasures: List<TreasureSpec>): List<String> {
        if (treasures.isEmpty()) return emptyList()
        val order = listOf(
            TreasureType.GIFT,
            TreasureType.FLOWER,
            TreasureType.STAR,
            TreasureType.CRYSTAL,
            TreasureType.HINT,
        )
        return treasures.groupBy { it.type }
            .entries
            .sortedBy { (type, _) -> order.indexOf(type).let { if (it < 0) order.size else it } }
            .take(3)
            .map { (type, list) ->
                val credits = list.sumOf { it.creditAmount }
                String.format(
                    Locale.US,
                    "%s x%d (%d cr)",
                    treasureTypeCompactCode(type),
                    list.size,
                    credits,
                )
            }
    }
}

internal object TreasureViewportNumberLogBuilder {
    fun fromTreasures(treasures: List<TreasureSpec>, zoom: Double): TreasureViewportNumberLogState {
        val displayZoom = GoldHuntConfig.TREASURE_DISPLAY_MIN_ZOOM
        if (zoom < displayZoom) {
            return TreasureViewportNumberLogState(
                title = "Generated count",
                countLine = "— (zoom in)",
                typeCountLines = emptyList(),
                treasuresInView = 0,
                footerLine = String.format(
                    Locale.US,
                    "Visible at zoom %.1f+",
                    displayZoom,
                ),
            )
        }
        if (treasures.isEmpty()) {
            return TreasureViewportNumberLogState(
                title = "Generated count",
                countLine = "0 in viewport",
                typeCountLines = emptyList(),
                treasuresInView = 0,
                footerLine = "Pan to scan grid cells",
            )
        }

        val typeCountLines = buildTypeCountLines(treasures, maxLines = 5)
        val hiddenTypes = (treasures.map { it.type }.toSet().size - typeCountLines.size).coerceAtLeast(0)
        val footerLine = buildString {
            if (hiddenTypes > 0) append("+$hiddenTypes more types")
            if (treasures.size >= GoldHuntConfig.MAX_TREASURE_FEATURES) {
                if (isNotEmpty()) append(" · ")
                append("cap ${GoldHuntConfig.MAX_TREASURE_FEATURES}")
            }
        }.ifBlank { null }

        return TreasureViewportNumberLogState(
            title = "Generated count",
            countLine = String.format(Locale.US, "%d treasures in view", treasures.size),
            typeCountLines = typeCountLines,
            treasuresInView = treasures.size,
            footerLine = footerLine,
        )
    }
}

private fun buildTypeCountLines(treasures: List<TreasureSpec>, maxLines: Int): List<String> {
    val order = listOf(
        TreasureType.STAR,
        TreasureType.FLOWER,
        TreasureType.CRYSTAL,
        TreasureType.GIFT,
        TreasureType.HINT,
    )
    return treasures.groupBy { it.type }
        .entries
        .sortedBy { (type, _) -> order.indexOf(type).let { if (it < 0) order.size else it } }
        .take(maxLines)
        .map { (type, list) ->
            String.format(Locale.US, "%s: %d", type.id.lowercase(), list.size)
        }
}

private fun treasureTypeCompactCode(type: TreasureType): String = when (type) {
    TreasureType.STAR -> "S"
    TreasureType.FLOWER -> "F"
    TreasureType.CRYSTAL -> "C"
    TreasureType.GIFT -> "G"
    TreasureType.HINT -> "H"
}
