package dev.sagi.monotask.ui.component.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.util.ext.toRelativeDate

@Composable
fun DueDateLabel(
    timestamp: Timestamp,
    small: Boolean = false,
    modifier: Modifier = Modifier
) {
    val relativeDate = timestamp.toRelativeDate()
    val dateColor = if (relativeDate.isOverdue)
        MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = if (small) 0.4f else 0.45f)

    val textStyle =
        MaterialTheme.typography.labelLarge.copy(
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Proportional,
                trim = LineHeightStyle.Trim.Both
            )
        )

    val fontSize = if (small) 9.sp else 14.sp

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        // Icon only for big label (dropped for big label as well for now)
//        if (!small) {
//            Icon(
//                painter = painterResource(R.drawable.ic_due_soon),
//                contentDescription = null,
//                modifier = Modifier
//                    .size(18.dp)
//                    .padding(1.dp),
//                tint = dateColor
//            )
//        }

        Text(
            text = relativeDate.text,
            style = textStyle,
            fontSize = fontSize,
            fontWeight = if (small) FontWeight.Normal else FontWeight.SemiBold,
            color = dateColor,
            textAlign = TextAlign.Center,
        )
    }
}


// ========== Previews ==========

@Preview(showBackground = true, name = "DueDateLabel, Upcoming")
@Composable
private fun DueDateLabelUpcomingPreview() {
    MonoTaskTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DueDateLabel(timestamp = Timestamp(System.currentTimeMillis() / 1000 + 86400 * 2, 0))
            DueDateLabel(timestamp = Timestamp(System.currentTimeMillis() / 1000 + 86400 * 2, 0), small = true)
        }
    }
}

@Preview(showBackground = true, name = "DueDateLabel, Overdue")
@Composable
private fun DueDateLabelOverduePreview() {
    MonoTaskTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DueDateLabel(timestamp = Timestamp(System.currentTimeMillis() / 1000 - 86400 * 2, 0))
            DueDateLabel(timestamp = Timestamp(System.currentTimeMillis() / 1000 - 86400 * 2, 0), small = true)
        }
    }
}