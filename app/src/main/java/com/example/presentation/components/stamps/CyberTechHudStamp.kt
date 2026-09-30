package com.example.presentation.components.stamps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
fun CyberTechHudStamp(
    settings: UserSettings,
    location: LocationData,
    heading: Float,
    currentTimeMillis: Long,
    modifier: Modifier = Modifier,
    isThumbnailPreview: Boolean = false
) {
    val hudColor = if (settings.textColorHex == 0xFFFFC107) Color(0xFF00E5FF) else Color(settings.textColorHex)
    val scale = if (isThumbnailPreview) 0.7f else settings.fontSize.scale
    val bgAlpha = settings.backgroundOpacity.coerceIn(0.35f, 0.95f)

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = bgAlpha))
            .drawBehind {
                val bracketLen = 14.dp.toPx() * scale
                val strokeW = 2.dp.toPx() * scale

                // Top-Left
                drawLine(hudColor, Offset(0f, 0f), Offset(bracketLen, 0f), strokeW)
                drawLine(hudColor, Offset(0f, 0f), Offset(0f, bracketLen), strokeW)

                // Top-Right
                drawLine(hudColor, Offset(size.width, 0f), Offset(size.width - bracketLen, 0f), strokeW)
                drawLine(hudColor, Offset(size.width, 0f), Offset(size.width, bracketLen), strokeW)

                // Bottom-Left
                drawLine(hudColor, Offset(0f, size.height), Offset(bracketLen, size.height), strokeW)
                drawLine(hudColor, Offset(0f, size.height), Offset(0f, size.height - bracketLen), strokeW)

                // Bottom-Right
                drawLine(hudColor, Offset(size.width, size.height), Offset(size.width - bracketLen, size.height), strokeW)
                drawLine(hudColor, Offset(size.width, size.height), Offset(size.width, size.height - bracketLen), strokeW)
            }
            .padding(14.dp * scale)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp * scale)) {
            // HUD Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYS // REC ● VERIFIED",
                    color = Color(0xFF00E676),
                    fontSize = (10f * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                val idHex = (currentTimeMillis % 0xFFFF).toString(16).uppercase(Locale.US)
                Text(
                    text = "ID: #$idHex",
                    color = hudColor.copy(alpha = 0.8f),
                    fontSize = (10f * scale).sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Timestamp in ISO Monospace
            val formattedTime = DateFormatter.format(currentTimeMillis, "yyyy-MM-dd'T'HH:mm:ss")
            Text(
                text = "UTC: $formattedTime",
                color = Color.White,
                fontSize = (16f * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Technical Telemetry Lines
            val latStr = String.format(Locale.US, "%.5f°", location.latitude)
            val lngStr = String.format(Locale.US, "%.5f°", location.longitude)
            Text(
                text = "LAT: $latStr | LON: $lngStr",
                color = hudColor,
                fontSize = (11f * scale).sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )

            val altStr = if (settings.altitudeUnit == AltitudeUnit.METERS) {
                "${String.format(Locale.US, "%.1f", location.altitude)}M"
            } else {
                "${String.format(Locale.US, "%.1f", location.altitude * 3.28084)}FT"
            }
            val dirStr = CoordinateFormatter.formatBearing(heading)
            val accStr = if (location.accuracy > 0) "±${location.accuracy.toInt()}M" else "N/A"

            Text(
                text = "ALT: $altStr | DIR: $dirStr | ACC: $accStr",
                color = Color(0xFF94A3B8),
                fontSize = (10f * scale).sp,
                fontFamily = FontFamily.Monospace
            )

            if (location.address.isNotBlank() && location.address != "Detecting location...") {
                Text(
                    text = "GEO: ${location.address.uppercase(Locale.US)}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = (10f * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}
