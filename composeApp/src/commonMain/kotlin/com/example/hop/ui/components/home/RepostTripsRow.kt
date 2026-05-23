package com.example.hop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

data class RepostTripTemplate(
    val tripId: String,
    val origin: String,
    val destination: String,
    val priceDkkPerSeat: Int,
)

/**
 * Horizontal carousel of recent completed trips that a driver can repost
 * with one tap. Tapping a card calls [onRepost] with the originating trip id;
 * the host wires that to the post-trip flow with prefilled fields.
 */
@Composable
fun RepostTripsRow(
    templates: List<RepostTripTemplate>,
    onRepost: (RepostTripTemplate) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(templates, key = { it.tripId }) { template ->
            RepostCard(template = template, onClick = { onRepost(template) })
        }
    }
}

@Composable
private fun RepostCard(
    template: RepostTripTemplate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(HopColors.cardSurface)
            .border(1.dp, HopColors.cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(HopSpacing.md),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Replay,
                    contentDescription = null,
                    tint = HopColors.primaryGreen,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Repost",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HopColors.authAccent,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(HopSpacing.sm))
            Text(
                text = "${template.origin} → ${template.destination}",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = HopColors.authTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                ),
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "DKK ${template.priceDkkPerSeat}/seat",
                style = MaterialTheme.typography.bodySmall.copy(color = HopColors.authTextSecondary),
            )
        }
    }
}
