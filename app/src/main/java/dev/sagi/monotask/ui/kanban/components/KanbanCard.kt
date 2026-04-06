package dev.sagi.monotask.ui.kanban.components

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import dev.sagi.monotask.ui.common.CustomTag
import dev.sagi.monotask.ui.common.DueDateLabel
import dev.sagi.monotask.designsystem.components.MonoConfirmDialog
import dev.sagi.monotask.ui.common.TagSize
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.monoShadow
import dev.sagi.monotask.designsystem.components.MonoDropdownActionItem
import dev.sagi.monotask.designsystem.components.MonoDropdownMenu
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.ripple
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.domain.service.XpEngine
import dev.sagi.monotask.designsystem.components.GlassSurface
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.designsystem.theme.glassBorder
import dev.sagi.monotask.designsystem.theme.googleSansRounded
import dev.sagi.monotask.ui.kanban.KanbanEvent


// Tracks which confirm dialog is pending
private enum class PendingAction { FocusNow, Restore, Delete }

@Composable
fun KanbanCard(
    task: Task,
    modifier: Modifier = Modifier,
    isArchive: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    onKanbanEvent: (KanbanEvent) -> Unit = {}
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<PendingAction?>(null) }
    var tapOffset by remember { mutableStateOf(IntOffset.Zero) }
    var cardWindowPos by remember { mutableStateOf(IntOffset.Zero) }
    val interactionSource = remember { MutableInteractionSource() }

    val color = if (task.isAce) MaterialTheme.customColors.ace else MaterialTheme.colorScheme.outlineVariant

    Box(modifier = modifier) {
        // ========== Card surface ==========
        GlassSurface(
            shape = shape,
            baseColor = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .fillMaxWidth()
                .glassBorder(shape = shape, color = color, width = 1.5.dp)
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInWindow()
                    cardWindowPos = IntOffset(pos.x.toInt(), pos.y.toInt())
                }
                .clip(shape)
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
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ========== Title ==========
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = googleSansRounded,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                // ========== Tags ==========

                if (task.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement   = Arrangement.spacedBy(3.dp),
                        maxLines = 2
                    ) {
                        task.tags.forEach { tag ->
                            CustomTag(size = TagSize.Small, label = tag)
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
            expanded = dropdownExpanded,
            isArchive = isArchive,
            tapOffset = tapOffset,
            onDismiss = { dropdownExpanded = false },
            onEditClick = { onKanbanEvent(KanbanEvent.OpenEditSheet(task)) },
            onFocusNowClick = { pendingAction = PendingAction.FocusNow },
            onRestoreClick = { pendingAction = PendingAction.Restore },
            onDeleteClick = { pendingAction = PendingAction.Delete }
        )
    }

    // ========== Confirm dialogs ==========
    when (pendingAction) {
        PendingAction.FocusNow -> MonoConfirmDialog(
            onDismissRequest = { pendingAction = null },
            title = "Focus on this Task?",
            message = "Your current focus task will be snoozed, " +
                                "receiving a ${XpEngine.SnoozeOption.MANUAL.penalty} XP penalty.",
            confirmLabel = "Focus",
            confirmColor = MaterialTheme.customColors.aceDim,
            onConfirm = { onKanbanEvent(KanbanEvent.FocusNow(task)) }
        )
        PendingAction.Restore -> MonoConfirmDialog(
            onDismissRequest = { pendingAction = null },
            title = "Restore Task?",
            message = "This will move the task back to active.\n" +
                    "${task.currentXp} XP received from completion will be rolled back.",
            confirmLabel = "Restore",
            onConfirm = { onKanbanEvent(KanbanEvent.RestoreTask(task)) }
        )
        PendingAction.Delete -> MonoConfirmDialog(
            onDismissRequest = { pendingAction = null },
            title = "Delete Task?",
            message = "This operation cannot be undone.",
            confirmLabel = "Delete",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onKanbanEvent(KanbanEvent.DeleteTask(task.id)) }
        )
        null -> Unit
    }
}

// ========================================
// Kanban Card Context Menu
// ========================================
// Active:  Edit Task / Focus Now / Delete Task
// Archive: Restore Task / Delete Task

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
    MonoDropdownMenu(
        expanded = expanded,
        onDismiss = onDismiss,
        tapOffset = tapOffset,
        modifier = modifier
    ) {
        if (!isArchive) {
            MonoDropdownActionItem(
                label = "Focus now",
                iconRes = IconPack.Focus,
                onClick = { onFocusNowClick(); onDismiss() }
            )
            MonoDropdownActionItem(
                label = "Edit task",
                iconRes = IconPack.EditAlt,
                onClick = { onEditClick(); onDismiss() }
            )
        } else {
            MonoDropdownActionItem(
                label = "Restore task",
                iconRes = IconPack.Restore,
                onClick = { onRestoreClick(); onDismiss() }
            )
        }
        MonoDropdownActionItem(
            label = "Delete task",
            iconRes = IconPack.Delete,
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
            modifier = Modifier.padding(16.dp),
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

