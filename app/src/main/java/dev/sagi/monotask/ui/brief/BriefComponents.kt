package dev.sagi.monotask.ui.brief

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import dev.sagi.monotask.designsystem.theme.IconPack
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.designsystem.components.CountBadge
import dev.sagi.monotask.designsystem.components.GlassSurface
import dev.sagi.monotask.designsystem.components.StateMessage
import dev.sagi.monotask.designsystem.components.IllustrationSize
import dev.sagi.monotask.designsystem.theme.LocalCustomColors
import dev.sagi.monotask.designsystem.theme.LocalScaffoldPadding
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.glassBackground
import dev.sagi.monotask.util.toRelativeDate
import kotlin.collections.forEach

enum class BriefStatus { ALL_CLEAR, ON_TRACK, OVERDUE }

@Composable
fun BriefStatus(
    status: BriefStatus,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val (imgRes, title, subtitle) = when (status) {
        BriefStatus.ALL_CLEAR -> Triple(
            IconPack.ImgBriefClear,
            "Nothing to report",
            "You have no tasks for today."
        )
        BriefStatus.ON_TRACK -> Triple(
            IconPack.ImgBriefOntrack,
            "You're on track",
            "Keep working through your open tasks."
        )
        BriefStatus.OVERDUE -> Triple(
            IconPack.ImgBriefOverdue,
            "Time to catch up",
            "You've got overdue tasks to address."
        )
    }

    StateMessage(
        imgRes = imgRes,
        title = title,
        subtitle = subtitle,
        size = IllustrationSize.Small,
        animate = animate,
        modifier = modifier
            .padding(vertical = 20.dp, horizontal = 16.dp)
    )
}

@Composable
fun ExpandableBriefSection(
    label: String,
    tasks: List<Task>,
    workspaceNames: Map<String, String>,
    color: Color,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "chevron_$label"
    )

    GlassSurface(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable { onExpandChange(!expanded) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CountBadge(
                        count = tasks.size,
                        color = color
                    )
                    Icon(
                        painter = painterResource(IconPack.Chevron),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(chevronRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded task list
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (tasks.isEmpty()) {
                        Text(
                            text = "No tasks here",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        tasks.forEach { task ->
                            BriefTaskRow(task = task, workspaceNames = workspaceNames)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BriefTaskRow(task: Task, workspaceNames: Map<String, String>) {
    val customColors = LocalCustomColors.current
    val subtleColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    val importanceColor = when (task.importance) {
        Importance.HIGH -> customColors.importanceHighContent
        Importance.MEDIUM -> customColors.importanceMediumContent
        Importance.LOW -> customColors.importanceLowContent
    }

    val workspaceName = workspaceNames[task.workspaceId] ?: task.workspaceId
    val dueDateStr = task.dueDate?.toRelativeDate()?.text

    val subtitleParts = buildList {
        add(workspaceName)
        dueDateStr?.let {
            add(dueDateStr)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Left border accent
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .padding(top = 4.dp, bottom = 2.dp)
                .clip(CircleShape)
                .glassBackground(baseColor = importanceColor)
        )
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitleParts.joinToString(separator = "  ·  "),
                style = MaterialTheme.typography.labelSmall,
                color = subtleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}



@Preview(showBackground = true, name = "BriefStatusCard: ALL_CLEAR")
@Composable
private fun BriefStatusAllClearPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            BriefStatus(status = BriefStatus.ALL_CLEAR, modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "BriefStatusCard: ON_TRACK")
@Composable
private fun BriefStatusOnTrackPreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            BriefStatus(status = BriefStatus.ON_TRACK, modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "BriefStatusCard: OVERDUE")
@Composable
private fun BriefStatusOverduePreview() {
    MonoTaskTheme {
        CompositionLocalProvider(LocalScaffoldPadding provides PaddingValues()) {
            BriefStatus(status = BriefStatus.OVERDUE, modifier = Modifier.padding(16.dp))
        }
    }
}
