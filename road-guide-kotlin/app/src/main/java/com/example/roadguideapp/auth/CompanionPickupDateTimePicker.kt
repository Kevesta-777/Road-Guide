package com.example.roadguideapp.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.roadguideapp.R
import com.example.roadguideapp.map.AppleMapsSheetTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatPickupDateTimeSummary(date: LocalDate?, time: LocalTime?): String {
    if (date == null || time == null) return ""
    val datePart = formatPickupDateLabel(date)
    val timePart = time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    return "$datePart · $timePart"
}

internal fun formatPickupDateLabel(date: LocalDate): String {
    val today = LocalDate.now()
    val pattern = if (date == today) {
        "Today, MMM d, yyyy"
    } else {
        "EEE, MMM d, yyyy"
    }
    return date.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
}

@Composable
internal fun CompanionPickupDateTimeField(
    selectedDate: LocalDate?,
    selectedTime: LocalTime?,
    sheetTheme: AppleMapsSheetTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = formatPickupDateTimeSummary(selectedDate, selectedTime)
    val hasValue = summary.isNotBlank()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.companion_field_pickup_time),
                color = sheetTheme.secondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = summary.ifBlank { stringResource(R.string.companion_hint_pickup_time) },
                color = if (hasValue) sheetTheme.primaryText else sheetTheme.searchFieldHint,
                fontSize = 16.sp,
                fontWeight = if (hasValue) FontWeight.Medium else FontWeight.Normal,
            )
        }
        AuthIconBadge(
            icon = Icons.Outlined.Schedule,
            sheetTheme = sheetTheme,
            size = 40.dp,
            iconSize = 20.dp,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CompanionPickupDateTimePickerSheet(
    visible: Boolean,
    initialDate: LocalDate?,
    initialTime: LocalTime?,
    sheetTheme: AppleMapsSheetTheme,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalTime) -> Unit,
) {
    if (!visible) return

    val seedDate = initialDate ?: LocalDate.now()
    val seedTime = initialTime ?: LocalTime.now().withSecond(0).withNano(0)
    val (seedHour12, seedAm) = to12Hour(seedTime.hour)

    var draftDate by remember { mutableStateOf(seedDate) }
    var draftHour by remember { mutableIntStateOf(seedHour12) }
    var draftMinute by remember { mutableIntStateOf(seedTime.minute) }
    var draftIsAm by remember { mutableStateOf(seedAm) }
    var calendarExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(visible, initialDate, initialTime) {
        if (!visible) return@LaunchedEffect
        draftDate = seedDate
        draftHour = seedHour12
        draftMinute = seedTime.minute
        draftIsAm = seedAm
        calendarExpanded = false
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = seedDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli(),
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        val millis = datePickerState.selectedDateMillis ?: return@LaunchedEffect
        draftDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    val accent = sheetTheme.accent
    val fieldFill = sheetTheme.searchFieldFill

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(24.dp),
            color = sheetTheme.sheetSurface,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AuthIconBadge(
                    icon = Icons.Outlined.Schedule,
                    sheetTheme = sheetTheme,
                    size = 48.dp,
                    iconSize = 24.dp,
                    shape = RoundedCornerShape(20.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.companion_time_dialog_title),
                    color = sheetTheme.primaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.companion_time_dialog_subtitle),
                    color = sheetTheme.secondaryText,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    PickupTimeBox(
                        label = stringResource(R.string.companion_picker_hour),
                        value = draftHour.toString().padStart(2, '0'),
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(2)
                            if (digits.isEmpty()) return@PickupTimeBox
                            draftHour = digits.toInt().coerceIn(1, 12)
                        },
                        modifier = Modifier.weight(1f),
                        sheetTheme = sheetTheme,
                    )
                    Text(
                        text = ":",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = sheetTheme.primaryText,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                    PickupTimeBox(
                        label = stringResource(R.string.companion_picker_minute),
                        value = draftMinute.toString().padStart(2, '0'),
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(2)
                            if (digits.isEmpty()) return@PickupTimeBox
                            draftMinute = digits.toInt().coerceIn(0, 59)
                        },
                        modifier = Modifier.weight(1f),
                        sheetTheme = sheetTheme,
                    )
                    PickupPeriodBox(
                        isAm = draftIsAm,
                        onSelect = { draftIsAm = it },
                        modifier = Modifier.weight(1f),
                        sheetTheme = sheetTheme,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(fieldFill)
                    .clickable { calendarExpanded = !calendarExpanded }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                    tint = accent,
                )
                Text(
                    text = formatPickupDateLabel(draftDate),
                        color = sheetTheme.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = if (calendarExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = sheetTheme.secondaryText,
                    )
                }

                if (calendarExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DatePicker(
                        state = datePickerState,
                        title = null,
                        headline = null,
                        showModeToggle = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(R.string.companion_book_cancel),
                            color = sheetTheme.secondaryText,
                            fontSize = 16.sp,
                        )
                    }
                    Button(
                        onClick = {
                            onConfirm(draftDate, to24HourTime(draftHour, draftMinute, draftIsAm))
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.companion_picker_confirm),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickupTimeBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = sheetTheme.primaryText,
                textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(sheetTheme.accent),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(sheetTheme.searchFieldFill)
                .padding(vertical = 16.dp, horizontal = 8.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = sheetTheme.secondaryText,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun PickupPeriodBox(
    isAm: Boolean,
    onSelect: (Boolean) -> Unit,
    sheetTheme: AppleMapsSheetTheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(sheetTheme.searchFieldFill)
                .clickable { onSelect(!isAm) }
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isAm) {
                    stringResource(R.string.companion_picker_am)
                } else {
                    stringResource(R.string.companion_picker_pm)
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = sheetTheme.primaryText,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = sheetTheme.secondaryText,
                modifier = Modifier.padding(start = 2.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.companion_picker_period),
            color = sheetTheme.secondaryText,
            fontSize = 12.sp,
        )
    }
}

private fun to12Hour(hour24: Int): Pair<Int, Boolean> {
    val isAm = hour24 < 12
    val hour12 = when (hour24 % 12) {
        0 -> 12
        else -> hour24 % 12
    }
    return hour12 to isAm
}

private fun to24HourTime(hour12: Int, minute: Int, isAm: Boolean): LocalTime {
    val normalizedHour = hour12.coerceIn(1, 12)
    val normalizedMinute = minute.coerceIn(0, 59)
    val hour24 = when {
        normalizedHour == 12 && isAm -> 0
        normalizedHour == 12 && !isAm -> 12
        !isAm -> normalizedHour + 12
        else -> normalizedHour
    }
    return LocalTime.of(hour24, normalizedMinute)
}
