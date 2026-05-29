package com.example.roadguideapp.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.roadguideapp.R
import com.example.roadguideapp.map.AppleMapsSheetTheme
import com.example.roadguideapp.map.MapTimeOfDay
import com.example.roadguideapp.map.appleMapsSheetTheme

/** Runs [block] on the main thread on the next frame (safe after dialog dismiss / navigation). */
internal fun runOnMainThread(context: Context, block: () -> Unit) {
    ContextCompat.getMainExecutor(context).execute(block)
}

@Composable
internal fun rememberAuthSheetTheme(): AppleMapsSheetTheme {
    return appleMapsSheetTheme(
        if (isSystemInDarkTheme()) MapTimeOfDay.Night else MapTimeOfDay.Day,
    )
}

fun authErrorMessage(context: Context, error: AuthError): String = when (error) {
    AuthError.NoAccount -> context.getString(R.string.auth_error_no_account)
    AuthError.AccountAlreadyExists -> context.getString(R.string.auth_error_account_exists)
    AuthError.InvalidCredentials -> context.getString(R.string.auth_error_invalid_credentials)
    AuthError.EmptyIdentifier -> context.getString(R.string.auth_error_empty_identifier)
    AuthError.IdentifierTooShort -> context.getString(
        R.string.auth_error_identifier_too_short,
        OfflineAuthStore.MIN_IDENTIFIER_LENGTH,
    )
    AuthError.EmptyPassword -> context.getString(R.string.auth_error_empty_password)
    AuthError.PasswordTooShort -> context.getString(
        R.string.auth_error_password_too_short,
        OfflineAuthStore.MIN_PASSWORD_LENGTH,
    )
    AuthError.PasswordMismatch -> context.getString(R.string.auth_error_password_mismatch)
    AuthError.EmptyFields -> context.getString(R.string.auth_error_empty_fields)
    AuthError.NetworkError -> context.getString(R.string.auth_error_network)
    AuthError.UnsupportedOperation -> context.getString(R.string.auth_error_unsupported)
}

fun identifierAbbreviation(identifier: String): String {
    val trimmed = identifier.trim()
    if (trimmed.isEmpty()) return ""
    val parts = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        trimmed.length >= 2 -> trimmed.take(2).uppercase()
        else -> trimmed.uppercase()
    }
}

@Composable
internal fun AuthPageScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sheetTheme: AppleMapsSheetTheme = rememberAuthSheetTheme(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(sheetTheme.sheetSurface)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.auth_back),
                    tint = sheetTheme.primaryText,
                )
            }
            Text(
                text = title,
                color = sheetTheme.primaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = sheetTheme.secondaryText,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            )
            Spacer(modifier = Modifier.height(28.dp))
            content()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.auth_offline_notice),
                color = sheetTheme.tertiaryText,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = sheetTheme.accent,
            contentColor = sheetTheme.onAccent,
        ),
    ) {
        Text(text = text, fontSize = 17.sp)
    }
}

@Composable
internal fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = text, color = sheetTheme.primaryText, fontSize = 17.sp)
    }
}

@Composable
internal fun AuthErrorText(
    message: String?,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    if (message != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            color = sheetTheme.accent,
            fontSize = 14.sp,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = sheetTheme.secondaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(sheetTheme.searchFieldFill, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            textStyle = TextStyle(
                color = sheetTheme.searchFieldText,
                fontSize = 17.sp,
            ),
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            cursorBrush = SolidColor(sheetTheme.searchFieldText),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = sheetTheme.searchFieldHint,
                            fontSize = 17.sp,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}
