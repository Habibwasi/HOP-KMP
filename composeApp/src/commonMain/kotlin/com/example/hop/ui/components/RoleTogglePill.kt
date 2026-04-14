package com.example.hop.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.hop.domain.model.UserRole
import com.example.hop.ui.theme.HopColors

/**
 * Passenger ↔ Driver role toggle pill.
 *
 * Dimensions: 160 × 36 dp.
 * Active segment: Lime fill (#C8F135), dark label.
 * Inactive segment: transparent, muted label.
 * Animated indicator slides with a spring on role change.
 */
@Composable
fun RoleTogglePill(
    selectedRole: UserRole,
    onRoleChange: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillWidth  = 160.dp
    val pillHeight =  36.dp
    val segmentWidth = pillWidth / 2          // 80 dp each
    val cornerRadius = pillHeight / 2          // fully rounded pill ends
    val shape = RoundedCornerShape(cornerRadius)

    // Animate indicator position: 0 dp = PASSENGER, 80 dp = DRIVER
    val indicatorOffset by animateDpAsState(
        targetValue = if (selectedRole == UserRole.DRIVER) segmentWidth else 0.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 700f),
        label = "roleIndicator",
    )

    Box(
        modifier = modifier
            .width(pillWidth)
            .height(pillHeight)
            .clip(shape)
            .border(width = 1.dp, color = Color(0xFF3A3A3A), shape = shape)
            .background(HopColors.surfaceElevated),
    ) {
        // Sliding lime indicator (behind labels)
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(shape)
                .background(HopColors.primaryLime),
        )

        // Labels row (above indicator)
        Row(
            modifier = Modifier
                .width(pillWidth)
                .fillMaxHeight(),
        ) {
            listOf(UserRole.PASSENGER, UserRole.DRIVER).forEach { role ->
                val isActive = selectedRole == role
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,   // pill has its own visual feedback
                            onClick = { onRoleChange(role) },
                        )
                        .semantics {
                            this.role = Role.Tab
                            contentDescription = "${role.name} role ${if (isActive) "selected" else ""}"
                        },
                ) {
                    Text(
                        text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isActive) Color(0xFF1A1A1A) else HopColors.textSecondary,
                    )
                }
            }
        }
    }
}
