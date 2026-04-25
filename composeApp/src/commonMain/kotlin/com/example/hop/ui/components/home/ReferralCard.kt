package com.example.hop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

/**
 * Compact referral promo card. Pure presentation — caller wires the share
 * action via [onShare]. Reward amount is given in DKK (already humanised).
 */
@Composable
fun ReferralCard(
    rewardDkk: Int,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.linearGradient(
        listOf(HopColors.referralStart, HopColors.referralEnd)
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .clickable(onClick = onShare)
            .padding(HopSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.CardGiftcard,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(HopSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Invite a friend, earn $rewardDkk DKK",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Both of you get credit on the first ride.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f)),
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}
