package com.example.roadguideapp.auth

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R
import com.example.roadguideapp.map.AppleMapsSheetTheme
import com.example.roadguideapp.map.SearchEmptyState
import com.example.roadguideapp.map.SearchFilterChip
import com.example.roadguideapp.map.SearchFilterChipRow
import com.example.roadguideapp.map.SearchResultsCountLabel
import com.example.roadguideapp.map.SearchTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class RideSortOption {
    Departure,
    PriceLow,
    PriceHigh,
    Seats,
}

@Composable
internal fun CompanionBrowseRidesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sheetTheme = rememberAuthSheetTheme()
    val scope = rememberCoroutineScope()
    val selfProfileId = remember { OfflineAuthStore.profileId(context).orEmpty() }

    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var posts by remember { mutableStateOf<List<CompanionClient.DriverPost>>(emptyList()) }
    var pendingBookPost by remember { mutableStateOf<CompanionClient.DriverPost?>(null) }
    var bookingPostId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(RideSortOption.Departure) }

    suspend fun refresh() {
        val token = OfflineAuthStore.sessionToken(context)
        if (token.isNullOrBlank()) {
            loading = false
            errorMessage = context.getString(R.string.companion_error_not_signed_in)
            posts = emptyList()
            return
        }
        loading = true
        errorMessage = null
        when (val result = withContext(Dispatchers.IO) { CompanionClient.listDriverPosts(token) }) {
            is CompanionClient.ListDriverPostsResult.Success -> {
                posts = result.posts
                loading = false
            }
            is CompanionClient.ListDriverPostsResult.Failure -> {
                posts = emptyList()
                errorMessage = result.message
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    val filteredPosts = remember(posts, searchQuery, sortOption) {
        val query = searchQuery.trim().lowercase()
        val matched = if (query.isEmpty()) {
            posts
        } else {
            posts.filter { post ->
                post.driverName.lowercase().contains(query) ||
                    post.from.lowercase().contains(query) ||
                    post.to.lowercase().contains(query) ||
                    post.route.lowercase().contains(query) ||
                    post.vehicle.lowercase().contains(query) ||
                    post.preferences.lowercase().contains(query) ||
                    post.routeStops().joinToString(" ").lowercase().contains(query)
            }
        }
        when (sortOption) {
            RideSortOption.Departure -> matched
            RideSortOption.PriceLow -> matched.sortedBy { it.pricePerSeat }
            RideSortOption.PriceHigh -> matched.sortedByDescending { it.pricePerSeat }
            RideSortOption.Seats -> matched.sortedByDescending { it.availableSeats }
        }
    }

    pendingBookPost?.let { post ->
        AlertDialog(
            onDismissRequest = { pendingBookPost = null },
            title = { Text(stringResource(R.string.companion_book_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.companion_book_confirm_message,
                        post.driverName,
                        post.from,
                        post.to,
                        post.date,
                        post.time,
                        post.pricePerSeat,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val token = OfflineAuthStore.sessionToken(context) ?: return@TextButton
                        val postId = post.id
                        pendingBookPost = null
                        bookingPostId = postId
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                CompanionClient.bookDriverPost(
                                    bearerToken = token,
                                    postId = postId,
                                    seats = 1,
                                )
                            }
                            bookingPostId = null
                            when (result) {
                                is CompanionClient.BookDriverPostResult.Success -> {
                                    Toast.makeText(
                                        context,
                                        R.string.companion_book_success,
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    refresh()
                                }
                                is CompanionClient.BookDriverPostResult.Failure -> {
                                    val message = when {
                                        result.statusCode == 409 ||
                                            result.message.contains("already booked", ignoreCase = true) ->
                                            context.getString(R.string.companion_book_already)
                                        else -> result.message
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.companion_book_seat))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingBookPost = null }) {
                    Text(stringResource(R.string.companion_book_cancel))
                }
            },
        )
    }

    AuthPageScaffold(
        title = stringResource(R.string.companion_browse_title),
        subtitle = stringResource(R.string.companion_browse_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        SearchTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = stringResource(R.string.companion_search_rides_hint),
            sheetTheme = sheetTheme,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.companion_compare_hint),
            color = sheetTheme.tertiaryText,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 4.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        SearchFilterChipRow(sheetTheme = sheetTheme) {
            SearchFilterChip(
                label = stringResource(R.string.companion_sort_departure),
                sheetTheme = sheetTheme,
                selected = sortOption == RideSortOption.Departure,
                onClick = { sortOption = RideSortOption.Departure },
            )
            SearchFilterChip(
                label = stringResource(R.string.companion_sort_price_low),
                sheetTheme = sheetTheme,
                selected = sortOption == RideSortOption.PriceLow,
                onClick = { sortOption = RideSortOption.PriceLow },
            )
            SearchFilterChip(
                label = stringResource(R.string.companion_sort_price_high),
                sheetTheme = sheetTheme,
                selected = sortOption == RideSortOption.PriceHigh,
                onClick = { sortOption = RideSortOption.PriceHigh },
            )
            SearchFilterChip(
                label = stringResource(R.string.companion_sort_seats),
                sheetTheme = sheetTheme,
                selected = sortOption == RideSortOption.Seats,
                onClick = { sortOption = RideSortOption.Seats },
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!loading && errorMessage == null && posts.isNotEmpty()) {
                SearchResultsCountLabel(
                    count = filteredPosts.size,
                    sheetTheme = sheetTheme,
                    labelRes = R.string.companion_rides_count,
                )
            }
            TextButton(
                onClick = { scope.launch { refresh() } },
                enabled = !loading && bookingPostId == null,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = sheetTheme.accent,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.companion_refresh),
                    color = sheetTheme.accent,
                    fontSize = 14.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        when {
            loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = sheetTheme.accent,
                    )
                    Text(
                        text = stringResource(R.string.companion_loading_rides),
                        color = sheetTheme.secondaryText,
                        fontSize = 14.sp,
                    )
                }
            }
            errorMessage != null -> {
                AuthErrorText(message = errorMessage, sheetTheme = sheetTheme)
            }
            posts.isEmpty() -> {
                SearchEmptyState(
                    title = stringResource(R.string.companion_empty_rides),
                    sheetTheme = sheetTheme,
                )
            }
            filteredPosts.isEmpty() -> {
                SearchEmptyState(
                    title = stringResource(R.string.companion_no_matching_rides),
                    sheetTheme = sheetTheme,
                    subtitle = stringResource(R.string.companion_search_rides_hint),
                )
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredPosts.forEach { post ->
                        CompanionRideCompareCard(
                            post = post,
                            sheetTheme = sheetTheme,
                            selfProfileId = selfProfileId,
                            isBooking = bookingPostId == post.id,
                            bookingEnabled = bookingPostId == null,
                            onBook = { pendingBookPost = post },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanionRideCompareCard(
    post: CompanionClient.DriverPost,
    sheetTheme: AppleMapsSheetTheme,
    selfProfileId: String,
    isBooking: Boolean,
    bookingEnabled: Boolean,
    onBook: () -> Unit,
) {
    val isOwnPost = selfProfileId.isNotBlank() &&
        post.driverId.equals(selfProfileId, ignoreCase = true)
    val canBook = !isOwnPost && post.availableSeats > 0
    val initials = post.driverName
        .trim()
        .split(Regex("\\s+"))
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifBlank { "?" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = sheetTheme.cardElevated,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(sheetTheme.accent.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials,
                        color = sheetTheme.accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.driverName,
                        color = sheetTheme.primaryText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (post.vehicle.isNotBlank()) {
                        Text(
                            text = post.vehicle,
                            color = sheetTheme.secondaryText,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                PriceBadge(
                    price = post.pricePerSeat,
                    sheetTheme = sheetTheme,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            RouteCompareRow(
                stops = post.routeStops(),
                sheetTheme = sheetTheme,
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaChip(
                    icon = Icons.Outlined.Event,
                    label = "${post.date} · ${post.time}",
                    sheetTheme = sheetTheme,
                    modifier = Modifier.weight(1f),
                )
                MetaChip(
                    icon = Icons.Outlined.DirectionsCar,
                    label = stringResource(
                        R.string.companion_available_seats,
                        post.availableSeats,
                        post.seats,
                    ),
                    sheetTheme = sheetTheme,
                )
            }

            if (post.preferences.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.preferences,
                    color = sheetTheme.tertiaryText,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            when {
                isOwnPost -> {
                    Text(
                        text = stringResource(R.string.companion_book_own_ride),
                        color = sheetTheme.tertiaryText,
                        fontSize = 12.sp,
                    )
                }
                isBooking -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = sheetTheme.accent,
                        )
                        Text(
                            text = stringResource(R.string.companion_booking),
                            color = sheetTheme.secondaryText,
                            fontSize = 13.sp,
                        )
                    }
                }
                canBook -> {
                    AuthPrimaryButton(
                        text = stringResource(R.string.companion_book_seat),
                        onClick = onBook,
                        sheetTheme = sheetTheme,
                        enabled = bookingEnabled,
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.companion_no_seats),
                        color = sheetTheme.tertiaryText,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceBadge(
    price: Double,
    sheetTheme: AppleMapsSheetTheme,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(sheetTheme.accent.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = stringResource(R.string.companion_price_per_seat, price),
            color = sheetTheme.accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RouteCompareRow(
    stops: List<String>,
    sheetTheme: AppleMapsSheetTheme,
) {
    if (stops.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(sheetTheme.searchFieldFill)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.Route,
            contentDescription = null,
            tint = sheetTheme.accent,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            stops.forEachIndexed { index, stop ->
                if (index > 0) {
                    Text(
                        text = "↓",
                        color = sheetTheme.tertiaryText,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 1.dp),
                    )
                }
                Text(
                    text = stop,
                    color = sheetTheme.primaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(sheetTheme.searchFieldFill)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = sheetTheme.secondaryText,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = sheetTheme.primaryText,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
