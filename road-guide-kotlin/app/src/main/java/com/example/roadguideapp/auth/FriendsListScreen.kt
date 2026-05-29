package com.example.roadguideapp.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R
import com.example.roadguideapp.map.AppleMapsSheetTheme

@Composable
internal fun FriendsListScreen(
    friends: List<OfflineFriend>,
    onBack: () -> Unit,
    onFriendRemoved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sheetTheme = rememberAuthSheetTheme()

    AuthPageScaffold(
        title = stringResource(R.string.friends_list_title),
        subtitle = stringResource(R.string.friends_list_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        if (friends.isEmpty()) {
            Text(
                text = stringResource(R.string.friends_empty),
                color = sheetTheme.secondaryText,
                fontSize = 15.sp,
            )
        } else {
            // Use Column (not LazyColumn): AuthPageScaffold already scrolls vertically.
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                friends.forEach { friend ->
                    FriendRow(
                        friend = friend,
                        sheetTheme = sheetTheme,
                        onRemove = {
                            OfflineFriendsStore.removeFriend(context, friend.profileId)
                            onFriendRemoved()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendRow(
    friend: OfflineFriend,
    sheetTheme: AppleMapsSheetTheme,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.displayName ?: stringResource(R.string.friends_unknown_name),
                color = sheetTheme.primaryText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = friend.profileId,
                color = sheetTheme.secondaryText,
                fontSize = 13.sp,
            )
        }
        TextButton(onClick = onRemove) {
            Text(
                text = stringResource(R.string.friends_remove),
                color = sheetTheme.accent,
            )
        }
    }
}
