package com.example.presentation.components.stamps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationData
import com.example.data.model.UserSettings
import com.example.timestamp.DateFormatter

@Composable
fun ModernMinimalStamp(
    settings: UserSettings,
    location: LocationData,
    currentTimeMillis: Long,
    modifier: Modifier = Modifier,
    isThumbnailPreview: Boolean = false
) {
    val stampColor = Color(settings.textColorHex)
    val scale = if (isThumbnailPreview) 0.7f else settings.fontSize.scale
    val bgAlpha = (settings.backgroundOpacity * 0.75f).coerceIn(0.15f, 0.85f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp * scale))
            .background(Color.Black.copy(alpha = bgAlpha))
            .padding(horizontal = 10.dp * scale, vertical = 8.dp * scale)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp * scale)
        ) {
            // Thin vertical accent bar on left
            Box(
                modifier = Modifier
                    .width(4.dp * scale)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp * scale))
                    .background(stampColor)
            )

            // Content Column: Time as primary element, Date + Short Location only
            Column(verticalArrangement = Arrangement.spacedBy(2.dp * scale)) {
                val timeStr = DateFormatter.format(currentTimeMillis, "hh:mm a")
                val dateStr = DateFormatter.format(currentTimeMillis, "MMM dd, yyyy")

                Text(
                    text = timeStr,
                    color = Color.White,
                    fontSize = (26f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateStr,
                        color = stampColor,
                        fontSize = (12f * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    )

                    val shortLoc = location.getShortLocation()
                    if (shortLoc.isNotBlank()) {
                        Text(
                            text = "•",
                            color = Color.Gray,
                            fontSize = (12f * scale).sp
                        )
                        Text(
                            text = shortLoc,
                            color = Color(0xFFCBD5E1),
                            fontSize = (12f * scale).sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
