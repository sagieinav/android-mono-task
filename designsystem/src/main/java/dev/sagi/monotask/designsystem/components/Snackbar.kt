package dev.sagi.monotask.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.R
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.googleSansRounded
import dev.sagi.monotask.designsystem.theme.monoShadow

private val SnackbarIconSize = 26.dp

data class MonoSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    @param:DrawableRes val leadingIcon: Int? = null
) : SnackbarVisuals

@Composable
fun MonoSnackbarDismissible(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    // Create the state that listens for the swipe gesture
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue != SwipeToDismissBoxValue.Settled) {
                snackbarData.dismiss()
                true
            } else {
                false
            }
        }
    )

    // Wrap the whole custom snackbar in a "Swipe to Dismiss" box
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { Spacer(modifier = Modifier.fillMaxSize()) },
        content = { MonoSnackbar(snackbarData, modifier) }
    )
}


@Composable
fun MonoSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    val monoVisuals = snackbarData.visuals as? MonoSnackbarVisuals

    GlassSurface(
        shape = CircleShape,
        baseColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .padding(10.dp) // external padding
            .wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp), // internal padding
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // (Optional) Leading Icon
            monoVisuals?.leadingIcon?. let {
                Icon(
                    painter = painterResource(it),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(SnackbarIconSize)
                )
            }

            // The main message (Takes up remaining space)
            Text(
                text = snackbarData.visuals.message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                fontFamily = googleSansRounded,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 22.dp)
            )

            // (Optional) Action Icon
            snackbarData.visuals.actionLabel?. let {
                // If an action is provided, SHOW ACTION
                IconButton(
                    onClick = { snackbarData.performAction() },
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .heightIn(max = SnackbarIconSize)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_undo),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Undo",
                        modifier = Modifier.size(SnackbarIconSize)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MonoSnackbarPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MonoSnackbar(
                snackbarData = object : SnackbarData {
                    override val visuals = MonoSnackbarVisuals(
                        message = "Task completed",
                        actionLabel = "Undo"
                    )
                    override fun dismiss() {}
                    override fun performAction() {}
                }
            )
        }
    }
}
