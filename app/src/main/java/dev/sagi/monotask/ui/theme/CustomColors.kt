package dev.sagi.monotask.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

data class CustomColors(
    // Importance
    val importanceHighBackground: Color,
    val importanceHighContent: Color,
    val importanceMediumBackground: Color,
    val importanceMediumContent: Color,
    val importanceLowBackground: Color,
    val importanceLowContent: Color,
    // Custom tags
    val tagIndigo: Pair<Color, Color>,
    val tagTeal: Pair<Color, Color>,
    val tagPurple: Pair<Color, Color>,
    val tagBlue: Pair<Color, Color>,
    val tagCyan: Pair<Color, Color>,
    val tagBrown: Pair<Color, Color>,
    val tagBlueGrey: Pair<Color, Color>,
    val tagLime: Pair<Color, Color>,
) {
    val customTagColors get() = listOf(
        tagIndigo, tagTeal, tagPurple, tagBlue,
        tagCyan, tagBrown, tagBlueGrey, tagLime
    )

    // Hash function to generate a tag color. How to call it:
    // 1. val (containerColor, contentColor) = tagColorFor(tag)
    // 2. TaskTag(label = tag, containerColor = containerColor, contentColor = contentColor)
    fun tagColorFor(tag: String): Pair<Color, Color> {
        val index = abs(tag.lowercase().hashCode()) % customTagColors.size
        return customTagColors[index]
    }
}

val lightCustomColors = CustomColors(
    importanceHighBackground   = ImportanceHighBackground,
    importanceHighContent      = ImportanceHighContent,
    importanceMediumBackground = ImportanceMediumBackground,
    importanceMediumContent    = ImportanceMediumContent,
    importanceLowBackground    = ImportanceLowBackground,
    importanceLowContent       = ImportanceLowContent,

    tagIndigo   = TagIndigoBackground   to TagIndigoContent,
    tagTeal     = TagTealBackground     to TagTealContent,
    tagPurple   = TagPurpleBackground   to TagPurpleContent,
    tagBlue     = TagBlueBackground     to TagBlueContent,
    tagCyan     = TagCyanBackground     to TagCyanContent,
    tagBrown    = TagBrownBackground    to TagBrownContent,
    tagBlueGrey = TagBlueGreyBackground to TagBlueGreyContent,
    tagLime     = TagLimeBackground     to TagLimeContent,
)

val localCustomColors = staticCompositionLocalOf { lightCustomColors }

// Add my custom colors to the theme
val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = localCustomColors.current
