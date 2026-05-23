package com.example.hop.ui.screens.shared

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.NotificationImportant
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.HopNotification
import com.example.hop.domain.model.NotificationType
import com.example.hop.presentation.notifications.NotificationsEffect
import com.example.hop.presentation.notifications.NotificationsEvent
import com.example.hop.presentation.notifications.NotificationsUiState
import com.example.hop.presentation.notifications.NotificationsViewModel
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * SH-05 — Notifications Route.
 *
 * Loads notifications on first composition, collects one-shot effects,
 * and delegates rendering to the stateless [NotificationsScreen].
 */
@Composable
fun NotificationsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToDriverSettlement: (String) -> Unit,
    onNavigateToPassengerSettlement: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(NotificationsEvent.Load)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is NotificationsEffect.NavigateToSearch -> onNavigateToSearch()
                is NotificationsEffect.NavigateToDriverSettlement ->
                    onNavigateToDriverSettlement(effect.bookingId)
                is NotificationsEffect.NavigateToPassengerSettlement ->
                    onNavigateToPassengerSettlement(effect.bookingId)
            }
        }
    }

    NotificationsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onEvent: (NotificationsEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .background(HopColors.background)
            .statusBarsPadding(),
        containerColor = HopColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        when {
            state.isLoading -> NotificationsLoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            state.notifications.isEmpty() -> NotificationsEmptyState(
                onGoToSearch = { onEvent(NotificationsEvent.GoToSearchTapped) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> NotificationsList(
                notifications = state.notifications,
                onNotificationClick = { notification ->
                    onEvent(NotificationsEvent.Tapped(notification))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

// ── Notifications List ────────────────────────────────────────────────────────

@Composable
private fun NotificationsList(
    notifications: List<HopNotification>,
    onNotificationClick: (HopNotification) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.navigationBarsPadding(),
    ) {
        items(
            items = notifications,
            key = { it.id },
        ) { notification ->
            NotificationRow(
                notification = notification,
                onClick = { onNotificationClick(notification) },
            )
        }
    }
}

// ── Notification Row ──────────────────────────────────────────────────────────

@Composable
private fun NotificationRow(
    notification: HopNotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowBackground = if (notification.isRead) HopColors.background else HopColors.authInputSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = rowBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm + 2.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        // Icon chip
        NotificationIconChip(type = notification.type)

        // Text block
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = notification.title,
                    color = HopColors.authTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                NotificationTimestamp(
                    isoTimestamp = notification.createdAt,
                    modifier = Modifier.padding(start = HopSpacing.xs),
                )
            }

            Text(
                text = notification.body,
                color = HopColors.authTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
            )
        }

        // Unread dot
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(HopColors.primaryLime),
            )
        }
    }
}

@Composable
private fun NotificationIconChip(
    type: NotificationType,
    modifier: Modifier = Modifier,
) {
    val (icon, tint) = iconForType(type)
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun iconForType(type: NotificationType): Pair<ImageVector, Color> = when (type) {
    NotificationType.BOOKING_CONFIRMED    -> Icons.Outlined.CheckCircle  to HopColors.success
    NotificationType.BOOKING_CANCELLED    -> Icons.Outlined.Cancel        to HopColors.error
    NotificationType.TRIP_REMINDER        -> Icons.Outlined.Timer         to HopColors.warning
    NotificationType.NEW_RATING           -> Icons.Outlined.Star          to HopColors.primaryLime
    NotificationType.THRESHOLD_MET        -> Icons.Outlined.TaskAlt       to HopColors.primaryGreen
    NotificationType.CHAT_MESSAGE         -> Icons.Outlined.Chat          to HopColors.authTextSecondary
    NotificationType.PAYMENT_MARKED_PAID  -> Icons.Outlined.Payments      to HopColors.primaryGreen
    NotificationType.PAYMENT_CONFIRMED    -> Icons.Outlined.CheckCircle   to HopColors.primaryGreen
    NotificationType.PAYMENT_DISPUTED     -> Icons.Outlined.Warning       to HopColors.error
    NotificationType.RIDE_AWAITING_PAYMENT -> Icons.Outlined.Timer        to HopColors.warning
    NotificationType.SEARCH_ALERT         -> Icons.Outlined.Search        to HopColors.primaryLime
    NotificationType.GENERAL              -> Icons.Outlined.NotificationImportant to HopColors.authTextSecondary
}

/**
 * Formats an ISO-8601 timestamp into a human-readable relative label.
 * Full date parsing is platform-specific; for MVP we show the raw time portion
 * (HH:mm) directly from the string to keep this in commonMain.
 */
@Composable
private fun NotificationTimestamp(
    isoTimestamp: String,
    modifier: Modifier = Modifier,
) {
    // Extract "HH:mm" from "2026-04-18T10:30:00Z" → "10:30"
    val display = isoTimestamp
        .substringAfter("T", missingDelimiterValue = isoTimestamp)
        .take(5)
        .ifBlank { isoTimestamp }

    Text(
        text = display,
        color = HopColors.authTextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        modifier = modifier,
    )
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun NotificationsEmptyState(
    onGoToSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = HopSpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(HopColors.background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = HopColors.primaryLime,
                modifier = Modifier.size(40.dp),
            )
        }

        Spacer(modifier = Modifier.height(HopSpacing.lg))

        Text(
            text = "You're all caught up",
            color = HopColors.authTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xs))

        Text(
            text = "No new notifications right now. Explore upcoming rides and book your next trip.",
            color = HopColors.authTextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(HopSpacing.xl))

        Button(
            onClick = onGoToSearch,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics { contentDescription = "Go to search" },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HopColors.primaryLime,
                contentColor = HopColors.background,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 4.dp),
            )
            Text(
                text = "Go to search",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }
}

// ── Loading State ─────────────────────────────────────────────────────────────

@Composable
private fun NotificationsLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = HopColors.primaryLime,
            modifier = Modifier.size(40.dp),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun NotificationsScreenWithItemsPreview() {
    HopTheme {
        NotificationsScreen(
            state = NotificationsUiState(
                notifications = listOf(
                    HopNotification(
                        id = "1",
                        type = NotificationType.BOOKING_CONFIRMED,
                        title = "Booking confirmed",
                        body = "Your seat on the Aarhus → Copenhagen trip is confirmed.",
                        createdAt = "2026-04-18T09:15:00Z",
                        isRead = false,
                    ),
                    HopNotification(
                        id = "2",
                        type = NotificationType.NEW_RATING,
                        title = "New rating",
                        body = "Lars rated you 5 stars as a passenger. Great ride!",
                        createdAt = "2026-04-17T18:45:00Z",
                        isRead = false,
                    ),
                    HopNotification(
                        id = "3",
                        type = NotificationType.TRIP_REMINDER,
                        title = "Trip in 2 hours",
                        body = "Don't forget your ride to Copenhagen departing at 11:00.",
                        createdAt = "2026-04-18T09:00:00Z",
                        isRead = true,
                    ),
                    HopNotification(
                        id = "4",
                        type = NotificationType.BOOKING_CANCELLED,
                        title = "Booking cancelled",
                        body = "The driver cancelled the Odense → Aarhus trip on 20 Apr.",
                        createdAt = "2026-04-16T14:20:00Z",
                        isRead = true,
                    ),
                    HopNotification(
                        id = "5",
                        type = NotificationType.THRESHOLD_MET,
                        title = "Trip is a go!",
                        body = "Your Model B trip has reached the minimum passenger threshold.",
                        createdAt = "2026-04-15T10:00:00Z",
                        isRead = true,
                    ),
                    HopNotification(
                        id = "6",
                        type = NotificationType.CHAT_MESSAGE,
                        title = "New message from Mia",
                        body = "\"Hey, I'll be at the pickup spot 5 minutes early.\"",
                        createdAt = "2026-04-18T08:30:00Z",
                        isRead = true,
                    ),
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun NotificationsEmptyStatePreview() {
    HopTheme {
        NotificationsScreen(
            state = NotificationsUiState(notifications = emptyList(), isLoading = false),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun NotificationsLoadingPreview() {
    HopTheme {
        NotificationsScreen(
            state = NotificationsUiState(isLoading = true),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}
