package com.example.roadguideapp.goldhunt.events.collectionbook.ui

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
internal fun SeasonalEventCollectionBookSheetContent(
    state: SeasonalEventCollectionBookUiState,
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
                    SeasonalEventCollectionBookProgressCard(state = state, sheetTheme = sheetTheme)
                    if (state.cycleEntries.isNotEmpty()) {
                        SeasonalEventCollectionBookCyclesSection(state = state, sheetTheme = sheetTheme)
                    }
                    if (state.missingFamilies.isNotEmpty()) {
                        SeasonalEventCollectionBookMissingFamiliesSection(state = state, sheetTheme = sheetTheme)
                    }
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
                    title = stringResource(R.string.seasonal_event_collection_book_title),
                    sheetTheme = sheetTheme,
                    onClose = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SeasonalEventCollectionBookProgressCard(
    state: SeasonalEventCollectionBookUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val accent = Color(0xFFFF9F0A)
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
                text = stringResource(R.string.seasonal_event_collection_book_summary),
                color = sheetTheme.onAccent.copy(alpha = 0.82f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.participatedProgressLabel,
                color = sheetTheme.onAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.participatedProgressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accent,
                trackColor = sheetTheme.onAccent.copy(alpha = 0.22f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.completedProgressLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.92f),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.completedProgressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF22C55E),
                trackColor = sheetTheme.onAccent.copy(alpha = 0.22f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.missingProgressLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.92f),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.missingProgressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = sheetTheme.onAccent.copy(alpha = 0.55f),
                trackColor = sheetTheme.onAccent.copy(alpha = 0.22f),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.seasonal_event_collection_book_total_rewards,
                    state.totalRewardsLabel,
                ),
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            Text(
                text = stringResource(
                    R.string.seasonal_event_collection_book_total_xp,
                    state.totalXpLabel,
                ),
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            Text(
                text = stringResource(
                    R.string.seasonal_event_collection_book_total_achievements,
                    state.totalAchievementsEarned,
                ),
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun SeasonalEventCollectionBookCyclesSection(
    state: SeasonalEventCollectionBookUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.seasonal_event_collection_book_cycles),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            state.cycleEntries.forEachIndexed { index, entry ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                SeasonalEventCollectionBookCycleRow(entry = entry, sheetTheme = sheetTheme)
            }
        }
    }
}

@Composable
private fun SeasonalEventCollectionBookMissingFamiliesSection(
    state: SeasonalEventCollectionBookUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.seasonal_event_collection_book_missing_families),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            state.missingFamilies.forEachIndexed { index, family ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.72f)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = family.displayName,
                            color = sheetTheme.primaryText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = family.statusLabel,
                            color = sheetTheme.secondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    SeasonalEventCollectionBookDetailLine(
                        label = stringResource(R.string.seasonal_event_collection_book_min_level_label),
                        value = family.minExplorerLevelLabel,
                        sheetTheme = sheetTheme,
                    )
                }
            }
        }
    }
}

@Composable
private fun SeasonalEventCollectionBookCycleRow(
    entry: SeasonalEventCollectionBookCycleUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${entry.displayName} (${entry.cycleYearLabel})",
                color = sheetTheme.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = entry.statusLabel,
                color = if (entry.isCompleted) Color(0xFF22C55E) else sheetTheme.accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        SeasonalEventCollectionBookDetailLine(
            label = stringResource(R.string.seasonal_event_collection_book_field_participated_date),
            value = entry.participatedDateLabel,
            sheetTheme = sheetTheme,
        )
        SeasonalEventCollectionBookDetailLine(
            label = stringResource(R.string.seasonal_event_collection_book_field_completion_date),
            value = entry.completionDateLabel,
            sheetTheme = sheetTheme,
        )
        SeasonalEventCollectionBookDetailLine(
            label = stringResource(R.string.seasonal_event_collection_book_field_rewards),
            value = entry.rewardsLabel,
            sheetTheme = sheetTheme,
        )
        SeasonalEventCollectionBookDetailLine(
            label = stringResource(R.string.seasonal_event_collection_book_field_xp),
            value = entry.xpLabel,
            sheetTheme = sheetTheme,
        )
        SeasonalEventCollectionBookDetailLine(
            label = stringResource(R.string.seasonal_event_collection_book_field_fragments),
            value = entry.fragmentsLabel,
            sheetTheme = sheetTheme,
        )
        SeasonalEventCollectionBookDetailLine(
            label = stringResource(R.string.seasonal_event_collection_book_field_achievements_earned),
            value = entry.achievementsEarnedLabel,
            sheetTheme = sheetTheme,
        )
        entry.participationAchievementLabel?.let { label ->
            SeasonalEventCollectionBookDetailLine(
                label = stringResource(R.string.seasonal_event_collection_book_field_participation_achievement),
                value = label,
                sheetTheme = sheetTheme,
            )
        }
        entry.completionAchievementLabel?.let { label ->
            SeasonalEventCollectionBookDetailLine(
                label = stringResource(R.string.seasonal_event_collection_book_field_completion_achievement),
                value = label,
                sheetTheme = sheetTheme,
            )
        }
        entry.masteryAchievementLabel?.let { label ->
            SeasonalEventCollectionBookDetailLine(
                label = stringResource(R.string.seasonal_event_collection_book_field_mastery_achievement),
                value = label,
                sheetTheme = sheetTheme,
            )
        }
        entry.storyFragmentLabel?.let { label ->
            SeasonalEventCollectionBookDetailLine(
                label = stringResource(R.string.seasonal_event_collection_book_field_story_fragment),
                value = label,
                sheetTheme = sheetTheme,
            )
        }
        entry.legendaryRelicLabel?.let { label ->
            SeasonalEventCollectionBookDetailLine(
                label = stringResource(R.string.seasonal_event_collection_book_field_legendary_relic),
                value = label,
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun SeasonalEventCollectionBookDetailLine(
    label: String,
    value: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            color = sheetTheme.secondaryText,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = sheetTheme.primaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.2f),
        )
    }
}
