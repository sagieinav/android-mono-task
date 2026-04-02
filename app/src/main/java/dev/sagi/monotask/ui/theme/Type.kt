@file:OptIn(ExperimentalTextApi::class)

package dev.sagi.monotask.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import dev.sagi.monotask.R

private const val UI_FONT_WIDTH = 95f
private const val UI_FONT_OPTICAL_SIZE = 18f
private const val DATA_FONT_OPTICAL_SIZE = 14f

private val flexWeights = listOf(
    FontWeight.Thin, FontWeight.ExtraLight, FontWeight.Light,
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold
)

// =====================================================
// Gloock: hero/display font
// =====================================================
val gloock = FontFamily(
    Font(R.font.gloock, FontWeight.Normal)
)

// =====================================================
// Lora: content font
// =====================================================
val lora = FontFamily(
    Font(R.font.lora,        FontWeight.Normal),
    Font(R.font.lora,        FontWeight.Medium),
    Font(R.font.lora,        FontWeight.SemiBold),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
)

// =====================================================
// Google Sans Rounded: main UI font
// =====================================================
val googleSansRounded = googleSansFamily(
    FontVariation.width(UI_FONT_WIDTH),
    FontVariation.Setting("ROND", 100f),
    FontVariation.Setting("opsz", UI_FONT_OPTICAL_SIZE)
)

// =====================================================
// Google Sans (Standard): labels font (mainly)
// =====================================================
val googleSans = googleSansFamily(
    FontVariation.Setting("opsz", DATA_FONT_OPTICAL_SIZE)
)

// =====================================================
// Harabara: for task tags
// =====================================================
val harabara = FontFamily(
    Font(R.font.harabara, FontWeight.Normal),
)


// =====================================================
// ACTIVE SELECTION
// =====================================================
private val heroFont = gloock
private val uiFont = googleSansRounded
private val dataFont = googleSans
private val contentFont = lora


// =====================================================
// HELPERS
// =====================================================

// Helper to remove line height padding
private fun TextStyle.withNoPadding() = copy(
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both
    )
)

// Helper for setting weights to Google Sans Flex family
private fun googleSansFamily(vararg extra: FontVariation.Setting) =
    FontFamily(flexWeights.map { fontWeight ->
        Font(
            resId = R.font.google_sans_flex,
            weight = fontWeight,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(fontWeight.weight),
                *extra
            )
        )
    })


// =====================================================
// TYPOGRAPHY SCALE
// =====================================================
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
    titleLarge  = baseline.titleLarge.copy(fontFamily = uiFont).withNoPadding(),
    titleMedium = baseline.titleMedium.copy(fontFamily = uiFont).withNoPadding(),
    titleSmall  = baseline.titleSmall.copy(fontFamily = uiFont).withNoPadding(),

    // ========== SMALL LABELS ==========
    labelLarge  = baseline.labelLarge.copy(fontFamily = dataFont).withNoPadding(),
    labelMedium = baseline.labelMedium.copy(
        fontFamily = dataFont, fontWeight = FontWeight.Light).withNoPadding(),
    labelSmall  = baseline.labelSmall.copy(
        fontFamily = dataFont, fontWeight = FontWeight.Light, fontSize = 10.sp).withNoPadding(),

    // ========== BODY / CONTENT ==========
    bodyLarge  = baseline.bodyLarge.copy(fontFamily = contentFont).withNoPadding(),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = contentFont).withNoPadding(),
    bodySmall  = baseline.bodySmall.copy(fontFamily = uiFont).withNoPadding()
)