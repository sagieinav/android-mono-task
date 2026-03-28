package dev.sagi.monotask.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.R

// ─────────────────────────────────────────────────────
// CATEGORY 1 — HERO / DISPLAY (Headlines, Focus Card title)
// Used for: headlineLarge, headlineMedium, displayLarge
// ─────────────────────────────────────────────────────
val gloock = FontFamily(
    Font(R.font.gloock, FontWeight.Normal)
)

val playfairDisplay = FontFamily(
    Font(R.font.playfair_display_bold,        FontWeight.Bold),
    Font(R.font.playfair_display,        FontWeight.SemiBold),
    Font(R.font.playfair_display,        FontWeight.Bold),
    Font(R.font.playfair_display_italic, FontWeight.Normal,  FontStyle.Italic),
)

// ─────────────────────────────────────────────────────
// CATEGORY 2 — BODY / READING SERIF (Descriptions, onboarding, profile)
// Used for: bodyLarge, bodyMedium, headlineSmall
// ─────────────────────────────────────────────────────

val lora = FontFamily(
    Font(R.font.lora,        FontWeight.Normal),
    Font(R.font.lora,        FontWeight.Medium),
    Font(R.font.lora,        FontWeight.SemiBold),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
)

val libreCaslon = FontFamily(
    Font(R.font.libre_caslon_text,        FontWeight.Normal),
    Font(R.font.libre_caslon_text_bold,   FontWeight.Bold),
    Font(R.font.libre_caslon_text_italic, FontWeight.Normal, FontStyle.Italic),
)

val notoSerif = FontFamily(
    Font(R.font.noto_serif,        FontWeight.Normal, FontStyle.Normal),
    Font(R.font.noto_serif_italic, FontWeight.Normal, FontStyle.Italic),
)

val plantagenet = FontFamily(
    Font(R.font.plantagenet, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.plantagenet_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.plantagenet, FontWeight.Bold, FontStyle.Normal)
)

val ptSerif = FontFamily(
    Font(R.font.pt_serif, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.pt_serif_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.pt_serif, FontWeight.Bold, FontStyle.Normal)
)

val merriweather = FontFamily(
    Font(R.font.merriweather, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.merriweather_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.merriweather, FontWeight.Bold, FontStyle.Normal)
)

// ─────────────────────────────────────────────────────
// CATEGORY 3 — UI CHROME (Buttons, chips, labels, nav, titles)
// Used for: titleLarge/Medium/Small, labelLarge/Medium, bodySmall
// ─────────────────────────────────────────────────────


val mplusRounded = FontFamily(
    Font(R.font.mplus_rounded)
)

val nationalPark = FontFamily(
    Font(R.font.national_park),
    Font(R.font.national_park_semi_bold, FontWeight.SemiBold),
    Font(R.font.national_park_bold, FontWeight.Bold)
)


// ─────────────────────────────────────────────────────
// CATEGORY 4 — DATA (small labels)
// ─────────────────────────────────────────────────────
val plusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans,        FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium,        FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semi_bold,        FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold,        FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_italic, FontWeight.Normal, FontStyle.Italic),
)

val googleSans = FontFamily(
    Font(R.font.google_sans,        FontWeight.Normal),
    Font(R.font.google_sans_medium,        FontWeight.Medium),
    Font(R.font.google_sans_semi_bold,        FontWeight.SemiBold),
    Font(R.font.google_sans_bold,        FontWeight.Bold),
    Font(R.font.google_sans_italic, FontWeight.Normal, FontStyle.Italic),
)
val harabara = FontFamily(
    Font(R.font.harabara, FontWeight.Normal),
    Font(R.font.harabara_bold,  FontWeight.Bold),
)

// ─────────────────────────────────────────────────────
// ACTIVE SELECTION
// To swap a font, change the variable assigned here.
// e.g. replace `gloock` with `playfairDisplay` for titles.
// ─────────────────────────────────────────────────────
private val heroFont        = gloock            // gloock, playfairDisplay
private val uiFont          = nationalPark       // mplusRounded, nationalPark
private val dataFont        = googleSans        // googleSans, plusJakartaSans
private val contentFont     = lora              // libreCaslon, notoSerif, lora, plantagenet, ptSerif, merriweather

// ─────────────────────────────────────────────────────
// TYPOGRAPHY SCALE
// M3 has 15 named styles. Rule of thumb:
//   display/headline → hero content
//   title            → section headers, screen names
//   body             → reading text
//   label            → UI chrome (smallest, most frequent)
// ─────────────────────────────────────────────────────

// Helper to remove line height padding
private fun TextStyle.withNoPadding() = copy(
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)
val baseline = Typography()

val AppTypography = Typography(

    // ========== DISPLAY (largest headings) ==========
    displayLarge  = baseline.displayLarge.copy(fontFamily = heroFont).withNoPadding(),
    displayMedium = baseline.displayMedium.copy(fontFamily = heroFont).withNoPadding(),
    displaySmall  = baseline.displaySmall.copy(fontFamily = heroFont).withNoPadding(),

    // ========== HEADLINE ==========
    headlineLarge  = baseline.headlineLarge.copy(fontFamily = heroFont).withNoPadding(),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = heroFont).withNoPadding(),
    headlineSmall  = baseline.headlineSmall.copy(fontFamily = uiFont).withNoPadding(),

    // ========== TITLE, UI ==========
    titleLarge  = baseline.titleLarge.copy(
        fontFamily = uiFont, fontWeight = FontWeight.Bold).withNoPadding(),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = uiFont, fontWeight = FontWeight.SemiBold).withNoPadding(),
    titleSmall  = baseline.titleSmall.copy(
        fontFamily = uiFont).withNoPadding(),

    // ========== SMALL LABELS ==========
    labelLarge  = baseline.labelLarge.copy(
        fontFamily = plusJakartaSans,
        fontWeight = FontWeight.Bold
    ).withNoPadding(),
    labelMedium = baseline.labelMedium.copy(
        fontFamily = dataFont, fontWeight = FontWeight.Bold).withNoPadding(),
    labelSmall  = baseline.labelSmall.copy(
        fontFamily = dataFont, fontSize = 10.sp).withNoPadding(),

    // ========== BODY / CONTENT ==========
    bodyLarge  = baseline.bodyLarge.copy(fontFamily = contentFont).withNoPadding(),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = contentFont).withNoPadding(),
    bodySmall  = baseline.bodySmall.copy(fontFamily = uiFont).withNoPadding()
)