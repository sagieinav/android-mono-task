package dev.sagi.monotask.ui.brief

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.display.StateMessage
import dev.sagi.monotask.ui.component.display.IllustrationSize
import dev.sagi.monotask.ui.theme.LocalScaffoldPadding
import dev.sagi.monotask.ui.theme.MonoTaskTheme

enum class BriefStatus { ALL_CLEAR, ON_TRACK, OVERDUE }

@Composable
fun BriefStatus(
    status: BriefStatus,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val (imgRes, title, subtitle) = when (status) {
        BriefStatus.ALL_CLEAR -> Triple(
            R.drawable.img_brief_clear,
            "Nothing to report",
            "You have no tasks for today."
        )
        BriefStatus.ON_TRACK -> Triple(
            R.drawable.img_brief_ontrack,
            "You're on track",
            "Keep working through your open tasks."
        )
        BriefStatus.OVERDUE -> Triple(
            R.drawable.img_brief_overdue,
            "Time to catch up",
            "You've got overdue tasks to address."
        )
    }

    StateMessage(
        imgRes   = imgRes,
        title    = title,
        subtitle = subtitle,
        size     = IllustrationSize.Small,
        animate  = animate,
        modifier = modifier
            .padding(vertical = 20.dp, horizontal = 16.dp)
    )
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
