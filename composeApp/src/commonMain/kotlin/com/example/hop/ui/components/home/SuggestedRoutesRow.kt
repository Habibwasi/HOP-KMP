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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
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

data class SuggestedRoute(
    val origin: String,
    val destination: String,
    val tagline: String,
    val tripsThisWeek: Int,
)

/**
 * Horizontal carousel of curated popular routes.
 * Tapping a card calls [onRouteClick] with the origin/destination so the
 * search card can be prefilled.
 *
 * Until the `/search/popular` endpoint ships, callers can pass
 * [DefaultSuggestedRoutes] which contains common Danish corridors.
 */
@Composable
fun SuggestedRoutesRow(
    routes: List<SuggestedRoute>,
    onRouteClick: (SuggestedRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(routes, key = { "${it.origin}-${it.destination}" }) { route ->
            SuggestedRouteCard(route = route, onClick = { onRouteClick(route) })
        }
    }
}

@Composable
private fun SuggestedRouteCard(
    route: SuggestedRoute,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.linearGradient(
        listOf(
            HopColors.gradientDayStart,
            Color.White,
        )
    )
    Box(
        modifier = modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .border(1.dp, HopColors.cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(HopSpacing.md),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = HopColors.primaryGreen,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${route.tripsThisWeek} trips this week",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HopColors.authAccent,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(HopSpacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = route.origin,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = HopColors.authTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = HopColors.authTextSecondary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = route.destination,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = HopColors.authTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = route.tagline,
                style = MaterialTheme.typography.bodySmall.copy(color = HopColors.authTextSecondary),
            )
        }
    }
}

/** Curated default until the popular-routes API ships. */
val DefaultSuggestedRoutes: List<SuggestedRoute> = listOf(
    SuggestedRoute("Aarhus", "København", "Most-booked corridor in DK", 42),
    SuggestedRoute("Aalborg", "Aarhus", "Daily commuters welcome", 28),
    SuggestedRoute("Odense", "København", "Quick weekend hop", 19),
    SuggestedRoute("Esbjerg", "Aarhus", "Cross-country savings", 11),
)
