package dev.sagi.monotask.ui.component.task

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Importance
import java.util.Date
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.core.ActionButton
import dev.sagi.monotask.ui.component.core.MonoTextField
import dev.sagi.monotask.ui.component.core.BottomSheet
import dev.sagi.monotask.ui.component.core.GlassChip
import dev.sagi.monotask.ui.component.core.TaskDatePicker
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors
import dev.sagi.monotask.util.ext.toRelativeDate

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
    extraContent: @Composable (() -> Unit)? = null   // Slot for delete button in edit mode
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var importance by remember { mutableStateOf(initialImportance) }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(initialTags) }
    var dueDateMillis by remember { mutableStateOf(initialDueDateMillis) }
    var showDateTimePicker by remember { mutableStateOf(false) }

    BottomSheet(
        title = sheetTitle,
        onDismissRequest = {
            onDraftSave?.invoke(title, description, importance, tags, dueDateMillis)
            onDismiss()
        }
    ) {
        // Column to override vertical gap for the 3 text fields
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

        // Spacer before chips
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
            TaskDatePicker(
                onDateSelected = { millis -> dueDateMillis = millis },
                onDismiss = { showDateTimePicker = false }
            )
        }

        // (Potentially) any extra button
        extraContent?.invoke()

        // Spacer before buttons
        Spacer(Modifier.height(8.dp))

        // Save Changes / Add Task
        ActionButton(
            onClick = {
                val finalTags =
                    if (tagInput.isNotBlank() && !tags.contains(tagInput.trim().lowercase()))
                        tags + tagInput.trim().lowercase() else tags
                onSubmit(title.trim(), description.trim(), importance, finalTags, dueDateMillis)
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
    // Title
    MonoTextField(
        value = title,
        onValueChange = onTitleChange,
        label = "Title",
        required = true,
        autoFocus = true,
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_title_pencil),
                contentDescription = null
            )
        },
        singleLine = false,
        maxLines = 2,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        )
    )

    // Description
    MonoTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = "Description",
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_description_alt),
                contentDescription = null
            )
        },
        singleLine = false,
        maxLines = 4,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        ),
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskSmartTagsInput(
    tagInput: String,
    onTagInputChange: (String) -> Unit,
    tags: List<String>,
    onTagsUpdated: (List<String>) -> Unit,
    shape: Shape = MaterialTheme.shapes.medium
) {
    fun commitTag(input: String) {
        val newTag = input.trim().lowercase()
        if (newTag.isNotEmpty() && !tags.contains(newTag)) {
            onTagsUpdated(tags + newTag)
        }
        onTagInputChange("")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MonoTextField(
            value = tagInput,
            onValueChange = { input ->
                when {
                    input.endsWith(",") || input.endsWith(" ") -> commitTag(input.dropLast(1))
                    input.endsWith("\n") -> commitTag(input.dropLast(1)) // physical Enter key
                    else -> onTagInputChange(input)
                }
            },
            label = "Tags",
            supportingText = "Space or Enter to add",
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_tag),
                    contentDescription = "Tags"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            singleLine = true,
            // keyboard "Done" button triggers tag commit
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { commitTag(tagInput) })
        )

        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    CustomTag(
                        label = tag,
                        onRemove = { onTagsUpdated(tags - tag) }
                    )
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
    val importanceColor: (Importance) -> Color = { imp ->
        when (imp) {
            Importance.LOW    -> customColors.importanceLowContent
            Importance.MEDIUM -> customColors.importanceMediumContent
            Importance.HIGH   -> customColors.importanceHighContent
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Importance.entries.forEach { imp ->
            GlassChip(
                label = imp.name.lowercase().replaceFirstChar { it.uppercase() },
                selected = selectedImportance == imp,
                selectedColor = importanceColor(imp),
                onClick = { onImportanceSelected(imp) }
            )
        }
    }
}


@Composable
private fun TaskDueDateSelector(
    dueDateMillis: Long?,
    onClick: () -> Unit
) {
        GlassChip(
            label = if (dueDateMillis != null) {
                Timestamp(Date(dueDateMillis)).toRelativeDate().text
            } else {
                "Select Date"
            },
            selected = dueDateMillis != null,
            selectedColor = MaterialTheme.colorScheme.onSurface,
            onClick = onClick,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search_activity),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateTaskSheetPreview() {
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