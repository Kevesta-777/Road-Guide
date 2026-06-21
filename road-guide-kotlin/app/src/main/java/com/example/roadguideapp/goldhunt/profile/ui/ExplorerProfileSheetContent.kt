package com.example.roadguideapp.goldhunt.profile.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.roadguideapp.R
import androidx.compose.foundation.clickable
import com.example.roadguideapp.auth.AuthGroupedCard
import com.example.roadguideapp.auth.AuthNavDivider
import com.example.roadguideapp.auth.AuthPageTopBar
import com.example.roadguideapp.auth.AuthSectionLabel
import com.example.roadguideapp.goldhunt.clusters.ClusterType
import com.example.roadguideapp.goldhunt.radar.RadarTargetCategory
import com.example.roadguideapp.goldhunt.treasure.rarity.TreasureRarity
import com.example.roadguideapp.map.AppleMapsSheetGestures
import com.example.roadguideapp.map.AppleMapsSheetTheme

private val ExplorerProfileStickyHeaderMinHeight = 52.dp

@Composable
internal fun ExplorerProfileSheetContent(
    state: ExplorerProfileUiState,
    isLoading: Boolean,
    sheetTheme: AppleMapsSheetTheme,
    scrollState: ScrollState,
    contentScrollEnabled: Boolean,
    sheetGestures: AppleMapsSheetGestures,
    onClose: () -> Unit,
    onOpenTreasureEncyclopedia: (() -> Unit)? = null,
    onOpenClusterEncyclopedia: (() -> Unit)? = null,
    onOpenSecretPlaceCollectionBook: (() -> Unit)? = null,
    onOpenPanoramaHuntJournal: (() -> Unit)? = null,
    onOpenSeasonalEventCollectionBook: (() -> Unit)? = null,
    onOpenAchievementJournal: (() -> Unit)? = null,
    onOpenAchievementBadges: (() -> Unit)? = null,
    onOpenAchievementTitles: (() -> Unit)? = null,
    onOpenAchievementCollectionBook: (() -> Unit)? = null,
    onOpenLegendaryRelicJournal: (() -> Unit)? = null,
    onOpenLegendaryRelicShowcase: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var stickyHeaderHeightPx = remember { mutableIntStateOf(0) }
    val stickyHeaderHeight = with(density) {
        maxOf(stickyHeaderHeightPx.intValue.toDp(), ExplorerProfileStickyHeaderMinHeight)
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
                    ExplorerLevelCard(
                        state = state,
                        sheetTheme = sheetTheme,
                    )
                    ExplorerStatisticsSection(
                        state = state,
                        sheetTheme = sheetTheme,
                        formatDistance = {
                            ExplorerProfileFormatters.formatDistance(context, state.distanceExploredM)
                        },
                    )
                    ExplorerClusterCompletionSection(
                        state = state,
                        sheetTheme = sheetTheme,
                    )
                    ExplorerPanoramaHuntStatisticsSection(
                        state = state,
                        sheetTheme = sheetTheme,
                    )
                    ExplorerRadarStatisticsSection(
                        state = state,
                        sheetTheme = sheetTheme,
                    )
                    ExplorerSeasonalEventStatisticsSection(
                        state = state,
                        sheetTheme = sheetTheme,
                    )
                    ExplorerAchievementStatisticsSection(
                        state = state,
                        sheetTheme = sheetTheme,
                    )
                    ExplorerLegendaryRelicStatisticsSection(
                        state = state,
                        sheetTheme = sheetTheme,
                    )
                    ExplorerHiddenRelicDiscoverySection(
                        state = state,
                        sheetTheme = sheetTheme,
                    )
                    if (onOpenLegendaryRelicJournal != null) {
                        ExplorerLegendaryRelicJournalSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenLegendaryRelicJournal,
                        )
                    }
                    if (onOpenLegendaryRelicShowcase != null) {
                        ExplorerLegendaryRelicShowcaseSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenLegendaryRelicShowcase,
                        )
                    }
                    if (onOpenAchievementCollectionBook != null) {
                        ExplorerAchievementCollectionBookSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenAchievementCollectionBook,
                        )
                    }
                    if (onOpenAchievementBadges != null) {
                        ExplorerAchievementBadgeSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenAchievementBadges,
                        )
                    }
                    if (onOpenAchievementTitles != null) {
                        ExplorerAchievementTitleSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenAchievementTitles,
                        )
                    }
                    if (onOpenAchievementJournal != null) {
                        ExplorerAchievementJournalSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenAchievementJournal,
                        )
                    }
                    if (onOpenSeasonalEventCollectionBook != null) {
                        ExplorerSeasonalEventCollectionBookSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenSeasonalEventCollectionBook,
                        )
                    }
                    if (onOpenPanoramaHuntJournal != null) {
                        ExplorerPanoramaHuntJournalSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenPanoramaHuntJournal,
                        )
                    }
                    ExplorerSecretPlaceCategoryStatisticsSection(
                        state = state,
                        sheetTheme = sheetTheme,
                    )
                    if (onOpenSecretPlaceCollectionBook != null) {
                        ExplorerSecretPlaceCollectionBookSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenSecretPlaceCollectionBook,
                        )
                    }
                    ExplorerTreasureRaritySection(
                        state = state,
                        sheetTheme = sheetTheme,
                    )
                    if (onOpenClusterEncyclopedia != null) {
                        ExplorerClusterEncyclopediaSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenClusterEncyclopedia,
                        )
                    }
                    if (onOpenTreasureEncyclopedia != null) {
                        ExplorerTreasureEncyclopediaSection(
                            state = state,
                            sheetTheme = sheetTheme,
                            onOpen = onOpenTreasureEncyclopedia,
                        )
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
                    title = stringResource(R.string.explorer_profile_title),
                    sheetTheme = sheetTheme,
                    onClose = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ExplorerLevelCard(
    state: ExplorerProfileUiState,
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
                text = stringResource(R.string.explorer_profile_level_label),
                color = sheetTheme.onAccent.copy(alpha = 0.82f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = state.level.toString(),
                    color = sheetTheme.onAccent,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alignByBaseline(),
                )
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.explorer_profile_rank_label),
                        color = sheetTheme.onAccent.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = state.rankTitle,
                        color = goldAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.explorer_profile_xp_progress_label),
                color = sheetTheme.onAccent.copy(alpha = 0.82f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = goldAccent,
                trackColor = sheetTheme.onAccent.copy(alpha = 0.22f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = ExplorerProfileFormatters.formatXp(state.lifetimeXp) +
                        " " + stringResource(R.string.explorer_profile_xp_total_suffix),
                    color = sheetTheme.onAccent.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                )
                Text(
                    text = if (state.xpToNextLevel > 0L) {
                        stringResource(
                            R.string.explorer_profile_xp_to_next,
                            ExplorerProfileFormatters.formatXp(state.xpToNextLevel),
                        )
                    } else {
                        stringResource(R.string.explorer_profile_xp_max_level)
                    },
                    color = sheetTheme.onAccent.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun ExplorerStatisticsSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    formatDistance: () -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.explorer_profile_statistics),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_credits),
                value = ExplorerProfileFormatters.formatCredits(state.totalCredits),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_treasures),
                value = ExplorerProfileFormatters.formatCount(state.treasuresCollected),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_secret_places),
                value = ExplorerProfileFormatters.formatCount(state.secretPlacesFound),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_distance),
                value = formatDistance(),
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun ExplorerSecretPlaceCollectionBookSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tealAccent = Color(0xFF2DD4BF)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.secret_place_collection_book_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = state.collectionBookProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.collectionBookProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = tealAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.secret_place_collection_book_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerClusterEncyclopediaSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val greenAccent = Color(0xFF22C55E)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.cluster_encyclopedia_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = state.clusterEncyclopediaProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.clusterEncyclopediaProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = greenAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.cluster_encyclopedia_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerTreasureEncyclopediaSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val goldAccent = Color(0xFFFFD60A)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.treasure_encyclopedia_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = state.encyclopediaProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.encyclopediaProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = goldAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.treasure_encyclopedia_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerSecretPlaceCategoryStatisticsSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val stats = state.secretPlaceCategoryStatistics
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.explorer_profile_secret_place_category_statistics),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_secret_places_discovered),
                value = ExplorerProfileFormatters.formatCount(stats.discoveredCount),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_secret_places_completed),
                value = ExplorerProfileFormatters.formatCount(stats.completedCount),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_secret_place_credits_earned),
                value = ExplorerProfileFormatters.formatCredits(stats.creditsEarned),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_secret_place_xp_earned),
                value = ExplorerProfileFormatters.formatXp(stats.xpEarned),
                sheetTheme = sheetTheme,
            )
            if (stats.bestCategory != null) {
                AuthNavDivider(sheetTheme = sheetTheme)
                ExplorerStatRow(
                    label = stringResource(R.string.explorer_profile_secret_place_best_category),
                    value = secretPlaceCategoryLabel(stats.bestCategory),
                    sheetTheme = sheetTheme,
                )
            }
            if (stats.rarestCategory != null) {
                AuthNavDivider(sheetTheme = sheetTheme)
                ExplorerStatRow(
                    label = stringResource(R.string.explorer_profile_secret_place_rarest_category),
                    value = secretPlaceCategoryLabel(stats.rarestCategory),
                    sheetTheme = sheetTheme,
                )
            }
            val entries = stats.completedEntriesOrdered().filter { it.second > 0 }
            entries.forEachIndexed { index, (category, count) ->
                AuthNavDivider(sheetTheme = sheetTheme)
                ExplorerStatRow(
                    label = secretPlaceCategoryLabel(category),
                    value = ExplorerProfileFormatters.formatCount(count),
                    sheetTheme = sheetTheme,
                )
            }
        }
    }
}

@Composable
private fun secretPlaceCategoryLabel(category: com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory): String =
    when (category) {
        com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory.NATURE_SANCTUARY ->
            stringResource(R.string.secret_place_category_nature_sanctuary)
        com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory.SCENIC_VIEWPOINT ->
            stringResource(R.string.secret_place_category_scenic_viewpoint)
        com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory.HISTORIC_LANDMARK ->
            stringResource(R.string.secret_place_category_historic_landmark)
        com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory.MYSTERY_ZONE ->
            stringResource(R.string.secret_place_category_mystery_zone)
        com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory.ANCIENT_RELIC_SITE ->
            stringResource(R.string.secret_place_category_ancient_relic_site)
        com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory.EXPLORER_HIDEOUT ->
            stringResource(R.string.secret_place_category_explorer_hideout)
        com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory.MYTHICAL_PLACE ->
            stringResource(R.string.secret_place_category_mythical_place)
        com.example.roadguideapp.goldhunt.secretplaces.categories.SecretPlaceCategory.LEGENDARY_SITE ->
            stringResource(R.string.secret_place_category_legendary_site)
    }

@Composable
private fun ExplorerPanoramaHuntStatisticsSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val stats = state.panoramaHuntStatistics
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.explorer_profile_panorama_hunt_statistics),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_panorama_hunts_total),
                value = ExplorerProfileFormatters.formatCount(stats.totalHunts),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_panorama_hunts_completed),
                value = ExplorerProfileFormatters.formatCount(stats.huntsCompleted),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_panorama_hidden_symbols_found),
                value = ExplorerProfileFormatters.formatCount(stats.hiddenSymbolsFound),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_panorama_objects_found),
                value = ExplorerProfileFormatters.formatCount(stats.objectsFound),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_panorama_codes_solved),
                value = ExplorerProfileFormatters.formatCount(stats.codesSolved),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_panorama_rewards_earned),
                value = ExplorerProfileFormatters.formatCredits(stats.rewardsEarned),
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun ExplorerSeasonalEventCollectionBookSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val amberAccent = Color(0xFFFF9F0A)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.seasonal_event_collection_book_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = state.seasonalEventCollectionBookProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.seasonalEventCollectionBookProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = amberAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.seasonal_event_collection_book_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerPanoramaHuntJournalSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val amberAccent = Color(0xFFFFB020)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.panorama_hunt_journal_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = state.panoramaHuntJournalDiscoveredProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.panoramaHuntJournalProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.panoramaHuntJournalMissingProgressLabel,
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.panoramaHuntJournalProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = amberAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.panorama_hunt_journal_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerAchievementCollectionBookSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val goldAccent = Color(0xFFFFD60A)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.achievement_collection_book_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = state.achievementCollectionBookProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.achievement_collection_book_completion_percentage,
                        state.achievementCollectionBookCompletionPercentageLabel,
                    ),
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.achievementCollectionBookRareLegendaryLabel,
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.achievementCollectionBookProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = goldAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.achievement_collection_book_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerAchievementTitleSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val goldAccent = Color(0xFFFFD60A)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.achievement_title_collection_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                state.activeAchievementTitleLabel?.let { activeLabel ->
                    Text(
                        text = activeLabel,
                        color = sheetTheme.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = state.achievementTitleProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.achievement_title_completion_percentage,
                        state.achievementTitleCompletionPercentageLabel,
                    ),
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.achievementTitleProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = goldAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.achievement_title_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerAchievementBadgeSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val goldAccent = Color(0xFFFFD60A)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.achievement_badge_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = state.achievementBadgeProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.achievement_badge_completion_percentage,
                        state.achievementBadgeCompletionPercentageLabel,
                    ),
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.achievementBadgeProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = goldAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.achievement_badge_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerAchievementJournalSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val goldAccent = Color(0xFFFFD60A)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.achievement_journal_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = state.achievementJournalProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        R.string.achievement_journal_completion_percentage,
                        state.achievementJournalCompletionPercentageLabel,
                    ),
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.achievementJournalProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = goldAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.achievement_journal_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerLegendaryRelicStatisticsSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val stats = state.legendaryRelicStatistics
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.explorer_profile_legendary_relic_statistics),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_legendary_relics_completed),
                value = state.legendaryRelicProgressLabel,
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_legendary_relics_completion_percentage),
                value = state.legendaryRelicCompletionPercentageLabel,
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_legendary_relics_pieces_collected),
                value = state.legendaryRelicPieceProgressLabel,
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_legendary_relics_piece_completion_percentage),
                value = state.legendaryRelicPieceCompletionPercentageLabel,
                sheetTheme = sheetTheme,
            )
            if (stats.hiddenRelicsCatalogCount > 0) {
                AuthNavDivider(sheetTheme = sheetTheme)
                ExplorerStatRow(
                    label = stringResource(R.string.explorer_profile_legendary_relics_hidden_discovered),
                    value = state.legendaryRelicHiddenDiscoveredLabel,
                    sheetTheme = sheetTheme,
                )
                AuthNavDivider(sheetTheme = sheetTheme)
                ExplorerStatRow(
                    label = stringResource(R.string.explorer_profile_legendary_relics_hidden_discovery_percentage),
                    value = state.legendaryRelicHiddenDiscoveryPercentageLabel,
                    sheetTheme = sheetTheme,
                )
            }
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_legendary_relics_epic_completed),
                value = ExplorerProfileFormatters.formatCount(stats.epicCompletedCount),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_legendary_relics_legendary_completed),
                value = ExplorerProfileFormatters.formatCount(stats.legendaryCompletedCount),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_legendary_relics_credits_earned),
                value = ExplorerProfileFormatters.formatCredits(stats.creditsEarned),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_legendary_relics_xp_earned),
                value = ExplorerProfileFormatters.formatXp(stats.xpEarned),
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun ExplorerLegendaryRelicShowcaseSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val relicAccent = Color(0xFFFFD60A)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.legendary_relic_showcase_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = state.legendaryRelicShowcaseCompletedProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.legendaryRelicShowcaseBadgesEarnedLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.legendaryRelicShowcaseTitlesEarnedLabel,
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.legendaryRelicShowcaseProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = relicAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.legendary_relic_showcase_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerLegendaryRelicJournalSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val relicAccent = Color(0xFFFFD60A)
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.legendary_relic_journal_title),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = state.legendaryRelicJournalCompletedProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.legendaryRelicJournalPiecesFoundProgressLabel,
                    color = sheetTheme.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.legendaryRelicJournalPiecesMissingProgressLabel,
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.legendaryRelicJournalProgressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = relicAccent,
                    trackColor = sheetTheme.secondaryText.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.legendary_relic_journal_open),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ExplorerHiddenRelicDiscoverySection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val stats = state.hiddenRelicDiscoveryStatistics
    if (stats.hiddenCatalogCount <= 0) return
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.explorer_profile_hidden_relic_discovery),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_hidden_relics_revealed),
                value = state.hiddenRelicRevealProgressLabel,
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_hidden_relics_reveal_percentage),
                value = state.hiddenRelicRevealPercentageLabel,
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_hidden_relics_undiscovered_label),
                value = state.hiddenRelicUndiscoveredLabel,
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun ExplorerAchievementStatisticsSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val stats = state.achievementStatistics
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.explorer_profile_achievement_statistics),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_achievements_completed),
                value = ExplorerProfileFormatters.formatCompletionProgress(
                    stats.completedCount,
                    stats.totalCount,
                ),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_achievements_completion_percentage),
                value = ExplorerProfileFormatters.formatCompletionPercentage(stats.completionPercentage),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_achievements_rare_completed),
                value = ExplorerProfileFormatters.formatCount(stats.rareCompletedCount),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_achievements_legendary_completed),
                value = ExplorerProfileFormatters.formatCount(stats.legendaryCompletedCount),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_achievements_credits_earned),
                value = ExplorerProfileFormatters.formatCredits(stats.creditsEarned),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_achievements_xp_earned),
                value = ExplorerProfileFormatters.formatXp(stats.xpEarned),
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun ExplorerSeasonalEventStatisticsSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val stats = state.seasonalEventStatistics
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.explorer_profile_seasonal_event_statistics),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_seasonal_events_joined),
                value = ExplorerProfileFormatters.formatCount(stats.eventsJoined),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_seasonal_events_completed),
                value = ExplorerProfileFormatters.formatCount(stats.eventsCompleted),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_seasonal_rewards_earned),
                value = ExplorerProfileFormatters.formatCount(stats.rewardsEarned),
                sheetTheme = sheetTheme,
            )
            if (stats.bestEventType != null) {
                AuthNavDivider(sheetTheme = sheetTheme)
                ExplorerStatRow(
                    label = stringResource(R.string.explorer_profile_seasonal_best_event),
                    value = stats.bestEventType.displayName,
                    sheetTheme = sheetTheme,
                )
            }
            if (stats.favoriteEventType != null) {
                AuthNavDivider(sheetTheme = sheetTheme)
                ExplorerStatRow(
                    label = stringResource(R.string.explorer_profile_seasonal_favorite_event),
                    value = stats.favoriteEventType.displayName,
                    sheetTheme = sheetTheme,
                )
            }
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_seasonal_treasures_collected),
                value = ExplorerProfileFormatters.formatCount(stats.treasuresCollected),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_seasonal_xp_earned),
                value = ExplorerProfileFormatters.formatXp(stats.xpEarned),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_seasonal_credits_earned),
                value = ExplorerProfileFormatters.formatCount(stats.creditsEarned),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_seasonal_fragments_earned),
                value = ExplorerProfileFormatters.formatCount(stats.fragmentsEarned),
                sheetTheme = sheetTheme,
            )
        }
    }
}

@Composable
private fun ExplorerRadarStatisticsSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val stats = state.radarStatistics
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.explorer_profile_radar_statistics),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_radar_total_scans),
                value = ExplorerProfileFormatters.formatCount(stats.totalScans),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_radar_successful_scans),
                value = ExplorerProfileFormatters.formatCount(stats.successfulScans),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            val entries = stats.detectionEntriesOrdered()
            entries.forEachIndexed { index, (category, count) ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                ExplorerStatRow(
                    label = radarTargetCategoryLabel(category),
                    value = ExplorerProfileFormatters.formatCount(count),
                    sheetTheme = sheetTheme,
                )
            }
        }
    }
}

@Composable
private fun radarTargetCategoryLabel(category: RadarTargetCategory): String = when (category) {
    RadarTargetCategory.TREASURE ->
        stringResource(R.string.explorer_profile_radar_treasures_found)
    RadarTargetCategory.CLUSTER ->
        stringResource(R.string.explorer_profile_radar_clusters_found)
    RadarTargetCategory.SECRET_PLACE ->
        stringResource(R.string.explorer_profile_radar_secret_places_found)
    RadarTargetCategory.STORY_FRAGMENT ->
        stringResource(R.string.explorer_profile_radar_story_discoveries)
    RadarTargetCategory.LEGENDARY_RELIC ->
        stringResource(R.string.explorer_profile_radar_legendary_discoveries)
}

@Composable
private fun ExplorerClusterCompletionSection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    val stats = state.clusterStatistics
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.explorer_profile_cluster_statistics),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_clusters_found),
                value = ExplorerProfileFormatters.formatCount(stats.clustersFound),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_clusters_completed),
                value = ExplorerProfileFormatters.formatCount(stats.clustersCompleted),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            ExplorerStatRow(
                label = stringResource(R.string.explorer_profile_cluster_rewards_earned),
                value = ExplorerProfileFormatters.formatCredits(stats.totalRewardsEarned),
                sheetTheme = sheetTheme,
            )
            AuthNavDivider(sheetTheme = sheetTheme)
            val entries = stats.completedEntriesOrdered()
            entries.forEachIndexed { index, (type, count) ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                ExplorerStatRow(
                    label = clusterTypeLabel(type),
                    value = ExplorerProfileFormatters.formatCount(count),
                    sheetTheme = sheetTheme,
                )
            }
        }
    }
}

@Composable
private fun clusterTypeLabel(type: ClusterType): String = when (type) {
    ClusterType.ROADSIDE_CACHE -> stringResource(R.string.cluster_type_roadside_cache)
    ClusterType.EXPLORER_NEST -> stringResource(R.string.cluster_type_explorer_nest)
    ClusterType.TREASURE_GARDEN -> stringResource(R.string.cluster_type_treasure_garden)
    ClusterType.ANCIENT_VAULT -> stringResource(R.string.cluster_type_ancient_vault)
    ClusterType.LEGENDARY_HOARD -> stringResource(R.string.cluster_type_legendary_hoard)
}

@Composable
private fun ExplorerTreasureRaritySection(
    state: ExplorerProfileUiState,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AuthSectionLabel(
            text = stringResource(R.string.explorer_profile_treasure_rarity),
            sheetTheme = sheetTheme,
        )
        AuthGroupedCard(sheetTheme = sheetTheme) {
            val entries = state.treasureRarityStats.entriesOrdered()
            entries.forEachIndexed { index, (rarity, count) ->
                if (index > 0) {
                    AuthNavDivider(sheetTheme = sheetTheme)
                }
                ExplorerStatRow(
                    label = rarityLabel(rarity),
                    value = ExplorerProfileFormatters.formatCount(count),
                    sheetTheme = sheetTheme,
                )
            }
        }
    }
}

@Composable
private fun rarityLabel(rarity: TreasureRarity): String = when (rarity) {
    TreasureRarity.COMMON -> stringResource(R.string.explorer_profile_rarity_common)
    TreasureRarity.UNCOMMON -> stringResource(R.string.explorer_profile_rarity_uncommon)
    TreasureRarity.RARE -> stringResource(R.string.explorer_profile_rarity_rare)
    TreasureRarity.EPIC -> stringResource(R.string.explorer_profile_rarity_epic)
    TreasureRarity.LEGENDARY -> stringResource(R.string.explorer_profile_rarity_legendary)
    TreasureRarity.MYTHIC -> stringResource(R.string.explorer_profile_rarity_mythic)
}

@Composable
private fun ExplorerStatRow(
    label: String,
    value: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = sheetTheme.primaryText,
            fontSize = 16.sp,
        )
        Text(
            text = value,
            color = sheetTheme.secondaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
