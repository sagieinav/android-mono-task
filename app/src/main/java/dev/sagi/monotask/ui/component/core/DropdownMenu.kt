package dev.sagi.monotask.ui.component.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import dev.sagi.monotask.ui.theme.monoShadow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.style.TextAlign
import dev.sagi.monotask.ui.theme.AceGold
import dev.sagi.monotask.ui.theme.AceGoldDim
import dev.sagi.monotask.ui.theme.glassBorder
import dev.sagi.monotask.ui.theme.gloock
import dev.sagi.monotask.ui.theme.harabara
import dev.sagi.monotask.ui.theme.lora
import dev.sagi.monotask.ui.theme.monoShadowWorkaround
import dev.sagi.monotask.ui.theme.notoSerif
import dev.sagi.monotask.ui.theme.playfairDisplay
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
            .monoShadowWorkaround(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp)
            )
            Spacer(Modifier.width(2.dp))
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                contentDescription = null,
                modifier = Modifier.rotate(chevronRotation),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}



// ========================================
// Material Style Container
// ========================================
@Composable
fun MonoDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.medium
    ) {
        content()
    }
}


// ========================================
// Glass Style Container
// ========================================
@Composable
fun MonoDropdownMenuGlass(
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
                        .padding(vertical = 6.dp)
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
fun MonoDropdownMenuGlass(
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
    MonoDropdownMenuGlass(
        expanded             = expanded,
        onDismiss            = onDismiss,
        popupPositionProvider = positionProvider,
        modifier             = modifier,
        content              = content
    )
}


// ========================================
// Generic Item
// ========================================
//@Composable
//fun MonoDropdownItem(
//    label: String,
//    selected: Boolean = false,
//    onClick: () -> Unit,
//    trailingContent: @Composable (() -> Unit)? = null
//) {
//    GlassSurface(
//        blurred = false,
//        shape = MaterialTheme.shapes.small,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 8.dp, vertical = 2.dp)
//            .clip(MaterialTheme.shapes.small)
//            .clickable(onClick = onClick)
//            // `selected` indication
//            .then(
//                if (!selected) Modifier
//                else Modifier.border(
//                    width = 0.4.dp,
//                    color = Color.Black.copy(alpha = 0.2f),
//                    shape = MaterialTheme.shapes.small
//                )
//            )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
////                .background(
////                    if (selected) MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.6f)
////                    else Color.Transparent
////                )
//                .padding(horizontal = 12.dp, vertical = 8.dp)
//,
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                text = label,
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//            trailingContent?.invoke()
//        }
//    }
//}
@Composable
fun MonoDropdownItem(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 0.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .then(
                if (!selected) Modifier
                else Modifier
                    .glassBorder(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        trailingContent?.invoke() // 'selected' icon
    }
}



// ========================================
// Generic Action/Footer Item
// ========================================
@Composable
fun MonoDropdownActionItem(
    label: String,
    iconRes: Int,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 0.dp) // outer padding
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
            modifier = Modifier.size(18.dp),
            tint = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color
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
            MonoDropdownItem(label = "Education", selected = false, onClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Item — Selected")
@Composable
private fun DropdownItemSelectedPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.width(240.dp).padding(8.dp)) {
            MonoDropdownItem(
                label = "Education",
                selected = true,
                onClick = {},
                trailingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "Action Item")
@Composable
private fun DropdownActionItemPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.width(240.dp).padding(8.dp)) {
            MonoDropdownActionItem(label = "New Workspace", iconRes = R.drawable.ic_add, onClick = {})
        }
    }
}
