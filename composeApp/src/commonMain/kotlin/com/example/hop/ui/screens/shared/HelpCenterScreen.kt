package com.example.hop.ui.screens.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hop.domain.model.FaqItem
import com.example.hop.presentation.helpcenter.HelpCenterEffect
import com.example.hop.presentation.helpcenter.HelpCenterEvent
import com.example.hop.presentation.helpcenter.HelpCenterTab
import com.example.hop.presentation.helpcenter.HelpCenterUiState
import com.example.hop.presentation.helpcenter.HelpCenterViewModel
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

// ── Route ─────────────────────────────────────────────────────────────────────

@Composable
fun HelpCenterRoute(
    onNavigateBack: () -> Unit,
    viewModel: HelpCenterViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HelpCenterEffect.NavigateBack -> onNavigateBack()
                is HelpCenterEffect.OpenUri -> uriHandler.openUri(effect.uri)
            }
        }
    }

    HelpCenterScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    state: HelpCenterUiState,
    onEvent: (HelpCenterEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.background(HopColors.surface),
        containerColor = HopColors.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Help Centre",
                        color = HopColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(HelpCenterEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = HopColors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HopColors.surface,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
        ) {
            // ── Search bar ────────────────────────────────────────────────

            item {
                HelpSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(HelpCenterEvent.SearchQueryChanged(it)) },
                    modifier = Modifier.padding(horizontal = HopSpacing.md, vertical = HopSpacing.sm),
                )
            }

            // ── How It Works (only shown when not searching) ──────────────

            if (state.searchQuery.isBlank()) {
                item {
                    SectionLabel("How It Works")
                }
                item {
                    HowItWorksTabs(
                        activeTab = state.activeTab,
                        onTabSelected = { onEvent(HelpCenterEvent.TabSelected(it)) },
                        modifier = Modifier.padding(horizontal = HopSpacing.md),
                    )
                }
                item { Spacer(modifier = Modifier.height(HopSpacing.sm)) }
                item {
                    val steps = if (state.activeTab == HelpCenterTab.Passenger) passengerSteps else driverSteps
                    HowItWorksSteps(
                        steps = steps,
                        modifier = Modifier.padding(horizontal = HopSpacing.md),
                    )
                }
                item { Spacer(modifier = Modifier.height(HopSpacing.lg)) }
            }

            // ── FAQ ───────────────────────────────────────────────────────

            item { SectionLabel("Frequently Asked Questions") }

            when {
                state.isLoading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(HopSpacing.xl),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = HopColors.primaryLime,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                state.error != null -> item {
                    ErrorRetry(
                        message = state.error ?: "",
                        onRetry = { onEvent(HelpCenterEvent.RetryLoad) },
                        modifier = Modifier.padding(HopSpacing.md),
                    )
                }

                state.filteredFaqs.isEmpty() -> item {
                    Text(
                        text = "No results for \"${state.searchQuery}\"",
                        color = HopColors.textSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = HopSpacing.md, vertical = HopSpacing.lg),
                    )
                }

                else -> {
                    state.faqsByTopic.forEach { (topic, faqs) ->
                        item(key = "topic_$topic") {
                            FaqTopicHeader(topic = topic)
                        }
                        items(faqs, key = { it.id }) { faq ->
                            FaqRow(
                                faq = faq,
                                expanded = state.expandedFaqId == faq.id,
                                onToggle = { onEvent(HelpCenterEvent.FaqToggled(faq.id)) },
                                modifier = Modifier.padding(horizontal = HopSpacing.md),
                            )
                        }
                        item(key = "topic_spacer_$topic") {
                            Spacer(modifier = Modifier.height(HopSpacing.sm))
                        }
                    }
                }
            }

            // ── Support actions ───────────────────────────────────────────

            item { Spacer(modifier = Modifier.height(HopSpacing.lg)) }
            item { SectionLabel("Support") }
            item {
                SupportActionRow(
                    icon = Icons.Outlined.BugReport,
                    label = "Report a problem",
                    description = "Tell us about a bug or unexpected behaviour",
                    onClick = { onEvent(HelpCenterEvent.ReportProblemTapped) },
                )
            }
            item {
                HorizontalDivider(
                    color = HopColors.surfaceElevated,
                    modifier = Modifier.padding(horizontal = HopSpacing.md),
                )
            }
            item {
                SupportActionRow(
                    icon = Icons.Outlined.Mail,
                    label = "Contact us",
                    description = "support@ridly.dk",
                    onClick = { onEvent(HelpCenterEvent.ContactUsTapped) },
                )
            }

            item { Spacer(modifier = Modifier.height(HopSpacing.xxl)) }
        }
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun HelpSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.surfaceElevated)
            .padding(horizontal = HopSpacing.md, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = HopColors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(color = HopColors.textPrimary, fontSize = 15.sp),
            cursorBrush = SolidColor(HopColors.primaryLime),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search help articles…",
                        color = HopColors.textSecondary,
                        fontSize = 15.sp,
                    )
                }
                inner()
            },
        )
    }
}

// ── How It Works tabs ─────────────────────────────────────────────────────────

@Composable
private fun HowItWorksTabs(
    activeTab: HelpCenterTab,
    onTabSelected: (HelpCenterTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HopColors.surfaceElevated),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        HowItWorksTab(
            icon = Icons.Outlined.Person,
            label = "Passenger",
            selected = activeTab == HelpCenterTab.Passenger,
            onClick = { onTabSelected(HelpCenterTab.Passenger) },
            modifier = Modifier.weight(1f),
        )
        HowItWorksTab(
            icon = Icons.Outlined.DirectionsCar,
            label = "Driver",
            selected = activeTab == HelpCenterTab.Driver,
            onClick = { onTabSelected(HelpCenterTab.Driver) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HowItWorksTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (selected) HopColors.primaryLime else Color.Transparent
    val textColor = if (selected) HopColors.surface else HopColors.textSecondary

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = HopSpacing.md),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    }
}

// ── How It Works step cards ───────────────────────────────────────────────────

private data class HowItWorksStep(val number: Int, val title: String, val description: String)

private val passengerSteps = listOf(
    HowItWorksStep(1, "Search for a trip", "Enter your origin, destination, and travel date to see available rides."),
    HowItWorksStep(2, "Pick your seat", "Browse trips, check the driver's profile and ratings, then book a seat."),
    HowItWorksStep(3, "Ride & settle", "Meet the driver at the pickup point. After the trip, pay via MobilePay."),
    HowItWorksStep(4, "Leave a rating", "Rate your driver so others can trust the community too."),
)

private val driverSteps = listOf(
    HowItWorksStep(1, "Set up your profile", "Add your car details and MobilePay number to get started."),
    HowItWorksStep(2, "Post a trip", "Enter your route, departure time, seats, and price before you leave."),
    HowItWorksStep(3, "Accept bookings", "Review passenger profiles and confirm the riders you are happy to take."),
    HowItWorksStep(4, "Collect payment", "Send a MobilePay request after the trip and get paid directly."),
)

@Composable
private fun HowItWorksSteps(
    steps: List<HowItWorksStep>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        steps.forEach { step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HopColors.surfaceElevated)
                    .padding(HopSpacing.md),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HopColors.primaryLime),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = step.number.toString(),
                        color = HopColors.surface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                }
                Spacer(modifier = Modifier.width(HopSpacing.md))
                Column {
                    Text(
                        text = step.title,
                        color = HopColors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = step.description,
                        color = HopColors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
            }
        }
    }
}

// ── FAQ components ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        color = HopColors.textSecondary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(
            start = HopSpacing.md,
            end = HopSpacing.md,
            top = HopSpacing.md,
            bottom = HopSpacing.sm,
        ),
    )
}

@Composable
private fun FaqTopicHeader(topic: String) {
    Text(
        text = topic,
        color = HopColors.primaryLime,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        modifier = Modifier.padding(
            start = HopSpacing.md,
            end = HopSpacing.md,
            top = HopSpacing.sm,
            bottom = 4.dp,
        ),
    )
}

@Composable
private fun FaqRow(
    faq: FaqItem,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.surfaceElevated)
            .padding(bottom = if (expanded) HopSpacing.md else 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(HopSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = faq.question,
                color = HopColors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(HopSpacing.sm))
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = HopColors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                HorizontalDivider(
                    color = HopColors.surface,
                    modifier = Modifier.padding(horizontal = HopSpacing.md),
                )
                Spacer(modifier = Modifier.height(HopSpacing.sm))
                Text(
                    text = faq.answer,
                    color = HopColors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(horizontal = HopSpacing.md),
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

// ── Support action row ────────────────────────────────────────────────────────

@Composable
private fun SupportActionRow(
    icon: ImageVector,
    label: String,
    description: String,
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(HopColors.surfaceElevated),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HopColors.primaryLime,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(HopSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = HopColors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
            Text(
                text = description,
                color = HopColors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
        }
        Icon(
            imageVector = Icons.Outlined.Mail,
            contentDescription = null,
            tint = HopColors.textSecondary,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ── Error / retry ─────────────────────────────────────────────────────────────

@Composable
private fun ErrorRetry(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HopColors.surfaceElevated)
            .padding(HopSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HopSpacing.sm),
    ) {
        Text(
            text = "Could not load FAQ",
            color = HopColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        )
        Text(
            text = message,
            color = HopColors.textSecondary,
            fontSize = 13.sp,
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(HopColors.primaryLime)
                .clickable(onClick = onRetry)
                .padding(horizontal = HopSpacing.lg, vertical = HopSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                tint = HopColors.surface,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Try again",
                color = HopColors.surface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}
