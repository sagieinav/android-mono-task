package dev.sagi.monotask.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sagi.monotask.designsystem.R
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.monoShadow
import dev.sagi.monotask.designsystem.theme.penaltyRed


private val ButtonHeight = 56.dp

@Composable
fun ActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable RowScope.() -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (enabled) color else MaterialTheme.colorScheme.outlineVariant,
        label = "ActionButtonColor"
    )

    GlassSurface(
        modifier = modifier
            .then(
                if (enabled) Modifier.monoShadow(shape = shape, strength = 0.5f)
                else Modifier
            )
            .fillMaxWidth()
            .height(ButtonHeight)
            .clip(shape)
            .clickable(
                enabled = enabled,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        shape = shape,
        baseColor = MaterialTheme.colorScheme.surfaceContainer,
        blurred = false
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}





@Preview(showBackground = true, backgroundColor = 0xFFF5F0EB)
@Composable
private fun ActionButtonPrimaryPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ActionButton(onClick = {}) {
                Text("Save Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0EB)
@Composable
private fun ActionButtonWithIconPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ActionButton(onClick = {}) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0EB)
@Composable
private fun ActionButtonDestructivePreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ActionButton(onClick = {}, color = penaltyRed) {
                Icon(painterResource(R.drawable.ic_delete_alt), contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Delete Task", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0EB)
@Composable
private fun ActionButtonDisabledPreview() {
    MonoTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ActionButton(onClick = {}, enabled = false) {
                Text("Save Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
