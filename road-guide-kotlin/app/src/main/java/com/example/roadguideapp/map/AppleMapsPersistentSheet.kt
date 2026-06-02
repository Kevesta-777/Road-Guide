package com.example.roadguideapp.map

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.roadguideapp.R

@Composable
internal fun AppleMapsPersistentSheetContent(
    sheetTheme: AppleMapsSheetTheme,
    scrollState: ScrollState,
    contentScrollEnabled: Boolean,
    sheetGestures: AppleMapsSheetGestures,
    profileAbbreviation: String? = null,
    isLoggedIn: Boolean = false,
    onProfileClick: () -> Unit = {},
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearchFocus: () -> Unit = {},
    onSearchCancel: () -> Unit = {},
    onSearchSubmit: (String) -> Unit = {},
    onSearchClear: () -> Unit = {},
    searchSuggestions: List<PeliasSearchResult> = emptyList(),
    searchLoading: Boolean = false,
    searchError: String? = null,
    activeNearbyCategory: AppleNearbyShortcut? = null,
    nearbyBrowseResults: List<PeliasSearchResult> = emptyList(),
    nearbyBrowseLoading: Boolean = false,
    nearbyBrowseError: String? = null,
    nearbyFilterState: NearbyResultsFilter.State = NearbyResultsFilter.State(),
    nearbyAvailableChains: List<String> = emptyList(),
    nearbyPickHoursByGid: Map<String, String> = emptyMap(),
    nearbyScopeOptions: List<NearbyScopeOption> = emptyList(),
    nearbySearchContext: NearbySearchContext = NearbySearchContext.MapCenter,
    onNearbySearchContextChange: (NearbySearchContext) -> Unit = {},
    onNearbyFilterChange: (NearbyResultsFilter.State) -> Unit = {},
    onSearchResultSelected: (PeliasSearchResult) -> Unit = {},
    onNearbyBrowseDone: () -> Unit = {},
    onNearbyPlaceSelected: (PeliasSearchResult) -> Unit = {},
    onNearbyShortcutClick: (AppleNearbyShortcut) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var stickyHeaderHeightPx by remember { mutableIntStateOf(0) }
    val stickyHeaderHeight = with(density) { stickyHeaderHeightPx.toDp() }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(sheetTheme.sheetSurface),
    ) {
        if (isSearchActive) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        sheetGestures.scrollContent(
                            scrollState = scrollState,
                            scrollEnabled = true,
                        ),
                    )
                    .padding(top = stickyHeaderHeight)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                PeliasSearchResultsList(
                    sheetTheme = sheetTheme,
                    suggestions = searchSuggestions,
                    loading = searchLoading,
                    errorMessage = searchError,
                    query = searchQuery,
                    onResultSelected = onSearchResultSelected,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else if (activeNearbyCategory != null) {
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
                    .padding(bottom = 12.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                NearbyCategoryResultsContent(
                    sheetTheme = sheetTheme,
                    categoryLabel = stringResource(activeNearbyCategory.labelRes),
                    results = nearbyBrowseResults,
                    loading = nearbyBrowseLoading,
                    errorMessage = nearbyBrowseError,
                    filterState = nearbyFilterState,
                    availableChains = nearbyAvailableChains,
                    pickHoursByGid = nearbyPickHoursByGid,
                    scopeOptions = nearbyScopeOptions,
                    selectedSearchContext = nearbySearchContext,
                    onScopeSelected = onNearbySearchContextChange,
                    onFilterChange = onNearbyFilterChange,
                    onResultSelected = onNearbyPlaceSelected,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            HomeSheetBrowseContent(
                sheetTheme = sheetTheme,
                scrollState = scrollState,
                contentScrollEnabled = contentScrollEnabled,
                stickyHeaderHeight = stickyHeaderHeight,
                onNearbyShortcutClick = onNearbyShortcutClick,
                sheetGestures = sheetGestures,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(1f)
                .background(sheetTheme.stickyHeaderSurface)
                .then(sheetGestures.chromeDrag)
                .onSizeChanged { stickyHeaderHeightPx = it.height },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(sheetGestures.grabberDrag),
            ) {
                AppleMapsSheetGrabber(grabberColor = sheetTheme.grabber)
            }
            if (activeNearbyCategory != null) {
                NearbyCategorySearchHeader(
                    sheetTheme = sheetTheme,
                    categoryLabel = stringResource(activeNearbyCategory.labelRes),
                    onClose = onNearbyBrowseDone,
                )
            } else {
                AppleMapsSheetSearchHeader(
                    sheetTheme = sheetTheme,
                    searchQuery = searchQuery,
                    isSearchActive = isSearchActive,
                    searchFocusRequester = searchFocusRequester,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearchFocus = onSearchFocus,
                    onSearchSubmit = onSearchSubmit,
                    onSearchClear = onSearchClear,
                    onSearchCancel = onSearchCancel,
                    profileAbbreviation = profileAbbreviation,
                    isLoggedIn = isLoggedIn,
                    onProfileClick = onProfileClick,
                )
            }
        }
    }
}

@Composable
private fun HomeSheetBrowseContent(
    sheetTheme: AppleMapsSheetTheme,
    scrollState: ScrollState,
    contentScrollEnabled: Boolean,
    stickyHeaderHeight: androidx.compose.ui.unit.Dp,
    onNearbyShortcutClick: (AppleNearbyShortcut) -> Unit,
    sheetGestures: AppleMapsSheetGestures,
) {
    val nearby = NearbyCategorySearch.shortcuts

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
            .padding(bottom = 12.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.apple_find_nearby),
            color = sheetTheme.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        nearby.chunked(2).forEach { rowShortcuts ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowShortcuts.forEach { item ->
                    AppleNearbyCell(
                        item = item,
                        sheetTheme = sheetTheme,
                        onClick = { onNearbyShortcutClick(item) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowShortcuts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.apple_guides_we_love),
            color = sheetTheme.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        val guideCards = listOf(0, 1, 2)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(guideCards, key = { it }) { idx ->
                AppleGuideCard(index = idx)
            }
        }
    }
}

@Composable
private fun AppleGuideCard(index: Int) {
    val gradients = remember {
        listOf(
            listOf(Color(0xFFFF6B35), Color(0xFFFF8E53)),
            listOf(Color(0xFF4A90D9), Color(0xFF7FB3E8)),
            listOf(Color(0xFF7B4397), Color(0xFFDC2430)),
        )
    }
    val titles = listOf(
        R.string.apple_guide_card_1 to R.string.apple_guide_pub_1,
        R.string.apple_guide_card_2 to R.string.apple_guide_pub_2,
        R.string.apple_guide_card_3 to R.string.apple_guide_pub_3,
    )
    val (titleRes, pubRes) = titles[index % titles.size]
    Column(
        modifier = Modifier
            .width(160.dp)
            .aspectRatio(0.58f)
            .appleMapsSheetInteractiveBlock()
            .background(
                brush = Brush.linearGradient(gradients[index % gradients.size]),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(pubRes),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(titleRes),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Single search header so the [BasicTextField] is not recreated when search mode activates
 * (avoids losing keyboard/cursor focus on the first typed character).
 */
@Composable
private fun AppleMapsSheetSearchHeader(
    sheetTheme: AppleMapsSheetTheme,
    searchQuery: String,
    isSearchActive: Boolean,
    searchFocusRequester: FocusRequester,
    onSearchQueryChange: (String) -> Unit,
    onSearchFocus: () -> Unit,
    onSearchSubmit: (String) -> Unit,
    onSearchClear: () -> Unit,
    onSearchCancel: () -> Unit,
    profileAbbreviation: String? = null,
    isLoggedIn: Boolean = false,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = if (isSearchActive) 0.dp else 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppleMapsSearchField(
            sheetTheme = sheetTheme,
            searchQuery = searchQuery,
            focusRequester = searchFocusRequester,
            onSearchQueryChange = onSearchQueryChange,
            onSearchFocus = onSearchFocus,
            onSearchSubmit = onSearchSubmit,
            onSearchClear = onSearchClear,
            modifier = Modifier.weight(1f),
        )
        if (isSearchActive) {
            TextButton(onClick = onSearchCancel) {
                Text(
                    text = stringResource(R.string.apple_search_cancel),
                    color = sheetTheme.accent,
                    fontSize = 17.sp,
                )
            }
        } else {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .appleMapsSheetInteractiveBlock()
                    .clickable(
                        onClick = onProfileClick,
                        onClickLabel = stringResource(
                            if (isLoggedIn) R.string.auth_profile_title else R.string.auth_sign_in,
                        ),
                    )
                    .background(sheetTheme.profileAvatarBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoggedIn && !profileAbbreviation.isNullOrEmpty()) {
                    Text(
                        text = profileAbbreviation,
                        color = sheetTheme.profileAvatarText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = stringResource(R.string.auth_sign_in),
                        tint = sheetTheme.profileAvatarText,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AppleMapsSearchField(
    sheetTheme: AppleMapsSheetTheme,
    searchQuery: String,
    focusRequester: FocusRequester,
    onSearchQueryChange: (String) -> Unit,
    onSearchFocus: () -> Unit,
    onSearchSubmit: (String) -> Unit,
    onSearchClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .background(sheetTheme.searchFieldFill, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = sheetTheme.searchFieldHint,
            modifier = Modifier
                .size(22.dp)
                .appleMapsSheetInteractiveBlock(),
        )
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        onSearchFocus()
                    }
                },
            textStyle = TextStyle(
                color = sheetTheme.searchFieldText,
                fontSize = 17.sp,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchSubmit(searchQuery) }),
            cursorBrush = SolidColor(sheetTheme.searchFieldText),
            decorationBox = { innerTextField ->
                Box(Modifier.fillMaxWidth()) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.apple_search_placeholder),
                            color = sheetTheme.searchFieldHint,
                            fontSize = 17.sp,
                        )
                    }
                    innerTextField()
                }
            },
        )
        if (searchQuery.isNotEmpty()) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.apple_close),
                tint = sheetTheme.searchFieldHint,
                modifier = Modifier
                    .size(22.dp)
                    .appleMapsSheetInteractiveBlock()
                    .clickable(onClick = onSearchClear),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = stringResource(R.string.apple_voice_search),
                tint = sheetTheme.searchFieldHint,
                modifier = Modifier
                    .size(22.dp)
                    .appleMapsSheetInteractiveBlock(),
            )
        }
    }
}

@Composable
private fun AppleNearbyCell(
    item: AppleNearbyShortcut,
    sheetTheme: AppleMapsSheetTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick)
            .background(sheetTheme.nearbyCellBackground, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(item.tint, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (item.labelRes == R.string.apple_cat_parking) {
                Text(
                    text = "P",
                    color = sheetTheme.nearbyCellText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            } else {
                Text(text = item.emoji, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(item.labelRes),
            color = sheetTheme.nearbyCellText,
            fontSize = 16.sp,
        )
    }
}
