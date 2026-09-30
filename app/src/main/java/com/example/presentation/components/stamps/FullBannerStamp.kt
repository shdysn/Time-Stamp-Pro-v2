package com.example.presentation.components.stamps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AltitudeUnit
import com.example.data.model.LocationData
import com.example.data.model.UserSettings
import com.example.timestamp.CoordinateFormatter
import com.example.timestamp.DateFormatter
import java.util.Locale

@Composable
fun FullBannerStamp(
    settings: UserSettings,
    location: LocationData,
    heading: Float,
    currentTimeMillis: Long,
    modifier: Modifier = Modifier,
    isThumbnailPreview: Boolean = false
) {
    val stampColor = Color(settings.textColorHex)
    val scale = if (isThumbnailPreview) 0.7f else settings.fontSize.scale
    val bgAlpha = settings.backgroundOpacity.coerceIn(0.45f, 0.98f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = bgAlpha), RoundedCornerShape(topStart = 8.dp * scale, topEnd = 8.dp * scale, bottomStart = 0.dp, bottomEnd = 0.dp))
            .border(
                width = 1.dp * scale,
                color = stampColor.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 8.dp * scale, topEnd = 8.dp * scale, bottomStart = 0.dp, bottomEnd = 0.dp)
            )
            .padding(horizontal = 14.dp * scale, vertical = 8.dp * scale)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group 1: Time & Date (Column)
            Column(verticalArrangement = Arrangement.spacedBy(1.dp * scale)) {
                val timeStr = DateFormatter.format(currentTimeMillis, "HH:mm:ss")
                val dateStr = DateFormatter.format(currentTimeMillis, "yyyy.MM.dd")
                Text(
                    text = timeStr,
                    color = stampColor,
                    fontSize = (18f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = dateStr,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = (11f * scale).sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Group 2: Location & Position (Column)
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp * scale),
                verticalArrangement = Arrangement.spacedBy(1.dp * scale)
            ) {
                if (location.address.isNotBlank() && location.address != "Detecting location...") {
                    Text(
                        text = location.address,
                        color = Color.White,
                        fontSize = (11f * scale).sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
                val coords = CoordinateFormatter.format(location.latitude, location.longitude, settings.coordinateFormat)
                Text(
                    text = coords,
                    color = Color(0xFFCBD5E1),
                    fontSize = (10f * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            // Group 3: Telemetry (Alt & Compass)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(1.dp * scale)
            ) {
                val altStr = if (settings.altitudeUnit == AltitudeUnit.METERS) {
                    "${String.format(Locale.US, "%.0f", location.altitude)}m"
                } else {
                    "${String.format(Locale.US, "%.0f", location.altitude * 3.28084)}ft"
                }
                val dirStr = CoordinateFormatter.formatBearing(heading)
                Text(
                    text = "ALT $altStr",
                    color = stampColor,
                    fontSize = (11f * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "DIR $dirStr",
                    color = Color(0xFF00E5FF),
                    fontSize = (10f * scale).sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
