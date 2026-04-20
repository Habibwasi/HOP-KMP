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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-05 — Post Trip Model Select Route.
 *
 * Presents Model A (Daily Commute) and Model B (One-Off Trip) selection cards.
 * Fires [DriverEvent.SelectModelA] or [DriverEvent.SelectModelB] on selection.
 */
@Composable
fun PostTripModelSelectRoute(
    onNavigateToModelA: () -> Unit,
    onNavigateToModelB: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverViewModel = koinViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverEffect.NavigateToModelAForm -> onNavigateToModelA()
                is DriverEffect.NavigateToModelBForm -> onNavigateToModelB()
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
        PostTripModelSelectScreen(
            onSelectModelA = { viewModel.onEvent(DriverEvent.SelectModelA) },
            onSelectModelB = { viewModel.onEvent(DriverEvent.SelectModelB) },
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-05 — Post Trip Model Select Screen.
 *
 * Stateless renderer. Two large selection cards guide the driver to choose
 * between a recurring daily commute (Model A) and a one-off trip (Model B).
 */
@Composable
fun PostTripModelSelectScreen(
    onSelectModelA: () -> Unit,
    onSelectModelB: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        PostTripSelectTopBar(onNavigateBack = onNavigateBack)

        // ── Scrollable body ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopSpacing.md)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
        ) {
            Spacer(modifier = Modifier.height(HopSpacing.sm))

            // Section subtitle
            Text(
                text = "How do you want to post this trip?",
                fontSize = 15.sp,
                color = HopColors.authTextSecondary,
                modifier = Modifier.padding(horizontal = HopSpacing.xs),
            )

            Spacer(modifier = Modifier.height(HopSpacing.xs))

            // ── Model A card ──────────────────────────────────────────────────
            TripModelCard(
                icon = Icons.Outlined.Repeat,
                title = "Daily Commute",
                modelLabel = "Model A",
                description = "You drive the same route on recurring days — Mon to Fri. " +
                    "The system creates a trip for each day in a 30-day rolling window. " +
                    "Passengers book and pay instantly. " +
                    "The trip runs regardless of how many seats fill — you absorb the occupancy risk.",
                bulletPoints = listOf(
                    "Confirmed bookings, immediate payment",
                    "Auto-extends every rolling 30 days",
                    "Cancel individual days up to 2 h before departure",
                ),
                onClick = onSelectModelA,
            )

            // ── Model B card ──────────────────────────────────────────────────
            TripModelCard(
                icon = Icons.Outlined.CalendarMonth,
                title = "One-Off Trip",
                modelLabel = "Model B",
                description = "You set a single date, route, and minimum passenger threshold. " +
                    "Payments are held until the threshold is met. " +
                    "If not enough passengers book by 6 h before departure, " +
                    "the trip cancels automatically and everyone is refunded.",
                bulletPoints = listOf(
                    "Trip only runs when enough passengers commit",
                    "Payments held — no charge unless confirmed",
                    "Great for long-distance or ad-hoc routes",
                ),
                onClick = onSelectModelB,
            )

            Spacer(modifier = Modifier.height(HopSpacing.lg))
        }
    }
}

// ── Subcomponents ─────────────────────────────────────────────────────────────

@Composable
private fun PostTripSelectTopBar(
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
                tint = HopColors.authTextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Text(
            text = "Post a Trip",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = HopColors.authTextPrimary,
        )
    }
}

@Composable
private fun TripModelCard(
    icon: ImageVector,
    title: String,
    modelLabel: String,
    description: String,
    bulletPoints: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.authInputSurface)
            .border(
                width = 1.dp,
                color = HopColors.authTextSecondary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(
                onClickLabel = "Select $title",
                role = androidx.compose.ui.semantics.Role.Button,
            ) { onClick() }
            .padding(HopSpacing.md),
    ) {
        Column {
            // Header row: icon + model badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(HopColors.primaryLime.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = HopColors.primaryLime,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(HopSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = HopColors.authTextPrimary,
                    )
                    Text(
                        text = modelLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = HopColors.primaryLime,
                        letterSpacing = 0.5.sp,
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = HopColors.authTextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.height(HopSpacing.md))

            // Description
            Text(
                text = description,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = HopColors.authTextSecondary,
            )

            if (bulletPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(HopSpacing.sm))
                bulletPoints.forEach { point ->
                    Row(
                        modifier = Modifier.padding(top = HopSpacing.xs),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(HopColors.primaryLime),
                        )
                        Spacer(modifier = Modifier.width(HopSpacing.sm))
                        Text(
                            text = point,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = HopColors.authTextSecondary,
                        )
                    }
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PostTripModelSelectScreenPreview() {
    HopTheme {
        PostTripModelSelectScreen(
            onSelectModelA = {},
            onSelectModelB = {},
            onNavigateBack = {},
        )
    }
}
