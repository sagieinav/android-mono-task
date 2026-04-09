package dev.sagi.monotask.ui.settings.components

import dev.sagi.monotask.designsystem.theme.IconPack
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.customColors
import dev.sagi.monotask.designsystem.util.Constants.Theme.SCREEN_PADDING

private const val GIT_URL = "https://github.com/sagieinav/android-mono-task"
private const val FEEDBACK_EMAIL = "mailto:sagi.einav@icloud.com?subject=MonoTask%20Feedback"
private val TRAILING_ICON_SIZE = 20.dp


@Composable
internal fun SettingsAboutSection() {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var showFaq by remember { mutableStateOf(false) }

    if (showFaq) {
        FaqBottomSheet(onDismiss = { showFaq = false })
    }

    SettingsSection("About") {

        // FAQ BottomSheet:
        SettingsAboutRow(
            label = "Frequently asked questions",
            leadingIcon = {
                SettingsRowIcon(
                    IconPack.HelpCircle,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            trailingIconRes = IconPack.ChevronRight,
            onClick = { showFaq = true }
        )
        SettingsDivider()

        // Send Feedback:
        SettingsAboutRow(
            label = "Send feedback",
            leadingIcon = {
                SettingsRowIcon(
                    IconPack.Feedback,
                    color = MaterialTheme.customColors.xp
                )
            },
            trailingIconRes = IconPack.ExternalLink,
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = FEEDBACK_EMAIL.toUri()
                }
                context.startActivity(intent)
            }
        )

        // GitHub Repo:
        SettingsDivider()
        SettingsAboutRow(
            label = "GitHub",
            leadingIcon = {
                SettingsRowIcon(
                    IconPack.Github,
                    color = SettingsIconColors.github
                )
            },
            trailingIconRes = IconPack.ExternalLink,
            onClick = { uriHandler.openUri(GIT_URL) }
        )
    }
}


// ==========================================
// Private row helper
// ==========================================

@Composable
private fun SettingsAboutRow(
    label : String,
    trailingIconRes: Int? = null,
    leadingIcon : (@Composable () -> Unit)? = null,
    onClick : (() -> Unit)? = null
) {
    SettingsRow(
        label = label,
        onClick = onClick,
        verticalPadding = 16.dp,
        leadingIcon = leadingIcon,
        trailingContent = trailingIconRes?.let { iconRes ->
            {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(TRAILING_ICON_SIZE),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}


// ==========================================
// Preview
// ==========================================

@Preview(showBackground = true, name = "About Section")
@Composable
private fun SettingsAboutSectionPreview() {
    MonoTaskTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(SCREEN_PADDING)) {
                SettingsAboutSection()
            }
        }
    }
}
