package com.example.hop.ui.screens.shared

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.RoleRated
import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserReview
import com.example.hop.domain.model.UserRole
import com.example.hop.presentation.profile.OtherProfileEffect
import com.example.hop.presentation.profile.OtherProfileEvent
import com.example.hop.presentation.profile.OtherProfileUiState
import com.example.hop.presentation.profile.OtherProfileViewModel
import com.example.hop.ui.components.AvatarSize
import com.example.hop.ui.components.HopAvatar
import com.example.hop.ui.components.StarRating
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * SH-03 — Other Profile Route.
 *
 * Loads the profile for [userId] on first composition and delegates
 * rendering to the stateless [OtherProfileScreen].
 */
@Composable
fun OtherProfileRoute(
    userId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtherProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.onEvent(OtherProfileEvent.Load(userId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OtherProfileEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is OtherProfileEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    OtherProfileScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun OtherProfileScreen(
    state: OtherProfileUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (OtherProfileEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = HopColors.surface,
        topBar = {
            ProfileTopBar(
                title = state.user?.fullName ?: "Profile",
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        if (state.isLoading && state.user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = HopColors.primaryLime)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = HopSpacing.xl),
        ) {
            // ── Avatar ────────────────────────────────────────────────────────
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = HopSpacing.xl),
                ) {
                    HopAvatar(
                        initials = state.user?.fullName?.toInitials().orEmpty(),
                        size = AvatarSize.Lg,
                        isVerified = state.user?.phoneVerified == true,
                    )
                }
            }

            // ── Name (read-only) ──────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(HopSpacing.lg))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = state.user?.fullName.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = HopColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // ── Contact info (read-only) ──────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(HopSpacing.sm))
                ReadOnlyInfoRow(
                    icon = {
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            tint = HopColors.textSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    label = state.user?.email.orEmpty(),
                )
                if (state.user?.phone != null) {
                    Spacer(modifier = Modifier.height(HopSpacing.xs))
                    ReadOnlyInfoRow(
                        icon = {
                            Icon(
                                Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = HopColors.textSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        label = state.user.phone,
                        trailing = if (state.user.phoneVerified) {
                            {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Phone verified",
                                    tint = HopColors.success,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else null,
                    )
                }
            }

            // ── Ratings ───────────────────────────────────────────────────────
            val driverRating = state.user?.ratingDriver
            val passengerRating = state.user?.ratingPassenger
            if (driverRating != null || passengerRating != null) {
                item {
                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    SectionDivider()
                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    SectionTitle("Ratings")
                    Spacer(modifier = Modifier.height(HopSpacing.md))
                    OtherRatingsBlock(driverRating = driverRating, passengerRating = passengerRating)
                }
            }

            // ── Reviews ───────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(HopSpacing.lg))
                SectionDivider()
                Spacer(modifier = Modifier.height(HopSpacing.lg))
                SectionTitle("Reviews")
                Spacer(modifier = Modifier.height(HopSpacing.md))
            }
            if (state.reviews.isEmpty()) {
                item {
                    Text(
                        text = "No reviews yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HopColors.textSecondary,
                        modifier = Modifier.padding(horizontal = HopSpacing.md),
                    )
                }
            } else {
                items(state.reviews.take(5), key = { it.id }) { review ->
                    ReviewCard(review = review)
                    Spacer(modifier = Modifier.height(HopSpacing.sm))
                }
            }

            // ── Car details (read-only, drivers only) ──────────────────────────
            val isDriver = state.user?.roles?.contains(UserRole.DRIVER) == true
            if (isDriver && state.carDetails != null) {
                item {
                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    SectionDivider()
                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    SectionTitle("Car details")
                    Spacer(modifier = Modifier.height(HopSpacing.md))
                    ReadOnlyCarDetails(carDetails = state.carDetails)
                }
            }

            // ── Report user ───────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(HopSpacing.xxl))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = { onEvent(OtherProfileEvent.ShowReportDialog) }) {
                        Text(
                            text = "Report user",
                            color = HopColors.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        // ── Report dialog ─────────────────────────────────────────────────────
        if (state.isReportDialogVisible) {
            ReportUserDialog(
                isSubmitting = state.isSubmittingReport,
                onDismiss = { onEvent(OtherProfileEvent.DismissReportDialog) },
                onSubmit = { reason -> onEvent(OtherProfileEvent.SubmitReport(reason)) },
            )
        }
    }
}

// ── Read-only info row ────────────────────────────────────────────────────────

@Composable
private fun ReadOnlyInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.xs),
    ) {
        icon()
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) trailing()
    }
}

// ── Ratings block (read-only) ─────────────────────────────────────────────────

@Composable
private fun OtherRatingsBlock(
    driverRating: Double?,
    passengerRating: Double?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        if (driverRating != null) {
            OtherRatingRow(label = "As driver", rating = driverRating.toFloat())
        }
        if (passengerRating != null) {
            OtherRatingRow(label = "As passenger", rating = passengerRating.toFloat())
        }
    }
}

@Composable
private fun OtherRatingRow(
    label: String,
    rating: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.textSecondary,
            modifier = Modifier.width(100.dp),
        )
        StarRating(
            rating = rating,
            starSize = 14.dp,
        )
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Read-only car details ─────────────────────────────────────────────────────

@Composable
private fun ReadOnlyCarDetails(
    carDetails: CarDetails,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md)
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.surfaceElevated)
            .padding(HopSpacing.md),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.DirectionsCar,
                contentDescription = null,
                tint = HopColors.textSecondary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = "${carDetails.make} ${carDetails.model} (${carDetails.year})",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.textPrimary,
                fontWeight = FontWeight.Medium,
            )
        }
        ReadOnlyCarDetailRow(label = "Colour", value = carDetails.colour)
        ReadOnlyCarDetailRow(label = "Plate", value = carDetails.licensePlate)
        ReadOnlyCarDetailRow(label = "Seats", value = carDetails.seatsAvailable.toString())
    }
}

@Composable
private fun ReadOnlyCarDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.textSecondary,
            modifier = Modifier.width(80.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.textPrimary,
        )
    }
}

// ── Report user dialog ────────────────────────────────────────────────────────

@Composable
private fun ReportUserDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var reason by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HopColors.surfaceElevated,
        title = {
            Text(
                text = "Report user",
                color = HopColors.textPrimary,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column {
                Text(
                    text = "Describe the issue. Our team will review your report within 24 hours.",
                    color = HopColors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(HopSpacing.md))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HopColors.primaryLime,
                        unfocusedBorderColor = HopColors.textSecondary,
                        focusedLabelColor = HopColors.primaryLime,
                        unfocusedLabelColor = HopColors.textSecondary,
                        focusedTextColor = HopColors.textPrimary,
                        unfocusedTextColor = HopColors.textPrimary,
                        cursorColor = HopColors.primaryLime,
                        focusedContainerColor = HopColors.surface,
                        unfocusedContainerColor = HopColors.surface,
                    ),
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel", color = HopColors.textSecondary)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(reason) },
                enabled = reason.isNotBlank() && !isSubmitting,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = HopColors.error,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Submit", color = HopColors.error, fontWeight = FontWeight.SemiBold)
                }
            }
        },
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun OtherProfileScreenPreview() {
    HopTheme {
        OtherProfileScreen(
            state = OtherProfileUiState(
                user = User(
                    id = "u2",
                    fullName = "Lars Hansen",
                    email = "lars@example.com",
                    phone = "+45 30 11 22 33",
                    phoneVerified = true,
                    roles = listOf(UserRole.DRIVER, UserRole.PASSENGER),
                    isBanned = false,
                    ratingDriver = 4.8,
                    ratingPassenger = 4.5,
                ),
                reviews = listOf(
                    UserReview("r1", "Emma Larsen", 5, "Super smooth ride, very punctual.", RoleRated.DRIVER),
                    UserReview("r2", "Tobias Berg", 4, "Comfortable car and good music.", RoleRated.DRIVER),
                ),
                carDetails = CarDetails(
                    make = "Tesla",
                    model = "Model 3",
                    year = 2022,
                    licensePlate = "XY 98 765",
                    colour = "Black",
                    seatsAvailable = 3,
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun OtherProfileReportDialogPreview() {
    HopTheme {
        OtherProfileScreen(
            state = OtherProfileUiState(
                user = User(
                    id = "u2",
                    fullName = "Lars Hansen",
                    email = "lars@example.com",
                    phone = null,
                    phoneVerified = false,
                    roles = listOf(UserRole.PASSENGER),
                    isBanned = false,
                    ratingDriver = null,
                    ratingPassenger = 3.2,
                ),
                reviews = emptyList(),
                isReportDialogVisible = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onNavigateBack = {},
        )
    }
}
