package com.example.hop.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverUiState
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.presentation.driver.ModelADraft
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopTextField
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-06 — Post Trip Model A Route.
 *
 * Collects route, day-of-week chips, departure time, and seat count for a
 * recurring daily commute. Submits a [ModelADraft] via [DriverEvent.SubmitModelADraft]
 * and navigates to DR-08 Price Review on effect.
 */
@Composable
fun PostTripModelARoute(
    onNavigateToPriceReview: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverEffect.NavigateToPriceReview -> onNavigateToPriceReview()
                is DriverEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        PostTripModelAScreen(
            state = state,
            onSubmit = { draft -> viewModel.onEvent(DriverEvent.SubmitModelADraft(draft)) },
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-06 — Post Trip Model A Screen.
 *
 * Single scrollable form — not a wizard.
 * All fields are collected here; form state is local until submitted.
 */
@Composable
fun PostTripModelAScreen(
    state: DriverUiState,
    onSubmit: (ModelADraft) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Local form state ──────────────────────────────────────────────────────
    var originName by remember { mutableStateOf("") }
    var destName by remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateListOf<String>() }
    var departureTime by remember { mutableStateOf("08:00") }
    var seatsTotal by remember { mutableIntStateOf(1) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Validation
    var originError by remember { mutableStateOf<String?>(null) }
    var destError by remember { mutableStateOf<String?>(null) }
    var daysError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        originError = if (originName.isBlank()) "Enter a departure location" else null
        destError = if (destName.isBlank()) "Enter a destination" else null
        daysError = if (selectedDays.isEmpty()) "Select at least one day" else null
        return originError == null && destError == null && daysError == null
    }

    // ── Time picker dialog ────────────────────────────────────────────────────
    if (showTimePicker) {
        val timeParts = departureTime.split(":").mapNotNull { it.toIntOrNull() }
        val timePickerState = rememberTimePickerState(
            initialHour = timeParts.getOrElse(0) { 8 },
            initialMinute = timeParts.getOrElse(1) { 0 },
            is24Hour = true,
        )
        TimePickerDialog(
            timePickerState = timePickerState,
            onConfirm = {
                departureTime = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
            .statusBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        ModelFormTopBar(title = "Daily Commute", onNavigateBack = onNavigateBack)

        // ── Scrollable form ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopSpacing.md),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.lg),
        ) {
            Spacer(modifier = Modifier.height(HopSpacing.xs))

            // Route section
            FormSection(title = "Route") {
                HopTextField(
                    value = originName,
                    onValueChange = {
                        originName = it
                        originError = null
                    },
                    label = "From",
                    placeholder = "e.g. Aarhus C",
                    errorMessage = originError,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(HopSpacing.sm))
                HopTextField(
                    value = destName,
                    onValueChange = {
                        destName = it
                        destError = null
                    },
                    label = "To",
                    placeholder = "e.g. Copenhagen Central",
                    errorMessage = destError,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Days section
            FormSection(title = "Recurring Days") {
                DayChipsRow(
                    selectedDays = selectedDays,
                    onToggleDay = { day ->
                        if (selectedDays.contains(day)) selectedDays.remove(day)
                        else selectedDays.add(day)
                        daysError = null
                    },
                )
                if (daysError != null) {
                    Spacer(modifier = Modifier.height(HopSpacing.xs))
                    Text(
                        text = daysError!!,
                        fontSize = 12.sp,
                        color = HopColors.error,
                    )
                }
            }

            // Departure time section
            FormSection(title = "Departure Time") {
                TimePickerRow(
                    time = departureTime,
                    onEditClick = { showTimePicker = true },
                )
            }

            // Seats section
            FormSection(title = "Available Seats") {
                SeatCounter(
                    seats = seatsTotal,
                    min = 1,
                    max = 4,
                    onDecrement = { if (seatsTotal > 1) seatsTotal-- },
                    onIncrement = { if (seatsTotal < 4) seatsTotal++ },
                )
            }

            Spacer(modifier = Modifier.height(HopSpacing.sm))
        }

        // ── Sticky CTA ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HopColors.surface)
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md)
                .navigationBarsPadding(),
        ) {
            HorizontalDivider(color = HopColors.textSecondary.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(HopSpacing.md))
            HopButton(
                text = "Next: Review Price",
                onClick = {
                    if (validate()) {
                        onSubmit(
                            ModelADraft(
                                originName = originName.trim(),
                                destName = destName.trim(),
                                recurrenceDays = selectedDays.toList(),
                                departureTime = departureTime,
                                seatsTotal = seatsTotal,
                                distanceMetres = 0, // resolved via routing API post-MVP
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Subcomponents ─────────────────────────────────────────────────────────────

@Composable
private fun ModelFormTopBar(
    title: String,
    onNavigateBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.xs, vertical = HopSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.Outlined.ArrowBackIosNew,
                contentDescription = "Back",
                tint = HopColors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Column {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = HopColors.textPrimary,
            )
            Text(
                text = "Model A",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = HopColors.primaryLime,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = HopColors.textSecondary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = HopSpacing.sm),
        )
        content()
    }
}

private val WEEK_DAYS = listOf("MON", "TUE", "WED", "THU", "FRI")

@Composable
private fun DayChipsRow(
    selectedDays: List<String>,
    onToggleDay: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        WEEK_DAYS.forEach { day ->
            val isSelected = selectedDays.contains(day)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) HopColors.primaryLime
                        else HopColors.surfaceElevated,
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent
                        else HopColors.textSecondary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                    )
                    .clickable { onToggleDay(day) }
                    .semantics { contentDescription = "$day ${if (isSelected) "selected" else "not selected"}" },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.take(2), // "Mo", "Tu", etc.
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color(0xFF1A1A1A) else HopColors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun TimePickerRow(
    time: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.surfaceElevated)
            .border(
                1.dp,
                HopColors.textSecondary.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp),
            )
            .clickable(onClickLabel = "Change departure time") { onEditClick() }
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = time,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = HopColors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Change",
            fontSize = 13.sp,
            color = HopColors.primaryLime,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun SeatCounter(
    seats: Int,
    min: Int,
    max: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    label: String = "seats",
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.surfaceElevated)
            .border(
                1.dp,
                HopColors.textSecondary.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp),
            )
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$seats $label",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = HopColors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        // Decrement
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (seats > min) HopColors.primaryLime.copy(alpha = 0.15f)
                    else HopColors.textSecondary.copy(alpha = 0.08f),
                )
                .clickable(enabled = seats > min, onClickLabel = "Decrease $label") { onDecrement() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Remove,
                contentDescription = "Decrease $label",
                tint = if (seats > min) HopColors.primaryLime else HopColors.textSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        // Increment
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (seats < max) HopColors.primaryLime.copy(alpha = 0.15f)
                    else HopColors.textSecondary.copy(alpha = 0.08f),
                )
                .clickable(enabled = seats < max, onClickLabel = "Increase $label") { onIncrement() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Increase $label",
                tint = if (seats < max) HopColors.primaryLime else HopColors.textSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Reusable Material3 time picker wrapped in a [Dialog].
 * Used by both DR-06 and DR-07.
 */
@Composable
internal fun TimePickerDialog(
    timePickerState: TimePickerState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(HopColors.surfaceElevated)
                .padding(HopSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Select Departure Time",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HopColors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = HopSpacing.md),
            )
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = HopColors.surface,
                    clockDialSelectedContentColor = Color(0xFF1A1A1A),
                    clockDialUnselectedContentColor = HopColors.textSecondary,
                    selectorColor = HopColors.primaryLime,
                    containerColor = HopColors.surfaceElevated,
                    periodSelectorBorderColor = HopColors.primaryLime.copy(alpha = 0.3f),
                    periodSelectorSelectedContainerColor = HopColors.primaryLime.copy(alpha = 0.2f),
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorSelectedContentColor = HopColors.primaryLime,
                    periodSelectorUnselectedContentColor = HopColors.textSecondary,
                    timeSelectorSelectedContainerColor = HopColors.primaryLime.copy(alpha = 0.15f),
                    timeSelectorUnselectedContainerColor = HopColors.surface,
                    timeSelectorSelectedContentColor = HopColors.primaryLime,
                    timeSelectorUnselectedContentColor = HopColors.textSecondary,
                ),
            )
            Spacer(modifier = Modifier.height(HopSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text("Cancel", color = HopColors.textSecondary)
                }
                Spacer(modifier = Modifier.width(HopSpacing.sm))
                androidx.compose.material3.TextButton(onClick = onConfirm) {
                    Text("OK", color = HopColors.primaryLime, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun PostTripModelAScreenPreview() {
    HopTheme {
        PostTripModelAScreen(
            state = DriverUiState(),
            onSubmit = {},
            onNavigateBack = {},
        )
    }
}
