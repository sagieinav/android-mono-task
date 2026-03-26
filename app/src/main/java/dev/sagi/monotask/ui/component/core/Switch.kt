package dev.sagi.monotask.ui.component.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder

private val TrackWidth   = 52.dp
private val TrackHeight  = 32.dp
private val ThumbSize    = TrackHeight - 6.dp // 3dp gap on each side
private val ThumbPad     = (TrackHeight - ThumbSize) / 2
private val ThumbTravel  = TrackWidth - ThumbSize - ThumbPad * 2

@Composable
fun GlassSwitch(
    checked         : Boolean,
    onCheckedChange : (Boolean) -> Unit,
    modifier        : Modifier = Modifier,
    accentColor     : Color?   = null,
    enabled         : Boolean  = true
) {
    val haptic      = LocalHapticFeedback.current
    val activeColor = accentColor ?: MaterialTheme.colorScheme.primary

    // Thumb x-offset: ThumbPad when off, ThumbPad + ThumbTravel when on
    val thumbOffset by animateDpAsState(
        targetValue   = if (checked) ThumbPad + ThumbTravel else ThumbPad,
        animationSpec = spring(
            stiffness    = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ),
        label = "glass_switch_thumb"
    )

    // Track fill color animates between neutral and accent
    val trackBase by animateColorAsState(
        targetValue   = if (checked)
            activeColor.copy(alpha = 0.35f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200),
        label         = "glass_switch_track_base"
    )
    val trackBorderColor: Color? = if (checked) activeColor else null

    Box(
        modifier = modifier
            .width(TrackWidth)
            .height(TrackHeight)
            .clip(CircleShape)
            .toggleable(
                value             = checked,
                enabled           = enabled,
                role              = Role.Switch,
                onValueChange     = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCheckedChange(it)
                }
            )
            .glassBorder(CircleShape, color = trackBorderColor)
            .glassBackground(
                accentColor = if (checked) activeColor else null,
                baseColor   = trackBase
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(ThumbSize)
                .shadow(
                    elevation    = 6.dp,
                    shape        = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor    = Color.Black.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .glassBorder(CircleShape)
                .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}


// =========================================
// Previews
// =========================================

@Preview(showBackground = true, name = "GlassSwitch - off")
@Composable
private fun GlassSwitchOffPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(24.dp)) {
                GlassSwitch(checked = false, onCheckedChange = {})
            }
        }
    }
}

@Preview(showBackground = true, name = "GlassSwitch - on (primary)")
@Composable
private fun GlassSwitchOnPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(24.dp)) {
                GlassSwitch(checked = true, onCheckedChange = {})
            }
        }
    }
}

@Preview(showBackground = true, name = "GlassSwitch - on (accent)")
@Composable
private fun GlassSwitchAccentPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(24.dp)) {
                GlassSwitch(
                    checked     = true,
                    onCheckedChange = {},
                    accentColor = Color(0xFF7C5CBF) // xp violet
                )
            }
        }
    }
}
