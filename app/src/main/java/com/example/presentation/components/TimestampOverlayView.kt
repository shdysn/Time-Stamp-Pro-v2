package com.example.presentation.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationData
import com.example.data.model.StampPosition
import com.example.data.model.StampTemplateType
import com.example.data.model.UserSettings
import com.example.presentation.components.stamps.ClassicCardStamp
import com.example.presentation.components.stamps.CompactPillStamp
import com.example.presentation.components.stamps.CyberTechHudStamp
import com.example.presentation.components.stamps.FramedOutlineStamp
import com.example.presentation.components.stamps.FullBannerStamp
import com.example.presentation.components.stamps.ModernMinimalStamp

private const val TAG = "TimestampOverlay"

@Composable
fun TimestampOverlayView(
    settings: UserSettings,
    location: LocationData,
    heading: Float,
    currentTimeMillis: Long,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "Rendering live preview overlay for template: ${settings.templateType}")

    val boxAlignment = when (settings.stampPosition) {
        StampPosition.TOP_LEFT -> Alignment.TopStart
        StampPosition.TOP_RIGHT -> Alignment.TopEnd
        StampPosition.BOTTOM_LEFT -> Alignment.BottomStart
        StampPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
        StampPosition.BOTTOM_BANNER -> Alignment.BottomCenter
    }

    val isBanner = settings.templateType == StampTemplateType.FULL_BANNER || settings.stampPosition == StampPosition.BOTTOM_BANNER

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Optional Custom Text Banner at top
        if (settings.isCustomTextEnabled && settings.customText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp, start = 20.dp, end = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(settings.customTextColorHex).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = settings.customText,
                        color = Color(settings.customTextColorHex),
                        fontSize = settings.customTextSize.sp,
                        fontWeight = if (settings.isCustomTextBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (settings.isCustomTextItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = if (settings.isCustomTextUnderline) TextDecoration.Underline else TextDecoration.None,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 2. Separate Distinct Stamp Composables via Exhaustive when
        if (settings.isStampVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isBanner) 0.dp else 16.dp),
                contentAlignment = if (isBanner) Alignment.BottomCenter else boxAlignment
            ) {
                when (settings.templateType) {
                    StampTemplateType.CLASSIC_CARD -> {
                        ClassicCardStamp(
                            settings = settings,
                            location = location,
                            heading = heading,
                            currentTimeMillis = currentTimeMillis
                        )
                    }

                    StampTemplateType.MODERN_MINIMAL -> {
                        ModernMinimalStamp(
                            settings = settings,
                            location = location,
                            currentTimeMillis = currentTimeMillis
                        )
                    }

                    StampTemplateType.CYBER_TECH_HUD -> {
                        CyberTechHudStamp(
                            settings = settings,
                            location = location,
                            heading = heading,
                            currentTimeMillis = currentTimeMillis
                        )
                    }

                    StampTemplateType.FRAMED_OUTLINE -> {
                        FramedOutlineStamp(
                            settings = settings,
                            location = location,
                            currentTimeMillis = currentTimeMillis
                        )
                    }

                    StampTemplateType.COMPACT_PILL -> {
                        CompactPillStamp(
                            settings = settings,
                            location = location,
                            currentTimeMillis = currentTimeMillis
                        )
                    }

                    StampTemplateType.FULL_BANNER -> {
                        FullBannerStamp(
                            settings = settings,
                            location = location,
                            heading = heading,
                            currentTimeMillis = currentTimeMillis
                        )
                    }
                }
            }
        }
    }
}
