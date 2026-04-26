package com.example.hop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing
import kotlinx.coroutines.delay

data class HomeTip(
    val title: String,
    val body: String,
)

/**
 * Auto-advancing horizontal pager that surfaces tips, announcements, and
 * onboarding nudges. Loops every 5 seconds; user swipes pause auto-advance
 * for one cycle.
 */
@Composable
fun TipsPager(
    tips: List<HomeTip>,
    modifier: Modifier = Modifier,
    autoAdvanceMillis: Long = 5_000,
) {
    if (tips.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { tips.size })

    LaunchedEffect(pagerState.currentPage, tips.size) {
        if (tips.size <= 1) return@LaunchedEffect
        delay(autoAdvanceMillis)
        val next = (pagerState.currentPage + 1) % tips.size
        pagerState.animateScrollToPage(next)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            TipCard(tip = tips[page])
        }
        Spacer(modifier = Modifier.height(HopSpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            for (index in tips.indices) {
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (active) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (active) HopColors.primaryLime else HopColors.cardBorder),
                )
            }
        }
    }
}

@Composable
private fun TipCard(tip: HomeTip, modifier: Modifier = Modifier) {
    val gradient = Brush.linearGradient(
        listOf(
            HopColors.gradientDayStart.copy(alpha = 0.9f),
            Color.White,
        )
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .padding(HopSpacing.md),
    ) {
        Column {
            Text(
                text = tip.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = HopColors.authTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tip.body,
                style = MaterialTheme.typography.bodySmall.copy(color = HopColors.authTextSecondary),
            )
        }
    }
}

/** Curated default until the announcements API ships. */
val DefaultHomeTips: List<HomeTip> = listOf(
    HomeTip(
        title = "Save your favourite places",
        body = "Tap Add above the search bar to one-tap your daily destinations.",
    ),
    HomeTip(
        title = "Travel greener with Hop",
        body = "Carpooling 100 km saves about 12 kg of CO₂ per seat.",
    ),
//    HomeTip(
//        title = "Invite a friend, earn 50 DKK",
//        body = "Share your referral code from the Profile tab.",
//    ),
)
