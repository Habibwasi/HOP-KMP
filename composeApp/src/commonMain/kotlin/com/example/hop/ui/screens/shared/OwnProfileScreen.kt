package com.example.hop.ui.screens.shared

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.CarDetails
import com.example.hop.domain.model.RoleRated
import com.example.hop.domain.model.User
import com.example.hop.domain.model.UserReview
import com.example.hop.domain.model.UserRole
import com.example.hop.presentation.profile.OwnProfileEffect
import com.example.hop.presentation.profile.OwnProfileEvent
import com.example.hop.presentation.profile.OwnProfileUiState
import com.example.hop.presentation.profile.OwnProfileViewModel
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
 * SH-02 — Own Profile Route.
 *
 * Loads the current user's profile on first composition and delegates
 * rendering to the stateless [OwnProfileScreen].
 */
@Composable
fun OwnProfileRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPhoneVerification: () -> Unit,
    onNavigateToEditCar: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OwnProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEvent(OwnProfileEvent.Load)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OwnProfileEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is OwnProfileEffect.NavigateBack -> onNavigateBack()
                is OwnProfileEffect.NavigateToPhoneVerification -> onNavigateToPhoneVerification()
                is OwnProfileEffect.NavigateToEditCar -> onNavigateToEditCar()
            }
        }
    }

    OwnProfileScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun OwnProfileScreen(
    state: OwnProfileUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (OwnProfileEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = HopColors.background,
        topBar = {
            ProfileTopBar(
                title = "Profile",
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = onNavigateToSettings,
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
                AvatarEditSection(
                    initials = state.user?.fullName?.toInitials().orEmpty(),
                    isVerified = state.user?.phoneVerified == true,
                )
            }

            // ── Name ──────────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(HopSpacing.lg))
                if (state.isEditingName) {
                    InlineNameEditor(
                        draft = state.nameDraft,
                        isSaving = state.isSavingName,
                        onDraftChanged = { onEvent(OwnProfileEvent.NameDraftChanged(it)) },
                        onSave = { onEvent(OwnProfileEvent.SaveName) },
                        onCancel = { onEvent(OwnProfileEvent.CancelEditName) },
                    )
                } else {
                    NameRow(
                        name = state.user?.fullName.orEmpty(),
                        onEditClick = { onEvent(OwnProfileEvent.StartEditName) },
                    )
                }
            }

            // ── Contact info ──────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(HopSpacing.sm))
                InfoRow(
                    icon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = HopColors.authTextSecondary, modifier = Modifier.size(18.dp)) },
                    label = state.user?.email.orEmpty(),
                )
                Spacer(modifier = Modifier.height(HopSpacing.xs))
                PhoneRow(
                    phone = state.user?.phone,
                    isVerified = state.user?.phoneVerified == true,
                    onAddPhoneClick = { onEvent(OwnProfileEvent.AddPhoneTapped) },
                )
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
                    RatingsBlock(driverRating = driverRating, passengerRating = passengerRating)
                }
            }

            // ── Reviews ───────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(HopSpacing.lg))
                SectionDivider()
                Spacer(modifier = Modifier.height(HopSpacing.lg))
                SectionTitle("Recent reviews")
                Spacer(modifier = Modifier.height(HopSpacing.md))
            }
            if (state.reviews.isEmpty()) {
                item {
                    EmptyReviews()
                }
            } else {
                items(state.reviews.take(5), key = { it.id }) { review ->
                    ReviewCard(review = review)
                    Spacer(modifier = Modifier.height(HopSpacing.sm))
                }
            }

            // ── Car details (drivers only) ─────────────────────────────────────
            val isDriver = state.user?.roles?.contains(UserRole.DRIVER) == true
            if (isDriver) {
                item {
                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    SectionDivider()
                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    CarDetailsSection(
                        carDetails = state.carDetails,
                        onEditClick = { onEvent(OwnProfileEvent.EditCarTapped) },
                    )
                }

                // ── MobilePay number (drivers only) ──────────────────────────
                item {
                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    SectionDivider()
                    Spacer(modifier = Modifier.height(HopSpacing.lg))
                    if (state.isEditingMobilepay) {
                        InlineMobilepayEditor(
                            draft = state.mobilepayDraft,
                            isSaving = state.isSavingMobilepay,
                            onDraftChanged = { onEvent(OwnProfileEvent.MobilepayDraftChanged(it)) },
                            onSave = { onEvent(OwnProfileEvent.SaveMobilepay) },
                            onCancel = { onEvent(OwnProfileEvent.CancelEditMobilepay) },
                        )
                    } else {
                        MobilepayRow(
                            number = state.user?.mobilepayNumber,
                            onEditClick = { onEvent(OwnProfileEvent.StartEditMobilepay) },
                        )
                    }
                }
            }
        }
    }
}

// ── Avatar edit section ───────────────────────────────────────────────────────

@Composable
private fun AvatarEditSection(
    initials: String,
    isVerified: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = HopSpacing.xl),
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            HopAvatar(
                initials = initials,
                size = AvatarSize.Lg,
                isVerified = isVerified,
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(HopColors.primaryLime)
                    .semantics { contentDescription = "Edit profile photo" }
                    .clickable { /* post-MVP: photo upload */ },
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = HopColors.authTextPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ── Name row (view) ───────────────────────────────────────────────────────────

@Composable
private fun NameRow(
    name: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            color = HopColors.authTextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit name",
                tint = HopColors.primaryLime,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Inline name editor ────────────────────────────────────────────────────────

@Composable
private fun InlineNameEditor(
    draft: String,
    isSaving: Boolean,
    onDraftChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md),
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChanged,
            label = { Text("Full name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSave() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HopColors.primaryLime,
                unfocusedBorderColor = HopColors.authTextSecondary,
                focusedLabelColor = HopColors.primaryLime,
                unfocusedLabelColor = HopColors.authTextSecondary,
                focusedTextColor = HopColors.authTextPrimary,
                unfocusedTextColor = HopColors.authTextPrimary,
                cursorColor = HopColors.primaryLime,
                focusedContainerColor = HopColors.authInputSurface,
                unfocusedContainerColor = HopColors.authInputSurface,
            ),
        )
        Spacer(modifier = Modifier.height(HopSpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCancel, enabled = !isSaving) {
                Text("Cancel", color = HopColors.authTextSecondary)
            }
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            TextButton(onClick = onSave, enabled = !isSaving && draft.isNotBlank()) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = HopColors.primaryLime,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Save", color = HopColors.primaryLime, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Info row (email / phone) ──────────────────────────────────────────────────

@Composable
private fun InfoRow(
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
            color = HopColors.authTextSecondary,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) trailing()
    }
}

@Composable
private fun PhoneRow(
    phone: String?,
    isVerified: Boolean,
    onAddPhoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (phone == null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.xs)
                .clickable(onClick = onAddPhoneClick)
                .semantics { contentDescription = "Add phone number" },
        ) {
            Icon(
                Icons.Outlined.Phone,
                contentDescription = null,
                tint = HopColors.authTextSecondary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Text(
                text = "Add phone number",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.primaryLime,
                fontWeight = FontWeight.Medium,
            )
        }
    } else {
        InfoRow(
            icon = {
                Icon(
                    Icons.Outlined.Phone,
                    contentDescription = null,
                    tint = HopColors.authTextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            },
            label = phone,
            modifier = modifier,
            trailing = if (isVerified) {
                {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Phone verified",
                        tint = HopColors.success,
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                null
            },
        )
    }
}

// ── Ratings block ─────────────────────────────────────────────────────────────

@Composable
private fun RatingsBlock(
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
            RatingRow(label = "As driver", rating = driverRating.toFloat())
        }
        if (passengerRating != null) {
            RatingRow(label = "As passenger", rating = passengerRating.toFloat())
        }
    }
}

@Composable
private fun RatingRow(
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
            color = HopColors.authTextSecondary,
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
            color = HopColors.authTextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── Review card ───────────────────────────────────────────────────────────────

@Composable
internal fun ReviewCard(
    review: UserReview,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md)
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.authInputSurface)
            .padding(HopSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        HopAvatar(
            initials = review.raterName.toInitials(),
            size = AvatarSize.Sm,
        )
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = review.raterName,
                    style = MaterialTheme.typography.labelMedium,
                    color = HopColors.authTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                StarRating(
                    rating = review.stars.toFloat(),
                    starSize = 12.dp,
                )
            }
            val comment = review.comment
            if (!comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(HopSpacing.xs))
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = HopColors.authTextSecondary,
                )
            }
        }
    }
}

// ── Car details section (drivers only) ───────────────────────────────────────

@Composable
private fun CarDetailsSection(
    carDetails: CarDetails?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SectionTitle("Car details", modifier = Modifier.weight(1f))
            TextButton(onClick = onEditClick) {
                Text("Edit", color = HopColors.primaryLime, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(HopSpacing.sm))

        if (carDetails == null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HopColors.authInputSurface)
                    .padding(HopSpacing.md),
            ) {
                Icon(
                    Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = HopColors.authTextSecondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(HopSpacing.sm))
                Text(
                    text = "No car added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HopColors.authTextSecondary,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HopColors.authInputSurface)
                    .padding(HopSpacing.md),
                verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
            ) {
                CarDetailRow(label = "Make / Model", value = "${carDetails.make} ${carDetails.model}")
                CarDetailRow(label = "Year", value = carDetails.year.toString())
                CarDetailRow(label = "Colour", value = carDetails.colour)
                CarDetailRow(label = "Plate", value = carDetails.licensePlate)
                CarDetailRow(label = "Seats", value = carDetails.seatsAvailable.toString())
            }
        }
    }
}

@Composable
private fun CarDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.authTextSecondary,
            modifier = Modifier.width(100.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.authTextPrimary,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ── MobilePay row (view) ──────────────────────────────────────────────────────

@Composable
private fun MobilepayRow(
    number: String?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SectionTitle("MobilePay number", modifier = Modifier.weight(1f).padding(horizontal = 0.dp))
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit MobilePay number",
                    tint = HopColors.primaryLime,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(HopSpacing.sm))
        if (number.isNullOrBlank()) {
            Text(
                text = "Not set — passengers pay you via MobilePay",
                style = MaterialTheme.typography.bodySmall,
                color = HopColors.authTextSecondary,
            )
        } else {
            Text(
                text = number,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = HopColors.authTextPrimary,
            )
        }
    }
}

// ── Inline MobilePay editor ───────────────────────────────────────────────────

@Composable
private fun InlineMobilepayEditor(
    draft: String,
    isSaving: Boolean,
    onDraftChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HopSpacing.md),
    ) {
        SectionTitle("MobilePay number", modifier = Modifier.padding(horizontal = 0.dp))
        Spacer(modifier = Modifier.height(HopSpacing.sm))
        OutlinedTextField(
            value = draft,
            onValueChange = { if (it.length <= 8) onDraftChanged(it.filter { c -> c.isDigit() }) },
            label = { Text("8-digit number") },
            placeholder = { Text("e.g. 20123456") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving,
            keyboardOptions = KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSave() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HopColors.primaryLime,
                unfocusedBorderColor = HopColors.authTextSecondary,
                focusedLabelColor = HopColors.primaryLime,
                unfocusedLabelColor = HopColors.authTextSecondary,
                focusedTextColor = HopColors.authTextPrimary,
                unfocusedTextColor = HopColors.authTextPrimary,
                cursorColor = HopColors.primaryLime,
                focusedContainerColor = HopColors.authInputSurface,
                unfocusedContainerColor = HopColors.authInputSurface,
            ),
        )
        Text(
            text = "Passengers will send money here after the ride",
            style = MaterialTheme.typography.bodySmall,
            color = HopColors.authTextSecondary,
            modifier = Modifier.padding(top = HopSpacing.xs),
        )
        Spacer(modifier = Modifier.height(HopSpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCancel, enabled = !isSaving) {
                Text("Cancel", color = HopColors.authTextSecondary)
            }
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            TextButton(
                onClick = onSave,
                enabled = !isSaving && draft.length == 8,
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = HopColors.primaryLime,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Save", color = HopColors.primaryLime, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Shared sub-composables ────────────────────────────────────────────────────

@Composable
internal fun ProfileTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(HopColors.background)
            .statusBarsPadding()
            .padding(horizontal = HopSpacing.xs, vertical = HopSpacing.sm),
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = HopColors.authTextPrimary,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = HopColors.authTextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (onNavigateToSettings != null) {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = HopColors.authTextPrimary,
                )
            }
        }
    }
}

@Composable
internal fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = HopColors.authTextPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(horizontal = HopSpacing.md),
    )
}

@Composable
internal fun SectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = HopSpacing.md),
        color = HopColors.authInputSurface,
        thickness = 1.dp,
    )
}

@Composable
private fun EmptyReviews(modifier: Modifier = Modifier) {
    Text(
        text = "No reviews yet",
        style = MaterialTheme.typography.bodyMedium,
        color = HopColors.authTextSecondary,
        modifier = modifier.padding(horizontal = HopSpacing.md),
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

internal fun String.toInitials(): String {
    val parts = trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}"
        parts.size == 1 -> parts[0].take(2)
        else -> "?"
    }.uppercase()
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun OwnProfileScreenPreview() {
    HopTheme {
        OwnProfileScreen(
            state = OwnProfileUiState(
                user = User(
                    id = "u1",
                    fullName = "Emma Larsen",
                    email = "emma@example.com",
                    phone = "+45 20 12 34 56",
                    phoneVerified = true,
                    roles = listOf(UserRole.DRIVER, UserRole.PASSENGER),
                    isBanned = false,
                    ratingDriver = 4.7,
                    ratingPassenger = 4.9,
                ),
                reviews = listOf(
                    UserReview("r1", "Mikkel Sørensen", 5, "Very clean car, great conversation!", RoleRated.DRIVER),
                    UserReview("r2", "Anna Nielsen", 4, "Punctual and friendly.", RoleRated.DRIVER),
                ),
                carDetails = CarDetails(
                    make = "Volkswagen",
                    model = "Golf",
                    year = 2021,
                    licensePlate = "AB 12 345",
                    colour = "White",
                    seatsAvailable = 3,
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onNavigateBack = {},
            onNavigateToSettings = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun OwnProfileEditingNamePreview() {
    HopTheme {
        OwnProfileScreen(
            state = OwnProfileUiState(
                user = User(
                    id = "u1",
                    fullName = "Emma Larsen",
                    email = "emma@example.com",
                    phone = null,
                    phoneVerified = false,
                    roles = listOf(UserRole.PASSENGER),
                    isBanned = false,
                    ratingDriver = null,
                    ratingPassenger = 4.2,
                ),
                isEditingName = true,
                nameDraft = "Emma L.",
                reviews = emptyList(),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onNavigateBack = {},
            onNavigateToSettings = {},
        )
    }
}
