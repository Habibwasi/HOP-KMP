package com.example.hop.ui.screens.driver

import android.content.Intent
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-14 — Annual Tax Report Download Route.
 *
 * Loads the presigned report URL via [TaxViewModel]. Handles:
 *  - Loading spinner while URL is fetched
 *  - Error state with retry
 *  - Download PDF → [LocalUriHandler.openUri]
 *  - Share → Android share sheet via Intent.ACTION_SEND
 */
@Composable
fun TaxReportDownloadRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaxViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(TaxEvent.LoadTaxReport(currentYear))
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
    ) { innerPadding ->
        TaxReportDownloadScreen(
            state = state,
            onBack = onNavigateBack,
            onDownload = { url -> uriHandler.openUri(url) },
            onShare = { url ->
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, url)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share tax report"))
            },
            onRetry = { viewModel.onEvent(TaxEvent.Retry) },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-14 — Stateless Annual Tax Report Download screen.
 */
@Composable
fun TaxReportDownloadScreen(
    state: TaxUiState,
    onBack: () -> Unit,
    onDownload: (url: String) -> Unit,
    onShare: (url: String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
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
                    tint = HopColors.textPrimary,
                )
            }
            Text(
                text = "Annual Tax Report",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = HopColors.textPrimary,
                ),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = HopColors.primaryLime)
                }
            }

            state.error != null -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = HopSpacing.md),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Could not load your tax report.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = HopColors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(HopSpacing.sm))
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = HopColors.error,
                            textAlign = TextAlign.Center,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    HopButton(
                        text = "Retry",
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            state.isReady -> {
                val reportUrl = state.reportUrl!!

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = HopSpacing.md),
                ) {
                    Spacer(modifier = Modifier.height(HopSpacing.md))

                    // ── Year headline ─────────────────────────────────────────
                    Text(
                        text = "${state.year}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = HopColors.primaryLime,
                        ),
                    )

                    Spacer(modifier = Modifier.height(HopSpacing.xs))

                    Text(
                        text = "Your report is ready",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = HopColors.textSecondary,
                        ),
                    )

                    Spacer(modifier = Modifier.height(HopSpacing.xl))

                    // ── Report summary card ───────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = HopColors.surfaceElevated,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .padding(HopSpacing.md),
                    ) {
                        TaxSummaryRow(
                            label = "Total Earnings",
                            amountOere = state.totalEarningsOere,
                        )
                        HorizontalDivider(
                            color = HopColors.surface,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = HopSpacing.sm),
                        )
                        TaxSummaryRow(
                            label = "Total Deduction",
                            amountOere = state.totalDeductionOere,
                        )
                        HorizontalDivider(
                            color = HopColors.surface,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = HopSpacing.sm),
                        )
                        TaxSummaryRow(
                            label = "Total Taxable",
                            amountOere = state.totalTaxableOere,
                            labelColor = HopColors.textPrimary,
                            amountColor = HopColors.primaryLime,
                        )
                    }

                    Spacer(modifier = Modifier.height(HopSpacing.xl))
                }

                // ── Bottom actions ────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HopSpacing.md)
                        .padding(bottom = HopSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
                ) {
                    HopButton(
                        text = "Download PDF",
                        onClick = { onDownload(reportUrl) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Download tax report PDF" },
                    )
                    HopButton(
                        text = "Share",
                        onClick = { onShare(reportUrl) },
                        variant = HopButtonVariant.Ghost,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// ── Tax summary row ───────────────────────────────────────────────────────────

@Composable
private fun TaxSummaryRow(
    label: String,
    amountOere: Int,
    modifier: Modifier = Modifier,
    labelColor: androidx.compose.ui.graphics.Color = HopColors.textSecondary,
    amountColor: androidx.compose.ui.graphics.Color = HopColors.textPrimary,
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
            ),
        )
        Text(
            text = "DKK ${amountOere / 100}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = HopMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                fontSize = 16.sp,
            ),
            modifier = Modifier.semantics {
                contentDescription = "$label: ${amountOere / 100} Danish Kroner"
            },
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun TaxReportDownloadScreenReadyPreview() {
    HopTheme {
        TaxReportDownloadScreen(
            state = TaxUiState(
                isLoading = false,
                year = 2025,
                totalEarningsOere = 6_931_200,
                totalDeductionOere = 2_399_816,
                totalTaxableOere = 4_531_384,
                reportUrl = "https://s3.amazonaws.com/hop-reports/2025.pdf",
                error = null,
            ),
            onBack = {},
            onDownload = {},
            onShare = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, name = "Loading")
@Composable
private fun TaxReportDownloadScreenLoadingPreview() {
    HopTheme {
        TaxReportDownloadScreen(
            state = TaxUiState(isLoading = true),
            onBack = {},
            onDownload = {},
            onShare = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, name = "Error")
@Composable
private fun TaxReportDownloadScreenErrorPreview() {
    HopTheme {
        TaxReportDownloadScreen(
            state = TaxUiState(error = "Network unavailable. Please try again."),
            onBack = {},
            onDownload = {},
            onShare = {},
            onRetry = {},
        )
    }
}
