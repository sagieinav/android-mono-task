package dev.sagi.monotask.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private val TAB_ORDER = TopLevelDestination.entries.map { it.routeQualifiedName }

const val navAnimationDuration = 300

internal fun isForward(from: String?, to: String?): Boolean {
    val fromIndex = TAB_ORDER.indexOf(from)
    val toIndex = TAB_ORDER.indexOf(to)
    if (fromIndex == -1 || toIndex == -1) return true
    return toIndex > fromIndex
}

internal fun tabSlideIn(from: String?, to: String?): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { if (isForward(from, to)) it else -it },
        animationSpec = tween(navAnimationDuration, easing = FastOutSlowInEasing)
    )

internal fun tabSlideOut(from: String?, to: String?): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { if (isForward(from, to)) -it else it },
        animationSpec = tween(navAnimationDuration, easing = FastOutSlowInEasing)
    )
