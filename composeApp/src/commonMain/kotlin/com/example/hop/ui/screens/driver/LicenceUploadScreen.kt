package com.example.hop.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.driver.DriverEffect
import com.example.hop.presentation.driver.DriverEvent
import com.example.hop.presentation.driver.DriverUiState
import com.example.hop.presentation.driver.DriverViewModel
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.components.HopButtonVariant
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * DR-03 — Licence Upload Route.
 *
 * [onLaunchGallery] and [onLaunchCamera] are provided by the navigation layer
 * (Android-specific launchers). Each receives a callback `(uri: String?) -> Unit`
 * that the screen calls once the user selects or captures a photo.
 *
 * Default no-ops are provided so the route can be rendered in previews and
 * standalone without a real activity result launcher.
 */
@Composable
fun LicenceUploadRoute(
    onNavigateToReviewPending: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onLaunchGallery: ((onResult: (String?) -> Unit) -> Unit) = { _ -> },
    onLaunchCamera: ((onResult: (String?) -> Unit) -> Unit) = { _ -> },
    viewModel: DriverViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverEffect.NavigateToReviewPending -> onNavigateToReviewPending()
                is DriverEffect.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                else -> Unit
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = HopColors.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        LicenceUploadScreen(
            state = state,
            selectedPhotoUri = selectedPhotoUri,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            onGalleryClick = { onLaunchGallery { uri -> selectedPhotoUri = uri } },
            onCameraClick = { onLaunchCamera { uri -> selectedPhotoUri = uri } },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * DR-03 — Licence Upload Screen.
 *
 * Step 2 of 3 in the driver onboarding flow. Prompts the driver to upload
 * a photo of their driving licence via camera or gallery. Fires
 * [DriverEvent.SubmitLicence] with the photo URI on Continue.
 */
@Composable
fun LicenceUploadScreen(
    state: DriverUiState,
    selectedPhotoUri: String?,
    onEvent: (DriverEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canContinue = selectedPhotoUri != null && !state.isSubmittingOnboarding

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.sm, vertical = HopSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = HopColors.textPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(HopSpacing.sm))
            Text(
                text = "Become a Driver",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = HopColors.textPrimary,
            )
        }

        // ── Step indicator ────────────────────────────────────────────────────
        OnboardingStepIndicator(
            currentStep = 2,
            totalSteps = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.xl, vertical = HopSpacing.md),
        )

        HorizontalDivider(color = Color(0xFF2E2E2E))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
        ) {
            Text(
                text = "Upload Driving Licence",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = HopColors.textPrimary,
            )
            Text(
                text = "Take a clear photo of the front of your driving licence. Make sure all details are legible.",
                style = MaterialTheme.typography.bodyMedium,
                color = HopColors.textSecondary,
            )

            Spacer(Modifier.height(HopSpacing.sm))

            // ── Photo preview or placeholder ──────────────────────────────────
            if (selectedPhotoUri != null) {
                LicencePhotoPreview(
                    photoUri = selectedPhotoUri,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LicencePhotoPlaceholder(modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(HopSpacing.sm))

            // ── Upload action buttons ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HopSpacing.md),
            ) {
                HopButton(
                    text = "Camera",
                    onClick = onCameraClick,
                    variant = HopButtonVariant.Ghost,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = HopColors.primaryLime,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
                HopButton(
                    text = "Gallery",
                    onClick = onGalleryClick,
                    variant = HopButtonVariant.Ghost,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Collections,
                            contentDescription = null,
                            tint = HopColors.primaryLime,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            if (selectedPhotoUri != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = HopSpacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = HopColors.success,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(HopSpacing.xs))
                    Text(
                        text = "Photo selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = HopColors.success,
                    )
                }
            }
        }

        // ── Continue button ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HopColors.surface)
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        ) {
            HopButton(
                text = "Continue",
                enabled = canContinue,
                isLoading = state.isSubmittingOnboarding,
                onClick = {
                    selectedPhotoUri?.let { uri ->
                        onEvent(DriverEvent.SubmitLicence(uri))
                    }
                },
            )
        }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun LicencePhotoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF242424))
            .border(
                width = 1.dp,
                color = Color(0xFF3A3A3A),
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Outlined.DocumentScanner,
                contentDescription = null,
                tint = HopColors.textSecondary,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = "No photo selected",
                style = MaterialTheme.typography.bodySmall,
                color = HopColors.textSecondary,
            )
        }
    }
}

@Composable
private fun LicencePhotoPreview(
    photoUri: String,
    modifier: Modifier = Modifier,
) {
    // Photo is displayed as a tinted surface with a checkmark overlay.
    // The calling layer (androidMain) is responsible for rendering actual bitmap
    // using Coil's AsyncImage once the nav graph provides a real URI.
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E2A14))
            .border(
                width = 2.dp,
                color = HopColors.primaryLime,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = "Photo selected",
                tint = HopColors.primaryLime,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = "Licence photo ready",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = HopColors.primaryLime,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun LicenceUploadScreenEmptyPreview() {
    HopTheme {
        LicenceUploadScreen(
            state = DriverUiState(),
            selectedPhotoUri = null,
            onEvent = {},
            onNavigateBack = {},
            onGalleryClick = {},
            onCameraClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun LicenceUploadScreenWithPhotoPreview() {
    HopTheme {
        LicenceUploadScreen(
            state = DriverUiState(),
            selectedPhotoUri = "file:///sample/licence.jpg",
            onEvent = {},
            onNavigateBack = {},
            onGalleryClick = {},
            onCameraClick = {},
        )
    }
}
