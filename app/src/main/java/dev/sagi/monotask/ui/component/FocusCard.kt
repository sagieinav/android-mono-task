import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import dev.sagi.monotask.R
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.CustomTag
import dev.sagi.monotask.ui.component.ImportanceTag
import dev.sagi.monotask.ui.component.XpDeltaBadge
import dev.sagi.monotask.ui.component.aceGlowBorder
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.util.toFormattedDate

@Composable
fun TaskCard(
    task: Task,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 28;
    val shape = RoundedCornerShape(cornerRadius.dp)

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        // tonalElevation for emphasizing the glass card look
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxSize()
            // AceGlowBorder wraps the card only if task.isAce
            .then(
                if (task.isAce) Modifier.aceGlowBorder(cornerRadius = cornerRadius.dp)
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ========== Top: Due date (if present) ==========
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                task.dueDate?.let { timestamp ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_due_calendar),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                        Text(
                            text = timestamp.toFormattedDate(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }

                XpDeltaBadge(20, true)
            }

            // ========== Middle: title + description ==========
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
//                    fontSize = 36.sp,
//                    lineHeight = 42.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
//                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ========== Bottom: tags ==========
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImportanceTag(importance = task.importance)
                if (task.tags.isNotEmpty()) {
                    task.tags.forEach { tag ->
                        CustomTag(label = tag)
                    }
                }

//                // Due date pushed to the end
//                task.dueDate?.let { timestamp ->
//                    Spacer(Modifier.weight(1f))
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_due_calendar),
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp),
//                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
//                        )
//                        Text(
//                            text = timestamp.toFormattedDate(),
//                            style = MaterialTheme.typography.labelMedium,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
//                        )
//                    }
//                }
            }
        }
    }
}

// ── Importance badge ──────────────────────────────────────────────────────────
@Composable
private fun ImportanceBadge(importance: Importance) {
    val (label, containerColor, contentColor) = when (importance) {
        Importance.HIGH   -> Triple(
            "High Priority",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        Importance.MEDIUM -> Triple(
            "Medium",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        Importance.LOW    -> Triple(
            "Low",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = RoundedCornerShape(100),
        color = containerColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun TaskCardPreview() {
    MonoTaskTheme {
        Box(
            modifier = Modifier
                .size(360.dp)
                .padding(18.dp)
        ) {
            TaskCard(
                task = Task(
                    id = "1",
                    title = "Finish Algorithms Task",
                    description = "Complete sections 3 and 4, submit before midnight.",
                    importance = Importance.HIGH,
                    tags = listOf("CS101", "ds"),
                    snoozeCount = 0, // isAce = true → gold border
                    dueDate = Timestamp.now()
                )
            )
        }
    }
}
