package dev.sagi.monotask.ui.component.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import dev.sagi.monotask.data.model.AchievementColorBronze
import dev.sagi.monotask.data.model.AchievementColorSilver
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.nationalPark
import kotlinx.coroutines.delay


@Composable
fun GlassTooltip(
    expanded  : Boolean,
    onDismiss : () -> Unit,
    modifier  : Modifier = Modifier,
    icon      : @Composable (() -> Unit)? = null,
    content   : @Composable ColumnScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var mounted by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded) {
            mounted = true
            delay(16)
            visible = true
        } else {
            visible = false
            delay(160)
            mounted = false
        }
    }
    if (!mounted) return

    val positionProvider = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds    : IntRect,
                windowSize      : IntSize,
                layoutDirection : LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val gap    = 8
                val x      = (anchorBounds.center.x - popupContentSize.width / 2)
                                 .coerceIn(0, windowSize.width - popupContentSize.width)
                val yAbove = anchorBounds.top - popupContentSize.height - gap
                val y      = if (yAbove >= 0) yAbove else anchorBounds.bottom + gap
                return IntOffset(x, y)
            }
        }
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest      = onDismiss,
        properties            = PopupProperties(focusable = true)
    ) {
        val alpha = remember { Animatable(0f) }

        LaunchedEffect(visible) {
            alpha.animateTo(
                targetValue   = if (visible) 1f else 0f,
                animationSpec = tween(if (visible) 200 else 150)
            )
        }


        GlassTooltipShell(
            modifier = modifier.graphicsLayer { this.alpha = alpha.value },
            icon     = icon,
            content  = content
        )
    }
}

// Extracted shell (for allowing Preview)
@Composable
private fun GlassTooltipShell(
    modifier: Modifier = Modifier,
    icon    : @Composable (() -> Unit)? = null,
    content : @Composable ColumnScope.() -> Unit
) {
    GlassSurface(
        shape    = MaterialTheme.shapes.small,
        modifier = modifier.widthIn(min = 130.dp, max = 280.dp)
    ) {
        ProvideTextStyle(
            MaterialTheme.typography.labelSmall.copy(fontFamily = nationalPark)
        ) {
            Row(
                modifier              = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon?. let { it() }
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    content             = content
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GlassTooltipPreview() {
    MonoTaskTheme {
        GlassTooltipShell {
            // Line 1: current earned tier
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text       = "CURRENT",
                    fontWeight = FontWeight.Bold,
                    color      = AchievementColorBronze
                )
                Text(
                    text  = "Complete 5 tasks",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            // Line 2: next tier
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text       = "NEXT",
                    fontWeight = FontWeight.Bold,
                    color      = AchievementColorSilver
                )
                Text(
                    text  = "Complete 100 tasks",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }
    }
}
