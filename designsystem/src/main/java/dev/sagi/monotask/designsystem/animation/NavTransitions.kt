package dev.sagi.monotask.designsystem.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Directional horizontal slide transitions for tab-level navigation.
 * The caller is responsible for computing [isForward] based on the current tab order.
 */
fun tabSlideIn(isForward: Boolean): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { if (isForward) it else -it },
        animationSpec = tween(MonoAnimations.TAB_TRANSITION_MS, easing = FastOutSlowInEasing)
    )

fun tabSlideOut(isForward: Boolean): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { if (isForward) -it else it },
        animationSpec = tween(MonoAnimations.TAB_TRANSITION_MS, easing = FastOutSlowInEasing)
    )
