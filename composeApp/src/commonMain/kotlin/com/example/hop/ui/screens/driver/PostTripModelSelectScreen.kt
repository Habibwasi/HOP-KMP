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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.CheckCircle
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
                text = "Choose the type of trip you want to offer.",
                fontSize = 15.sp,
                color = HopColors.authTextSecondary,
                modifier = Modifier.padding(horizontal = HopSpacing.xs),
            )

            Spacer(modifier = Modifier.height(HopSpacing.xs))

            // ── Model A card ──────────────────────────────────────────────────
            TripModelCard(
                icon = Icons.Outlined.Repeat,
                title = "Daily Commute",
                badge = "Model A",
                bullets = listOf(
                    "Same route, repeating weekly",
                    "Pick the days you drive",
                    "Passengers book per ride",
                    "Best for commuters",
                ),
                onClick = onSelectModelA,
            )

            // ── Model B card ──────────────────────────────────────────────────
            TripModelCard(
                icon = Icons.Outlined.CalendarMonth,
                title = "One-off Long Distance",
                badge = "Model B",
                bullets = listOf(
                    "Single trip on a chosen date",
                    "Set a minimum passengers threshold",
                    "Trip auto-cancels if not met",
                    "Best for inter-city journeys",
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
    badge: String,
    bullets: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = bullets.firstOrNull() ?: ""
    val checkmarks = bullets.drop(1)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.cardSurface)
            .border(
                width = 1.dp,
                color = HopColors.cardBorder,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(
                onClickLabel = "Select $title",
                role = Role.Button,
            ) { onClick() }
            .padding(HopSpacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(HopSpacing.sm)) {
            // ── Header: icon + title + badge ──────────────────────────────
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Icon tile
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(HopColors.primaryLime.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = HopColors.authTextPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HopColors.authTextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = badge,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HopColors.authTextPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(HopColors.primaryLime)
                                .padding(horizontal = HopSpacing.xs, vertical = 2.dp),
                        )
                    }
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = HopColors.authTextSecondary,
                    )
                }
            }

            // ── Divider ───────────────────────────────────────────────────
            HorizontalDivider(color = HopColors.cardBorder)

            // ── Checkmark bullets ─────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                checkmarks.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(HopSpacing.xs),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = HopColors.primaryGreen,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .size(15.dp),
                        )
                        Text(
                            text = item,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = HopColors.authTextPrimary,
                        )
                    }
                }
            }

            // ── Choose → footer ───────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Choose",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HopColors.authTextPrimary,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = HopColors.authTextPrimary,
                    modifier = Modifier.size(14.dp),
                )
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
