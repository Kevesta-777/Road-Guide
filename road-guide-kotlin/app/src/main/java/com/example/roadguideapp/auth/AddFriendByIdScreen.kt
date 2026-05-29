package com.example.roadguideapp.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R

@Composable
internal fun AddFriendByIdScreen(
    onBack: () -> Unit,
    onFriendAdded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val sheetTheme = rememberAuthSheetTheme()

    var profileIdInput by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingPayload by remember { mutableStateOf<FriendQrPayloadData?>(null) }

    fun submitInput() {
        focusManager.clearFocus()
        val normalized = profileIdInput.trim()
        if (normalized.isEmpty()) {
            errorMessage = friendErrorMessage(context, FriendError.EmptyProfileId)
            return
        }
        if (!OfflineFriendsStore.isValidProfileId(normalized)) {
            errorMessage = friendErrorMessage(context, FriendError.InvalidProfileId)
            return
        }
        errorMessage = null
        pendingPayload = FriendQrPayloadData(profileId = normalized, displayName = null)
    }

    pendingPayload?.let { payload ->
        val displayLabel = payload.displayName
            ?: stringResource(R.string.friends_unknown_name)
        AlertDialog(
            onDismissRequest = { pendingPayload = null },
            title = { Text(stringResource(R.string.friends_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.friends_confirm_message,
                        displayLabel,
                        payload.profileId,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (val result = OfflineFriendsStore.addFriend(context, payload.profileId, payload.displayName)) {
                            AddFriendResult.Success -> {
                                Toast.makeText(context, R.string.friends_added, Toast.LENGTH_SHORT).show()
                                pendingPayload = null
                                profileIdInput = ""
                                runOnMainThread(context) { onFriendAdded() }
                            }
                            is AddFriendResult.Failure -> {
                                errorMessage = friendErrorMessage(context, result.error)
                                pendingPayload = null
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.friends_add_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPayload = null }) {
                    Text(stringResource(R.string.friends_add_cancel))
                }
            },
        )
    }

    AuthPageScaffold(
        title = stringResource(R.string.friends_add_by_id_title),
        subtitle = stringResource(R.string.friends_add_by_id_subtitle),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        AuthField(
            label = stringResource(R.string.auth_profile_id_label),
            value = profileIdInput,
            onValueChange = {
                profileIdInput = it
                errorMessage = null
            },
            placeholder = stringResource(R.string.friends_profile_id_hint),
            sheetTheme = sheetTheme,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submitInput() }),
        )
        AuthErrorText(errorMessage, sheetTheme)
        Spacer(modifier = Modifier.height(24.dp))
        AuthPrimaryButton(
            text = stringResource(R.string.friends_add_confirm),
            onClick = { submitInput() },
            sheetTheme = sheetTheme,
        )
    }
}
