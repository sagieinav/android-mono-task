package dev.sagi.monotask.designsystem.animation

/**
 * Central animation timing constants for MonoTask.
 * Reference these instead of scattering magic numbers across the codebase.
 */
object MonoAnimations {
    // Navigation
    const val TAB_TRANSITION_MS = 300

    // Focus card lifecycle
    const val CARD_EXIT_MS       = 280
    const val CARD_ENTRY_FADE_MS = 200
    const val CARD_ENTRY_SLIDE_MS = 350
    const val BORDER_ANIM_MS     = 1000
}
