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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.basicMonoTask


// ─────────────────────────────────────────
// Trigger Pill
// ─────────────────────────────────────────
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
    Row(
        modifier = modifier
            .basicMonoTask(CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceBright)
            .clickable(onClick = onClick)
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


// ─────────────────────────────────────────
// Material Style Container
// ─────────────────────────────────────────
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


// ─────────────────────────────────────────
// Glass Style Container
// ─────────────────────────────────────────
@Composable
fun MonoDropdownMenuGlass(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    offset: IntOffset = IntOffset(0, 130),
    content: @Composable ColumnScope.() -> Unit
) {
    if (!expanded) return

    Popup(
        alignment = Alignment.TopStart,
        offset = offset,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        GlassSurface(
            shape = MaterialTheme.shapes.medium,
            modifier = modifier.widthIn(min = 100.dp, max = 240.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}


// ─────────────────────────────────────────
// Generic Item
// ─────────────────────────────────────────
@Composable
fun MonoDropdownItem(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.7f)
                else Color.Transparent
            )
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        trailingContent?.invoke()
    }
}


// ─────────────────────────────────────────
// Generic Action/Footer Item
// ─────────────────────────────────────────
@Composable
fun MonoDropdownActionItem(
    label: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// ─────────────────────────────────────────
// Previews
// ─────────────────────────────────────────
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
            MonoDropdownItem(label = "Education", isSelected = false, onClick = {})
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
                isSelected = true,
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
