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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-04 — Review Pending Route.
 *
 * Terminal screen of the driver onboarding flow. No ViewModel interaction
 * required — the submission was completed in DR-03. Back-to-home is the
 * only action.
 */
@Composable
fun ReviewPendingRoute(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = HopColors.background,
    ) { innerPadding ->
        ReviewPendingScreen(
            onBackToHome = onNavigateToHome,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-04 — Review Pending Screen.
 *
 * Step 3 of 3 (completed) in the driver onboarding flow. Informs the driver
 * that their details are under review and will be processed within 24 hours.
 */
@Composable
fun ReviewPendingScreen(
    onBackToHome: () -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Become a Driver",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = HopColors.authTextPrimary,
            )
        }

        // ── Step indicator (all complete) ─────────────────────────────────────
        OnboardingStepIndicator(
            currentStep = 4, // beyond last step → all circles filled
            totalSteps = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.xl, vertical = HopSpacing.md),
        )

        HorizontalDivider(color = HopColors.authInputBorder)

        // ── Content ───────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = HopSpacing.md),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Success icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(HopColors.primaryLime.copy(alpha = 0.15f)),
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = HopColors.primaryLime,
                    modifier = Modifier.size(52.dp),
                )
            }

            Spacer(Modifier.height(HopSpacing.lg))

            Text(
                text = "Submitted!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = HopColors.authTextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(HopSpacing.sm))

            // Amber "Under Review" badge
            UnderReviewBadge()

            Spacer(Modifier.height(HopSpacing.lg))

            Text(
                text = "We'll review your details within 24 hours.",
                style = MaterialTheme.typography.bodyLarge,
                color = HopColors.authTextSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(HopSpacing.sm))

            Text(
                text = "You'll receive a notification once your driver account is approved. In the meantime, you can continue using Hop as a passenger.",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.authTextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        // ── Back to Home button ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HopColors.background)
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        ) {
            HopButton(
                text = "Back to Home",
                onClick = onBackToHome,
                variant = HopButtonVariant.Ghost,
            )
        }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun UnderReviewBadge(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.xs),
        modifier = modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2D2000))
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.AccessTime,
            contentDescription = null,
            tint = HopColors.warning,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Under Review",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = HopColors.warning,
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ReviewPendingScreenPreview() {
    HopTheme {
        ReviewPendingScreen(
            onBackToHome = {},
        )
    }
}
