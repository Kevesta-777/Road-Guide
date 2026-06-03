package com.example.roadguideapp.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R

@Composable
internal fun NearbyCategorySearchHeader(
    sheetTheme: AppleMapsSheetTheme,
    categoryLabel: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchTextField(
            value = categoryLabel,
            onValueChange = {},
            placeholder = categoryLabel,
            sheetTheme = sheetTheme,
            modifier = Modifier.weight(1f),
            readOnly = true,
            showClearWhenNonEmpty = false,
            trailingContent = {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.apple_close),
                        tint = sheetTheme.searchFieldHint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )
    }
}

@Composable
internal fun NearbyCategoryResultsContent(
    sheetTheme: AppleMapsSheetTheme,
    categoryLabel: String,
    results: List<PeliasSearchResult>,
    loading: Boolean,
    errorMessage: String?,
    filterState: NearbyResultsFilter.State,
    availableChains: List<String>,
    pickHoursByGid: Map<String, String>,
    scopeOptions: List<NearbyScopeOption>,
    selectedSearchContext: NearbySearchContext,
    onScopeSelected: (NearbySearchContext) -> Unit,
    onFilterChange: (NearbyResultsFilter.State) -> Unit,
    onResultSelected: (PeliasSearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        NearbyCategoryScopePicker(
            sheetTheme = sheetTheme,
            options = scopeOptions,
            selected = selectedSearchContext,
            onSelect = onScopeSelected,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        NearbyCategoryFilterRow(
            sheetTheme = sheetTheme,
            filterState = filterState,
            availableChains = availableChains,
            onFilterChange = onFilterChange,
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            loading -> {
                SearchLoadingRow(
                    sheetTheme = sheetTheme,
                    message = stringResource(R.string.apple_search_loading),
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }

            results.isEmpty() -> {
                SearchEmptyState(
                    title = stringResource(R.string.apple_nearby_no_results),
                    sheetTheme = sheetTheme,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            else -> {
                results.forEachIndexed { index, result ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = sheetTheme.divider,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                    NearbyCategoryResultCard(
                        result = result,
                        categoryLabel = categoryLabel,
                        hoursSummary = pickHoursByGid[result.gid],
                        sheetTheme = sheetTheme,
                        onClick = { onResultSelected(result) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyCategoryFilterRow(
    sheetTheme: AppleMapsSheetTheme,
    filterState: NearbyResultsFilter.State,
    availableChains: List<String>,
    onFilterChange: (NearbyResultsFilter.State) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val chainLabel = filterState.selectedChain ?: stringResource(R.string.apple_nearby_filter_chains)
    val priceLabel = when (filterState.priceTier) {
        NearbyResultsFilter.NearbyPriceTier.All -> stringResource(R.string.apple_nearby_filter_prices)
        NearbyResultsFilter.NearbyPriceTier.Budget -> stringResource(R.string.apple_nearby_filter_price_budget)
        NearbyResultsFilter.NearbyPriceTier.Moderate -> stringResource(R.string.apple_nearby_filter_price_moderate)
        NearbyResultsFilter.NearbyPriceTier.Premium -> stringResource(R.string.apple_nearby_filter_price_premium)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NearbyFilterChip(
            sheetTheme = sheetTheme,
            label = stringResource(R.string.apple_nearby_filter_open_now),
            selected = filterState.openNowOnly,
            onClick = {
                onFilterChange(filterState.copy(openNowOnly = !filterState.openNowOnly))
            },
        )
        NearbyFilterChip(
            sheetTheme = sheetTheme,
            label = chainLabel,
            selected = filterState.selectedChain != null,
            showDropdown = true,
            onClick = {
                val nextChain = when (filterState.selectedChain) {
                    null -> availableChains.firstOrNull()
                    else -> {
                        val idx = availableChains.indexOf(filterState.selectedChain)
                        if (idx < 0 || idx >= availableChains.lastIndex) null else availableChains[idx + 1]
                    }
                }
                onFilterChange(filterState.copy(selectedChain = nextChain))
            },
        )
        NearbyFilterChip(
            sheetTheme = sheetTheme,
            label = priceLabel,
            selected = filterState.priceTier != NearbyResultsFilter.NearbyPriceTier.All,
            showDropdown = true,
            onClick = {
                val next = when (filterState.priceTier) {
                    NearbyResultsFilter.NearbyPriceTier.All -> NearbyResultsFilter.NearbyPriceTier.Budget
                    NearbyResultsFilter.NearbyPriceTier.Budget -> NearbyResultsFilter.NearbyPriceTier.Moderate
                    NearbyResultsFilter.NearbyPriceTier.Moderate -> NearbyResultsFilter.NearbyPriceTier.Premium
                    NearbyResultsFilter.NearbyPriceTier.Premium -> NearbyResultsFilter.NearbyPriceTier.All
                }
                onFilterChange(filterState.copy(priceTier = next))
            },
        )
    }
}

@Composable
private fun NearbyFilterChip(
    sheetTheme: AppleMapsSheetTheme,
    label: String,
    selected: Boolean = false,
    showDropdown: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchFilterChip(
        label = label,
        sheetTheme = sheetTheme,
        selected = selected,
        showDropdown = showDropdown,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun NearbyCategoryResultCard(
    result: PeliasSearchResult,
    categoryLabel: String,
    hoursSummary: String?,
    sheetTheme: AppleMapsSheetTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = resultSubtitle(result, categoryLabel)
    val locality = resultLocality(result)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = result.name,
            color = sheetTheme.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = sheetTheme.secondaryText,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (locality.isNotEmpty()) {
                Text(
                    text = locality,
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = stringResource(R.string.apple_nearby_price_symbol),
                color = sheetTheme.secondaryText,
                fontSize = 14.sp,
            )
            Text(
                text = "·",
                color = sheetTheme.secondaryText,
                fontSize = 14.sp,
            )
            Text(
                text = hoursSummary?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.apple_place_hours_unknown),
                color = sheetTheme.secondaryText,
                fontSize = 14.sp,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) { index ->
                NearbyResultPhotoPlaceholder(
                    sheetTheme = sheetTheme,
                    index = index,
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                )
            }
        }
    }
}

@Composable
private fun NearbyResultPhotoPlaceholder(
    sheetTheme: AppleMapsSheetTheme,
    index: Int,
    modifier: Modifier = Modifier,
) {
    val gradients = listOf(
        listOf(Color(0xFFFFB347), Color(0xFFFFCC80)),
        listOf(Color(0xFF90CAF9), Color(0xFFB3E5FC)),
        listOf(Color(0xFFA5D6A7), Color(0xFFC8E6C9)),
    )
    val colors = gradients[index % gradients.size]
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(colors),
                shape = RoundedCornerShape(10.dp),
            ),
    )
}

private fun resultSubtitle(result: PeliasSearchResult, categoryLabel: String): String {
    val layerName = result.layer?.replace('_', ' ')?.replaceFirstChar { c ->
        if (c.isLowerCase()) c.titlecase() else c.toString()
    }
    return when {
        layerName != null && layerName.equals("venue", ignoreCase = true) ->
            "$categoryLabel Restaurant"
        layerName != null -> layerName
        else -> categoryLabel
    }
}

private fun resultLocality(result: PeliasSearchResult): String {
    val label = result.label
    if (label == result.name) return ""
    val withoutName = label.removePrefix(result.name).trimStart(',', ' ')
    val parts = withoutName.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    return parts.firstOrNull().orEmpty()
}
