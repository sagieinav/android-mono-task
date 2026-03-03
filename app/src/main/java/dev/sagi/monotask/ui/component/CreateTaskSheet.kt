package dev.sagi.monotask.ui.component

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors

@Composable
fun CreateTaskSheet(
    onDismissRequest: () -> Unit,
    onAddTask: (title: String, description: String, importance: Importance, tags: List<String>, dueDateMillis: Long?) -> Unit
) {
    // States
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var importance by remember { mutableStateOf(Importance.MEDIUM) }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDateTimePicker by remember { mutableStateOf(false) }

    val shape = MaterialTheme.shapes.medium

    BottomSheet(
        title = "Create New Task",
        onDismissRequest = onDismissRequest
    ) {
        TaskTitleAndDescriptionInput(
            title = title,
            onTitleChange = { title = it },
            description = description,
            onDescriptionChange = { description = it },
        )

        TaskSmartTagsInput(
            tagInput = tagInput,
            onTagInputChange = { tagInput = it },
            tags = tags,
            onTagsUpdated = { tags = it },
        )

        Spacer(modifier = Modifier.height(0.dp))
//        Divider()

        TaskImportanceSelector(
            selectedImportance = importance,
            onImportanceSelected = { importance = it },
        )

        TaskDueDateSelector(
            dueDateMillis = dueDateMillis,
            onClick = { showDateTimePicker = true },
        )

        // Render the time picker when triggered
        if (showDateTimePicker) {
            TaskDateTimePicker(
                onDateTimeSelected = { millis -> dueDateMillis = millis },
                onDismiss = { showDateTimePicker = false }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        AddTaskButton(
            isEnabled = title.isNotBlank(),
            onClick = {
                val finalTags = if (tagInput.isNotBlank() && !tags.contains(tagInput.trim().lowercase())) {
                    tags + tagInput.trim().lowercase()
                } else tags

                onAddTask(title.trim(), description.trim(), importance, finalTags, dueDateMillis)
                onDismissRequest()
            },
            shape = shape,
        )
    }
}


@Composable
private fun TaskTitleAndDescriptionInput(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
) {
    // Title
    CustomTextField(
        value = title,
        onValueChange = onTitleChange,
        label = "Task Title"
    )

    // Description
    CustomTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = "Description (Optional)",
        singleLine = false,
        maxLines = 3
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
    // Extracted so both onValueChange and KeyboardActions share the same logic
    fun commitTag(input: String) {
        val newTag = input.trim().lowercase()
        if (newTag.isNotEmpty() && !tags.contains(newTag)) {
            onTagsUpdated(tags + newTag)
        }
        onTagInputChange("")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CustomTextField(
            value = tagInput,
            onValueChange = { input ->
                when {
                    input.endsWith(",") || input.endsWith(" ") -> commitTag(input.dropLast(1))
                    input.endsWith("\n") -> commitTag(input.dropLast(1)) // physical Enter key
                    else -> onTagInputChange(input)
                }
            },
            label = "Tags (Space or comma to add)",
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
    onImportanceSelected: (Importance) -> Unit,
    borderWidth: Dp = 2.5.dp
) {
    val customColors = MaterialTheme.customColors
    val importanceColor: (Importance) -> Color = { imp ->
        when (imp) {
            Importance.LOW    -> customColors.importanceLowContent
            Importance.MEDIUM -> customColors.importanceMediumContent
            Importance.HIGH   -> customColors.importanceHighContent
        }
    }

    Column {
        Text(text = "Importance", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Importance.entries.forEach { imp ->
                val color = importanceColor(imp)
                val isSelected = selectedImportance == imp

                FilterChip(
                    selected = isSelected,
                    onClick = { onImportanceSelected(imp) },
                    label = {
                        Text(
                            imp.name.lowercase().capitalize(Locale.ROOT),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        // unselected state
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        // selected state — transparent bg, colored label
                        selectedContainerColor = Color.Transparent,
                        selectedLabelColor = color,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        // unselected border
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        borderWidth = 1.dp,
                        // selected border
                        selectedBorderColor = color,
                        selectedBorderWidth = borderWidth,
                    )
                )
            }
        }
    }
}


@Composable
private fun TaskDueDateSelector(
    dueDateMillis: Long?,
    onClick: () -> Unit
) {
    Column() {
        Text(text = "Due Date", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier
            .fillMaxWidth(),
        ) {
            AssistChip(
                onClick = onClick,
                label = {
                    Text(
                        if (dueDateMillis != null) {
                            // Format the timestamp if a date is selected
                            val sdf = SimpleDateFormat("MMM d, yyyy h:mm a", LocalLocale.current.platformLocale)
                            sdf.format(Date(dueDateMillis))
                        } else {
                            "Select Date"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        // A small fix to match the `importance` chip height
                        modifier = Modifier.padding(vertical = 7.dp)
                        )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search_activity),
                        contentDescription = "Calendar Icon",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.scrim
                    )
                }
            )
        }
    }
}

@Composable
private fun AddTaskButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    shape: Shape = MaterialTheme.shapes.medium
) {
    Button(
        onClick = onClick,
        shape = shape,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Text("Add Task", style = MaterialTheme.typography.titleMedium)
    }
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
                onDismissRequest = {},
                onAddTask = { _, _, _, _, _ -> }
            )
        }
    }
}