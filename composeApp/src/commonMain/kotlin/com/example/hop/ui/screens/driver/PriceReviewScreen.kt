package com.example.hop.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverUiState
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.presentation.driver.ModelADraft
import com.example.hop.presentation.driver.ModelBDraft
import com.example.hop.pricing.PricingEngine
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopMonoFontFamily
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import com.example.hop.ui.util.formatDkk
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-08 — Price Review Route.
 *
 * Reads [DriverUiState.pendingModelADraft] or [DriverUiState.pendingModelBDraft],
 * computes the price via [PricingEngine], and presents a read-only summary.
 * "Confirm & Post" fires [DriverEvent.ConfirmAndPostTrip]; on success navigates to DR-09.
 */
@Composable
fun PriceReviewRoute(
    onNavigateToMyTrips: () -> Unit,
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
                is DriverEffect.NavigateToMyTrips -> onNavigateToMyTrips()
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
        PriceReviewScreen(
            state = state,
            onConfirmAndPost = { viewModel.onEvent(DriverEvent.ConfirmAndPostTrip) },
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-08 — Price Review Screen.
 *
 * Stateless renderer. Displays a read-only trip summary and a system-calculated
 * price breakdown. Price is not editable by the driver.
 */
@Composable
fun PriceReviewScreen(
    state: DriverUiState,
    onConfirmAndPost: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val modelADraft = state.pendingModelADraft
    val modelBDraft = state.pendingModelBDraft

    // Derive display fields from whichever draft is pending
    val originName = modelADraft?.originName ?: modelBDraft?.originName ?: ""
    val destName = modelADraft?.destName ?: modelBDraft?.destName ?: ""
    val seatsTotal = modelADraft?.seatsTotal ?: modelBDraft?.seatsTotal ?: 1
    val distanceMetres = modelADraft?.distanceMetres ?: modelBDraft?.distanceMetres ?: 0
    val isModelA = modelADraft != null

    // Compute price — only available when distanceMetres > 0
    val priceResult = remember(distanceMetres, seatsTotal) {
        if (distanceMetres > 0) PricingEngine.calculate(distanceMetres, seatsTotal) else null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        PriceReviewTopBar(onNavigateBack = onNavigateBack)

        // ── Scrollable body ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopSpacing.md),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
        ) {
            Spacer(modifier = Modifier.height(HopSpacing.xs))

            // ── Trip summary card ─────────────────────────────────────────────
            TripSummaryCard(
                originName = originName,
                destName = destName,
                seatsTotal = seatsTotal,
                isModelA = isModelA,
                modelADraft = modelADraft,
                modelBDraft = modelBDraft,
            )

            // ── Price breakdown card ──────────────────────────────────────────
            PriceBreakdownCard(
                distanceMetres = distanceMetres,
                seatsTotal = seatsTotal,
                priceResult = priceResult,
            )

            // ── Warning banner ────────────────────────────────────────────────
            SystemPriceWarning()

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
                text = "Confirm & Post",
                onClick = onConfirmAndPost,
                isLoading = state.isPostingTrip,
                enabled = priceResult != null,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Subcomponents ─────────────────────────────────────────────────────────────

@Composable
private fun PriceReviewTopBar(onNavigateBack: () -> Unit) {
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
        Text(
            text = "Review Price",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = HopColors.authTextPrimary,
        )
    }
}

@Composable
private fun TripSummaryCard(
    originName: String,
    destName: String,
    seatsTotal: Int,
    isModelA: Boolean,
    modelADraft: ModelADraft?,
    modelBDraft: ModelBDraft?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.authInputSurface)
            .border(1.dp, HopColors.authTextSecondary.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Trip Summary",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = HopColors.authTextSecondary,
                letterSpacing = 0.5.sp,
            )
            // Model badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(HopColors.authAccent.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = if (isModelA) "Model A" else "Model B",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HopColors.authAccent,
                    letterSpacing = 0.3.sp,
                )
            }
        }

        HorizontalDivider(color = HopColors.authTextSecondary.copy(alpha = 0.1f))

        // Route
        SummaryRow(label = "From", value = originName.ifBlank { "—" })
        SummaryRow(label = "To", value = destName.ifBlank { "—" })

        // Model-specific details
        if (modelADraft != null) {
            SummaryRow(
                label = "Days",
                value = modelADraft.recurrenceDays
                    .joinToString(", ") { it.take(2) }
                    .ifBlank { "—" },
            )
            SummaryRow(label = "Departure", value = modelADraft.departureTime.ifBlank { "—" })
        } else if (modelBDraft != null) {
            SummaryRow(label = "Date", value = modelBDraft.date.ifBlank { "—" })
            SummaryRow(label = "Departure", value = modelBDraft.departureTime.ifBlank { "—" })
            SummaryRow(
                label = "Min. passengers",
                value = "${modelBDraft.minThreshold}",
            )
        }

        SummaryRow(label = "Seats offered", value = "$seatsTotal")
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = HopColors.authTextSecondary,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = HopColors.authTextPrimary,
        )
    }
}

@Composable
private fun PriceBreakdownCard(
    distanceMetres: Int,
    seatsTotal: Int,
    priceResult: PricingEngine.PriceResult?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.authInputSurface)
            .border(1.dp, HopColors.authTextSecondary.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        // Header with lock icon indicating price is read-only
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = HopColors.authTextSecondary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(HopSpacing.xs))
            Text(
                text = "Price Breakdown",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = HopColors.authTextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "System-Calculated",
                fontSize = 11.sp,
                color = HopColors.authAccent.copy(alpha = 0.7f),
            )
        }

        HorizontalDivider(color = HopColors.authTextSecondary.copy(alpha = 0.1f))

        if (priceResult == null) {
            // Distance not yet resolved — placeholder state
            Text(
                text = "Price will be calculated once the route is confirmed.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = HopColors.authTextSecondary,
            )
        } else {
            val distanceKm = distanceMetres / 1000.0
            PriceRow(
                label = "Distance",
                value = "%.1f km".format(distanceKm),
                isAmount = false,
            )
            PriceRow(
                label = "Passengers pay / seat",
                value = formatDkk(priceResult.pricePerSeatOere),
                isAmount = true,
                highlight = true,
            )
            HorizontalDivider(color = HopColors.primaryLime.copy(alpha = 0.2f))
            PriceRow(
                label = "You receive / seat",
                value = formatDkk(priceResult.pricePerSeatOere),
                isAmount = true,
                highlight = true,
                labelWeight = FontWeight.Bold,
                valueSize = 18.sp,
            )
            if (seatsTotal > 1) {
                PriceRow(
                    label = "Max. total earnings",
                    value = formatDkk(priceResult.pricePerSeatOere * seatsTotal),
                    isAmount = true,
                    valueColor = HopColors.success,
                )
            }
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    isAmount: Boolean,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    labelWeight: FontWeight = FontWeight.Normal,
    valueSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    valueColor: Color = HopColors.authTextPrimary,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = labelWeight,
            color = if (highlight) HopColors.authTextPrimary else HopColors.authTextSecondary,
        )
        Text(
            text = value,
            fontSize = valueSize,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
            fontFamily = if (isAmount) HopMonoFontFamily else FontFamily.Default,
        )
    }
}

/**
 * Warning banner: "Price is set by the system and cannot be changed."
 */
@Composable
private fun SystemPriceWarning(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.warning.copy(alpha = 0.08f))
            .border(1.dp, HopColors.warning.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(HopSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = HopColors.warning,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 1.dp),
        )
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Text(
            text = "Price is set by the system and cannot be changed. " +
                "Rates follow SKAT's reimbursement rules (DKK 2.28 / km).",
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = HopColors.warning,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Converts an Int in øre to a DKK display string, e.g. 2038 → "DKK 20.38". */


// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PriceReviewScreenModelAPreview() {
    HopTheme {
        PriceReviewScreen(
            state = DriverUiState(
                pendingModelADraft = ModelADraft(
                    originName = "Aarhus C",
                    destName = "Copenhagen Central",
                    recurrenceDays = listOf("MON", "WED", "FRI"),
                    departureTime = "07:30",
                    seatsTotal = 3,
                    distanceMetres = 304_000,
                ),
            ),
            onConfirmAndPost = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PriceReviewScreenModelBPreview() {
    HopTheme {
        PriceReviewScreen(
            state = DriverUiState(
                pendingModelBDraft = ModelBDraft(
                    originName = "Odense",
                    destName = "Aalborg",
                    date = "2026-05-10",
                    departureTime = "09:00",
                    seatsTotal = 4,
                    minThreshold = 2,
                    distanceMetres = 230_000,
                ),
            ),
            onConfirmAndPost = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Price not yet resolved")
@Composable
private fun PriceReviewScreenNoPricePreview() {
    HopTheme {
        PriceReviewScreen(
            state = DriverUiState(
                pendingModelBDraft = ModelBDraft(
                    originName = "Odense",
                    destName = "Aalborg",
                    date = "2026-05-10",
                    departureTime = "09:00",
                    seatsTotal = 2,
                    minThreshold = 1,
                    distanceMetres = 0,
                ),
            ),
            onConfirmAndPost = {},
            onNavigateBack = {},
        )
    }
}
