package com.example.hop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.domain.model.RecentSearch
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

/**
 * Horizontal carousel of the user's recent search queries.
 * Tapping a chip re-runs the search; tapping the close icon deletes it
 * (with optimistic UI in the host VM). Returns Unit early when [searches]
 * is empty so callers can include it unconditionally as a `LazyColumn` item.
 */
@Composable
fun RecentSearchesRow(
    searches: List<RecentSearch>,
    onSearchClick: (RecentSearch) -> Unit,
    onDeleteSearch: (RecentSearch) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (searches.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(searches, key = { it.id }) { search ->
            RecentSearchChip(
                search = search,
                onClick = { onSearchClick(search) },
                onDelete = { onDeleteSearch(search) },
            )
        }
    }
}

@Composable
private fun RecentSearchChip(
    search: RecentSearch,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(HopColors.surfaceElevated)
            .border(1.dp, HopColors.cardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(start = HopSpacing.sm, end = 4.dp, top = 6.dp, bottom = 6.dp)
            .semantics {
                contentDescription = "Recent search ${search.originLabel} to ${search.destLabel}"
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            tint = HopColors.authTextSecondary,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = search.originLabel,
            style = MaterialTheme.typography.labelMedium.copy(
                color = HopColors.authTextPrimary,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = HopColors.authTextSecondary,
            modifier = Modifier.size(12.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = search.destLabel,
            style = MaterialTheme.typography.labelMedium.copy(
                color = HopColors.authTextPrimary,
                fontWeight = FontWeight.Medium,
            ),
        )

        if (search.useCount > 1) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HopColors.primaryLime)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = "×${search.useCount}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.width(2.dp))
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onDelete)
                .semantics {
                    contentDescription = "Remove recent search"
                    role = Role.Button
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null,
                tint = HopColors.authTextSecondary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
