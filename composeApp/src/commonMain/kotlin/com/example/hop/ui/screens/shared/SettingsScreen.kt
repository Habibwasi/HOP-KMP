package com.example.hop.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.presentation.settings.SettingsEffect
import com.example.hop.presentation.settings.SettingsEvent
import com.example.hop.presentation.settings.SettingsUiState
import com.example.hop.presentation.settings.SettingsViewModel
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * SH-06 — Settings Route.
 *
 * Collects one-shot effects and delegates rendering to the stateless [SettingsScreen].
 * Logout is surfaced as [onLogout] so the NavGraph can dispatch to the root-scoped
 * AuthViewModel — the one [AuthEffectHandler] is actually observing.
 */
@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToHelpCentre: () -> Unit,
    onNavigateToContactUs: () -> Unit,
    onNavigateToTermsOfService: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
    appVersion: String = "1.0",
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettingsEffect.NavigateToEditProfile -> onNavigateToEditProfile()
                is SettingsEffect.NavigateToChangePassword -> onNavigateToChangePassword()
                is SettingsEffect.NavigateToHelpCentre -> onNavigateToHelpCentre()
                is SettingsEffect.NavigateToContactUs -> onNavigateToContactUs()
                is SettingsEffect.NavigateToTermsOfService -> onNavigateToTermsOfService()
                is SettingsEffect.NavigateToPrivacyPolicy -> onNavigateToPrivacyPolicy()
                is SettingsEffect.Logout -> onLogout()
            }
        }
    }

    SettingsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        appVersion = appVersion,
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    onNavigateBack: () -> Unit,
    appVersion: String = "1.0",
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.background(HopColors.background),
        containerColor = HopColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = HopColors.authTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back"
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = HopColors.authTextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HopColors.background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = HopSpacing.md)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
        ) {
            item { Spacer(modifier = Modifier.height(HopSpacing.sm)) }

            // ── Account ───────────────────────────────────────────────────────
            item {
                SettingsSection(title = "Account") {
                    SettingsLinkRow(
                        icon = Icons.Outlined.Person,
                        label = "Edit profile",
                        onClick = { onEvent(SettingsEvent.EditProfileTapped) },
                    )
                    SettingsDivider()
                    SettingsLinkRow(
                        icon = Icons.Outlined.Lock,
                        label = "Change password",
                        onClick = { onEvent(SettingsEvent.ChangePasswordTapped) },
                    )
                }
            }

            // ── Notifications ─────────────────────────────────────────────────
            item {
                SettingsSection(title = "Notifications") {
                    SettingsToggleRow(
                        icon = Icons.Outlined.Notifications,
                        label = "Push notifications",
                        checked = state.pushNotificationsEnabled,
                        onCheckedChange = { onEvent(SettingsEvent.TogglePushNotifications) },
                    )
                }
            }

            // ── Support ───────────────────────────────────────────────────────
            item {
                SettingsSection(title = "Support") {
                    SettingsLinkRow(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        label = "Help centre",
                        onClick = { onEvent(SettingsEvent.HelpCentreTapped) },
                    )
                    SettingsDivider()
                    SettingsLinkRow(
                        icon = Icons.Outlined.Mail,
                        label = "Contact us",
                        onClick = { onEvent(SettingsEvent.ContactUsTapped) },
                    )
                }
            }

            // ── Legal ─────────────────────────────────────────────────────────
            item {
                SettingsSection(title = "Legal") {
                    SettingsLinkRow(
                        icon = Icons.Outlined.Description,
                        label = "Terms of Service",
                        onClick = { onEvent(SettingsEvent.TermsOfServiceTapped) },
                    )
                    SettingsDivider()
                    SettingsLinkRow(
                        icon = Icons.Outlined.PrivacyTip,
                        label = "Privacy Policy",
                        onClick = { onEvent(SettingsEvent.PrivacyPolicyTapped) },
                    )
                }
            }

            // ── Log Out ───────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(HopSpacing.xs))
                Button(
                    onClick = { onEvent(SettingsEvent.LogoutTapped) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Log out of your account" },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HopColors.error.copy(alpha = 0.12f),
                        contentColor = HopColors.error,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = HopSpacing.sm)
                            .size(18.dp),
                    )
                    Text(
                        text = "Log Out",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }

            // ── App version ───────────────────────────────────────────────────
            item {
                Text(
                    text = "Version $appVersion",
                    color = HopColors.authTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = HopSpacing.xl)
                        .semantics { contentDescription = "App version $appVersion" },
                )
            }
        }
    }

    // ── Logout confirm dialog ─────────────────────────────────────────────────
    if (state.showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = { onEvent(SettingsEvent.LogoutConfirmed) },
            onDismiss = { onEvent(SettingsEvent.LogoutDismissed) },
        )
    }
}

// ── Leaf composables ──────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title.uppercase(),
            color = HopColors.authTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = HopSpacing.xs, bottom = HopSpacing.sm),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HopColors.authInputSurface),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsLinkRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md)
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.md),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HopColors.authTextSecondary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            color = HopColors.authTextPrimary,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = HopColors.authTextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCheckedChange)
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.md),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HopColors.authTextSecondary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            color = HopColors.authTextPrimary,
            fontSize = 15.sp,
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "$label toggle, ${if (checked) "on" else "off"}" },
        )
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = HopColors.background,
                checkedTrackColor = HopColors.primaryLime,
                uncheckedThumbColor = HopColors.authTextSecondary,
                uncheckedTrackColor = HopColors.authInputSurface,
                uncheckedBorderColor = HopColors.authTextSecondary,
            ),
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = HopColors.authInputBorder,
        thickness = 0.5.dp,
    )
}

@Composable
private fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HopColors.authInputSurface,
        title = {
            Text(
                text = "Log out?",
                color = HopColors.authTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
        },
        text = {
            Text(
                text = "You'll need to sign in again to use Hop.",
                color = HopColors.authTextSecondary,
                fontSize = 14.sp,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = HopColors.error),
            ) {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = HopColors.authTextSecondary),
            ) {
                Text(text = "Cancel")
            }
        },
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Settings — Default", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingsScreenPreview() {
    HopTheme {
        SettingsScreen(
            state = SettingsUiState(pushNotificationsEnabled = true),
            onEvent = {},
            onNavigateBack = {},
            appVersion = "1.0",
        )
    }
}

@Preview(name = "Settings — Notifications off", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingsScreenNotificationsOffPreview() {
    HopTheme {
        SettingsScreen(
            state = SettingsUiState(pushNotificationsEnabled = false),
            onEvent = {},
            onNavigateBack = {},
            appVersion = "1.0",
        )
    }
}

@Preview(name = "Settings — Logout dialog", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingsScreenLogoutDialogPreview() {
    HopTheme {
        SettingsScreen(
            state = SettingsUiState(showLogoutDialog = true),
            onEvent = {},
            onNavigateBack = {},
            appVersion = "1.0",
        )
    }
}
