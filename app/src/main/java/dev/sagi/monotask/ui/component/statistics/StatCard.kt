package dev.sagi.monotask.ui.component.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.component.core.GlassSurface

// Usage: StatCard(title = "XP this week") { ...content... }
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    headline: String? = null,
    badge: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassSurface(
        modifier  = modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.large,
        baseColor = MaterialTheme.colorScheme.surfaceContainer,
        blurred   = false
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header & Headline (if any)
            if (title != null || headline != null) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column {
                        if (title != null) {
                            Text(
                                text  = title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                            )
                        }
                        if (headline != null) {
                            Text(
                                text       = headline,
                                style      = MaterialTheme.typography.headlineMedium,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    badge?.invoke()
                }
            }
            content()
        }
    }
}
