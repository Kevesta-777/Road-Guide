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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        color = sheetTheme.cardElevated,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            when {
                loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = sheetTheme.accent,
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.apple_search_loading),
                            color = sheetTheme.secondaryText,
                            fontSize = 15.sp,
                        )
                    }
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
                    Text(
                        text = stringResource(R.string.apple_search_type_to_search),
                        color = sheetTheme.secondaryText,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    )
                }

                suggestions.isEmpty() -> {
                    Text(
                        text = stringResource(
                            if (isNearbyCategoryResults) {
                                R.string.apple_nearby_no_results
                            } else {
                                R.string.apple_search_no_results
                            },
                        ),
                        color = sheetTheme.secondaryText,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    )
                }

                else -> {
                    suggestions.forEachIndexed { index, result ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = sheetTheme.divider,
                                modifier = Modifier.padding(start = 68.dp),
                            )
                        }
                        PeliasSearchResultRow(
                            result = result,
                            sheetTheme = sheetTheme,
                            onClick = { onResultSelected(result) },
                        )
                    }
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
    val iconStyle = layerIconStyle(result.layer)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconStyle.background, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = iconStyle.glyph,
                color = iconStyle.foreground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
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
                )
            }
        }
    }
}

private data class LayerIconStyle(
    val glyph: String,
    val background: Color,
    val foreground: Color,
)

private fun layerIconStyle(layer: String?): LayerIconStyle {
    return when (layer?.lowercase()) {
        "venue", "address" -> LayerIconStyle("📍", Color(0xFFE8D4F8), Color(0xFF7D3BB8))
        "street", "intersection" -> LayerIconStyle("🛣", Color(0xFFE5E5EA), Color(0xFF48484A))
        "locality", "localadmin", "borough", "neighbourhood" ->
            LayerIconStyle("🏙", Color(0xFFE5E5EA), Color(0xFF48484A))
        "region", "macroregion", "country" -> LayerIconStyle("🌍", Color(0xFFE5E5EA), Color(0xFF48484A))
        else -> LayerIconStyle("•", Color(0xFFE5E5EA), Color(0xFF48484A))
    }
}
