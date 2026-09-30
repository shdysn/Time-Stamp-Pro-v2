package com.example.presentation.components.stamps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationData
import com.example.data.model.UserSettings
import com.example.timestamp.DateFormatter

@Composable
fun CompactPillStamp(
    settings: UserSettings,
    location: LocationData,
    currentTimeMillis: Long,
    modifier: Modifier = Modifier,
    isThumbnailPreview: Boolean = false
) {
    val stampColor = Color(settings.textColorHex)
    val scale = if (isThumbnailPreview) 0.7f else settings.fontSize.scale
    val bgAlpha = settings.backgroundOpacity.coerceIn(0.35f, 0.95f)

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = bgAlpha), RoundedCornerShape(24.dp * scale))
            .border(1.dp * scale, stampColor.copy(alpha = 0.75f), RoundedCornerShape(24.dp * scale))
            .padding(horizontal = 14.dp * scale, vertical = 6.dp * scale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp * scale)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = stampColor,
                modifier = Modifier.size(13.dp * scale)
            )

            val timeStr = DateFormatter.format(currentTimeMillis, "HH:mm")
            Text(
                text = timeStr,
                color = Color.White,
                fontSize = (13f * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "•",
                color = Color.Gray,
                fontSize = (11f * scale).sp
            )

            val dateStr = DateFormatter.format(currentTimeMillis, "MM/dd")
            Text(
                text = dateStr,
                color = stampColor,
                fontSize = (12f * scale).sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )

            val shortPlace = location.locality.ifBlank {
                location.address.split(",").firstOrNull()?.trim() ?: ""
            }
            if (shortPlace.isNotBlank()) {
                Text(
                    text = "•",
                    color = Color.Gray,
                    fontSize = (11f * scale).sp
                )
                Text(
                    text = shortPlace,
                    color = Color(0xFFE2E8F0),
                    fontSize = (12f * scale).sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
