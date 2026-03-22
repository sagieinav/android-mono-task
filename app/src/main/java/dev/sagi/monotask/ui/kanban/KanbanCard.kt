package dev.sagi.monotask.ui.kanban

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import dev.sagi.monotask.ui.component.task.CustomTag
import dev.sagi.monotask.ui.component.task.DueDateLabel
import dev.sagi.monotask.ui.component.core.GlassConfirmDialog
import dev.sagi.monotask.ui.component.task.TagSize
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.invincibleBorder
import dev.sagi.monotask.ui.theme.monoShadow
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.GlassDropdownActionItem
import dev.sagi.monotask.ui.component.core.GlassDropdownMenu
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.ripple
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import dev.sagi.monotask.domain.util.XpEvents
import dev.sagi.monotask.ui.theme.AceGoldDim
import dev.sagi.monotask.ui.theme.glassBorder


// Tracks which confirm dialog is pending
private enum class PendingAction { FOCUS_NOW, RESTORE, DELETE }

@Composable
fun KanbanCard(
    task: Task,
    isArchive: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    onKanbanEvent: (KanbanEvent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var pendingAction    by remember { mutableStateOf<PendingAction?>(null) }
    var tapOffset        by remember { mutableStateOf(IntOffset.Zero) }
    var cardWindowPos    by remember { mutableStateOf(IntOffset.Zero) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        // ========== Card surface ==========
        Surface(
            modifier = Modifier
                .fillMaxWidth()
//                .invincibleBorder(shape)
                .glassBorder(shape)
                .monoShadow(shape, strength = 0.8f)
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
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ========== Title ==========
                Text(
                    text     = task.title,
                    style    = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // ========== Tags ==========

                if (task.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement   = Arrangement.spacedBy(2.dp),
                        maxLines = 2
                    ) {
                        task.tags.forEach { tag ->
                            CustomTag(size = TagSize.SMALL, label = tag)
                        }
                    }
                }

                // ========== Due date ==========
                if (!isArchive) {
                    task.dueDate?.let { DueDateLabel(timestamp = it, small = true) }
                }
            }
        }

        // ========== Context menu ==========
        KanbanCardDropdown(
            expanded        = dropdownExpanded,
            isArchive       = isArchive,
            tapOffset       = tapOffset,
            onDismiss       = { dropdownExpanded = false },
            onEditClick     = { onKanbanEvent(KanbanEvent.OpenEditSheet(task)) },
            onFocusNowClick = { pendingAction = PendingAction.FOCUS_NOW },
            onRestoreClick  = { pendingAction = PendingAction.RESTORE },
            onDeleteClick   = { pendingAction = PendingAction.DELETE }
        )
    }

    // ========== Confirm dialogs ==========
    when (pendingAction) {
        PendingAction.FOCUS_NOW -> GlassConfirmDialog(
            onDismissRequest = { pendingAction = null },
            title            = "Focus on this Task?",
            message          = "Your current focus task will be snoozed, " +
                                "receiving a ${XpEvents.SnoozeOption.MANUAL.penalty} XP penalty.",
            confirmLabel     = "Focus",
            confirmColor     = AceGoldDim,
            onConfirm        = { onKanbanEvent(KanbanEvent.FocusNow(task)) }
        )
        PendingAction.RESTORE -> GlassConfirmDialog(
            onDismissRequest = { pendingAction = null },
            title            = "Restore Task?",
            message          = "This will move the task back to active.\n" +
                    "${task.currentXp} XP received from completion will be rolled back.",
            confirmLabel     = "Restore",
            onConfirm        = { onKanbanEvent(KanbanEvent.RestoreTask(task)) }
        )
        PendingAction.DELETE -> GlassConfirmDialog(
            onDismissRequest = { pendingAction = null },
            title            = "Delete Task?",
            message          = "This operation cannot be undone.",
            confirmLabel     = "Delete",
            confirmColor     = MaterialTheme.colorScheme.error,
            onConfirm        = { onKanbanEvent(KanbanEvent.DeleteTask(task.id)) }
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
    GlassDropdownMenu(
        expanded  = expanded,
        onDismiss = onDismiss,
        tapOffset    = tapOffset,
        modifier  = modifier
    ) {
        if (!isArchive) {
            GlassDropdownActionItem(
                label   = "Edit Task",
                iconRes = R.drawable.ic_edit_alt,
                onClick = { onEditClick(); onDismiss() }
            )
            GlassDropdownActionItem(
                label   = "Focus Now",
                iconRes = R.drawable.ic_focus,
                onClick = { onFocusNowClick(); onDismiss() }
            )
        } else {
            GlassDropdownActionItem(
                label   = "Restore Task",
                iconRes = R.drawable.ic_restore,
                onClick = { onRestoreClick(); onDismiss() }
            )
        }
        GlassDropdownActionItem(
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

