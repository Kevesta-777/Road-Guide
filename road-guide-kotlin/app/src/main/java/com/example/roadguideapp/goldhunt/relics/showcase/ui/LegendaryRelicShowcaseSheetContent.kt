package com.example.roadguideapp.goldhunt.relics.showcase.ui

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
internal fun LegendaryRelicShowcaseSheetContent(
    state: LegendaryRelicShowcaseUiState,
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
                    LegendaryRelicShowcaseProgressCard(state = state, sheetTheme = sheetTheme)
                    if (state.entries.isNotEmpty()) {
                        LegendaryRelicShowcaseEntriesSection(state = state, sheetTheme = sheetTheme)
                    }
                    if (state.badgeHighlights.isNotEmpty()) {
                        LegendaryRelicShowcaseBadgesSection(state = state, sheetTheme = sheetTheme)
                    }
                    if (state.titleHighlights.isNotEmpty()) {
                        LegendaryRelicShowcaseTitlesSection(state = state, sheetTheme = sheetTheme)
                    }
                    if (state.entries.isEmpty()) {
                        LegendaryRelicShowcaseEmptyState(sheetTheme = sheetTheme)
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
                    title = stringResource(R.string.legendary_relic_showcase_title),
                    sheetTheme = sheetTheme,
                    onClose = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun LegendaryRelicShowcaseProgressCard(
    state: LegendaryRelicShowcaseUiState,
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
                text = stringResource(R.string.legendary_relic_showcase_summary),
                color = sheetTheme.onAccent.copy(alpha = 0.82f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.completedProgressLabel,
                color = sheetTheme.onAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = state.completionPercentageLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.completionProgressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF22C55E),
                trackColor = sheetTheme.onAccent.copy(alpha = 0.22f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.badgesEarnedLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.92f),
                fontSize = 16.sp,
            )
            Text(
                text = state.titlesEarnedLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.92f),
                fontSize = 16.sp,
            )
            Text(
                text = state.achievementLinksLabel,
                color = sheetTheme.onAccent.copy(alpha = 0.92f),
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.legendary_relic_showcase_total_rewards,
                    state.totalRewardsLabel,
                ),
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
            Text(
                text = stringResource(
                    R.string.legendary_relic_showcase_total_xp,
                    state.totalXpLabel,
                ),
                color = sheetTheme.onAccent.copy(alpha = 0.9f),
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun LegendaryRelicShowcaseEntriesSection(
    state: LegendaryRelicShowcaseUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.legendary_relic_showcase_completed_relics),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            state.entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                LegendaryRelicShowcaseEntryRow(entry = entry, sheetTheme = sheetTheme)
            }
        }
    }
}

@Composable
private fun LegendaryRelicShowcaseBadgesSection(
    state: LegendaryRelicShowcaseUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.legendary_relic_showcase_earned_badges),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            state.badgeHighlights.forEachIndexed { index, badge ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                LegendaryRelicShowcaseBadgeRow(badge = badge, sheetTheme = sheetTheme)
            }
        }
    }
}

@Composable
private fun LegendaryRelicShowcaseTitlesSection(
    state: LegendaryRelicShowcaseUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.legendary_relic_showcase_earned_titles),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            state.titleHighlights.forEachIndexed { index, title ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                LegendaryRelicShowcaseTitleRow(title = title, sheetTheme = sheetTheme)
            }
        }
    }
}

@Composable
private fun LegendaryRelicShowcaseEmptyState(
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.legendary_relic_showcase_empty),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        color = sheetTheme.secondaryText,
        fontSize = 15.sp,
    )
}

@Composable
private fun LegendaryRelicShowcaseEntryRow(
    entry: LegendaryRelicShowcaseEntryUiState,
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
                text = entry.displayName,
                color = sheetTheme.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            LegendaryRelicShowcaseRarityBadge(
                label = entry.rarityLabel,
                sheetTheme = sheetTheme,
            )
        }
        ShowcaseDetailLine(
            label = stringResource(R.string.legendary_relic_showcase_field_completion_date),
            value = entry.completionDateLabel,
            sheetTheme = sheetTheme,
        )
        ShowcaseDetailLine(
            label = stringResource(R.string.legendary_relic_showcase_field_category),
            value = entry.categoryLabel,
            sheetTheme = sheetTheme,
        )
        ShowcaseDetailLine(
            label = stringResource(R.string.legendary_relic_showcase_field_rewards),
            value = entry.creditsLabel,
            sheetTheme = sheetTheme,
        )
        ShowcaseDetailLine(
            label = stringResource(R.string.legendary_relic_showcase_field_xp),
            value = entry.xpLabel,
            sheetTheme = sheetTheme,
        )
        entry.badge?.let { badge ->
            ShowcaseDetailLine(
                label = stringResource(R.string.legendary_relic_showcase_field_badge),
                value = "${badge.title} · ${badge.unlockDateLabel}",
                sheetTheme = sheetTheme,
            )
        }
        entry.title?.let { title ->
            ShowcaseDetailLine(
                label = stringResource(R.string.legendary_relic_showcase_field_title),
                value = buildString {
                    append(title.displayTitle)
                    append(" · ")
                    append(title.unlockDateLabel)
                    title.activeLabel?.let { active ->
                        append(" · ")
                        append(active)
                    }
                },
                sheetTheme = sheetTheme,
            )
        }
        entry.story?.let { story ->
            ShowcaseDetailLine(
                label = stringResource(R.string.legendary_relic_showcase_field_story),
                value = story.title,
                sheetTheme = sheetTheme,
            )
            ShowcaseDetailLine(
                label = stringResource(R.string.legendary_relic_showcase_field_story_fragment),
                value = story.storyFragmentLabel,
                sheetTheme = sheetTheme,
            )
            ShowcaseDetailLine(
                label = stringResource(R.string.legendary_relic_showcase_field_story_chapters),
                value = story.chaptersProgressLabel,
                sheetTheme = sheetTheme,
            )
            ShowcaseDetailLine(
                label = stringResource(R.string.legendary_relic_showcase_field_story_lore),
                value = story.loreProgressLabel,
                sheetTheme = sheetTheme,
            )
            ShowcaseDetailLine(
                label = stringResource(R.string.legendary_relic_showcase_field_story_completion),
                value = "${story.completionStatusLabel} · ${story.unlockDateLabel}",
                sheetTheme = sheetTheme,
            )
        }
        if (entry.achievementLinks.isNotEmpty()) {
            Text(
                text = stringResource(R.string.legendary_relic_showcase_field_achievements),
                color = sheetTheme.secondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            entry.achievementLinks.forEach { link ->
                Text(
                    text = "${link.title} · ${link.linkTypeLabel} · ${link.statusLabel}",
                    color = sheetTheme.primaryText.copy(alpha = if (link.isCompleted) 1f else 0.78f),
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun LegendaryRelicShowcaseBadgeRow(
    badge: LegendaryRelicShowcaseBadgeUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (badge.earned) 1f else 0.75f)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = badge.title,
            color = sheetTheme.primaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        ShowcaseDetailLine(
            label = stringResource(R.string.legendary_relic_showcase_field_rarity),
            value = badge.rarityLabel,
            sheetTheme = sheetTheme,
        )
        ShowcaseDetailLine(
            label = stringResource(R.string.legendary_relic_showcase_field_unlock_date),
            value = badge.unlockDateLabel,
            sheetTheme = sheetTheme,
        )
    }
}

@Composable
private fun LegendaryRelicShowcaseTitleRow(
    title: LegendaryRelicShowcaseTitleUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (title.earned) 1f else 0.75f)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title.displayTitle,
            color = sheetTheme.primaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        ShowcaseDetailLine(
            label = stringResource(R.string.legendary_relic_showcase_field_rarity),
            value = title.rarityLabel,
            sheetTheme = sheetTheme,
        )
        ShowcaseDetailLine(
            label = stringResource(R.string.legendary_relic_showcase_field_unlock_date),
            value = title.unlockDateLabel,
            sheetTheme = sheetTheme,
        )
        title.activeLabel?.let { active ->
            ShowcaseDetailLine(
                label = stringResource(R.string.legendary_relic_showcase_field_active_title),
                value = active,
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun LegendaryRelicShowcaseRarityBadge(
    label: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFD60A).copy(alpha = 0.18f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = Color(0xFFFFD60A),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ShowcaseDetailLine(
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
            modifier = Modifier.weight(1f),
        )
    }
}
