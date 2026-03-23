package dev.sagi.monotask.ui.component.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.ui.component.core.GlassSurface

// Usage: StatCard(title = "XP Earned") { ...content... }
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    headlineValue: String? = null,
    headlineUnit: String? = null,
    badge: (@Composable () -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassSurface(
        modifier  = modifier.fillMaxWidth(),
        shape     = shape,
        baseColor = MaterialTheme.colorScheme.surfaceContainer,
        blurred   = false
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header & Headline (if any)
            if (title != null || headlineValue != null) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.Top
                ) {
                    Column {
                        title?. let {
                            Text(
                                text  = title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                            )
                        }
                        headlineValue?. let {
                            ValueLabel(headlineValue, headlineUnit)
                        }
                    }
                    badge?.invoke()
                }
            }
            content()
        }
    }
}
