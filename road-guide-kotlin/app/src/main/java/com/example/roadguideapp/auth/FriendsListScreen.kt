package com.example.roadguideapp.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadguideapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun FriendsListScreen(
    friends: List<OfflineFriend>,
    onBack: () -> Unit,
    onFriendRemoved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sheetTheme = rememberAuthSheetTheme()
    val scope = rememberCoroutineScope()

    var pendingRemove by remember { mutableStateOf<OfflineFriend?>(null) }
    var removingId by remember { mutableStateOf<String?>(null) }

    pendingRemove?.let { friend ->
        val displayName = friend.displayName ?: stringResource(R.string.friends_unknown_name)
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            title = { Text(stringResource(R.string.friends_remove_confirm_title)) },
            text = {
                Text(stringResource(R.string.friends_remove_confirm_message, displayName))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val profileId = friend.profileId
                        pendingRemove = null
                        removingId = profileId
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                OfflineFriendsStore.removeFriend(context, profileId)
                            }
                            removingId = null
                            when (result) {
                                RemoveFriendResult.Success -> {
                                    Toast.makeText(context, R.string.friends_removed, Toast.LENGTH_SHORT).show()
                                    onFriendRemoved()
                                }
                                is RemoveFriendResult.Failure -> {
                                    Toast.makeText(
                                        context,
                                        friendErrorMessage(context, result.error),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.friends_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemove = null }) {
                    Text(stringResource(R.string.friends_add_cancel))
                }
            },
        )
    }

    AuthPageScaffold(
        title = stringResource(R.string.friends_list_title),
        subtitle = stringResource(R.string.friends_list_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        if (friends.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.friends_empty),
                    color = sheetTheme.secondaryText,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                friends.forEach { friend ->
                    val displayName = friend.displayName ?: stringResource(R.string.friends_unknown_name)
                    val isRemoving = removingId == friend.profileId
                    AuthFriendRow(
                        displayName = displayName,
                        profileId = friend.profileId,
                        sheetTheme = sheetTheme,
                        removing = isRemoving,
                        onRemove = {
                            if (!isRemoving) pendingRemove = friend
                        },
                    )
                }
                if (removingId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}
