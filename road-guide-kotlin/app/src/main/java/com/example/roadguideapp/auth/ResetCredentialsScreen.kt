package com.example.roadguideapp.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.roadguideapp.R
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
internal fun ResetCredentialsScreen(
    onBack: () -> Unit,
    onCredentialsUpdated: () -> Unit,
    initialIdentifier: String = "",
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val sheetTheme = rememberAuthSheetTheme()

    var identifier by rememberSaveable { mutableStateOf(initialIdentifier) }
    var password by rememberSaveable { mutableStateOf("") }
    var newIdentifier by rememberSaveable {
        mutableStateOf(
            initialIdentifier.ifEmpty { OfflineAuthStore.storedIdentifier(context).orEmpty() },
        )
    }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (isSubmitting) return
        focusManager.clearFocus()
        val currentId = identifier.trim()
        val nextId = newIdentifier.trim()
        val immediateError = when {
            currentId.isEmpty() || password.isEmpty() ||
                nextId.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() ->
                AuthError.EmptyFields
            newPassword != confirmPassword -> AuthError.PasswordMismatch
            else -> null
        }
        if (immediateError != null) {
            errorMessage = authErrorMessage(context, immediateError)
            return
        }
        isSubmitting = true
        scope.launch(Dispatchers.IO) {
            val result = OfflineAuthStore.resetCredentials(
                context = context,
                currentIdentifier = currentId,
                currentPassword = password,
                newIdentifier = nextId,
                newPassword = newPassword,
            )
            runOnMainThread(context) {
                isSubmitting = false
                when (result) {
                    AuthResult.Success -> onCredentialsUpdated()
                    is AuthResult.Failure -> {
                        errorMessage = authErrorMessage(context, result.error, result.detail)
                        if (result.error == AuthError.InvalidCredentials) password = ""
                    }
                }
            }
        }
    }

    AuthPageScaffold(
        title = stringResource(R.string.auth_reset_title),
        subtitle = stringResource(R.string.auth_subtitle_reset),
        onBack = onBack,
        modifier = modifier,
        sheetTheme = sheetTheme,
    ) {
        AuthField(
            label = stringResource(R.string.auth_current_identifier_label),
            value = identifier,
            onValueChange = {
                identifier = it
                errorMessage = null
            },
            placeholder = stringResource(R.string.auth_identifier_hint),
            sheetTheme = sheetTheme,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthField(
            label = stringResource(R.string.auth_current_password_label),
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            placeholder = stringResource(R.string.auth_password_hint),
            sheetTheme = sheetTheme,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthField(
            label = stringResource(R.string.auth_new_identifier_label),
            value = newIdentifier,
            onValueChange = {
                newIdentifier = it
                errorMessage = null
            },
            placeholder = stringResource(R.string.auth_new_identifier_hint),
            sheetTheme = sheetTheme,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthField(
            label = stringResource(R.string.auth_new_password_label),
            value = newPassword,
            onValueChange = {
                newPassword = it
                errorMessage = null
            },
            placeholder = stringResource(R.string.auth_new_password_hint),
            sheetTheme = sheetTheme,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthField(
            label = stringResource(R.string.auth_confirm_password_label),
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            placeholder = stringResource(R.string.auth_confirm_password_hint),
            sheetTheme = sheetTheme,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )
        AuthErrorText(errorMessage, sheetTheme)
        Spacer(modifier = Modifier.height(24.dp))
        AuthPrimaryButton(
            text = stringResource(
                if (isSubmitting) R.string.auth_resetting else R.string.auth_reset_credentials,
            ),
            onClick = { submit() },
            sheetTheme = sheetTheme,
            enabled = !isSubmitting,
        )
    }
}
