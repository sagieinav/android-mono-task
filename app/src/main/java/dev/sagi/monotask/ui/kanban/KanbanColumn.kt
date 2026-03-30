package dev.sagi.monotask.ui.kanban

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.component.display.CountBadge
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.ui.theme.customColors
import dev.sagi.monotask.ui.theme.gloock
import dev.sagi.monotask.ui.theme.monoShadowWorkaround
import dev.sagi.monotask.util.Constants.Theme.KANBAN_PADDING
import kotlinx.coroutines.delay

private const val ANIM_DURATION   = 250
private const val COLUMN_SLIDE_PX = -48  // column entrance: slides down from above

@Composable
fun KanbanColumn(
    title: String,
    importance: Importance,
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    isArchive: Boolean = false,
    onKanbanEvent: (KanbanEvent) -> Unit,
    animationDelayMs: Int = 0
) {
    // ========== Column entrance animation (first load only) ==========
    var columnVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(animationDelayMs.toLong())
        columnVisible = true
    }

    val colors = MaterialTheme.customColors
    val (containerColor, contentColor) = when (importance) {
        Importance.HIGH   -> colors.importanceHighBackground   to colors.importanceHighContent
        Importance.MEDIUM -> colors.importanceMediumBackground to colors.importanceMediumContent
        Importance.LOW    -> colors.importanceLowBackground    to colors.importanceLowContent
    }

    AnimatedVisibility(
        visible = columnVisible,
        enter   = slideInVertically(initialOffsetY = { COLUMN_SLIDE_PX }, animationSpec = tween(ANIM_DURATION)) +
                fadeIn(tween(ANIM_DURATION))
    ) {
        Column(
            modifier = modifier
                .width(170.dp)
//                .fillMaxHeight()
            ,
            // Header-Content gap
            verticalArrangement = Arrangement.spacedBy(KANBAN_PADDING)
        ) {
            val colShape = MaterialTheme.shapes.medium
            val cardShape = MaterialTheme.shapes.small

            // ========== Header ==========
            GlassSurface(
                blurred = false,
                shape = colShape,
                modifier = Modifier
                    .monoShadowWorkaround(colShape)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text        = title,
                        style       = MaterialTheme.typography.titleLarge.copy(
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Proportional,
                                trim      = LineHeightStyle.Trim.Both
                            )
                        ),
                        fontSize = 20.sp,
                        fontFamily  = gloock,
                        fontWeight  = FontWeight.Bold,
//                        color       = contentColor,
                    )

                    // Push to the right
                    Spacer(Modifier.weight(1f))

                    // Task count badge:
                    CountBadge(
                        count          = tasks.size,
                        color   = contentColor
                    )
                }
            }

            // ========== Body ==========
            GlassSurface(
                blurred = false,
                shape = colShape,
                modifier = Modifier
                    .fillMaxSize()
                    .monoShadowWorkaround(colShape)
            ) {
                AnimatedContent(
                    targetState    = tasks,
                    contentKey     = { isArchive },
                    transitionSpec = {
                        (slideInVertically(tween(ANIM_DURATION)) { COLUMN_SLIDE_PX } + fadeIn(tween(ANIM_DURATION))) togetherWith
                                fadeOut(tween(0))
                    },
                    label = "cards"
                ) { displayedTasks ->
                    LazyColumn(
                        modifier            = Modifier
                            .clip(cardShape),
                        verticalArrangement = Arrangement.spacedBy(KANBAN_PADDING),
                        // card padding, handles shadow overflow nicely:
                        contentPadding = PaddingValues(KANBAN_PADDING),
                    ) {
                        items(displayedTasks, key = { it.id }) { task ->
                            KanbanCard(
                                task          = task,
                                isArchive     = isArchive,
                                shape         = cardShape,
                                onKanbanEvent = onKanbanEvent
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(heightDp = 600, showBackground = true, backgroundColor = 0xEAEAEAFF)
@Composable
fun KanbanColumnPreview() {
    MonoTaskTheme {
        val fakeTasks = listOf(
            Task(id = "1", title = "Fix login crash",  importance = Importance.HIGH,   tags = listOf("auth", "bug")),
            Task(id = "2", title = "Write unit tests", importance = Importance.MEDIUM, tags = listOf("testing")),
            Task(id = "3", title = "Update README",    importance = Importance.LOW,    tags = listOf("docs")),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier              = Modifier.padding(16.dp)
        ) {
            KanbanColumn(title = "High",   tasks = fakeTasks.filter { it.importance == Importance.HIGH },   onKanbanEvent = {}, importance = Importance.HIGH,   animationDelayMs = 0)
            KanbanColumn(title = "Medium", tasks = fakeTasks.filter { it.importance == Importance.MEDIUM }, onKanbanEvent = {}, importance = Importance.MEDIUM, animationDelayMs = 80)
            KanbanColumn(title = "Low",    tasks = fakeTasks.filter { it.importance == Importance.LOW },    onKanbanEvent = {}, importance = Importance.LOW,    animationDelayMs = 160)
        }
    }
}
