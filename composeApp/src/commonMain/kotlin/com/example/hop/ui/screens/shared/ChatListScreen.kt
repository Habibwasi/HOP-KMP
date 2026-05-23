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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.ChatThread
import com.example.hop.presentation.chatlist.ChatListEffect
import com.example.hop.presentation.chatlist.ChatListEvent
import com.example.hop.presentation.chatlist.ChatListUiState
import com.example.hop.presentation.chatlist.ChatListViewModel
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

@Composable
fun ChatListRoute(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (bookingId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatListEvent.Load)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ChatListEffect.NavigateToChat -> onNavigateToChat(effect.bookingId)
                is ChatListEffect.ShowError -> Unit // errors shown inline
            }
        }
    }

    ChatListScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ChatListScreen(
    state: ChatListUiState,
    onEvent: (ChatListEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.background),
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        ChatListTopBar(onNavigateBack = onNavigateBack)

        // ── Content ──────────────────────────────────────────────────────────
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HopColors.primaryLime)
                }
            }
            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Could not load chats: ${state.error}",
                        color = HopColors.authTextSecondary,
                    )
                }
            }
            state.threads.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No chats yet.\nBook a trip to start chatting! 💬",
                        color = HopColors.authTextSecondary,
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .navigationBarsPadding(),
                ) {
                    items(state.threads, key = { it.bookingId }) { thread ->
                        ChatThreadRow(
                            thread = thread,
                            onClick = { onEvent(ChatListEvent.OpenChat(thread.bookingId)) },
                        )
                        HorizontalDivider(
                            color = HopColors.authInputBorder,
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun ChatListTopBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HopColors.background)
            .statusBarsPadding()
            .padding(horizontal = HopSpacing.xs, vertical = HopSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.semantics { contentDescription = "Navigate back" },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = HopColors.authTextPrimary,
            )
        }

        Text(
            text = "My Chats",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                color = HopColors.authTextPrimary,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

// ── Thread row ────────────────────────────────────────────────────────────────

@Composable
private fun ChatThreadRow(
    thread: ChatThread,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = HopSpacing.md, vertical = HopSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(HopColors.primaryLime.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Chat,
                contentDescription = null,
                tint = HopColors.primaryLime,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(modifier = Modifier.width(HopSpacing.md))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = thread.otherPartyName,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = HopColors.authTextPrimary,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${thread.tripOrigin} → ${thread.tripDest}",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall.copy(
                    color = HopColors.authTextSecondary,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatChatDeparture(thread.departureAt),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                    color = HopColors.authTextSecondary,
                ),
            )
        }

        // Role badge
        Box(
            modifier = Modifier
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .background(
                    if (thread.myRole == "DRIVER") HopColors.primaryGreen.copy(alpha = 0.15f)
                    else HopColors.primaryLime.copy(alpha = 0.15f)
                )
                .padding(horizontal = HopSpacing.sm, vertical = 2.dp),
        ) {
            Text(
                text = if (thread.myRole == "DRIVER") "Driver" else "Passenger",
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                    color = if (thread.myRole == "DRIVER") HopColors.primaryGreen else HopColors.primaryLime,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatChatDeparture(iso: String): String {
    return try {
        val instant = Instant.parse(iso)
        val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val h = ldt.hour.toString().padStart(2, '0')
        val m = ldt.minute.toString().padStart(2, '0')
        "${ldt.dayOfMonth} ${ldt.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)} · $h:$m"
    } catch (_: Exception) {
        iso
    }
}
