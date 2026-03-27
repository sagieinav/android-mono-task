package dev.sagi.monotask.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.GlassSwitch
import dev.sagi.monotask.ui.component.core.MonoSlider
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING


@Composable
internal fun SettingsFocusPrefsSection(
    hardcoreModeEnabled : Boolean,
    dueDateWeight       : Float,
    onUpdateHardcoreMode: (Boolean) -> Unit,
    onUpdateWeights     : (dueDateWeight: Float) -> Unit
) {
    var localDueDateWeight by remember(dueDateWeight) { mutableFloatStateOf(dueDateWeight) }

    SettingsSection("Focus Preferences") {
        // Hyperfocus (ON/OFF Switch)
        SettingsRow(
            label           = "Hyperfocus",
            infoText        = "Locks the Kanban board.\nJust you and your next task.",
            verticalPadding = 16.dp,
            leadingIcon     = {
                SettingsRowIcon(R.drawable.ic_cognition, color = SettingsIconColors.hyperfocus)
            },
            trailingContent = {
                GlassSwitch(
                    checked         = hardcoreModeEnabled,
                    onCheckedChange = onUpdateHardcoreMode
                )
            }
        )
        SettingsDivider()

        // Task Priority Calculation (Slider)
        SettingsRow(
            label           = "Task priority calculation",
            infoText        = "Controls how tasks are ranked. Balance urgency against importance to match your style.",
            leadingIcon     = { SettingsRowIcon(R.drawable.ic_low_priority, color = SettingsIconColors.priority) },
            verticalPadding = 10.dp
        ) {
            Column {
                MonoSlider(
                    value                 = localDueDateWeight,
                    onValueChange         = { localDueDateWeight = it },
                    onValueChangeFinished = { onUpdateWeights(it) },
                    modifier              = Modifier.fillMaxWidth()
                )
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "Due date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                    Text(
                        text  = "Importance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}


// ==========================================
// Preview
// ==========================================

@Preview(showBackground = true, name = "FocusPreferences Section")
@Composable
private fun SettingsFocusPrefsSectionPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(SCREEN_PADDING)) {
                SettingsFocusPrefsSection(
                    hardcoreModeEnabled  = false,
                    dueDateWeight        = 0.6f,
                    onUpdateHardcoreMode = {},
                    onUpdateWeights      = { _ -> }
                )
            }
        }
    }
}
