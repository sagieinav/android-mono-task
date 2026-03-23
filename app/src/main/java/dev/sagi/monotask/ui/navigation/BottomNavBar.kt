@file:OptIn(ExperimentalHazeMaterialsApi::class)

package dev.sagi.monotask.ui.navigation

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.sagi.monotask.R
import dev.sagi.monotask.ui.component.core.GlassSurface
import dev.sagi.monotask.ui.theme.MonoTaskTheme
import dev.sagi.monotask.util.Constants

enum class NavTab(val label: String, val iconRes: Int) {
    BOARD("Board", R.drawable.ic_stack_tick),
    BRIEF("Brief", R.drawable.ic_flag_bolt),
    FOCUS("Focus", R.drawable.ic_focus),
    STATISTICS("Statistics", R.drawable.ic_statistics),
    PROFILE("MonoTask", R.drawable.ic_user_circle)
}

@Composable
fun BottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = NavTab.entries
    val selectedIndex = tabs.indexOf(selectedTab) // index of the enum NavTab

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ),
        label = "nav_dot_slide"
    )
    val dotColor = MaterialTheme.colorScheme.scrim

    GlassSurface(
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .padding(Constants.Theme.SCREEN_PADDING),
//            baseColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val tabWidth = maxWidth / tabs.size
            val dotX = tabWidth * (animatedIndex + 0.5f) - 2.dp // -2.dp centers the 4.dp dot

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    NavDockItem(
                        tab = tab,
                        isSelected = tab == selectedTab,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }

            // Sliding dot indicator
            Box(
                modifier = Modifier
                    .offset(x = dotX, y = 50.dp) // Boring math
                    .size(4.dp)
                    .background(dotColor, CircleShape)
            )
        }
    }
}


@Composable
private fun RowScope.NavDockItem(
    tab: NavTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconSize = 36.dp

    Box(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
//            .clip(MaterialTheme.shapes.medium)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Because of the Box, the icon is centered
        Icon(
            painter = painterResource(tab.iconRes),
            contentDescription = tab.label,
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
    var selected by remember { mutableStateOf(NavTab.FOCUS) }

    MonoTaskTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter
        ) {

            // ========== THE FOREGROUND (hazeEffect) ==========
            BottomNavBar(
                selectedTab = selected,
                onTabSelected = { selected = it },
            )
        }
    }
}