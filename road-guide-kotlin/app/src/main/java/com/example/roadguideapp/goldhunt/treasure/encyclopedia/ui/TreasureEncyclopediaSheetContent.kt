package com.example.roadguideapp.goldhunt.treasure.encyclopedia.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.roadguideapp.R
import com.example.roadguideapp.auth.AuthGroupedCard
import com.example.roadguideapp.auth.AuthNavDivider
import com.example.roadguideapp.auth.AuthPageTopBar
import com.example.roadguideapp.auth.AuthSectionLabel
import com.example.roadguideapp.map.AppleMapsSheetGestures
import com.example.roadguideapp.map.AppleMapsSheetTheme

private val StickyHeaderMinHeight = 52.dp

@Composable
internal fun TreasureEncyclopediaSheetContent(
    state: TreasureEncyclopediaUiState,
    isLoading: Boolean,
    sheetTheme: AppleMapsSheetTheme,
    scrollState: ScrollState,
    contentScrollEnabled: Boolean,
    sheetGestures: AppleMapsSheetGestures,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var stickyHeaderHeightPx = remember { mutableIntStateOf(0) }
    val stickyHeaderHeight = with(density) {
        maxOf(stickyHeaderHeightPx.intValue.toDp(), StickyHeaderMinHeight)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(sheetTheme.sheetSurface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    sheetGestures.scrollContent(
                        scrollState = scrollState,
                        scrollEnabled = contentScrollEnabled,
                    ),
                )
                .padding(top = stickyHeaderHeight)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = sheetTheme.accent)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    EncyclopediaProgressCard(state = state, sheetTheme = sheetTheme)
                    EncyclopediaEntriesSection(state = state, sheetTheme = sheetTheme)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(1f)
                .background(sheetTheme.sheetSurface)
                .then(sheetGestures.chromeDrag)
                .onSizeChanged { stickyHeaderHeightPx.intValue = it.height },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                AuthPageTopBar(
                    title = stringResource(R.string.treasure_encyclopedia_title),
                    sheetTheme = sheetTheme,
                    onClose = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun EncyclopediaProgressCard(
    state: TreasureEncyclopediaUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val goldAccent = Color(0xFFFFD60A)
    val cardBrush = Brush.linearGradient(
        colors = listOf(
            sheetTheme.summaryCardBackground,
            sheetTheme.summaryCardBackground.copy(alpha = 0.88f),
        ),
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBrush)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text(
                text = stringResource(R.string.treasure_encyclopedia_summary),
                color = sheetTheme.onAccent.copy(alpha = 0.82f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.progressLabel,
                color = sheetTheme.onAccent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { state.progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = goldAccent,
                trackColor = sheetTheme.onAccent.copy(alpha = 0.22f),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.treasure_encyclopedia_total_credits,
                    state.totalCreditsLabel,
                ),
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun EncyclopediaEntriesSection(
    state: TreasureEncyclopediaUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.treasure_encyclopedia_entries),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            state.entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                EncyclopediaEntryRow(entry = entry, sheetTheme = sheetTheme)
            }
        }
    }
}

@Composable
private fun EncyclopediaEntryRow(
    entry: TreasureEncyclopediaEntryUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val contentAlpha = if (entry.isLocked) 0.55f else 1f
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = entry.displayName,
                color = sheetTheme.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            EncyclopediaStatusBadge(
                label = entry.statusLabel,
                isFound = entry.isFound,
                sheetTheme = sheetTheme,
            )
        }
        EncyclopediaDetailLine(
            label = stringResource(R.string.treasure_encyclopedia_field_rarity),
            value = entry.rarityLabel,
            sheetTheme = sheetTheme,
        )
        EncyclopediaDetailLine(
            label = stringResource(R.string.treasure_encyclopedia_field_first_discovery),
            value = entry.firstDiscoveryLabel,
            sheetTheme = sheetTheme,
        )
        EncyclopediaDetailLine(
            label = stringResource(R.string.treasure_encyclopedia_field_credits),
            value = entry.creditsLabel,
            sheetTheme = sheetTheme,
        )
        if (entry.isFound) {
            EncyclopediaDetailLine(
                label = stringResource(R.string.treasure_encyclopedia_field_collections),
                value = entry.collectionCountLabel,
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun EncyclopediaStatusBadge(
    label: String,
    isFound: Boolean,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val background = if (isFound) {
        Color(0xFF22C55E).copy(alpha = 0.18f)
    } else {
        sheetTheme.secondaryText.copy(alpha = 0.12f)
    }
    val textColor = if (isFound) Color(0xFF16A34A) else sheetTheme.secondaryText
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = background,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun EncyclopediaDetailLine(
    label: String,
    value: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = sheetTheme.secondaryText,
            fontSize = 14.sp,
        )
        Text(
            text = value,
            color = sheetTheme.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
