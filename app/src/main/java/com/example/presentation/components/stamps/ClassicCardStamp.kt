package com.example.presentation.components.stamps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AltitudeUnit
import com.example.data.model.LocationData
import com.example.data.model.UserSettings
import com.example.presentation.designsystem.AppColors
import com.example.timestamp.CoordinateFormatter
import com.example.timestamp.DateFormatter
import java.util.Locale

@Composable
fun ClassicCardStamp(
    settings: UserSettings,
    location: LocationData,
    heading: Float,
    currentTimeMillis: Long,
    modifier: Modifier = Modifier,
    isThumbnailPreview: Boolean = false
) {
    val stampColor = Color(settings.textColorHex)
    val scale = if (isThumbnailPreview) 0.7f else settings.fontSize.scale
    val bgAlpha = settings.backgroundOpacity.coerceIn(0.2f, 1f)

    Surface(
        color = Color.Black.copy(alpha = bgAlpha),
        shape = RoundedCornerShape(12.dp * scale),
        border = androidx.compose.foundation.BorderStroke(1.2.dp * scale, stampColor.copy(alpha = 0.85f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp * scale, vertical = 10.dp * scale),
            verticalArrangement = Arrangement.spacedBy(4.dp * scale)
        ) {
            // Header: Large Time + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (settings.showTimestamp) {
                    val timeStr = DateFormatter.format(currentTimeMillis, "HH:mm:ss")
                    val dateStr = DateFormatter.format(currentTimeMillis, "yyyy-MM-dd")
                    Text(
                        text = timeStr,
                        color = stampColor,
                        fontSize = (22f * scale).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = dateStr,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = (13f * scale).sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Mock Warning
            if (location.isMock) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AppColors.AccentRed, modifier = Modifier.size(13.dp * scale))
                    Spacer(modifier = Modifier.size(4.dp * scale))
                    Text("MOCK GPS DETECTED", color = AppColors.AccentRed, fontSize = (10f * scale).sp, fontWeight = FontWeight.Bold)
                }
            }

            // Location Address
            if (settings.showAddress && location.address.isNotBlank() && location.address != "Detecting location...") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = stampColor, modifier = Modifier.size(13.dp * scale))
                    Spacer(modifier = Modifier.size(4.dp * scale))
                    Text(
                        text = location.address,
                        color = Color.White,
                        fontSize = (12f * scale).sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            // GPS Coordinates
            if (settings.showCoordinates) {
                val coords = CoordinateFormatter.format(location.latitude, location.longitude, settings.coordinateFormat)
                Text(
                    text = "GPS: $coords",
                    color = stampColor.copy(alpha = 0.9f),
                    fontSize = (11f * scale).sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Chips Row: Altitude, Compass, Accuracy
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp * scale),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (settings.showAltitude) {
                    val altText = if (settings.altitudeUnit == AltitudeUnit.METERS) {
                        "${String.format(Locale.US, "%.1f", location.altitude)}m"
                    } else {
                        "${String.format(Locale.US, "%.1f", location.altitude * 3.28084)}ft"
                    }
                    ClassicChip(text = "ALT: $altText", scale = scale)
                }

                if (settings.showCompass) {
                    val bearing = CoordinateFormatter.formatBearing(heading)
                    ClassicChip(text = "DIR: $bearing", scale = scale, icon = {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = AppColors.AccentCyan,
                            modifier = Modifier.size(11.dp * scale).rotate(-heading)
                        )
                    })
                }

                if (location.accuracy > 0 && !isThumbnailPreview) {
                    ClassicChip(text = "±${location.accuracy.toInt()}m", scale = scale)
                }
            }
        }
    }
}

@Composable
private fun ClassicChip(
    text: String,
    scale: Float,
    icon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(4.dp * scale))
            .padding(horizontal = 6.dp * scale, vertical = 2.dp * scale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp * scale)
        ) {
            icon?.invoke()
            Text(
                text = text,
                color = Color(0xFFE2E8F0),
                fontSize = (10f * scale).sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
