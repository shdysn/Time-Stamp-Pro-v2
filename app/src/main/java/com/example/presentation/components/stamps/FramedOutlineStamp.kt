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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationData
import com.example.data.model.UserSettings
import com.example.timestamp.CoordinateFormatter
import com.example.timestamp.DateFormatter
import java.util.Locale

@Composable
fun FramedOutlineStamp(
    settings: UserSettings,
    location: LocationData,
    currentTimeMillis: Long,
    modifier: Modifier = Modifier,
    isThumbnailPreview: Boolean = false
) {
    val stampColor = Color(settings.textColorHex)
    val scale = if (isThumbnailPreview) 0.7f else settings.fontSize.scale
    val bgAlpha = (settings.backgroundOpacity * 0.35f).coerceIn(0.08f, 0.5f)

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = bgAlpha), RoundedCornerShape(4.dp * scale))
            .border(1.6.dp * scale, stampColor.copy(alpha = 0.9f), RoundedCornerShape(4.dp * scale))
            .padding(horizontal = 16.dp * scale, vertical = 10.dp * scale)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp * scale)
        ) {
            // Document Header
            Text(
                text = "— OFFICIAL INSPECTION RECORD —",
                color = stampColor,
                fontSize = (9f * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            // Centered Date and Time in Uppercase
            val dateStr = DateFormatter.format(currentTimeMillis, "EEEE, MMMM dd, yyyy").uppercase(Locale.US)
            val timeStr = DateFormatter.format(currentTimeMillis, "HH:mm:ss z").uppercase(Locale.US)
            Text(
                text = "$dateStr • $timeStr",
                color = Color.White,
                fontSize = (12f * scale).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Coordinates Centered
            if (settings.showCoordinates) {
                val coords = CoordinateFormatter.format(location.latitude, location.longitude, settings.coordinateFormat)
                Text(
                    text = "POSITION: $coords",
                    color = Color(0xFFE2E8F0),
                    fontSize = (11f * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }

            // Location Address in Uppercase
            if (settings.showAddress && location.address.isNotBlank() && location.address != "Detecting location...") {
                Text(
                    text = "LOCATION: ${location.address.uppercase(Locale.US)}",
                    color = stampColor.copy(alpha = 0.9f),
                    fontSize = (10f * scale).sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
