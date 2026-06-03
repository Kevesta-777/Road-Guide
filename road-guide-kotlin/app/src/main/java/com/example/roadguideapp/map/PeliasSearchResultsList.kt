package com.example.roadguideapp.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R

@Composable
internal fun PeliasSearchResultsList(
    sheetTheme: AppleMapsSheetTheme,
    suggestions: List<PeliasSearchResult>,
    loading: Boolean,
    errorMessage: String?,
    query: String,
    onResultSelected: (PeliasSearchResult) -> Unit,
    isNearbyCategoryResults: Boolean = false,
    embeddedInForm: Boolean = false,
    minQueryLengthToShow: Int = 0,
    modifier: Modifier = Modifier,
) {
    val trimmedQuery = query.trim()
    if (
        minQueryLengthToShow > 0 &&
        trimmedQuery.length < minQueryLengthToShow &&
        !loading &&
        errorMessage == null
    ) {
        return
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (embeddedInForm) {
                    Modifier
                } else {
                    Modifier.padding(horizontal = 16.dp)
                },
            ),
        shape = RoundedCornerShape(SearchCardCornerRadius),
        color = sheetTheme.cardElevated,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            when {
                loading -> {
                    SearchLoadingRow(
                        sheetTheme = sheetTheme,
                        message = stringResource(R.string.apple_search_loading),
                    )
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        color = sheetTheme.accent,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    )
                }

                query.trim().isEmpty() -> {
                    if (minQueryLengthToShow > 0) {
                        return@Column
                    }
                    SearchEmptyState(
                        title = stringResource(R.string.apple_search_type_to_search),
                        sheetTheme = sheetTheme,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                suggestions.isEmpty() -> {
                    SearchEmptyState(
                        title = stringResource(
                            if (isNearbyCategoryResults) {
                                R.string.apple_nearby_no_results
                            } else {
                                R.string.apple_search_no_results
                            },
                        ),
                        sheetTheme = sheetTheme,
                        subtitle = if (!isNearbyCategoryResults) {
                            stringResource(R.string.apple_search_no_results_hint)
                        } else {
                            null
                        },
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                else -> {
                    SearchResultsCountLabel(
                        count = suggestions.size,
                        sheetTheme = sheetTheme,
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp),
                    )
                    suggestions.forEachIndexed { index, result ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = sheetTheme.divider,
                                modifier = Modifier.padding(start = 68.dp, end = 12.dp),
                            )
                        }
                        PeliasSearchResultRow(
                            result = result,
                            sheetTheme = sheetTheme,
                            onClick = { onResultSelected(result) },
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun PeliasSearchResultRow(
    result: PeliasSearchResult,
    sheetTheme: AppleMapsSheetTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconStyle = peliasLayerIconStyle(result.layer)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconStyle.background, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconStyle.icon,
                contentDescription = null,
                tint = iconStyle.foreground,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.name,
                color = sheetTheme.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (result.label != result.name) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = result.label,
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}
