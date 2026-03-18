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

// Option A: Gloock — editorial, magazine-grade serif. ← ACTIVE DEFAULT
val gloock = FontFamily(
    Font(R.font.gloock, FontWeight.Normal)
)

val playfairDisplay = FontFamily(
    Font(R.font.playfair_display_bold,        FontWeight.Bold),
    Font(R.font.playfair_display,        FontWeight.SemiBold),
    Font(R.font.playfair_display,        FontWeight.Bold),
    Font(R.font.playfair_display_italic, FontWeight.Normal,  FontStyle.Italic),
)

// Other fonts considered: Playfair Display, Libre Caslon, Noto Serif, Google Sans

// ─────────────────────────────────────────────────────
// CATEGORY 2 — BODY / READING SERIF (Descriptions, onboarding, profile)
// Used for: bodyLarge, bodyMedium, headlineSmall
// ─────────────────────────────────────────────────────

// Option A: Lora — warm, screen-optimized serif. ← ACTIVE DEFAULT
// Variable font: reference same file for all weights.
val lora = FontFamily(
    Font(R.font.lora,        FontWeight.Normal),
    Font(R.font.lora,        FontWeight.Medium),
    Font(R.font.lora,        FontWeight.SemiBold),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
)

// Option B: Libre Caslon Text — traditional print serif, heavier weight.
// Has separate bold file unlike the variable fonts above.
val libreCaslon = FontFamily(
    Font(R.font.libre_caslon_text,        FontWeight.Normal),
    Font(R.font.libre_caslon_text_bold,   FontWeight.Bold),
    Font(R.font.libre_caslon_text_italic, FontWeight.Normal, FontStyle.Italic),
)

// Option C: Noto Serif — highly neutral, great multilingual support.
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

// Option A: Plus Jakarta Sans — geometric, purpose-built for UI. ← ACTIVE DEFAULT
// Variable font: one file handles all weights.
val plusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans,        FontWeight.Normal),
    Font(R.font.plus_jakarta_sans,        FontWeight.Medium),
    Font(R.font.plus_jakarta_sans,        FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold,        FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_italic, FontWeight.Normal, FontStyle.Italic),
)

// Option B: Google Sans — clean, modern, Google's own product font.
val googleSans = FontFamily(
    Font(R.font.google_sans,        FontWeight.Normal),
    Font(R.font.google_sans,        FontWeight.Medium),
    Font(R.font.google_sans_italic, FontWeight.Normal, FontStyle.Italic),
)


val mplusRounded = FontFamily(
    Font(R.font.mplus_rounded)
)

val nationalPark = FontFamily(
    Font(R.font.national_park)
)

val asap = FontFamily(
    Font(R.font.asap)
)

val NationalParkTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = nationalPark,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontFamily = nationalPark,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = TextStyle(
        fontFamily = nationalPark,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    ),
    titleSmall = TextStyle(
        fontFamily = nationalPark,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),
    labelLarge = TextStyle(
        fontFamily = nationalPark,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),

)


// ─────────────────────────────────────────────────────
// CATEGORY 4 — DATA / MONOSPACE (XP values, levels, due dates, stats)
// Used for: labelSmall
// ─────────────────────────────────────────────────────

// IBM Plex Mono — precise, intentional, technical. Only option needed.
val ibmPlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_italic,  FontWeight.Normal, FontStyle.Italic),
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
private val uiFont          = nationalPark       // Normal Sans: googleSans, roboto, plusJakartaSans
                                            // Rounded Sans: mplusRounded, nationalPark
private val dataFont        = googleSans       // no alternative needed
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

    displayLarge  = baseline.displayLarge.copy(fontFamily = heroFont).withNoPadding(),
    displayMedium = baseline.displayMedium.copy(fontFamily = heroFont).withNoPadding(),
    displaySmall  = baseline.displaySmall.copy(fontFamily = heroFont).withNoPadding(),

    headlineLarge  = baseline.headlineLarge.copy(fontFamily = heroFont).withNoPadding(),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = heroFont).withNoPadding(),
    headlineSmall  = baseline.headlineSmall.copy(fontFamily = uiFont).withNoPadding(),

    titleLarge  = baseline.titleLarge.copy(
        fontFamily = uiFont, fontWeight = FontWeight.Bold).withNoPadding(),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = uiFont, fontWeight = FontWeight.SemiBold).withNoPadding(),
    titleSmall  = baseline.titleSmall.copy(
        fontFamily = uiFont).withNoPadding(),

    labelLarge  = baseline.labelLarge.copy(
        fontFamily = plusJakartaSans,
        fontWeight = FontWeight.Bold
    ).withNoPadding(),

    labelMedium = baseline.labelMedium.copy(
        fontFamily = dataFont, fontWeight = FontWeight.Bold).withNoPadding(),
    labelSmall  = baseline.labelSmall.copy(
        fontFamily = dataFont, fontSize = 10.sp).withNoPadding(),

    bodyLarge  = baseline.bodyLarge.copy(fontFamily = contentFont).withNoPadding(),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = contentFont).withNoPadding(),
    bodySmall  = baseline.bodySmall.copy(fontFamily = uiFont).withNoPadding()
)

//val AppTypography = Typography(
//
//    // ========== DISPLAY (largest headings) ==========
//    displayLarge  = baseline.displayLarge.copy(fontFamily = heroFont),
//    displayMedium = baseline.displayMedium.copy(fontFamily = heroFont),
//    displaySmall  = baseline.displaySmall.copy(fontFamily = heroFont),
//
//    // ========== HEADLINE ==========
//    headlineLarge  = baseline.headlineLarge.copy(fontFamily = heroFont),
//    headlineMedium = baseline.headlineMedium.copy(fontFamily = heroFont),
//    headlineSmall  = baseline.headlineSmall.copy(fontFamily = uiFont),
////    headlineSmall  = NationalParkTypography.headlineSmall,
//
//    // ========== TITLE, UI ==========
//    titleLarge  = baseline.titleLarge.copy(
//        fontFamily = uiFont, fontWeight = FontWeight.Bold),
//    titleMedium = baseline.titleMedium.copy(
//        fontFamily = uiFont, fontWeight = FontWeight.SemiBold,
//    ),
////    titleLarge = NationalParkTypography.titleLarge,
////    titleMedium = NationalParkTypography.titleMedium,
//    titleSmall  = baseline.titleSmall.copy(
//        fontFamily = uiFont,
//    ),
//
//    // ========== SMALL LABELS ==========
//    labelLarge  = baseline.labelLarge.copy(
//        fontFamily = plusJakartaSans,
//        fontWeight = FontWeight.Bold,
//        lineHeightStyle = LineHeightStyle(
//            alignment = LineHeightStyle.Alignment.Center,
//            trim = LineHeightStyle.Trim.FirstLineTop
//        )
//    ),
//
////    labelLarge = NationalParkTypography.labelLarge,
//    labelMedium = baseline.labelMedium.copy(
//        fontFamily = dataFont, fontWeight = FontWeight.Bold,
//    ),
//
//    labelSmall  = baseline.labelSmall.copy(
//        fontFamily = dataFont, fontSize = 10.sp,
//    ),
//
//    // ========== BODY / CONTENT ==========
//    bodyLarge  = baseline.bodyLarge.copy(fontFamily = contentFont),
//    bodyMedium = baseline.bodyMedium.copy(fontFamily = contentFont),
//    bodySmall  = baseline.bodySmall.copy(fontFamily = uiFont)
//)
