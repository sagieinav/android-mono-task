package dev.sagi.monotask.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

data class CustomColors(
    // Concept colors: single source of truth for semantic roles
    val xp     : Color,   // XP / level accent
    val streak : Color,   // streak fire accent
    val ace    : Color,   // ace task gold accent
    val aceDim : Color,   // ace gold, slightly darker (borders, muted accents)
    // Achievement tier badge background colors (static metallic shades)
    val achievementGold   : Color,
    val achievementSilver : Color,
    val achievementBronze : Color,
    val achievementLocked : Color,
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
    val chartColors: List<Color>,
) {
    val customTagColors get() = listOf(
        tagIndigo, tagTeal, tagPurple, tagBlue,
        tagCyan, tagBrown, tagBlueGrey, tagLime
    )


    // Returns a (containerColor, contentColor) pair for a tag label.
    fun tagColorFor(tag: String): Pair<Color, Color> {
        val index = abs(tag.lowercase().hashCode()) % customTagColors.size
        return customTagColors[index]
    }

    // Returns a single Color for any use
    fun colorFor(key: String): Color {
        val index = abs(key.lowercase().hashCode()) % customTagColors.size
        return customTagColors[index].second   // content (vivid) color
    }
}

val lightCustomColors = CustomColors(
    xp              = XpViolet,
    streak          = StreakFire,
    ace             = AceGold,
    aceDim          = AceGoldDim,
    achievementGold   = Color(0xFFEFB73D),
    achievementSilver = Color(0xFFCACACC),
    achievementBronze = Color(0xFFB78457),
    achievementLocked = Color(0xFFE1E1E1),

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

    chartColors = listOf(
        Color(0xFF4F6EF7),  // indigo blue
        Color(0xFF9B4DCA),  // violet
        Color(0xFF0EA875),  // emerald
        Color(0xFFE8622A),  // burnt orange
        Color(0xFF2BB5D8),  // cyan
        Color(0xFFD4455E),  // crimson
        Color(0xFF3DBD8A),  // teal green
        Color(0xFFE0A020),  // golden amber
    )
)


// Add custom colors to the theme
val MaterialTheme.customColors: CustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColors.current
