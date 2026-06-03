package com.example.roadguideapp.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

fun authErrorMessage(context: Context, error: AuthError, detail: String? = null): String = when (error) {
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
    AuthError.ServerError -> detail ?: context.getString(R.string.auth_error_server)
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
internal fun AuthBackButton(
    onClick: () -> Unit,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.auth_back),
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(sheetTheme.headerIconSecondaryBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = contentDescription,
            tint = sheetTheme.primaryText,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
internal fun AuthPageTopBar(
    title: String?,
    sheetTheme: AppleMapsSheetTheme,
    onBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            AuthBackButton(onClick = onBack, sheetTheme = sheetTheme)
            Spacer(modifier = Modifier.width(12.dp))
        }
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                color = sheetTheme.primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        if (onClose != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(sheetTheme.headerIconSecondaryBg)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.apple_close),
                    tint = sheetTheme.secondaryText,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
internal fun AuthIconBadge(
    icon: ImageVector,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = sheetTheme.accent.copy(alpha = if (sheetTheme.isLight) 0.10f else 0.18f),
    iconTint: Color = sheetTheme.accent,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
internal fun AuthPageScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sheetTheme: AppleMapsSheetTheme = rememberAuthSheetTheme(),
    showOfflineNotice: Boolean = false,
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            AuthPageTopBar(
                title = title,
                sheetTheme = sheetTheme,
                onBack = onBack,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(start = 48.dp, end = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
            if (showOfflineNotice) {
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
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = sheetTheme.accent,
            contentColor = sheetTheme.onAccent,
            disabledContainerColor = sheetTheme.accent.copy(alpha = 0.4f),
            disabledContentColor = sheetTheme.onAccent.copy(alpha = 0.7f),
        ),
    ) {
        Text(text = text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun AuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            color = sheetTheme.primaryText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        )
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
        com.example.roadguideapp.map.SearchTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            sheetTheme = sheetTheme,
            leadingIcon = null,
            showClearWhenNonEmpty = false,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
        )
    }
}

@Composable
internal fun AuthSectionLabel(
    text: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        color = sheetTheme.tertiaryText,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.6.sp,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp, top = 4.dp),
    )
}

@Composable
internal fun AuthGroupedCard(
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = sheetTheme.cardElevated,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            content = content,
        )
    }
}

@Composable
internal fun AuthNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    sheetTheme: AppleMapsSheetTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuthIconBadge(
            icon = icon,
            sheetTheme = sheetTheme,
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = sheetTheme.primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = sheetTheme.secondaryText,
                    fontSize = 13.sp,
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = sheetTheme.tertiaryText,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun AuthNavDivider(sheetTheme: AppleMapsSheetTheme) {
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp),
        color = sheetTheme.divider.copy(alpha = 0.6f),
    )
}

@Composable
internal fun AuthFormDivider(sheetTheme: AppleMapsSheetTheme) {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp),
        color = sheetTheme.divider.copy(alpha = 0.6f),
    )
}

@Composable
internal fun AuthInfoRow(
    label: String,
    value: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            color = sheetTheme.secondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = sheetTheme.primaryText,
            fontSize = 16.sp,
        )
    }
}

@Composable
internal fun AuthProfileHero(
    abbreviation: String,
    title: String,
    subtitle: String,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    sheetTheme.accent.copy(alpha = if (sheetTheme.isLight) 0.12f else 0.22f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = abbreviation,
                color = sheetTheme.accent,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = sheetTheme.primaryText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = sheetTheme.secondaryText,
            fontSize = 14.sp,
        )
    }
}

@Composable
internal fun AuthOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    sheetTheme: AppleMapsSheetTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = sheetTheme.searchFieldFill,
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuthIconBadge(
                icon = icon,
                sheetTheme = sheetTheme,
                size = 48.dp,
                iconSize = 24.dp,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = sheetTheme.primaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = sheetTheme.tertiaryText,
            )
        }
    }
}

@Composable
internal fun AuthFriendRow(
    displayName: String,
    profileId: String,
    sheetTheme: AppleMapsSheetTheme,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    removing: Boolean = false,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = sheetTheme.cardElevated,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        sheetTheme.accent.copy(alpha = if (sheetTheme.isLight) 0.12f else 0.20f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = identifierAbbreviation(displayName),
                    color = sheetTheme.accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    color = sheetTheme.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profileId,
                    color = sheetTheme.secondaryText,
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
            TextButton(onClick = onRemove, enabled = !removing) {
                Text(
                    text = stringResource(R.string.friends_remove),
                    color = sheetTheme.stopGlyphSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
