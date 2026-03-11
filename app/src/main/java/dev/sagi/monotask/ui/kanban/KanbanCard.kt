package dev.sagi.monotask.ui.kanban

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.core.CustomTag
import dev.sagi.monotask.ui.component.core.DueDateLabel
import dev.sagi.monotask.ui.component.core.GlassConfirmDialog
import dev.sagi.monotask.ui.component.core.TagSize
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.invincibleBorder
import dev.sagi.monotask.ui.theme.monoShadow
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.MonoDropdownActionItem
import dev.sagi.monotask.ui.component.core.MonoDropdownMenuGlass
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ripple
import androidx.compose.ui.graphics.Shape
import dev.sagi.monotask.domain.util.XpEvents
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.theme.AceGold
import dev.sagi.monotask.ui.theme.AceGoldDim


// Tracks which confirm dialog is pending
private enum class PendingAction { FOCUS_NOW, RESTORE, DELETE }

@Composable
fun KanbanCard(
    task: Task,
    isArchive: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    onEditClick: () -> Unit = {},
    onFocusNowClick: () -> Unit = {},
    onRestoreClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var pendingAction    by remember { mutableStateOf<PendingAction?>(null) }
    var tapOffset        by remember { mutableStateOf(IntOffset.Zero) }
    var cardWindowPos    by remember { mutableStateOf(IntOffset.Zero) }
    val interactionSource = remember { MutableInteractionSource() }

    val cardShape = MaterialTheme.shapes.small

    Box(modifier = modifier) {
        // ── Card surface ────────────────────────────────────────────────────
        Surface(
//            onClick  = {},  // consumed by pointerInput below
            modifier = Modifier
                .fillMaxWidth()
                .monoShadow(cardShape)
                .invincibleBorder(cardShape)
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInWindow()
                    cardWindowPos = IntOffset(pos.x.toInt(), pos.y.toInt())
                }
                .indication(interactionSource, ripple())
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            tryAwaitRelease()
                            interactionSource.emit(PressInteraction.Release(press))
                        },
                        onTap = { localOffset ->
                            tapOffset = IntOffset(
                                x = cardWindowPos.x + localOffset.x.toInt(),
                                y = cardWindowPos.y + localOffset.y.toInt()
                            )
                            dropdownExpanded = true
                        }
                    )
                },
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Title ───────────────────────────────────────────────────
                Text(
                    text     = task.title,
                    style    = MaterialTheme.typography.titleMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // ── Tags ────────────────────────────────────────────────────
                if (task.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement   = Arrangement.spacedBy(4.dp)
                    ) {
                        task.tags.forEach { tag ->
                            CustomTag(size = TagSize.SMALL, label = tag)
                        }
                    }
                }

                // ── Due date ────────────────────────────────────────────────
                task.dueDate?.let { DueDateLabel(timestamp = it, small = true) }
            }
        }

        // ── Context menu ────────────────────────────────────────────────────
        KanbanCardDropdown(
            expanded        = dropdownExpanded,
            isArchive       = isArchive,
            tapOffset       = tapOffset,
            onDismiss       = { dropdownExpanded = false },
            onEditClick     = onEditClick,
            onFocusNowClick = { pendingAction = PendingAction.FOCUS_NOW },
            onRestoreClick  = { pendingAction = PendingAction.RESTORE },
            onDeleteClick   = { pendingAction = PendingAction.DELETE }
        )
    }

    // ── Confirm dialogs ──────────────────────────────────────────────────────
    when (pendingAction) {
        PendingAction.FOCUS_NOW -> GlassConfirmDialog(
            onDismissRequest = { pendingAction = null },
            title            = "Focus on this Task?",
            message          = "Your current focus task will be snoozed, " +
                                "receiving a ${XpEvents.SnoozeOption.MANUAL.penalty} XP penalty.",
            confirmLabel     = "Focus",
            confirmColor     = AceGoldDim,
            onConfirm        = onFocusNowClick
        )
        PendingAction.RESTORE -> GlassConfirmDialog(
            onDismissRequest = { pendingAction = null },
            title            = "Restore Task?",
            message          = "This will move the task back to active.\n" +
                    "${task.currentXp} XP received from completion will be rolled back.",
            confirmLabel     = "Restore",
            onConfirm        = onRestoreClick
        )
        PendingAction.DELETE -> GlassConfirmDialog(
            onDismissRequest = { pendingAction = null },
            title            = "Delete Task?",
            message          = "This operation cannot be undone.",
            confirmLabel     = "Delete",
            confirmColor     = MaterialTheme.colorScheme.error,
            onConfirm        = onDeleteClick
        )
        null -> Unit
    }
}

// ========================================
// Kanban Card Context Menu
// ========================================
// Active:  Edit Task | Focus Now | Delete Task
// Archive: Restore Task | Delete Task

@Composable
fun KanbanCardDropdown(
    expanded: Boolean,
    isArchive: Boolean,
    tapOffset: IntOffset,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onFocusNowClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MonoDropdownMenuGlass(
        expanded  = expanded,
        onDismiss = onDismiss,
        tapOffset    = tapOffset,
        modifier  = modifier
    ) {
        if (!isArchive) {
            MonoDropdownActionItem(
                label   = "Edit Task",
                iconRes = R.drawable.ic_edit,
//                color = MaterialTheme.colorScheme.primary,
                onClick = { onEditClick(); onDismiss() }
            )
            MonoDropdownActionItem(
                label   = "Focus Now",
                iconRes = R.drawable.ic_fire,
//                color = AceGoldDim,
                onClick = { onFocusNowClick(); onDismiss() }
            )
        } else {
            MonoDropdownActionItem(
                label   = "Restore Task",
                iconRes = R.drawable.ic_restore,
                onClick = { onRestoreClick(); onDismiss() }
            )
        }
        MonoDropdownActionItem(
            label   = "Delete Task",
            iconRes = R.drawable.ic_delete,
            color = MaterialTheme.colorScheme.error,
            onClick = { onDeleteClick(); onDismiss() }
        )
    }
}



// ========== Preview ==========
@Preview(showBackground = true)
@Composable
fun KanbanCardPreview() {
    MonoTaskTheme {
        Column(
            modifier          = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KanbanCard(
                task = Task(id = "1", title = "Submit Final Report", tags = listOf("Urgent"), importance = Importance.HIGH, dueDate = Timestamp.now())
            )
            KanbanCard(
                task = Task(id = "2", title = "Client Meeting", tags = listOf("internal"), importance = Importance.MEDIUM)
            )
            KanbanCard(
                task = Task(id = "3", title = "Read Industry News", importance = Importance.LOW, dueDate = Timestamp.now())
            )
        }
    }
}


@Composable
private fun KanbanCardDropdownContent(isArchive: Boolean) {
    GlassSurface(
        shape    = MaterialTheme.shapes.medium,
        modifier = Modifier.widthIn(min = 100.dp, max = 220.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (!isArchive) {
                MonoDropdownActionItem(label = "Edit Task",  iconRes = R.drawable.ic_edit,    onClick = {})
                MonoDropdownActionItem(label = "Focus Now",  iconRes = R.drawable.ic_fire,    onClick = {})
            } else {
                MonoDropdownActionItem(label = "Restore Task", iconRes = R.drawable.ic_restore, onClick = {})
            }
            MonoDropdownActionItem(label = "Delete Task", iconRes = R.drawable.ic_delete, onClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Dropdown — Active")
@Composable
private fun KanbanCardDropdownActivePreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            KanbanCardDropdownContent(isArchive = false)
        }
    }
}

@Preview(showBackground = true, name = "Dropdown — Archive")
@Composable
private fun KanbanCardDropdownArchivePreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            KanbanCardDropdownContent(isArchive = true)
        }
    }
}
