package com.example.hop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.domain.model.SavedPlace
import com.example.hop.domain.model.SavedPlaceKind
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

data class SavedPlaceChipData(
    val label: String,
    val address: String?,
    val icon: ImageVector,
)

@Composable
fun SavedPlacesRow(
    places: List<SavedPlaceChipData>,
    onPlaceClick: (SavedPlaceChipData) -> Unit,
    onAddPlace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(places, key = { it.label }) { place ->
            SavedPlaceChip(place = place, onClick = { onPlaceClick(place) })
        }
        item { AddPlaceChip(onClick = onAddPlace) }
    }
}

@Composable
private fun SavedPlaceChip(
    place: SavedPlaceChipData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(HopColors.cardSurfaceMuted)
            .border(1.dp, HopColors.cardBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = HopSpacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = place.icon,
            contentDescription = null,
            tint = HopColors.primaryGreen,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = place.label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = HopColors.authTextPrimary,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun AddPlaceChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, HopColors.authInputBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = HopSpacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add place",
            tint = HopColors.authTextSecondary,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Add",
            style = MaterialTheme.typography.labelMedium.copy(
                color = HopColors.authTextSecondary,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

/** Default placeholder set used until the saved-places API ships. */
val DefaultSavedPlaces: List<SavedPlaceChipData> = listOf(
    SavedPlaceChipData("Home", null, Icons.Outlined.Home),
    SavedPlaceChipData("Work", null, Icons.Outlined.Work),
    SavedPlaceChipData("Saved", null, Icons.Outlined.Bookmark),
)

/** Maps a server-side [SavedPlace] into the row's UI-only chip representation. */
fun SavedPlace.toChipData(): SavedPlaceChipData = SavedPlaceChipData(
    label = label,
    address = address,
    icon = when (kind) {
        SavedPlaceKind.HOME -> Icons.Outlined.Home
        SavedPlaceKind.WORK -> Icons.Outlined.Work
        SavedPlaceKind.CUSTOM -> Icons.Outlined.Bookmark
    },
)
