package dev.sagi.monotask.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dev.sagi.monotask.R

// ─────────────────────────────────────────────────────
// CATEGORY 1 — HERO / DISPLAY (Headlines, Focus Card title)
// Used for: headlineLarge, headlineMedium, displayLarge
// ─────────────────────────────────────────────────────

// Option A: Gloock — editorial, magazine-grade serif. ← ACTIVE DEFAULT
val gloock = FontFamily(
    Font(R.font.gloock, FontWeight.Normal)
)

// Option B: Playfair Display — high-contrast classic serif.
// Variable font: one file handles all weights.
val playfairDisplay = FontFamily(
    Font(R.font.playfair_display,        FontWeight.Normal),
    Font(R.font.playfair_display,        FontWeight.SemiBold),
    Font(R.font.playfair_display,        FontWeight.Bold),
    Font(R.font.playfair_display_italic, FontWeight.Normal,  FontStyle.Italic),
)

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
    Font(R.font.noto_serif,        FontWeight.Normal),
    Font(R.font.noto_serif_italic, FontWeight.Normal, FontStyle.Italic),
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
    Font(R.font.plus_jakarta_sans,        FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_italic, FontWeight.Normal, FontStyle.Italic),
)

// Option B: Google Sans — clean, modern, Google's own product font.
val googleSans = FontFamily(
    Font(R.font.google_sans,        FontWeight.Normal),
    Font(R.font.google_sans,        FontWeight.Medium),
    Font(R.font.google_sans_italic, FontWeight.Normal, FontStyle.Italic),
)

// Option C: Roboto — Android system default. Safe fallback.
val roboto = FontFamily(
    Font(R.font.roboto,        FontWeight.Normal),
    Font(R.font.roboto_italic, FontWeight.Normal, FontStyle.Italic),
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

// ─────────────────────────────────────────────────────
// ACTIVE SELECTION
// To swap a font, change the variable assigned here.
// e.g. replace `gloock` with `playfairDisplay` for titles.
// ─────────────────────────────────────────────────────
private val heroFont    = gloock          // ← swap to: playfairDisplay
private val bodyFont    = lora            // ← swap to: libreCaslon, notoSerif
private val uiFont      = plusJakartaSans // ← swap to: googleSans, roboto
private val dataFont    = ibmPlexMono     // no alternative needed

// ─────────────────────────────────────────────────────
// TYPOGRAPHY SCALE
// M3 has 15 named styles. Rule of thumb:
//   display/headline → hero content
//   title            → section headers, screen names
//   body             → reading text
//   label            → UI chrome (smallest, most frequent)
// ─────────────────────────────────────────────────────
val baseline = Typography()

val AppTypography = Typography(

    // Splash / onboarding hero text
    displayLarge  = baseline.displayLarge.copy(fontFamily = heroFont),
    displayMedium = baseline.displayMedium.copy(fontFamily = heroFont),
    displaySmall  = baseline.displaySmall.copy(fontFamily = heroFont),

    // THE Focus Card task title (most prominent text in the app)
    headlineLarge  = baseline.headlineLarge.copy(fontFamily = heroFont),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = heroFont),
    headlineSmall  = baseline.headlineSmall.copy(fontFamily = bodyFont),

    // Screen names ("Hello, Sagi"), workspace headers
    titleLarge  = baseline.titleLarge.copy(
        fontFamily = uiFont, fontWeight = FontWeight.SemiBold),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = uiFont, fontWeight = FontWeight.SemiBold),
    titleSmall  = baseline.titleSmall.copy(
        fontFamily = uiFont, fontWeight = FontWeight.Medium),

    // Task descriptions, onboarding copy, settings text
    bodyLarge  = baseline.bodyLarge.copy(fontFamily = bodyFont),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFont),
    bodySmall  = baseline.bodySmall.copy(fontFamily = uiFont),

    // Buttons, chips, priority tags
    labelLarge  = baseline.labelLarge.copy(
        fontFamily = uiFont, fontWeight = FontWeight.SemiBold),
    labelMedium = baseline.labelMedium.copy(fontFamily = uiFont),

    // XP values (+100 XP), level (Level 12), due dates
    labelSmall  = baseline.labelSmall.copy(fontFamily = dataFont),
)
