package dev.sagi.monotask.ui.settings.components

import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.components.GlassSurface
import dev.sagi.monotask.designsystem.components.MonoTooltip
import dev.sagi.monotask.designsystem.components.SectionTitle
import dev.sagi.monotask.designsystem.util.Constants.Theme.SCREEN_PADDING
import dev.sagi.monotask.designsystem.util.Constants.Theme.TRAILING_BUTTON_SIZE


// ==========================================
// Leading icon color constants
// ==========================================

object SettingsIconColors {
    val hyperfocus = Color(0xFFA21F39)
    val priority = Color(0xFFDC4F07)
    val workspace = Color(0xFF008F72)
    val accountName = Color(0xFF3949AB)
    val email = Color(0xFF00B3C7)
    val github = Color(0xFF2AA843)
}

// ==========================================
// Settings row leading icon
// ==========================================

@Composable
fun SettingsRowIcon(
    iconRes : Int,
    modifier : Modifier = Modifier,
    iconModifier : Modifier = Modifier,
    color : Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
) {
    GlassSurface(
        shape = MaterialTheme.shapes.extraSmall,
        accentColor = color,
        modifier = modifier.size(34.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color.copy(alpha = 0.9f),
            modifier = iconModifier
                .size(26.dp)
                .align(Alignment.Center)
        )
    }
}


// ==========================================
// Generic section container
// ==========================================

@Composable
internal fun SettingsSection(
    title : String,
    content: @Composable ColumnScope.() -> Unit
) {
    SectionTitle(text = title)
    GlassSurface(
        blurred = false,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        baseColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            content = content
        )
    }
}


// ==========================================
// Generic row container
// ==========================================

@Composable
internal fun SettingsRow(
    modifier : Modifier = Modifier,
    label : String? = null,
    infoText : String? = null,
    leadingIcon : (@Composable () -> Unit)? = null,
    trailingContent : (@Composable () -> Unit)? = null,
    onClick : (() -> Unit)? = null,
    verticalPadding : Dp = 16.dp,
    content : (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp, vertical = verticalPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            leadingIcon?.let {
                it()
                Spacer(Modifier.width(12.dp))
            }

            Box(Modifier.weight(1f)) {
                if (label != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Normal
                        )
                        if (infoText != null) {
                            InfoIconButton(infoText)
                        }
                    }
                } else {
                    content?.invoke()
                }
            }

            trailingContent?.invoke()
        }

        // Extra content below the label row (e.g. slider), indented past the icon
        if (label != null && content != null) {
            Row(modifier = Modifier.padding(top = 4.dp)) {
                leadingIcon?. let {
                    Spacer(Modifier.width(46.dp))
                }
                Box(Modifier.weight(1f)) {
                    content()
                }
            }
        }
    }
}


// ==========================================
// Action icon button (edit/delete..)
// ==========================================

@Composable
internal fun SettingsActionIconButton(
    iconRes : Int,
    contentDescription : String,
    onClick : () -> Unit,
    modifier : Modifier = Modifier,
    enabled : Boolean = true,
    tint : Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(TRAILING_BUTTON_SIZE + 8.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(TRAILING_BUTTON_SIZE),
            tint = tint
        )
    }
}


// ==========================================
// Info icon button with tooltip
// ==========================================

@Composable
internal fun InfoIconButton(text: String, size: Dp = 20.dp) {
    var showTooltip by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { showTooltip = true },
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(size + 2.dp)
        ) {
            Icon(
                painter = painterResource(IconPack.InfoCircle),
                contentDescription = "More info",
                modifier = Modifier.size(size),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
        }
        MonoTooltip(
            expanded = showTooltip,
            onDismiss = { showTooltip = false }
        ) {
            Text(text)
        }
    }
}


// ==========================================
// Divider
// ==========================================

@Composable
internal fun SettingsDivider(startPadding: Dp = 60.dp) {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        modifier = Modifier.padding(start = startPadding, end = SCREEN_PADDING)
    )
}


