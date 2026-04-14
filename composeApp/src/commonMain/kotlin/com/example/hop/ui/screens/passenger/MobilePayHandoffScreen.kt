package com.example.hop.ui.screens.passenger

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.booking.BookingEffect
import com.example.hop.presentation.booking.BookingEvent
import com.example.hop.presentation.booking.BookingViewModel
import com.example.hop.presentation.booking.PaymentState
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

// ── Screen phase ──────────────────────────────────────────────────────────────

private enum class HandoffPhase {
    /** Launching the MobilePay app via deep link. Shows "Opening MobilePay…" spinner. */
    LaunchingApp,
    /** MobilePay app launched; awaiting the user's return. Shows "Waiting…" spinner. */
    Waiting,
    /** Payment failed or was cancelled. Shows error card + retry / go-back buttons. */
    Failed,
}

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * PA-05 — MobilePay Handoff Route.
 *
 * Responsibilities:
 *  1. Initialise [BookingViewModel] for this destination via [BookingEvent.BeginHandoff].
 *  2. Launch the MobilePay deep-link intent on entry.
 *  3. Fire [BookingEvent.ConfirmPaymentSuccess] when the Activity resumes (user returns
 *     from MobilePay). The ViewModel's idempotency guard prevents duplicate transitions.
 *  4. Collect [BookingEffect.NavigateToSuccess] → call [onNavigateToSuccess].
 *  5. Mirror [PaymentState.FAILED] → switch screen phase to Failed.
 */
@Composable
fun MobilePayHandoffRoute(
    bookingId: String,
    onNavigateToSuccess: (bookingId: String) -> Unit,
    onNavigateBackToBookingConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
    bookingViewModel: BookingViewModel = koinViewModel(),
) {
    val state by bookingViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var phase by remember { mutableStateOf(HandoffPhase.LaunchingApp) }
    var hasLaunchedMobilePay by remember { mutableStateOf(false) }

    // Step 1: Put the ViewModel into AWAITING_PAYMENT so ConfirmPaymentSuccess
    // passes the idempotency guard when the user returns from MobilePay.
    LaunchedEffect(bookingId) {
        bookingViewModel.onEvent(BookingEvent.BeginHandoff(bookingId))
    }

    // Step 2: Launch MobilePay once. Transitions phase to Waiting once the intent fires.
    // Deep-link scheme: mobilepay://merchant (MobilePay Denmark sandbox URL scheme).
    // The orderId query parameter links the in-app payment record to the MobilePay transaction.
    LaunchedEffect(Unit) {
        val mobilePayUri = Uri.parse("mobilepay://merchant?orderId=$bookingId")
        val intent = Intent(Intent.ACTION_VIEW, mobilePayUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        // Only start if MobilePay is installed; otherwise stay on waiting screen so the user
        // can retry. In production, fall back to the MobilePay web checkout URL.
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
        hasLaunchedMobilePay = true
        phase = HandoffPhase.Waiting
    }

    // Step 3: When the Activity resumes after the MobilePay app-switch, ask the
    // ViewModel to confirm the payment. The `hasLaunchedMobilePay` guard prevents
    // this from firing on the initial composition (Activity already RESUMED).
    val currentHasLaunched by rememberUpdatedState(hasLaunchedMobilePay)
    val currentBookingId by rememberUpdatedState(bookingId)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (currentHasLaunched) {
            bookingViewModel.onEvent(BookingEvent.ConfirmPaymentSuccess(currentBookingId))
        }
    }

    // Step 4: Mirror FAILED payment state to the screen phase.
    LaunchedEffect(state.paymentState) {
        if (state.paymentState == PaymentState.FAILED) {
            phase = HandoffPhase.Failed
        }
    }

    // Step 5: Collect one-shot navigation effects.
    LaunchedEffect(bookingViewModel) {
        bookingViewModel.effect.collectLatest { effect ->
            when (effect) {
                is BookingEffect.NavigateToSuccess ->
                    onNavigateToSuccess(effect.bookingId)
                is BookingEffect.NavigateToMobilePay -> Unit // already on this screen
                is BookingEffect.ShowSnackbar -> Unit        // not surfaced here
            }
        }
    }

    MobilePayHandoffScreen(
        phase = phase,
        onRetry = onNavigateBackToBookingConfirmation,
        onGoBack = onNavigateBackToBookingConfirmation,
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * PA-05 — MobilePay Handoff Screen (stateless renderer).
 *
 * Renders one of three phases:
 *  - [HandoffPhase.LaunchingApp] : "Opening MobilePay…" with spinner
 *  - [HandoffPhase.Waiting]      : "Waiting for payment confirmation…" with spinner
 *  - [HandoffPhase.Failed]       : Error card with retry and go-back buttons
 */
@Composable
private fun MobilePayHandoffScreen(
    phase: HandoffPhase,
    onRetry: () -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        when (phase) {
            HandoffPhase.LaunchingApp -> LaunchingContent()
            HandoffPhase.Waiting     -> WaitingContent()
            HandoffPhase.Failed      -> FailedContent(onRetry = onRetry, onGoBack = onGoBack)
        }
    }
}

// ── Phase composables ─────────────────────────────────────────────────────────

@Composable
private fun LaunchingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = HopSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HopSpacing.lg),
    ) {
        MobilePayLogoBadge()

        CircularProgressIndicator(
            color = HopColors.primaryLime,
            strokeWidth = 3.dp,
            modifier = Modifier
                .size(44.dp)
                .semantics { contentDescription = "Loading" },
        )

        Text(
            text = "Opening MobilePay...",
            color = HopColors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "You're being redirected to MobilePay\nto complete your payment.",
            color = HopColors.textSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WaitingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = HopSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HopSpacing.lg),
    ) {
        MobilePayLogoBadge()

        CircularProgressIndicator(
            color = HopColors.primaryLime,
            strokeWidth = 3.dp,
            modifier = Modifier
                .size(44.dp)
                .semantics { contentDescription = "Waiting for payment confirmation" },
        )

        Text(
            text = "Waiting for payment confirmation...",
            color = HopColors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Please complete the payment in MobilePay\nand return to this screen.",
            color = HopColors.textSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FailedContent(
    onRetry: () -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = HopSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HopSpacing.lg),
    ) {
        // Error icon badge
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(HopColors.error.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null,
                tint = HopColors.error,
                modifier = Modifier.size(40.dp),
            )
        }

        Text(
            text = "Payment failed",
            color = HopColors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Your payment could not be completed.\nNo money has been taken from your account.",
            color = HopColors.textSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(HopSpacing.sm))

        HopButton(
            text = "Try again",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        )

        HopButton(
            text = "Go back",
            onClick = onGoBack,
            variant = HopButtonVariant.Ghost,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── MobilePay logo badge ──────────────────────────────────────────────────────

/** Rounded blue badge that stands in for the MobilePay app icon. */
@Composable
private fun MobilePayLogoBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(88.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF5A78FF)),   // MobilePay brand blue
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "MP",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, name = "Launching App")
@Composable
private fun PreviewLaunching() {
    HopTheme {
        MobilePayHandoffScreen(
            phase = HandoffPhase.LaunchingApp,
            onRetry = {},
            onGoBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, name = "Waiting Confirmation")
@Composable
private fun PreviewWaiting() {
    HopTheme {
        MobilePayHandoffScreen(
            phase = HandoffPhase.Waiting,
            onRetry = {},
            onGoBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A, name = "Payment Failed")
@Composable
private fun PreviewFailed() {
    HopTheme {
        MobilePayHandoffScreen(
            phase = HandoffPhase.Failed,
            onRetry = {},
            onGoBack = {},
        )
    }
}
