package dev.sagi.monotask.ui.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors
import dev.sagi.monotask.util.Constants.Theme.SCREEN_PADDING
import androidx.core.net.toUri

// ==========================================
// Trailing icon defaults
// ==========================================

private val ICON_SIZE = 20.dp

@Composable
private fun iconTint() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
internal fun SettingsAboutSection() {
    val uriHandler = LocalUriHandler.current
    val gitUrl = "https://github.com/sagieinav/android-mono-task"

    val context = LocalContext.current

    SettingsSection("About") {

        // FAQ BottomSheet:
        SettingsAboutRow(
            label           = "Frequently asked questions",
            leadingIcon     = { SettingsRowIcon(R.drawable.ic_help_circle, color = MaterialTheme.colorScheme.primary) },
            trailingIconRes = R.drawable.ic_arrow_right_alt,
            // TODO OnClick: BottomSheet with FAQ
        )
        SettingsDivider()

        // Send Feedback:
        SettingsAboutRow(
            label           = "Send feedback",
            leadingIcon     = { SettingsRowIcon(R.drawable.ic_feedback, color = MaterialTheme.customColors.xp) },
            trailingIconRes = R.drawable.ic_external_link,
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:sagi.einav@icloud.com?subject=MonoTask%20Feedback".toUri()
                }
                context.startActivity(intent)
            }
        )

        // Rate the app is disabled for now
//        SettingsDivider()
//        SettingsAboutRow(
//            label           = "Rate the app",
//            leadingIcon     = { SettingsRowIcon(R.drawable.ic_star, color = MaterialTheme.colorScheme.secondary) },
//            trailingIconRes = R.drawable.ic_external_link
//        )

        // GitHub Repo:
        SettingsDivider()
        SettingsAboutRow(
            label           = "GitHub",
            leadingIcon     = { SettingsRowIcon(R.drawable.ic_github, color = SettingsIconColors.github) },
            trailingIconRes = R.drawable.ic_external_link,
            onClick = {
                uriHandler.openUri(gitUrl)
            }
        )
    }
}


// ==========================================
// Private row helper
// ==========================================

@Composable
private fun SettingsAboutRow(
    label          : String,
    trailingIconRes: Int? = null,
    leadingIcon    : (@Composable () -> Unit)? = null,
    onClick        : () -> Unit = {}
) {
    SettingsRow(
        label           = label,
        onClick         = onClick,
        verticalPadding = 16.dp,
        leadingIcon     = leadingIcon,
        trailingContent = trailingIconRes?.let { iconRes -> {
            Icon(
                painter            = painterResource(iconRes),
                contentDescription = null,
                modifier           = Modifier.size(ICON_SIZE),
                tint               = iconTint()
            )
        }}
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
