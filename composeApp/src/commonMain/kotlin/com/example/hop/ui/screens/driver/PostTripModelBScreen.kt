package com.example.hop.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverUiState
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.presentation.driver.ModelBDraft
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.screens.passenger.LocationPickerOverlay
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-07 — Post Trip Model B Route.
 *
 * Collects route, date, time, seat count, and minimum threshold for a one-off
 * long-distance trip. Submits a [ModelBDraft] via [DriverEvent.SubmitModelBDraft]
 * and navigates to DR-08 Price Review on effect.
 */
@Composable
fun PostTripModelBRoute(
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
        containerColor = HopColors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        PostTripModelBScreen(
            state = state,
            onSubmit = { draft -> viewModel.onEvent(DriverEvent.SubmitModelBDraft(draft)) },
            onCalculateRoute = { origin, dest ->
                viewModel.onEvent(DriverEvent.CalculateRouteDistance(origin, dest))
            },
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-07 — Post Trip Model B Screen.
 *
 * Single scrollable form — not a wizard.
 * Includes an inline threshold explainer so the driver understands the Model B
 * confirmation mechanic before submitting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostTripModelBScreen(
    state: DriverUiState,
    onSubmit: (ModelBDraft) -> Unit,
    onCalculateRoute: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ── Local form state ──────────────────────────────────────────────────────
    var originName by remember { mutableStateOf("") }
    var destName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("09:00") }
    var seatsTotal by remember { mutableIntStateOf(2) }
    var minThreshold by remember { mutableIntStateOf(1) }
    var locationPickerField by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Validation
    var originError by remember { mutableStateOf<String?>(null) }
    var destError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    // Ensure minThreshold never exceeds seatsTotal
    LaunchedEffect(seatsTotal) {
        if (minThreshold > seatsTotal) minThreshold = seatsTotal
    }

    // Auto-trigger route calculation when both addresses are set
    LaunchedEffect(originName, destName) {
        if (originName.isNotBlank() && destName.isNotBlank()) {
            onCalculateRoute(originName, destName)
        }
    }

    fun validate(): Boolean {
        originError = if (originName.isBlank()) "Enter a departure location" else null
        destError = if (destName.isBlank()) "Enter a destination" else null
        dateError = if (selectedDate.isBlank()) "Pick a trip date" else null
        return originError == null && destError == null && dateError == null &&
            state.routeDistanceMetres > 0
    }

    // ── Date picker ───────────────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = java.util.Calendar.getInstance(
                                java.util.TimeZone.getTimeZone("UTC")
                            )
                            cal.timeInMillis = millis
                            selectedDate = "%04d-%02d-%02d".format(
                                cal.get(java.util.Calendar.YEAR),
                                cal.get(java.util.Calendar.MONTH) + 1,
                                cal.get(java.util.Calendar.DAY_OF_MONTH),
                            )
                            dateError = null
                        }
                        showDatePicker = false
                    },
                ) { Text("OK", color = HopColors.primaryLime, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = HopColors.authTextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = HopColors.background,
                titleContentColor = HopColors.authTextSecondary,
                headlineContentColor = HopColors.authTextPrimary,
                weekdayContentColor = HopColors.authTextSecondary,
                subheadContentColor = HopColors.authTextSecondary,
                navigationContentColor = HopColors.authTextPrimary,
                yearContentColor = HopColors.authTextPrimary,
                disabledYearContentColor = HopColors.authTextSecondary.copy(alpha = 0.38f),
                currentYearContentColor = HopColors.primaryLime,
                selectedYearContentColor = HopColors.authTextPrimary,
                selectedYearContainerColor = HopColors.primaryLime,
                dayContentColor = HopColors.authTextPrimary,
                disabledDayContentColor = HopColors.authTextSecondary.copy(alpha = 0.38f),
                selectedDayContentColor = HopColors.authTextPrimary,
                disabledSelectedDayContentColor = HopColors.authTextPrimary.copy(alpha = 0.38f),
                selectedDayContainerColor = HopColors.primaryLime,
                todayContentColor = HopColors.primaryLime,
                todayDateBorderColor = HopColors.primaryLime,
                dayInSelectionRangeContentColor = HopColors.authTextPrimary,
                dayInSelectionRangeContainerColor = HopColors.primaryLime.copy(alpha = 0.2f),
            ),
        ) {
            DatePicker(
                state = datePickerState,
                title = null,
                headline = null,
                showModeToggle = false,
            )
        }
    }

    // ── Time picker ───────────────────────────────────────────────────────────
    if (showTimePicker) {
        val timeParts = departureTime.split(":").mapNotNull { it.toIntOrNull() }
        val timePickerState = rememberTimePickerState(
            initialHour = timeParts.getOrElse(0) { 9 },
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
            .background(HopColors.background)
            .statusBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        ModelBTopBar(onNavigateBack = onNavigateBack)

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
                // From tappable row
                AddressPickerRow(
                    label = "From",
                    value = originName,
                    placeholder = "e.g. Aarhus C",
                    errorMessage = originError,
                    onClick = { locationPickerField = "from" },
                )
                Spacer(modifier = Modifier.height(HopSpacing.sm))
                // To tappable row
                AddressPickerRow(
                    label = "To",
                    value = destName,
                    placeholder = "e.g. Copenhagen Central",
                    errorMessage = destError,
                    onClick = { locationPickerField = "to" },
                )
                // Route summary: spinner → distance + price preview
                if (originName.isNotBlank() && destName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(HopSpacing.sm))
                    RouteSummaryRow(
                        isCalculating = state.isCalculatingRoute,
                        distanceMetres = state.routeDistanceMetres,
                        seatsTotal = seatsTotal,
                    )
                }
            }

            // Date + time section
            FormSection(title = "Date & Time") {
                DatePickerRow(
                    date = selectedDate,
                    errorMessage = dateError,
                    onEditClick = { showDatePicker = true },
                )
                Spacer(modifier = Modifier.height(HopSpacing.sm))
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

            // Threshold section
            FormSection(title = "Minimum Passengers") {
                SeatCounter(
                    seats = minThreshold,
                    min = 1,
                    max = seatsTotal,
                    onDecrement = { if (minThreshold > 1) minThreshold-- },
                    onIncrement = { if (minThreshold < seatsTotal) minThreshold++ },
                    label = "minimum",
                )
                Spacer(modifier = Modifier.height(HopSpacing.sm))
                ThresholdExplainer(threshold = minThreshold)
            }

            Spacer(modifier = Modifier.height(HopSpacing.sm))
        }

        // ── Sticky CTA ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(HopColors.background)
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md)
                .navigationBarsPadding(),
        ) {
            HorizontalDivider(color = HopColors.authTextSecondary.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(HopSpacing.md))
            HopButton(
                text = "Next: Review Price",
                onClick = {
                    if (validate()) {
                        onSubmit(
                            ModelBDraft(
                                originName = originName.trim(),
                                destName = destName.trim(),
                                date = selectedDate,
                                departureTime = departureTime,
                                seatsTotal = seatsTotal,
                                minThreshold = minThreshold,
                                // distanceMetres and lat/lng are enriched by DriverViewModel.submitModelBDraft
                                distanceMetres = 0,
                            )
                        )
                    }
                },
                enabled = !state.isCalculatingRoute,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    // ── Location picker overlay ───────────────────────────────────────────────
    locationPickerField?.let { field ->
        Popup(
            properties = PopupProperties(focusable = true),
            onDismissRequest = { locationPickerField = null },
        ) {
            BackHandler { locationPickerField = null }
            LocationPickerOverlay(
                title = if (field == "from") "Where from?" else "Where to?",
                initialText = if (field == "from") originName else destName,
                savedPlaces = emptyList(),
                recentSearches = emptyList(),
                onDismiss = { locationPickerField = null },
                onConfirm = { address ->
                    if (field == "from") {
                        originName = address
                        originError = null
                    } else {
                        destName = address
                        destError = null
                    }
                    locationPickerField = null
                },
                onRouteConfirm = { origin, dest ->
                    originName = origin
                    destName = dest
                    originError = null
                    destError = null
                    locationPickerField = null
                },
                onRequestAddPlace = { locationPickerField = null },
            )
        }
    }
}

// ── Subcomponents ─────────────────────────────────────────────────────────────

@Composable
private fun ModelBTopBar(onNavigateBack: () -> Unit) {
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
                tint = HopColors.authTextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Column {
            Text(
                text = "One-Off Trip",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = HopColors.authTextPrimary,
            )
            Text(
                text = "Model B",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = HopColors.primaryLime,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
private fun DatePickerRow(
    date: String,
    errorMessage: String?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HopColors.authInputSurface)
                .border(
                    1.dp,
                    if (errorMessage != null) HopColors.error
                    else HopColors.authTextSecondary.copy(alpha = 0.15f),
                    RoundedCornerShape(12.dp),
                )
                .clickable(onClickLabel = "Pick trip date") { onEditClick() }
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = HopColors.authTextSecondary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = if (date.isBlank()) "Pick a date" else date,
                fontSize = if (date.isBlank()) 15.sp else 18.sp,
                fontWeight = if (date.isBlank()) FontWeight.Normal else FontWeight.Bold,
                color = if (date.isBlank()) HopColors.authTextSecondary else HopColors.authTextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Change",
                fontSize = 13.sp,
                color = HopColors.primaryLime,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(HopSpacing.xs))
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = HopColors.error,
                modifier = Modifier.padding(start = HopSpacing.xs),
            )
        }
    }
}

/**
 * Inline explainer card: "Trip only runs if at least X passengers book".
 * Rendered below the threshold counter in DR-07.
 */
@Composable
private fun ThresholdExplainer(
    threshold: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.warning.copy(alpha = 0.08f))
            .border(
                1.dp,
                HopColors.warning.copy(alpha = 0.25f),
                RoundedCornerShape(12.dp),
            )
            .padding(HopSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = HopColors.warning,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 1.dp),
        )
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Text(
            text = "Trip only runs if at least $threshold " +
                "${if (threshold == 1) "passenger books" else "passengers book"}. " +
                "If the threshold isn't met by 6 h before departure, " +
                "the trip is cancelled and all payments are refunded.",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = HopColors.warning,
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PostTripModelBScreenPreview() {
    HopTheme {
        PostTripModelBScreen(
            state = DriverUiState(),
            onSubmit = {},
            onCalculateRoute = { _, _ -> },
            onNavigateBack = {},
        )
    }
}
