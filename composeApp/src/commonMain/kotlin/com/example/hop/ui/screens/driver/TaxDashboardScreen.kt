package com.example.hop.ui.screens.driver

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.TaxMonthlySummary
import com.example.hop.presentation.tax.TaxEffect
import com.example.hop.presentation.tax.TaxEvent
import com.example.hop.presentation.tax.TaxUiState
import com.example.hop.presentation.tax.TaxViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopMonoFontFamily
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import org.koin.compose.viewmodel.koinViewModel
import java.util.Calendar

// ── Helpers ───────────────────────────────────────────────────────────────────

private val MONTH_NAMES = arrayOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private fun formatDkk(oere: Int): String = "DKK ${oere / 100}"

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-13 — Monthly Tax Dashboard Route.
 *
 * Initialises to current month/year. Handles the [TaxEffect.OpenReportUrl]
 * one-shot effect by opening the presigned URL in the device browser.
 */
@Composable
fun TaxDashboardRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaxViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based

    LaunchedEffect(Unit) {
        viewModel.onEvent(TaxEvent.LoadDashboard(currentYear, currentMonth))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TaxEffect.OpenReportUrl -> uriHandler.openUri(effect.url)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
    ) { innerPadding ->
        TaxDashboardScreen(
            state = state,
            onBack = onNavigateBack,
            onPreviousMonth = { viewModel.onEvent(TaxEvent.PreviousMonth) },
            onNextMonth = { viewModel.onEvent(TaxEvent.NextMonth) },
            onDownloadReport = { year ->
                viewModel.onEvent(TaxEvent.GetReportUrl(year))
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-13 — Stateless Monthly Tax Dashboard screen.
 *
 * Layout:
 * - Top bar with back arrow
 * - Month/year selector with prev/next arrows
 * - Summary card: estimated tax headline + breakdown
 * - Befordringsfradrag info banner
 * - "Download Annual Report [year]" Ghost button
 */
@Composable
fun TaxDashboardScreen(
    state: TaxUiState,
    onBack: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDownloadReport: (year: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.semantics { contentDescription = "Go back" },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = HopColors.authTextPrimary,
                )
            }
            Text(
                text = "Tax Dashboard",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = HopColors.authTextPrimary,
                ),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        when {
            state.isLoadingDashboard -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = HopColors.primaryLime)
                }
            }

            state.error != null && state.summary == null -> {
                val errorMessage = state.error
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = HopSpacing.md),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Could not load your tax data.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = HopColors.authTextPrimary,
                            fontWeight = FontWeight.Medium,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(HopSpacing.sm))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = HopColors.error,
                            textAlign = TextAlign.Center,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                DashboardContent(
                    state = state,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onDownloadReport = onDownloadReport,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Dashboard content ─────────────────────────────────────────────────────────

@Composable
private fun DashboardContent(
    state: TaxUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDownloadReport: (year: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HopSpacing.md),
    ) {
        Spacer(modifier = Modifier.height(HopSpacing.md))

        // ── Month / year selector ─────────────────────────────────────────────
        MonthYearSelector(
            year = state.selectedYear,
            month = state.selectedMonth,
            onPrevious = onPreviousMonth,
            onNext = onNextMonth,
        )

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        // ── Summary card ──────────────────────────────────────────────────────
        SummaryCard(summary = state.summary)

        Spacer(modifier = Modifier.height(HopSpacing.md))

        // ── Befordringsfradrag info banner ────────────────────────────────────
        BefordringsfradragBanner()

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Download Annual Report button ─────────────────────────────────────
        HopButton(
            text = "Download Annual Report ${state.selectedYear}",
            onClick = { onDownloadReport(state.selectedYear) },
            variant = HopButtonVariant.Ghost,
            isLoading = state.isLoadingReportUrl,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription =
                        "Download annual tax report for ${state.selectedYear}"
                },
        )

        Spacer(modifier = Modifier.height(HopSpacing.md))
    }
}

// ── Month / year selector ─────────────────────────────────────────────────────

@Composable
private fun MonthYearSelector(
    year: Int,
    month: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthLabel = if (month in 1..12) MONTH_NAMES[month - 1] else ""

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.semantics { contentDescription = "Previous month" },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = HopColors.authTextPrimary,
                modifier = Modifier.size(28.dp),
            )
        }

        Text(
            text = "$monthLabel $year",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = HopColors.authTextPrimary,
            ),
        )

        IconButton(
            onClick = onNext,
            modifier = Modifier.semantics { contentDescription = "Next month" },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = HopColors.authTextPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

// ── Summary card ──────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    summary: TaxMonthlySummary?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HopColors.surfaceElevated,
    ) {
        Column(modifier = Modifier.padding(HopSpacing.md)) {
            if (summary == null) {
                // Placeholder when data hasn't loaded yet but screen is visible
                Text(
                    text = "No data for this period",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = HopColors.authTextSecondary,
                    ),
                )
                return@Column
            }

            // Headline: estimated tax
            Text(
                text = "Your estimated tax this month is",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = HopColors.authTextSecondary,
                ),
            )
            Spacer(modifier = Modifier.height(HopSpacing.xs))
            Text(
                text = formatDkk(summary.estimatedTaxOere),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = HopMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = HopColors.success,
                    fontSize = 28.sp,
                ),
                modifier = Modifier.semantics {
                    contentDescription =
                        "Estimated tax: ${summary.estimatedTaxOere / 100} Danish Kroner"
                },
            )

            Spacer(modifier = Modifier.height(HopSpacing.lg))

            // Breakdown rows
            TaxBreakdownRow(
                label = "Gross earnings",
                amountOere = summary.grossOere,
            )
            HorizontalDivider(
                color = HopColors.surface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = HopSpacing.sm),
            )
            TaxBreakdownRow(
                label = "Befordringsfradrag",
                amountOere = summary.befordringsfradragOere,
                amountColor = HopColors.primaryGreen,
            )
            HorizontalDivider(
                color = HopColors.surface,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = HopSpacing.sm),
            )
            TaxBreakdownRow(
                label = "Taxable amount",
                amountOere = summary.taxableOere,
                labelColor = HopColors.authTextPrimary,
                amountColor = HopColors.authTextPrimary,
                isBold = true,
            )
        }
    }
}

// ── Tax breakdown row ─────────────────────────────────────────────────────────

@Composable
private fun TaxBreakdownRow(
    label: String,
    amountOere: Int,
    modifier: Modifier = Modifier,
    labelColor: androidx.compose.ui.graphics.Color = HopColors.authTextSecondary,
    amountColor: androidx.compose.ui.graphics.Color = HopColors.authTextPrimary,
    isBold: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = labelColor,
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            ),
        )
        Text(
            text = formatDkk(amountOere),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = HopMonoFontFamily,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = amountColor,
                fontSize = 15.sp,
            ),
            modifier = Modifier.semantics {
                contentDescription = "$label: ${amountOere / 100} Danish Kroner"
            },
        )
    }
}

// ── Befordringsfradrag banner ─────────────────────────────────────────────────

@Composable
private fun BefordringsfradragBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = HopColors.surfaceElevated,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(HopSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Based on DKK 2.28/km — the SKAT 2026 rate",
            style = MaterialTheme.typography.bodySmall.copy(
                color = HopColors.authTextSecondary,
            ),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TaxDashboardScreenPreview() {
    HopTheme {
        TaxDashboardScreen(
            state = TaxUiState(
                isLoading = false,
                selectedYear = 2026,
                selectedMonth = 4,
                summary = TaxMonthlySummary(
                    year = 2026,
                    month = 4,
                    grossOere = 1_320_000,
                    befordringsfradragOere = 456_000,
                    taxableOere = 864_000,
                    estimatedTaxOere = 259_200,
                ),
            ),
            onBack = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onDownloadReport = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Loading")
@Composable
private fun TaxDashboardScreenLoadingPreview() {
    HopTheme {
        TaxDashboardScreen(
            state = TaxUiState(isLoadingDashboard = true, selectedYear = 2026, selectedMonth = 4),
            onBack = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onDownloadReport = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Error")
@Composable
private fun TaxDashboardScreenErrorPreview() {
    HopTheme {
        TaxDashboardScreen(
            state = TaxUiState(
                selectedYear = 2026,
                selectedMonth = 4,
                error = "Network unavailable. Please try again.",
            ),
            onBack = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onDownloadReport = {},
        )
    }
}
