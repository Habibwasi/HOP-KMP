package com.example.hop.ui.screens.auth

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

// ── Data ─────────────────────────────────────────────────────────────────────

private data class OnboardingSlide(
    val illustrationDescription: String,
    val headline: String,
    val subheadline: String,
)

private val slides = listOf(
    OnboardingSlide(
        illustrationDescription = "Two cars side by side",
        headline = "Fewer cars.\nBetter Journeys.",
        subheadline = "Denmark's carpooling platform.\nShare a ride, shrink your footprint",
    ),
    OnboardingSlide(
        illustrationDescription = "Leaf and road illustration",
        headline = "Every seat\nfilled matters.",
        subheadline = "One shared trip can cut CO₂\nemissions in half. Small change,\nbig difference.",
    ),
    OnboardingSlide(
        illustrationDescription = "Green journey illustration",
        headline = "Move together.\nLive lighter.",
        subheadline = "Join thousands of Danes choosing\nsmarter, greener travel \none ride at a time.",
        ),
)
// ── Screen ───────────────────────────────────────────────────────────────────

/**
 * ON-01 — Onboarding Screen.
 *
 * Three swipeable slides (HorizontalPager) introduce Hop's value proposition.
 * Bottom area is pinned: slide-dot indicators, "Get Started" (Primary) and
 * "Log In" (Ghost) buttons.
 *
 * Navigation is handled by the caller via [onNavigateToSignUp] and [onNavigateToLogin]
 * to keep this composable stateless and nav-framework-agnostic.
 */
@Composable
fun OnboardingScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    var chromeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { chromeVisible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background),
    ) {

        // ── Slides ───────────────────────────────────────────────────────

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            key = { index -> index },
        ) { page ->
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 8 })
                        .togetherWith(fadeOut(tween(200)))
                },
                label = "slideContent",
            ) { targetPage ->
                OnboardingSlide(slide = slides[targetPage])
            }
        }

        // ── Bottom chrome (dots + CTAs) ──────────────────────────────────

        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.lg)
                .navigationBarsPadding()
                .padding(bottom = HopSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
        ) {
            SlideDots(
                pageCount = slides.size,
                currentPage = pagerState.currentPage,
            )

            Spacer(modifier = Modifier.height(HopSpacing.sm))

            HopButton(
                text = "Get Started",
                onClick = onNavigateToSignUp,
                variant = HopButtonVariant.Primary,
            )

            HopButton(
                text = "Log In",
                onClick = onNavigateToLogin,
                variant = HopButtonVariant.Ghost,
            )
        }
        } // end AnimatedVisibility
    }
}

// ── Slide ─────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingSlide(
    slide: OnboardingSlide,
    modifier: Modifier = Modifier,
) {
    var illustrationVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { illustrationVisible = true }
    val illustrationScale by animateFloatAsState(
        targetValue = if (illustrationVisible) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "illustrationScale",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = HopSpacing.lg)
            // Reserve bottom space so content doesn't underlap the CTA chrome.
            // Approx: dots(12) + spacing(16) + button×2(52+52) + gaps(16+16) + nav-bar padding
            .padding(bottom = 240.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // ── Illustration placeholder ─────────────────────────────────────

        Box(
            modifier = Modifier
                .size(width = 280.dp, height = 200.dp)
                .graphicsLayer {
                    scaleX = illustrationScale
                    scaleY = illustrationScale
                    alpha = illustrationScale
                }
                .clip(RoundedCornerShape(16.dp))
                .background(HopColors.authInputSurface),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = slide.illustrationDescription,
                style = MaterialTheme.typography.bodySmall,
                color = HopColors.authTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(HopSpacing.md),
            )
        }

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        // ── Headline ─────────────────────────────────────────────────────

        Text(
            text = slide.headline,
            style = MaterialTheme.typography.headlineLarge,
            color = HopColors.authTextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(HopSpacing.sm))

        // ── Sub-headline ──────────────────────────────────────────────

        Text(
            text = slide.subheadline,
            style = MaterialTheme.typography.bodyLarge,
            color = HopColors.authTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Slide dots ────────────────────────────────────────────────────────────────

@Composable
private fun SlideDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Slide ${currentPage + 1} of $pageCount"
        },
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val dotWidth by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "dotWidth_$index",
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(dotWidth)
                    .clip(CircleShape)
                    .background(
                        color = if (isActive) HopColors.authAccent else HopColors.authTextSecondary.copy(alpha = 0.35f),
                    ),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Onboarding – Light", showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(
        onNavigateToSignUp = {},
        onNavigateToLogin = {},
    )
}

