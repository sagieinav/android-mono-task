@file:OptIn(ExperimentalHazeMaterialsApi::class)

package dev.sagi.monotask.ui.shell

import dev.sagi.monotask.ui.navigation.TopLevelDestination
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.sagi.monotask.designsystem.theme.LocalHazeState
import dev.sagi.monotask.designsystem.theme.MonoTaskTheme
import dev.sagi.monotask.designsystem.theme.glassBackground
import dev.sagi.monotask.designsystem.theme.glassBorderPremium
import dev.sagi.monotask.designsystem.theme.monoShadow
import dev.sagi.monotask.designsystem.util.Constants

@Composable
fun BottomNavBar(
    selectedDestination: TopLevelDestination,
    onDestinationSelected: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = TopLevelDestination.entries
    val selectedIndex = destinations.indexOf(selectedDestination)

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ),
        label = "nav_dot_slide"
    )
    val dotColor = MaterialTheme.colorScheme.scrim
    val hazeState = LocalHazeState.current
    val shape = MaterialTheme.shapes.large

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Constants.Theme.SCREEN_PADDING)
//            .monoShadowWorkaround(shape)
            .monoShadow(shape)
            .clip(shape)
            .hazeEffect(hazeState, HazeMaterials.ultraThin())
            .glassBorderPremium(shape)
            .glassBackground(baseColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val dotSize = 4.dp
            val tabWidth = maxWidth / destinations.size
            val dotX = tabWidth * (animatedIndex + 0.5f) - dotSize / 2

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEach { dest ->
                    NavDockItem(
                        destination = dest,
                        isSelected = dest == selectedDestination,
                        onClick = { onDestinationSelected(dest) }
                    )
                }
            }

            // Sliding dot indicator
            Box(
                modifier = Modifier
                    .offset(x = dotX, y = 50.dp)
                    .size(4.dp)
                    .background(dotColor, CircleShape)
            )
        }
    }
}


@Composable
private fun RowScope.NavDockItem(
    destination: TopLevelDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconSize = 36.dp

    Box(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(destination.iconRes),
            contentDescription = destination.label,
            modifier = Modifier.size(iconSize),
            tint = if (isSelected)
                MaterialTheme.colorScheme.scrim
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}


// ========== Preview ==========
@Preview(showBackground = true)
@Composable
fun BottomNavBarPreview() {
    var selected by remember { mutableStateOf(TopLevelDestination.FOCUS) }

    MonoTaskTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomNavBar(
                selectedDestination = selected,
                onDestinationSelected = { selected = it },
            )
        }
    }
}
