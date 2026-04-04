package dev.sagi.monotask.ui.common

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.designsystem.component.ActionButton
import dev.sagi.monotask.designsystem.component.MonoBottomSheet
import dev.sagi.monotask.designsystem.component.MonoChipSelector
import dev.sagi.monotask.designsystem.component.MonoDatePicker
import dev.sagi.monotask.designsystem.component.MonoTextField
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.util.toRelativeDate
import java.util.Date

@Composable
private fun TaskSheet(
    sheetTitle: String,
    submitLabel: String,
    initialTitle: String = "",
    initialDescription: String = "",
    initialImportance: Importance = Importance.MEDIUM,
    initialTags: List<String> = emptyList(),
    initialDueDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, importance: Importance, tags: List<String>, dueDateMillis: Long?) -> Unit,
    onDraftSave: ((title: String, description: String, importance: Importance, tags: List<String>, dueDateMillis: Long?) -> Unit)? = null,
    extraContent: @Composable (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var importance by remember { mutableStateOf(initialImportance) }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(initialTags) }
    var dueDateMillis by remember { mutableStateOf(initialDueDateMillis) }
    var showDateTimePicker by remember { mutableStateOf(false) }

    fun flushTagInput(): List<String> {
        val trimmed = tagInput.trim().lowercase()
        return if (trimmed.isNotEmpty() && trimmed !in tags) tags + trimmed else tags
    }

    MonoBottomSheet(
        title = sheetTitle,
        onDismissRequest = {
            onDraftSave?.invoke(title, description, importance, tags, dueDateMillis)
            onDismiss()
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            TaskTitleAndDescriptionInput(
                title = title, onTitleChange = { title = it },
                description = description, onDescriptionChange = { description = it }
            )
            TaskSmartTagsInput(
                tagInput = tagInput, onTagInputChange = { tagInput = it },
                tags = tags, onTagsUpdated = { tags = it }
            )
        }

        Spacer(Modifier.height(2.dp))

        TaskImportanceSelector(
            selectedImportance = importance,
            onImportanceSelected = { importance = it }
        )
        TaskDueDateSelector(
            dueDateMillis = dueDateMillis,
            onClick = { showDateTimePicker = true }
        )
        if (showDateTimePicker) {
            MonoDatePicker(
                onDateSelected = { millis -> dueDateMillis = millis },
                onDismiss = { showDateTimePicker = false }
            )
        }

        extraContent?.invoke()

        Spacer(Modifier.height(8.dp))

        ActionButton(
            onClick = {
                onSubmit(title.trim(), description.trim(), importance, flushTagInput(), dueDateMillis)
                onDismiss()
            },
            enabled = title.isNotBlank()
        ) {
            Text(submitLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}


@Composable
fun CreateTaskSheet(
    onDismiss: () -> Unit,
    onAddTask: (title: String, description: String, importance: Importance, tags: List<String>, dueDateMillis: Long?) -> Unit,
    initialTitle: String = "",
    initialDescription: String = "",
    initialImportance: Importance = Importance.MEDIUM,
    initialTags: List<String> = emptyList(),
    initialDueDateMillis: Long? = null,
    onDraftSaved: ((title: String, description: String, importance: Importance, tags: List<String>, dueDateMillis: Long?) -> Unit)? = null
) {
    TaskSheet(
        sheetTitle = "Create New Task",
        submitLabel = "Add task",
        initialTitle = initialTitle,
        initialDescription = initialDescription,
        initialImportance = initialImportance,
        initialTags = initialTags,
        initialDueDateMillis = initialDueDateMillis,
        onDismiss = onDismiss,
        onSubmit = onAddTask,
        onDraftSave = onDraftSaved
    )
}


@Composable
fun EditTaskSheet(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, importance: Importance, tags: List<String>, dueDateMillis: Long?) -> Unit,
) {
    TaskSheet(
        sheetTitle = "Edit Task",
        submitLabel = "Save changes",
        initialTitle = task.title,
        initialDescription = task.description,
        initialImportance = task.importance,
        initialTags = task.tags,
        initialDueDateMillis = task.dueDate?.toDate()?.time,
        onDismiss = onDismiss,
        onSubmit = onSave
    )
}


@Composable
private fun TaskTitleAndDescriptionInput(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
) {
    MonoTextField(
        value = title,
        onValueChange = onTitleChange,
        label = "Title",
        required = true,
        autoFocus = true,
        leadingIcon = {
            Icon(
                painter = painterResource(IconPack.TitlePencil),
                contentDescription = null
            )
        },
        singleLine = false,
        maxLines = 2,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
    MonoTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = "Description",
        leadingIcon = {
            Icon(
                painter = painterResource(IconPack.Description),
                contentDescription = null
            )
        },
        singleLine = false,
        maxLines = 4,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskSmartTagsInput(
    tagInput: String,
    onTagInputChange: (String) -> Unit,
    tags: List<String>,
    onTagsUpdated: (List<String>) -> Unit,
) {
    fun commitTag(input: String) {
        val newTag = input.trim().lowercase()
        if (newTag.isNotEmpty() && newTag !in tags) onTagsUpdated(tags + newTag)
        onTagInputChange("")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MonoTextField(
            value = tagInput,
            onValueChange = { input ->
                when {
                    input.endsWith(",") || input.endsWith(" ") -> commitTag(input.dropLast(1))
                    input.endsWith("\n") -> commitTag(input.dropLast(1))
                    else -> onTagInputChange(input)
                }
            },
            label = "Tags",
            supportingText = "Space or Enter to add",
            leadingIcon = {
                Icon(
                    painter = painterResource(IconPack.Tag),
                    contentDescription = "Tags"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { commitTag(tagInput) })
        )

        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    CustomTag(label = tag, onRemove = { onTagsUpdated(tags - tag) })
                }
            }
        }
    }
}


@Composable
private fun TaskImportanceSelector(
    selectedImportance: Importance,
    onImportanceSelected: (Importance) -> Unit
) {
    val customColors = MaterialTheme.customColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Importance.entries.forEach { imp ->
            val isSelected = selectedImportance == imp
            val (color, iconRes) = when (imp) {
                Importance.LOW    -> Pair(
                    customColors.importanceLowContent,
                    IconPack.ImportanceLowAlt
                )
                Importance.MEDIUM -> Pair(
                    customColors.importanceMediumContent,
                    IconPack.ImportanceMediumAlt
                )
                Importance.HIGH   -> Pair(
                    customColors.importanceHighContent,
                    IconPack.ImportanceHighAlt
                )
            }
            MonoChipSelector(
                label = imp.name.lowercase().replaceFirstChar { it.uppercase() },
                selected = isSelected,
                selectedColor = color,
                onClick = { onImportanceSelected(imp) },
                iconRes = if (isSelected) iconRes else null
            )
        }
    }
}


@Composable
private fun TaskDueDateSelector(
    dueDateMillis: Long?,
    onClick: () -> Unit
) {
    val relativeDate = dueDateMillis?.let { Timestamp(Date(it)).toRelativeDate() }
    val contentColor = if (relativeDate?.isOverdue == true) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurfaceVariant

    MonoChipSelector(
        label = relativeDate?.text ?: "Select Date",
        selected = dueDateMillis != null,
        selectedColor = contentColor,
        onClick = onClick,
        iconRes = IconPack.CalendarPlus
    )
}


// ========== Previews ==========

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CreateTaskSheetPreview() {
    MonoTaskTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CreateTaskSheet(
                onDismiss = {},
                onAddTask = { _, _, _, _, _ -> }
            )
        }
    }
}
