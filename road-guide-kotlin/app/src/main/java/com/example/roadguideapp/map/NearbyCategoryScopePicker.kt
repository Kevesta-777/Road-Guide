package com.example.roadguideapp.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class NearbyScopeOption(
    val context: NearbySearchContext,
    val label: String,
)

@Composable
internal fun NearbyCategoryScopePicker(
    sheetTheme: AppleMapsSheetTheme,
    options: List<NearbyScopeOption>,
    selected: NearbySearchContext,
    onSelect: (NearbySearchContext) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (options.size <= 1) return

    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (option in options) {
            val isSelected = selected::class == option.context::class &&
                when (selected) {
                    is NearbySearchContext.NearPlace -> {
                        val other = option.context as? NearbySearchContext.NearPlace
                        other != null &&
                            selected.location.latitude == other.location.latitude &&
                            selected.location.longitude == other.location.longitude
                    }
                    is NearbySearchContext.AlongRoute -> option.context is NearbySearchContext.AlongRoute
                    NearbySearchContext.MapCenter -> option.context is NearbySearchContext.MapCenter
                }
            ScopeChip(
                sheetTheme = sheetTheme,
                label = option.label,
                selected = isSelected,
                onClick = { onSelect(option.context) },
            )
        }
    }
}

@Composable
private fun ScopeChip(
    sheetTheme: AppleMapsSheetTheme,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) sheetTheme.accent.copy(alpha = 0.18f) else sheetTheme.searchFieldFill
    val textColor = if (selected) sheetTheme.accent else sheetTheme.secondaryText
    Text(
        text = label,
        color = textColor,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

internal fun nearbyScopeOptions(
    mapCenterLabel: String,
    placeFallbackLabel: String,
    routeLabel: String,
    placeContext: NearbySearchContext.NearPlace?,
    routeContext: NearbySearchContext.AlongRoute?,
): List<NearbyScopeOption> = buildList {
    add(NearbyScopeOption(NearbySearchContext.MapCenter, mapCenterLabel))
    if (placeContext != null) {
        val label = placeContext.label?.takeIf { it.isNotBlank() } ?: placeFallbackLabel
        add(NearbyScopeOption(placeContext, label))
    }
    if (routeContext != null) {
        add(NearbyScopeOption(routeContext, routeLabel))
    }
}
