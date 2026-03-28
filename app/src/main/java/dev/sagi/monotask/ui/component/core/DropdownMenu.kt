package dev.sagi.monotask.ui.component.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import dev.sagi.monotask.ui.theme.glassBackground
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.ui.theme.monoShadowWorkaround
import dev.sagi.monotask.util.Constants
import kotlinx.coroutines.delay

// ========================================
// Trigger Pill
// ========================================
@Composable
fun DropdownTriggerPill(
    text: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "chevron"
    )

    GlassSurface(
        blurred = false,
        shape = CircleShape,
//        baseColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
            .height(Constants.Theme.TOP_BAR_ITEM_HEIGHT)
            .monoShadowWorkaround(CircleShape)
            .clickable(onClick = onClick),
        baseColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 10.dp)
                .align(Alignment.Center)
                ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val color = MaterialTheme.colorScheme.onSurfaceVariant
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 140.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(chevronRotation)
            )
        }
    }
}


// ========================================
// Glass Style Container
// ========================================
@Composable
fun GlassDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    popupPositionProvider: PopupPositionProvider = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset = IntOffset(
                x = anchorBounds.left.coerceIn(0, windowSize.width - popupContentSize.width),
                y = (anchorBounds.bottom + 4).coerceIn(0, windowSize.height - popupContentSize.height)
            )
        }
    },
    content: @Composable ColumnScope.() -> Unit
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

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest      = onDismiss,
        properties            = PopupProperties(focusable = true)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = expandVertically(
                animationSpec = tween(350),
                expandFrom    = Alignment.Top
            ) + fadeIn(tween(450)),
            exit = shrinkVertically(
                animationSpec = tween(350),
                shrinkTowards = Alignment.Top
            ) + fadeOut(tween(350))
        ) {
            GlassSurface(
                shape    = MaterialTheme.shapes.medium,
                modifier = modifier.widthIn(min = 100.dp, max = 220.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .width(IntrinsicSize.Max),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun GlassDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    tapOffset: IntOffset,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val positionProvider = remember(tapOffset) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset = IntOffset(
//                x = tapOffset.x.coerceIn(0, windowSize.width  - popupContentSize.width), // anchored to left
                x = (tapOffset.x - popupContentSize.width / 2).coerceIn(0, windowSize.width - popupContentSize.width), // centered
                y = tapOffset.y.coerceIn(0, windowSize.height - popupContentSize.height)
            )
        }
    }
    GlassDropdownMenu(
        expanded             = expanded,
        onDismiss            = onDismiss,
        popupPositionProvider = positionProvider,
        modifier             = modifier,
        content              = content
    )
}

@Composable
fun GlassDropdownItem(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val selectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .then(
                if (!selected) Modifier
                else Modifier
                    .glassBorder(MaterialTheme.shapes.small)
                    .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainerHigh)
//                    .background(MaterialTheme.colorScheme.surfaceContainer)
            )
            .padding(horizontal = 12.dp, vertical = 9.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) selectedColor else unselectedColor
        )

        // 'selected' icon inside a box, to ensure padding on long titles:
        if (selected) {
            Box(Modifier.padding(start = 8.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = selectedColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}



// ========================================
// Generic Action/Footer Item
// ========================================
@Composable
fun GlassDropdownActionItem(
    label: String,
    iconRes: Int,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 12.dp)
            .padding(vertical = 10.dp), // inner padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
//            fontFamily = googleSans,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier
        )
    }
}


// ========================================
// Previews
// ========================================
@Preview(showBackground = true, name = "Trigger Pill — Closed")
@Composable
private fun TriggerPillClosedPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DropdownTriggerPill(text = "My Workspace", expanded = false, onClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Trigger Pill — Open")
@Composable
private fun TriggerPillOpenPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DropdownTriggerPill(text = "My Workspace", expanded = true, onClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Item — Unselected")
@Composable
private fun DropdownItemPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.width(240.dp).padding(8.dp)) {
            GlassDropdownItem(label = "Education", selected = false, onClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Item — Selected")
@Composable
private fun DropdownItemSelectedPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.width(240.dp).padding(8.dp)) {
            GlassDropdownItem(
                label = "Education",
                selected = true,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Action Item")
@Composable
private fun DropdownActionItemPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.width(240.dp).padding(8.dp)) {
            GlassDropdownActionItem(label = "New Workspace", iconRes = R.drawable.ic_add, onClick = {})
        }
    }
}
