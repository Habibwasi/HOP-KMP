package com.example.hop.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.booking.BookingEffect
import com.example.hop.presentation.booking.BookingEvent
import com.example.hop.presentation.booking.BookingViewModel
import com.example.hop.ui.components.AvatarSize
import com.example.hop.ui.components.HopAvatar
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.StarRatingInput
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val COMMENT_MAX_CHARS = 280

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-12 — Rate Passenger Route.
 *
 * Mirror of PA-09 [RateDriverRoute] but for rating a passenger.
 * Uses [BookingViewModel.SubmitRating] and on success (NavigateToMyTripsPassenger
 * effect) navigates to DriverHome clearing the back stack.
 *
 * @param bookingId          Booking to attach the rating to.
 * @param passengerName      Passenger's display name shown in the headline.
 * @param passengerInitials  Initials for the avatar fallback (e.g. "MA").
 * @param onNavigateBack     Called when the user taps the back arrow.
 * @param onNavigateToDriverHome Called on successful rating submission.
 */
@Composable
fun RatePassengerRoute(
    bookingId: String,
    passengerName: String,
    passengerInitials: String,
    onNavigateBack: () -> Unit,
    onNavigateToDriverHome: () -> Unit,
    modifier: Modifier = Modifier,
    bookingViewModel: BookingViewModel = koinViewModel(),
) {
    val bookingState by bookingViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(bookingViewModel) {
        bookingViewModel.effect.collectLatest { effect ->
            when (effect) {
                // SubmitRating for a driver-side rating maps to DriverHome navigation.
                is BookingEffect.NavigateToMyTripsPassenger -> onNavigateToDriverHome()
                is BookingEffect.ShowSnackbar -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is BookingEffect.NavigateToSuccess -> Unit            // not reachable here
                is BookingEffect.NavigateToCancellationConfirmation -> Unit // not reachable here
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        RatePassengerScreen(
            passengerName = passengerName,
            passengerInitials = passengerInitials,
            isSubmitting = bookingState.isLoading,
            onBack = onNavigateBack,
            onSkip = onNavigateToDriverHome,
            onSubmit = { stars, comment ->
                bookingViewModel.onEvent(
                    BookingEvent.SubmitRating(
                        bookingId = bookingId,
                        stars = stars,
                        comment = comment.ifBlank { null },
                    )
                )
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

/**
 * Stateless DR-12 screen. All UI-local state (star selection, comment text)
 * lives here since it is ephemeral input — not business state.
 */
@Composable
fun RatePassengerScreen(
    passengerName: String,
    passengerInitials: String,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onSubmit: (stars: Int, comment: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var stars by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Top bar ────────────────────────────────────────────────────────
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
                text = "Rate your passenger",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = HopColors.authTextPrimary,
                ),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // ── Scrollable content ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(HopSpacing.xl))

            // Passenger avatar
            HopAvatar(
                initials = passengerInitials,
                size = AvatarSize.Lg,
            )

            Spacer(modifier = Modifier.height(HopSpacing.md))

            // Passenger name
            Text(
                text = passengerName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = HopColors.authTextPrimary,
                    fontSize = 18.sp,
                ),
            )

            Spacer(modifier = Modifier.height(HopSpacing.lg))

            // Headline
            Text(
                text = "How was $passengerName as a passenger?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = HopColors.authTextPrimary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(HopSpacing.xl))

            // Interactive star rating (44 dp tap targets via StarRatingInput internals)
            StarRatingInput(
                rating = stars,
                onRatingChange = { stars = it },
                modifier = Modifier.semantics {
                    contentDescription = "Rate passenger: $stars out of 5 stars selected"
                },
            )

            Spacer(modifier = Modifier.height(HopSpacing.xl))

            // Optional comment field
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (it.length <= COMMENT_MAX_CHARS) comment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = {
                        Text(
                            text = "Add a comment (optional)",
                            color = HopColors.authTextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HopColors.authTextPrimary,
                        unfocusedTextColor = HopColors.authTextPrimary,
                        focusedBorderColor = HopColors.primaryLime,
                        unfocusedBorderColor = HopColors.authInputBorder,
                        focusedContainerColor = HopColors.background,
                        unfocusedContainerColor = HopColors.background,
                        cursorColor = HopColors.primaryLime,
                    ),
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = HopColors.authTextPrimary,
                    ),
                )
            }

            // Character counter
            Text(
                text = "${comment.length} / $COMMENT_MAX_CHARS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = HopColors.authTextSecondary,
                    fontSize = 12.sp,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = HopSpacing.xs),
                textAlign = TextAlign.End,
            )

            Spacer(modifier = Modifier.height(HopSpacing.lg))
        }

        // ── Bottom action ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.md)
                .padding(bottom = HopSpacing.md),
        ) {
            HopButton(
                text = "Submit Rating",
                onClick = { onSubmit(stars, comment) },
                enabled = stars > 0,
                isLoading = isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Submit rating button" },
            )
            androidx.compose.material3.TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Maybe later",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = HopColors.authTextSecondary,
                    ),
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun RatePassengerScreenPreview() {
    HopTheme {
        RatePassengerScreen(
            passengerName = "Mette Andersen",
            passengerInitials = "MA",
            isSubmitting = false,
            onBack = {},
            onSkip = {},
            onSubmit = { _, _ -> },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Stars selected")
@Composable
private fun RatePassengerScreenFilledPreview() {
    HopTheme {
        RatePassengerScreen(
            passengerName = "Mette Andersen",
            passengerInitials = "MA",
            isSubmitting = false,
            onBack = {},
            onSkip = {},
            onSubmit = { _, _ -> },
        )
    }
}
