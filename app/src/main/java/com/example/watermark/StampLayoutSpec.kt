package com.example.watermark

import android.graphics.Color
import com.example.data.model.StampTemplateType

data class StampLayoutSpec(
    val templateType: StampTemplateType,
    val textColor: Int,
    val backgroundColor: Int,
    val borderColor: Int,
    val accentColor: Int,
    val backgroundOpacity: Float,
    val cornerRadiusDp: Float,
    val borderWidthDp: Float,
    val horizontalPaddingDp: Float,
    val verticalPaddingDp: Float,
    val timeFontSizeSp: Float,
    val bodyFontSizeSp: Float,
    val showTime: Boolean,
    val showDate: Boolean,
    val showCoordinates: Boolean,
    val showAddress: Boolean,
    val showAltitude: Boolean,
    val showCompass: Boolean,
    val showAccuracy: Boolean,
    val showChips: Boolean,
    val showAccentBar: Boolean,
    val showCornerBrackets: Boolean,
    val isFullWidth: Boolean,
    val isCentered: Boolean,
    val isMonospace: Boolean,
    val isUppercaseOnly: Boolean
) {
    companion object {
        fun create(
            templateType: StampTemplateType,
            textColorHex: Long,
            backgroundOpacity: Float
        ): StampLayoutSpec {
            val primaryColor = textColorHex.toInt()

            return when (templateType) {
                StampTemplateType.CLASSIC_CARD -> StampLayoutSpec(
                    templateType = templateType,
                    textColor = primaryColor,
                    backgroundColor = Color.BLACK,
                    borderColor = primaryColor,
                    accentColor = 0xFF00E5FF.toInt(),
                    backgroundOpacity = backgroundOpacity.coerceIn(0.25f, 0.95f),
                    cornerRadiusDp = 12f,
                    borderWidthDp = 1.2f,
                    horizontalPaddingDp = 14f,
                    verticalPaddingDp = 10f,
                    timeFontSizeSp = 22f,
                    bodyFontSizeSp = 12f,
                    showTime = true,
                    showDate = true,
                    showCoordinates = true,
                    showAddress = true,
                    showAltitude = true,
                    showCompass = true,
                    showAccuracy = true,
                    showChips = true,
                    showAccentBar = false,
                    showCornerBrackets = false,
                    isFullWidth = false,
                    isCentered = false,
                    isMonospace = true,
                    isUppercaseOnly = false
                )

                StampTemplateType.MODERN_MINIMAL -> StampLayoutSpec(
                    templateType = templateType,
                    textColor = primaryColor,
                    backgroundColor = Color.BLACK,
                    borderColor = Color.TRANSPARENT,
                    accentColor = primaryColor,
                    backgroundOpacity = (backgroundOpacity * 0.7f).coerceIn(0.15f, 0.8f),
                    cornerRadiusDp = 6f,
                    borderWidthDp = 0f,
                    horizontalPaddingDp = 12f,
                    verticalPaddingDp = 8f,
                    timeFontSizeSp = 26f,
                    bodyFontSizeSp = 11.5f,
                    showTime = true,
                    showDate = true,
                    showCoordinates = false, // Minimal: coordinates hidden
                    showAddress = true,      // Short address only
                    showAltitude = false,    // Minimal: altitude hidden
                    showCompass = false,     // Minimal: compass hidden
                    showAccuracy = false,    // Minimal: accuracy hidden
                    showChips = false,
                    showAccentBar = true,    // Left vertical accent bar
                    showCornerBrackets = false,
                    isFullWidth = false,
                    isCentered = false,
                    isMonospace = false,     // Clean sans-serif
                    isUppercaseOnly = false
                )

                StampTemplateType.CYBER_TECH_HUD -> StampLayoutSpec(
                    templateType = templateType,
                    textColor = if (textColorHex == 0xFFFFC107) 0xFF00E5FF.toInt() else primaryColor,
                    backgroundColor = Color.BLACK,
                    borderColor = if (textColorHex == 0xFFFFC107) 0xFF00E5FF.toInt() else primaryColor,
                    accentColor = 0xFF00E676.toInt(),
                    backgroundOpacity = backgroundOpacity.coerceIn(0.35f, 0.95f),
                    cornerRadiusDp = 0f,
                    borderWidthDp = 1.6f,
                    horizontalPaddingDp = 16f,
                    verticalPaddingDp = 12f,
                    timeFontSizeSp = 20f,
                    bodyFontSizeSp = 11.5f,
                    showTime = true,
                    showDate = true,
                    showCoordinates = true,
                    showAddress = true,
                    showAltitude = true,
                    showCompass = true,
                    showAccuracy = true,
                    showChips = false,
                    showAccentBar = false,
                    showCornerBrackets = true, // Tactical corner brackets
                    isFullWidth = false,
                    isCentered = false,
                    isMonospace = true,
                    isUppercaseOnly = true
                )

                StampTemplateType.FRAMED_OUTLINE -> StampLayoutSpec(
                    templateType = templateType,
                    textColor = primaryColor,
                    backgroundColor = Color.BLACK,
                    borderColor = primaryColor,
                    accentColor = primaryColor,
                    backgroundOpacity = (backgroundOpacity * 0.35f).coerceIn(0.08f, 0.5f),
                    cornerRadiusDp = 4f,
                    borderWidthDp = 1.8f,
                    horizontalPaddingDp = 18f,
                    verticalPaddingDp = 12f,
                    timeFontSizeSp = 20f,
                    bodyFontSizeSp = 12f,
                    showTime = true,
                    showDate = true,
                    showCoordinates = true,
                    showAddress = true,
                    showAltitude = false,
                    showCompass = false,
                    showAccuracy = false,
                    showChips = false,
                    showAccentBar = false,
                    showCornerBrackets = false,
                    isFullWidth = false,
                    isCentered = true, // Centered formal certificate layout
                    isMonospace = false,
                    isUppercaseOnly = true
                )

                StampTemplateType.COMPACT_PILL -> StampLayoutSpec(
                    templateType = templateType,
                    textColor = primaryColor,
                    backgroundColor = Color.BLACK,
                    borderColor = primaryColor,
                    accentColor = primaryColor,
                    backgroundOpacity = backgroundOpacity.coerceIn(0.4f, 0.9f),
                    cornerRadiusDp = 24f, // Capsule pill
                    borderWidthDp = 1.0f,
                    horizontalPaddingDp = 14f,
                    verticalPaddingDp = 6f,
                    timeFontSizeSp = 15f,
                    bodyFontSizeSp = 11f,
                    showTime = true,
                    showDate = true,
                    showCoordinates = false, // Hidden for ultra-compact size
                    showAddress = true,
                    showAltitude = false,
                    showCompass = false,
                    showAccuracy = false,
                    showChips = false,
                    showAccentBar = false,
                    showCornerBrackets = false,
                    isFullWidth = false,
                    isCentered = false,
                    isMonospace = false,
                    isUppercaseOnly = false
                )

                StampTemplateType.FULL_BANNER -> StampLayoutSpec(
                    templateType = templateType,
                    textColor = primaryColor,
                    backgroundColor = Color.BLACK,
                    borderColor = primaryColor,
                    accentColor = 0xFF00E5FF.toInt(),
                    backgroundOpacity = backgroundOpacity.coerceIn(0.45f, 0.95f),
                    cornerRadiusDp = 0f, // Zero bottom radius
                    borderWidthDp = 1.4f,
                    horizontalPaddingDp = 16f,
                    verticalPaddingDp = 10f,
                    timeFontSizeSp = 19f,
                    bodyFontSizeSp = 11.5f,
                    showTime = true,
                    showDate = true,
                    showCoordinates = true,
                    showAddress = true,
                    showAltitude = true,
                    showCompass = true,
                    showAccuracy = true,
                    showChips = true,
                    showAccentBar = false,
                    showCornerBrackets = false,
                    isFullWidth = true, // Docked edge-to-edge
                    isCentered = false,
                    isMonospace = true,
                    isUppercaseOnly = false
                )
            }
        }
    }
}
