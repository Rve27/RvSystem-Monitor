@file:OptIn(ExperimentalTextApi::class)

package com.rve.systemmonitor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.rve.systemmonitor.R

private val RoundVariationSetting = FontVariation.Setting("ROND", 100.0f)

private fun googleSansFlexFontFamily(weight: Int, width: Float = 100f): FontFamily = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        variationSettings =
            FontVariation.Settings(
                FontVariation.weight(weight),
                FontVariation.width(width),
                RoundVariationSetting,
            ),
    ),
)

val defaultTypography = Typography()
val appTypography =
    Typography(
        displayLarge =
            defaultTypography.displayLarge.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Bold.weight),
            ),
        displayMedium =
            defaultTypography.displayMedium.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Bold.weight),
            ),
        displaySmall =
            defaultTypography.displaySmall.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Bold.weight),
            ),
        headlineLarge =
            defaultTypography.headlineLarge.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.SemiBold.weight),
            ),
        headlineMedium =
            defaultTypography.headlineMedium.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.SemiBold.weight),
            ),
        headlineSmall =
            defaultTypography.headlineSmall.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.SemiBold.weight),
            ),
        titleLarge =
            defaultTypography.titleLarge.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Medium.weight),
            ),
        titleMedium =
            defaultTypography.titleMedium.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Medium.weight),
            ),
        titleSmall =
            defaultTypography.titleSmall.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Medium.weight),
            ),
        bodyLarge =
            defaultTypography.bodyLarge.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Normal.weight),
            ),
        bodyMedium =
            defaultTypography.bodyMedium.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Normal.weight),
            ),
        bodySmall =
            defaultTypography.bodySmall.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Normal.weight),
            ),
        labelLarge =
            defaultTypography.labelLarge.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Medium.weight),
            ),
        labelMedium =
            defaultTypography.labelMedium.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Medium.weight),
            ),
        labelSmall =
            defaultTypography.labelSmall.copy(
                fontFamily = googleSansFlexFontFamily(FontWeight.Medium.weight),
            ),
    )
