package com.example.hop.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hop.domain.model.SavedPlaceKind
import com.example.hop.ui.components.HopButton
import com.example.hop.ui.theme.HopColors
import com.example.hop.ui.theme.HopSpacing

/**
 * Modal sheet for creating a new saved place. Returns label, address and
 * optional [SavedPlaceKind] via [onSave]. The host is responsible for
 * dispatching the upsert event and dismissing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavedPlaceSheet(
    onDismiss: () -> Unit,
    onSave: (label: String, address: String, kind: SavedPlaceKind?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var label by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedKind by remember { mutableStateOf<SavedPlaceKind?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HopColors.cardSurface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HopSpacing.md)
                .padding(bottom = HopSpacing.md)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(HopSpacing.md),
        ) {
            Text(
                text = "Add saved place",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = HopColors.authTextPrimary,
            )

            // ── Kind selector ────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(HopSpacing.sm)) {
                KindChip(
                    label = "Home",
                    icon = Icons.Outlined.Home,
                    selected = selectedKind == SavedPlaceKind.HOME,
                    onClick = { selectedKind = SavedPlaceKind.HOME },
                )
                KindChip(
                    label = "Work",
                    icon = Icons.Outlined.Work,
                    selected = selectedKind == SavedPlaceKind.WORK,
                    onClick = { selectedKind = SavedPlaceKind.WORK },
                )
                KindChip(
                    label = "Other",
                    icon = Icons.Outlined.Place,
                    selected = selectedKind == SavedPlaceKind.CUSTOM,
                    onClick = { selectedKind = SavedPlaceKind.CUSTOM },
                )
            }

            // ── Label ────────────────────────────────────────────────────────
            SheetField(
                value = label,
                onValueChange = { label = it },
                placeholder = "Label (e.g. Home, Mom's place)",
            )

            // ── Address ──────────────────────────────────────────────────────
            SheetField(
                value = address,
                onValueChange = { address = it },
                placeholder = "Address — street, city",
            )

            HopButton(
                text = "Save",
                onClick = {
                    if (label.isNotBlank() && address.isNotBlank()) {
                        onSave(label.trim(), address.trim(), selectedKind)
                        onDismiss()
                    }
                },
                enabled = label.isNotBlank() && address.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun KindChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) HopColors.primaryLime else Color.Transparent
    val border = if (selected) HopColors.primaryLime else HopColors.cardBorder
    val fg = if (selected) Color(0xFF1A1A1A) else HopColors.authTextSecondary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = HopSpacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = fg,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun SheetField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, HopColors.cardBorder, RoundedCornerShape(12.dp))
            .background(HopColors.cardSurfaceMuted)
            .padding(horizontal = HopSpacing.md, vertical = 14.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(HopColors.primaryGreen),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = HopColors.authTextPrimary),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFB0B0B0)),
                    )
                }
                inner()
            },
        )
        // Spacer to enforce min height
        Spacer(modifier = Modifier.height(0.dp))
    }
}
