package com.example.hop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hop.ui.theme.HopColors

/**
 * Lightweight unread indicator — renders nothing when [count] <= 0.
 *
 *  - 0          → returns immediately (no node).
 *  - 1..9       → numeric pill (16 dp).
 *  - 10..99     → numeric pill (still 16 dp, slightly wider via padding).
 *  - >= 100     → "99+" pill.
 *
 * Designed to be `align(Alignment.TopEnd)` over an [androidx.compose.material3.IconButton]
 * inside a wrapping Box. The thin white outline keeps it readable on any
 * surface colour without the caller having to specify a contrast border.
 */
@Composable
fun UnreadBadge(
    count: Int,
    modifier: Modifier = Modifier,
    background: Color = HopColors.unreadBadge,
    onColor: Color = Color.White,
    outline: Color = Color.White,
) {
    if (count <= 0) return

    val label = when {
        count >= 100 -> "99+"
        else -> count.toString()
    }
    val isDot = false // future: allow caller to opt into a 8 dp dot for "any unread"

    Box(
        modifier = modifier
            .defaultBadgeSize(label.length)
            .clip(CircleShape)
            .background(background)
            .border(width = 1.5.dp, color = outline, shape = CircleShape)
            .padding(horizontal = if (label.length > 1) 4.dp else 0.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = onColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        )
    }
}

/** A simple solid dot used when only "has-unread" is interesting (no count). */
@Composable
fun UnreadDot(
    visible: Boolean,
    modifier: Modifier = Modifier,
    color: Color = HopColors.unreadBadge,
    outline: Color = Color.White,
) {
    if (!visible) return
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
            .border(width = 1.5.dp, color = outline, shape = CircleShape),
    )
}

private fun Modifier.defaultBadgeSize(charCount: Int): Modifier =
    if (charCount <= 1) size(16.dp) else size(width = 22.dp, height = 16.dp)
