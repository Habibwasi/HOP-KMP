package com.example.hop.ui.screens.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.chat.ConnectionState
import com.example.hop.chat.Message
import com.example.hop.presentation.chat.ChatEffect
import com.example.hop.presentation.chat.ChatEvent
import com.example.hop.presentation.chat.ChatUiState
import com.example.hop.presentation.chat.ChatViewModel
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import com.example.hop.ui.theme.HopTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

/**
 * SH-04 — Chat Route.
 *
 * Connects to the chat session for [bookingId] on entry and disconnects when
 * the composable leaves composition. Delegates rendering to the stateless
 * [ChatScreen].
 *
 * @param bookingId  ID of the booking whose chat thread to open.
 * @param token      JWT access token for socket authentication.
 * @param currentUserId  Authenticated user's ID — used to distinguish own messages.
 * @param onNavigateBack  Called when the user presses Back.
 */
@Composable
fun ChatRoute(
    bookingId: String,
    token: String,
    currentUserId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Connect once when the Route enters composition
    LaunchedEffect(bookingId, token) {
        viewModel.onEvent(ChatEvent.Connect(bookingId = bookingId, token = token))
    }

    // One-shot effects
    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ChatEffect.ShowError -> scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
                // ScrollToBottom is handled inside ChatScreen via the list state
                is ChatEffect.ScrollToBottom -> Unit
            }
        }
    }

    ChatScreen(
        state = state,
        currentUserId = currentUserId,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ChatScreen(
    state: ChatUiState,
    currentUserId: String,
    snackbarHostState: SnackbarHostState,
    onEvent: (ChatEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to the last message whenever the list grows
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(HopColors.surface),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = HopColors.surface,
        topBar = {
            ChatTopBar(
                connectionState = state.connectionState,
                onNavigateBack = onNavigateBack,
            )
        },
        bottomBar = {
            ChatInputRow(
                inputText = state.inputText,
                onInputChanged = { onEvent(ChatEvent.InputChanged(it)) },
                onSend = { onEvent(ChatEvent.SendMessage) },
                isConnected = state.connectionState is ConnectionState.Connected,
            )
        },
    ) { innerPadding ->
        if (state.messages.isEmpty()) {
            ChatEmptyHint(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = HopSpacing.md),
                verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
                reverseLayout = false,
            ) {
                item { Spacer(Modifier.size(HopSpacing.sm)) }
                items(
                    items = state.messages,
                    key = { it.id },
                ) { message ->
                    MessageBubble(
                        message = message,
                        isOwn = message.senderId == currentUserId,
                    )
                }
                item { Spacer(Modifier.size(HopSpacing.sm)) }
            }
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun ChatTopBar(
    connectionState: ConnectionState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HopColors.surfaceElevated)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.sm, vertical = HopSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.semantics { contentDescription = "Navigate back" },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = HopColors.textPrimary,
                )
            }

            Spacer(Modifier.width(HopSpacing.xs))

            Text(
                text = "Chat",
                style = MaterialTheme.typography.titleMedium,
                color = HopColors.textPrimary,
                modifier = Modifier.weight(1f),
            )

            ConnectionIndicator(connectionState = connectionState)

            Spacer(Modifier.width(HopSpacing.md))
        }

        HorizontalDivider(
            color = HopColors.surfaceElevated,
            thickness = 1.dp,
        )
    }
}

// ── Connection indicator ──────────────────────────────────────────────────────

@Composable
private fun ConnectionIndicator(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
) {
    val (dotColor, label) = when (connectionState) {
        is ConnectionState.Connected    -> HopColors.success to "Connected"
        is ConnectionState.Connecting   -> HopColors.warning to "Reconnecting…"
        is ConnectionState.Disconnected -> HopColors.textSecondary to "Disconnected"
        is ConnectionState.Error        -> HopColors.error to "Error"
    }

    Row(
        modifier = modifier.semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        AnimatedVisibility(
            visible = connectionState !is ConnectionState.Connected,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = dotColor,
            )
        }
    }
}

// ── Message bubble ────────────────────────────────────────────────────────────

@Composable
private fun MessageBubble(
    message: Message,
    isOwn: Boolean,
    modifier: Modifier = Modifier,
) {
    val bubbleShape = if (isOwn) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 4.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp,
        )
    } else {
        RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp,
        )
    }

    val bubbleColor = if (isOwn) HopColors.primaryLime else HopColors.surfaceElevated
    val textColor   = if (isOwn) Color(0xFF1A1A1A) else HopColors.textPrimary
    val metaColor   = if (isOwn) Color(0x991A1A1A) else HopColors.textSecondary

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start,
        ) {
            if (!isOwn) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = HopColors.textSecondary,
                    modifier = Modifier.padding(
                        start = HopSpacing.xs,
                        bottom = 2.dp,
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleColor)
                    .padding(
                        horizontal = HopSpacing.md,
                        vertical = HopSpacing.sm,
                    ),
            ) {
                Text(
                    text = message.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                )
            }

            Text(
                text = formatTimestamp(message.timestampMs),
                style = MaterialTheme.typography.labelSmall,
                color = metaColor,
                modifier = Modifier.padding(
                    top = 2.dp,
                    start = HopSpacing.xs,
                    end = HopSpacing.xs,
                ),
            )
        }
    }
}

/**
 * Formats an epoch-ms timestamp to a short `HH:mm` time string.
 * KMP-compatible: uses epoch arithmetic only, no java.time or NSDate.
 */
private fun formatTimestamp(epochMs: Long): String {
    val totalSeconds = epochMs / 1000
    val totalMinutes = totalSeconds / 60
    val totalHours   = totalMinutes / 60
    val hours        = totalHours % 24
    val minutes      = totalMinutes % 60
    return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
}

// ── Input row ─────────────────────────────────────────────────────────────────

@Composable
private fun ChatInputRow(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HopColors.surfaceElevated)
            .navigationBarsPadding()
            .imePadding(),
    ) {
        HorizontalDivider(color = Color(0xFF2E2E2E), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        ) {
            TextField(
                value = inputText,
                onValueChange = onInputChanged,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = {
                    Text(
                        text = if (isConnected) "Type a message…" else "Connecting…",
                        color = HopColors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                maxLines = 4,
                enabled = isConnected,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = HopColors.textPrimary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color(0xFF2E2E2E),
                    unfocusedContainerColor = Color(0xFF2E2E2E),
                    disabledContainerColor  = Color(0xFF252525),
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor  = Color.Transparent,
                    cursorColor             = HopColors.primaryLime,
                    focusedTextColor        = HopColors.textPrimary,
                    unfocusedTextColor      = HopColors.textPrimary,
                ),
            )

            val sendEnabled = isConnected && inputText.isNotBlank()
            IconButton(
                onClick = onSend,
                enabled = sendEnabled,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (sendEnabled) HopColors.primaryLime else Color(0xFF2E2E2E),
                    )
                    .semantics { contentDescription = "Send message" },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = if (sendEnabled) Color(0xFF1A1A1A) else HopColors.textSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

// ── Empty hint ────────────────────────────────────────────────────────────────

@Composable
private fun ChatEmptyHint(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No messages yet.\nSay hello! 👋",
            style = MaterialTheme.typography.bodyMedium,
            color = HopColors.textSecondary,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewMessages = listOf(
    Message(
        id = "1",
        bookingId = "b1",
        senderId = "other-user",
        senderName = "Lars",
        body = "Hey, I'm on my way! Should be there in 5 minutes.",
        timestampMs = 1_713_000_000_000L,
    ),
    Message(
        id = "2",
        bookingId = "b1",
        senderId = "me",
        senderName = "Me",
        body = "Great, I'm waiting outside the main entrance 👍",
        timestampMs = 1_713_000_060_000L,
    ),
    Message(
        id = "3",
        bookingId = "b1",
        senderId = "other-user",
        senderName = "Lars",
        body = "Perfect, see you soon!",
        timestampMs = 1_713_000_120_000L,
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ChatScreenPreview() {
    HopTheme {
        ChatScreen(
            state = ChatUiState(
                messages = previewMessages,
                inputText = "On my way!",
                connectionState = ConnectionState.Connected,
            ),
            currentUserId = "me",
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ChatScreenReconnectingPreview() {
    HopTheme {
        ChatScreen(
            state = ChatUiState(
                messages = previewMessages,
                inputText = "",
                connectionState = ConnectionState.Connecting,
            ),
            currentUserId = "me",
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ChatScreenEmptyPreview() {
    HopTheme {
        ChatScreen(
            state = ChatUiState(connectionState = ConnectionState.Connected),
            currentUserId = "me",
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onNavigateBack = {},
        )
    }
}
