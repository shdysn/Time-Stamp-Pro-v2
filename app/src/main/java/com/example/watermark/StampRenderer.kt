package com.example.watermark

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import com.example.data.model.AltitudeUnit
import com.example.data.model.StampPosition
import com.example.data.model.StampTemplateType
import com.example.data.model.UserSettings
import com.example.timestamp.CoordinateFormatter
import com.example.timestamp.DateFormatter
import java.util.Locale
import kotlin.math.max

object StampRenderer {

    private const val TAG = "StampRenderer"

    fun render(request: StampRenderRequest): Bitmap {
        Log.i(TAG, "render called with template: ${request.templateType}")

        val source = request.sourceBitmap
        val outputBitmap = if (source.isMutable) {
            source
        } else {
            source.copy(Bitmap.Config.ARGB_8888, true)
        }

        val canvas = Canvas(outputBitmap)
        val imageWidth = outputBitmap.width.toFloat()
        val imageHeight = outputBitmap.height.toFloat()

        // Resolution-proportional scaling:
        // Baseline 1400px reference dimension, scales smoothly for 1080p (1080x1920), 1440p, and 4K (2160x3840)
        val refDimension = max(imageWidth, imageHeight)
        val resScale = (refDimension / 1400f).coerceAtLeast(0.6f) * request.settings.fontSize.scale

        val spec = StampLayoutSpec.create(
            templateType = request.templateType,
            textColorHex = request.settings.textColorHex,
            backgroundOpacity = request.settings.backgroundOpacity
        )

        // 1. Optional Custom User Text Banner
        if (request.settings.isCustomTextEnabled && request.settings.customText.isNotBlank()) {
            drawCustomTextOverlay(canvas, request.settings, imageWidth, resScale)
        }

        // 2. Render Selected Template with explicit routing
        if (request.settings.isStampVisible) {
            when (request.templateType) {
                StampTemplateType.CLASSIC_CARD -> renderClassicCard(canvas, request, spec, imageWidth, imageHeight, resScale)
                StampTemplateType.MODERN_MINIMAL -> renderModernMinimal(canvas, request, spec, imageWidth, imageHeight, resScale)
                StampTemplateType.CYBER_TECH_HUD -> renderCyberTechHud(canvas, request, spec, imageWidth, imageHeight, resScale)
                StampTemplateType.FRAMED_OUTLINE -> renderFramedOutline(canvas, request, spec, imageWidth, imageHeight, resScale)
                StampTemplateType.COMPACT_PILL -> renderCompactPill(canvas, request, spec, imageWidth, imageHeight, resScale)
                StampTemplateType.FULL_BANNER -> renderFullBanner(canvas, request, spec, imageWidth, imageHeight, resScale)
            }
        }

        return outputBitmap
    }

    fun renderClassicCard(
        canvas: Canvas,
        request: StampRenderRequest,
        spec: StampLayoutSpec,
        imageWidth: Float,
        imageHeight: Float,
        scale: Float
    ) {
        val timeStr = DateFormatter.format(request.timestampMillis, "HH:mm:ss")
        val dateStr = DateFormatter.format(request.timestampMillis, "yyyy-MM-dd")
        val coordsStr = if (request.settings.showCoordinates) {
            CoordinateFormatter.format(request.location.latitude, request.location.longitude, request.settings.coordinateFormat)
        } else ""
        val addressStr = if (request.settings.showAddress && request.location.address.isNotBlank() && request.location.address != "Detecting location...") {
            request.location.address
        } else ""
        val altStr = if (request.settings.showAltitude) {
            if (request.settings.altitudeUnit == AltitudeUnit.METERS) {
                "${String.format(Locale.US, "%.1f", request.location.altitude)}m"
            } else {
                "${String.format(Locale.US, "%.1f", request.location.altitude * 3.28084)}ft"
            }
        } else ""
        val dirStr = if (request.settings.showCompass) CoordinateFormatter.formatBearing(request.heading) else ""

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.textColor
            textSize = spec.timeFontSizeSp * scale * 1.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setShadowLayer(4f * scale, 2f * scale, 2f * scale, Color.BLACK)
        }

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 13f * scale * 1.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            setShadowLayer(3f * scale, 1f * scale, 1f * scale, Color.BLACK)
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            textSize = spec.bodyFontSizeSp * scale * 1.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            setShadowLayer(3f * scale, 1f * scale, 1f * scale, Color.BLACK)
        }

        val chipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF1F5F9.toInt()
            textSize = 10f * scale * 1.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        val pad = spec.horizontalPaddingDp * scale
        val cardWidth = (imageWidth * 0.75f).coerceIn(420f * scale, 840f * scale)
        val cardHeight = 175f * scale

        val margin = 28f * scale
        val rect = calculatePositionRect(request.settings.stampPosition, cardWidth, cardHeight, imageWidth, imageHeight, margin)

        // Draw card background & thin amber/theme border
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.backgroundColor
            alpha = (spec.backgroundOpacity * 255).toInt()
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.borderColor
            alpha = (spec.backgroundOpacity * 255).toInt().coerceAtLeast(180)
            style = Paint.Style.STROKE
            strokeWidth = spec.borderWidthDp * scale
        }
        val corner = spec.cornerRadiusDp * scale
        canvas.drawRoundRect(rect, corner, corner, bgPaint)
        canvas.drawRoundRect(rect, corner, corner, borderPaint)

        // Header: Time on left, Date on right
        var cy = rect.top + pad + timePaint.textSize * 0.8f
        canvas.drawText(timeStr, rect.left + pad, cy, timePaint)
        val dateWidth = datePaint.measureText(dateStr)
        canvas.drawText(dateStr, rect.right - pad - dateWidth, cy, datePaint)

        // Address below
        if (addressStr.isNotBlank()) {
            cy += 24f * scale
            val maxLen = cardWidth - (pad * 2)
            val trimmedAddr = trimTextToWidth(addressStr, bodyPaint, maxLen)
            canvas.drawText(trimmedAddr, rect.left + pad, cy, bodyPaint)
        }

        // Coordinates below
        if (coordsStr.isNotBlank()) {
            cy += 22f * scale
            canvas.drawText("GPS: $coordsStr", rect.left + pad, cy, bodyPaint)
        }

        // Chips for altitude, direction, accuracy
        cy += 26f * scale
        var chipX = rect.left + pad
        val chipHeight = 22f * scale
        val chipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 35
            style = Paint.Style.FILL
        }

        fun drawChip(text: String) {
            val tw = chipTextPaint.measureText(text)
            val cw = tw + 16f * scale
            val chipRect = RectF(chipX, cy - 14f * scale, chipX + cw, cy - 14f * scale + chipHeight)
            canvas.drawRoundRect(chipRect, 4f * scale, 4f * scale, chipBgPaint)
            canvas.drawText(text, chipX + 8f * scale, cy + 2f * scale, chipTextPaint)
            chipX += cw + 8f * scale
        }

        if (altStr.isNotBlank()) drawChip("ALT: $altStr")
        if (dirStr.isNotBlank()) drawChip("DIR: $dirStr")
        if (request.location.accuracy > 0) drawChip("±${request.location.accuracy.toInt()}m")
    }

    fun renderModernMinimal(
        canvas: Canvas,
        request: StampRenderRequest,
        spec: StampLayoutSpec,
        imageWidth: Float,
        imageHeight: Float,
        scale: Float
    ) {
        val timeStr = DateFormatter.format(request.timestampMillis, "hh:mm a")
        val dateStr = DateFormatter.format(request.timestampMillis, "MMM dd, yyyy")
        val shortLoc = request.location.getShortLocation()

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = spec.timeFontSizeSp * scale * 1.55f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            setShadowLayer(4f * scale, 2f * scale, 2f * scale, Color.BLACK)
        }

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.textColor
            textSize = spec.bodyFontSizeSp * scale * 1.45f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            setShadowLayer(3f * scale, 1f * scale, 1f * scale, Color.BLACK)
        }

        val subtitleText = if (shortLoc.isNotBlank()) "$dateStr  •  $shortLoc" else dateStr
        val contentW = max(timePaint.measureText(timeStr), subtitlePaint.measureText(subtitleText))
        val boxWidth = contentW + 40f * scale
        val boxHeight = 85f * scale

        val margin = 28f * scale
        val rect = calculatePositionRect(request.settings.stampPosition, boxWidth, boxHeight, imageWidth, imageHeight, margin)

        // Draw translucent background with no complete outer border
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.backgroundColor
            alpha = (spec.backgroundOpacity * 255).toInt()
            style = Paint.Style.FILL
        }
        val corner = spec.cornerRadiusDp * scale
        canvas.drawRoundRect(rect, corner, corner, bgPaint)

        // Draw thin vertical accent bar on left
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.accentColor
            style = Paint.Style.FILL
        }
        val barRect = RectF(rect.left + 8f * scale, rect.top + 8f * scale, rect.left + 13f * scale, rect.bottom - 8f * scale)
        canvas.drawRoundRect(barRect, 2f * scale, 2f * scale, barPaint)

        // Draw Time (primary) and Date + short location
        val textX = rect.left + 22f * scale
        val timeY = rect.top + 38f * scale
        canvas.drawText(timeStr, textX, timeY, timePaint)
        val subY = timeY + 28f * scale
        canvas.drawText(subtitleText, textX, subY, subtitlePaint)
    }

    fun renderCyberTechHud(
        canvas: Canvas,
        request: StampRenderRequest,
        spec: StampLayoutSpec,
        imageWidth: Float,
        imageHeight: Float,
        scale: Float
    ) {
        val hudColor = spec.borderColor
        val timeStr = DateFormatter.format(request.timestampMillis, "yyyy-MM-dd'T'HH:mm:ss")
        val latStr = String.format(Locale.US, "%.5f°", request.location.latitude)
        val lngStr = String.format(Locale.US, "%.5f°", request.location.longitude)
        val altStr = if (request.settings.altitudeUnit == AltitudeUnit.METERS) {
            "${String.format(Locale.US, "%.1f", request.location.altitude)}M"
        } else {
            "${String.format(Locale.US, "%.1f", request.location.altitude * 3.28084)}FT"
        }
        val dirStr = CoordinateFormatter.formatBearing(request.heading)
        val accStr = if (request.location.accuracy > 0) "±${request.location.accuracy.toInt()}M" else "N/A"

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF00E676.toInt()
            textSize = 10f * scale * 1.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = spec.timeFontSizeSp * scale * 1.35f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = hudColor
            textSize = spec.bodyFontSizeSp * scale * 1.4f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF94A3B8.toInt()
            textSize = 10f * scale * 1.4f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        val pad = spec.horizontalPaddingDp * scale
        val boxWidth = (imageWidth * 0.74f).coerceIn(440f * scale, 860f * scale)
        val boxHeight = 152f * scale

        val margin = 28f * scale
        val rect = calculatePositionRect(request.settings.stampPosition, boxWidth, boxHeight, imageWidth, imageHeight, margin)

        // Dark background (non-rounded card)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.backgroundColor
            alpha = (spec.backgroundOpacity * 255).toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRect(rect, bgPaint)

        // Four tactical corner brackets
        val bracketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = hudColor
            strokeWidth = 2.4f * scale
            style = Paint.Style.STROKE
        }
        val bl = 20f * scale
        // Top-Left
        canvas.drawLine(rect.left, rect.top, rect.left + bl, rect.top, bracketPaint)
        canvas.drawLine(rect.left, rect.top, rect.left, rect.top + bl, bracketPaint)
        // Top-Right
        canvas.drawLine(rect.right, rect.top, rect.right - bl, rect.top, bracketPaint)
        canvas.drawLine(rect.right, rect.top, rect.right, rect.top + bl, bracketPaint)
        // Bottom-Left
        canvas.drawLine(rect.left, rect.bottom, rect.left + bl, rect.bottom, bracketPaint)
        canvas.drawLine(rect.left, rect.bottom, rect.left, rect.bottom - bl, bracketPaint)
        // Bottom-Right
        canvas.drawLine(rect.right, rect.bottom, rect.right - bl, rect.bottom, bracketPaint)
        canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - bl, bracketPaint)

        // Status Header
        var cy = rect.top + pad + 6f * scale
        canvas.drawText("SYS // REC ● VERIFIED", rect.left + pad, cy, headerPaint)
        val idHex = (request.timestampMillis % 0xFFFF).toString(16).uppercase(Locale.US)
        val idStr = "ID: #$idHex"
        canvas.drawText(idStr, rect.right - pad - subPaint.measureText(idStr), cy, subPaint)

        // ISO Timestamp
        cy += 28f * scale
        canvas.drawText("UTC: $timeStr", rect.left + pad, cy, timePaint)

        // Technical Telemetry
        cy += 26f * scale
        canvas.drawText("LAT: $latStr | LON: $lngStr", rect.left + pad, cy, hudPaint)

        cy += 24f * scale
        canvas.drawText("ALT: $altStr | DIR: $dirStr | ACC: $accStr", rect.left + pad, cy, subPaint)

        if (request.location.address.isNotBlank() && request.location.address != "Detecting location...") {
            cy += 22f * scale
            val geoText = trimTextToWidth("GEO: ${request.location.address.uppercase(Locale.US)}", subPaint, boxWidth - pad * 2)
            canvas.drawText(geoText, rect.left + pad, cy, subPaint)
        }
    }

    fun renderFramedOutline(
        canvas: Canvas,
        request: StampRenderRequest,
        spec: StampLayoutSpec,
        imageWidth: Float,
        imageHeight: Float,
        scale: Float
    ) {
        val dateStr = DateFormatter.format(request.timestampMillis, "EEEE, MMMM dd, yyyy").uppercase(Locale.US)
        val timeStr = DateFormatter.format(request.timestampMillis, "HH:mm:ss z").uppercase(Locale.US)
        val coords = if (request.settings.showCoordinates) {
            CoordinateFormatter.format(request.location.latitude, request.location.longitude, request.settings.coordinateFormat)
        } else ""
        val address = if (request.settings.showAddress && request.location.address.isNotBlank() && request.location.address != "Detecting location...") {
            request.location.address.uppercase(Locale.US)
        } else ""

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.textColor
            textSize = 9f * scale * 1.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.08f
            textAlign = Paint.Align.CENTER
        }

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 12f * scale * 1.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            textSize = 10.5f * scale * 1.5f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val pad = spec.horizontalPaddingDp * scale
        val boxWidth = (imageWidth * 0.72f).coerceIn(400f * scale, 780f * scale)
        val boxHeight = 138f * scale

        val margin = 28f * scale
        val rect = calculatePositionRect(request.settings.stampPosition, boxWidth, boxHeight, imageWidth, imageHeight, margin)

        // Nearly transparent background with thin crisp outline
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = (spec.backgroundOpacity * 255).toInt()
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.borderColor
            alpha = 230
            style = Paint.Style.STROKE
            strokeWidth = spec.borderWidthDp * scale
        }
        val corner = spec.cornerRadiusDp * scale
        canvas.drawRoundRect(rect, corner, corner, bgPaint)
        canvas.drawRoundRect(rect, corner, corner, borderPaint)

        // Balanced Centered Inspection Layout (No Chips)
        val centerX = rect.centerX()
        var cy = rect.top + pad + 8f * scale
        canvas.drawText("— OFFICIAL INSPECTION RECORD —", centerX, cy, headerPaint)

        cy += 28f * scale
        canvas.drawText("$dateStr • $timeStr", centerX, cy, datePaint)

        if (coords.isNotBlank()) {
            cy += 24f * scale
            canvas.drawText("POSITION: $coords", centerX, cy, bodyPaint)
        }

        if (address.isNotBlank()) {
            cy += 22f * scale
            val trimmedLoc = trimTextToWidth("LOCATION: $address", bodyPaint, boxWidth - pad * 2)
            canvas.drawText(trimmedLoc, centerX, cy, bodyPaint)
        }
    }

    fun renderCompactPill(
        canvas: Canvas,
        request: StampRenderRequest,
        spec: StampLayoutSpec,
        imageWidth: Float,
        imageHeight: Float,
        scale: Float
    ) {
        val timeStr = DateFormatter.format(request.timestampMillis, "HH:mm")
        val dateStr = DateFormatter.format(request.timestampMillis, "MM/dd")
        val shortPlace = request.location.getShortLocation()

        val pillText = if (shortPlace.isNotBlank()) "$timeStr  •  $dateStr  •  $shortPlace" else "$timeStr  •  $dateStr"

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = spec.timeFontSizeSp * scale * 1.4f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setShadowLayer(3f * scale, 1f * scale, 1f * scale, Color.BLACK)
        }

        val textWidth = textPaint.measureText(pillText)
        val boxWidth = textWidth + 36f * scale
        val boxHeight = 48f * scale

        val margin = 28f * scale
        val rect = calculatePositionRect(request.settings.stampPosition, boxWidth, boxHeight, imageWidth, imageHeight, margin)

        // High corner radius capsule
        val pillRadius = boxHeight / 2f
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = (spec.backgroundOpacity * 255).toInt()
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.borderColor
            alpha = 190
            style = Paint.Style.STROKE
            strokeWidth = spec.borderWidthDp * scale
        }
        canvas.drawRoundRect(rect, pillRadius, pillRadius, bgPaint)
        canvas.drawRoundRect(rect, pillRadius, pillRadius, borderPaint)

        // Centered single-line text
        val textY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(pillText, rect.left + 18f * scale, textY, textPaint)
    }

    fun renderFullBanner(
        canvas: Canvas,
        request: StampRenderRequest,
        spec: StampLayoutSpec,
        imageWidth: Float,
        imageHeight: Float,
        scale: Float
    ) {
        val bannerHeight = 88f * scale
        val rect = RectF(0f, imageHeight - bannerHeight, imageWidth, imageHeight)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = (spec.backgroundOpacity * 255).toInt()
            style = Paint.Style.FILL
        }
        val topBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.borderColor
            alpha = 200
            strokeWidth = spec.borderWidthDp * scale
        }
        canvas.drawRect(rect, bgPaint)
        canvas.drawLine(0f, rect.top, imageWidth, rect.top, topBorderPaint)

        val timeStr = DateFormatter.format(request.timestampMillis, "HH:mm:ss")
        val dateStr = DateFormatter.format(request.timestampMillis, "yyyy.MM.dd")
        val coords = CoordinateFormatter.format(request.location.latitude, request.location.longitude, request.settings.coordinateFormat)
        val altStr = if (request.settings.altitudeUnit == AltitudeUnit.METERS) {
            "${String.format(Locale.US, "%.0f", request.location.altitude)}m"
        } else {
            "${String.format(Locale.US, "%.0f", request.location.altitude * 3.28084)}ft"
        }
        val dirStr = CoordinateFormatter.formatBearing(request.heading)

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.textColor
            textSize = spec.timeFontSizeSp * scale * 1.35f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 11f * scale * 1.35f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = spec.bodyFontSizeSp * scale * 1.3f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCBD5E1.toInt()
            textSize = 10f * scale * 1.3f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        val pad = 18f * scale

        // Column 1 (Left): Time & Date
        var y1 = rect.top + 32f * scale
        canvas.drawText(timeStr, pad, y1, timePaint)
        y1 += 26f * scale
        canvas.drawText(dateStr, pad, y1, datePaint)

        // Column 2 (Center): Location & Coordinates
        val col2X = pad + 180f * scale
        val maxCol2W = imageWidth - col2X - 160f * scale
        if (maxCol2W > 80f) {
            var y2 = rect.top + 30f * scale
            if (request.location.address.isNotBlank() && request.location.address != "Detecting location...") {
                val addr = trimTextToWidth(request.location.address, bodyPaint, maxCol2W)
                canvas.drawText(addr, col2X, y2, bodyPaint)
            }
            y2 += 26f * scale
            val coordTrim = trimTextToWidth(coords, subPaint, maxCol2W)
            canvas.drawText(coordTrim, col2X, y2, subPaint)
        }

        // Column 3 (Right): Telemetry Alt & Dir
        var y3 = rect.top + 30f * scale
        val altFull = "ALT $altStr"
        val dirFull = "DIR $dirStr"
        val rightX = imageWidth - pad - timePaint.measureText(altFull)
        canvas.drawText(altFull, rightX, y3, timePaint)
        y3 += 26f * scale
        canvas.drawText(dirFull, imageWidth - pad - subPaint.measureText(dirFull), y3, subPaint)
    }

    private fun calculatePositionRect(
        pos: StampPosition,
        boxW: Float,
        boxH: Float,
        imgW: Float,
        imgH: Float,
        margin: Float
    ): RectF {
        return when (pos) {
            StampPosition.TOP_LEFT -> RectF(margin, margin, margin + boxW, margin + boxH)
            StampPosition.TOP_RIGHT -> RectF(imgW - margin - boxW, margin, imgW - margin, margin + boxH)
            StampPosition.BOTTOM_LEFT -> RectF(margin, imgH - margin - boxH, margin + boxW, imgH - margin)
            StampPosition.BOTTOM_RIGHT -> RectF(imgW - margin - boxW, imgH - margin - boxH, imgW - margin, imgH - margin)
            StampPosition.BOTTOM_BANNER -> RectF(0f, imgH - boxH - margin, imgW, imgH)
        }
    }

    private fun drawCustomTextOverlay(
        canvas: Canvas,
        settings: UserSettings,
        imageWidth: Float,
        scale: Float
    ) {
        val customTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = settings.customTextColorHex.toInt()
            textSize = (settings.customTextSize * 1.6f) * scale
            isUnderlineText = settings.isCustomTextUnderline
            val fontStyle = when {
                settings.isCustomTextBold && settings.isCustomTextItalic -> Typeface.BOLD_ITALIC
                settings.isCustomTextBold -> Typeface.BOLD
                settings.isCustomTextItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            typeface = Typeface.create(Typeface.DEFAULT, fontStyle)
            setShadowLayer(6f * scale, 2f * scale, 2f * scale, Color.BLACK)
        }

        val textLines = settings.customText.split("\n")
        val customLineHeight = customTextPaint.textSize * 1.25f

        val customPadding = 20f * scale
        var maxTextW = 0f
        for (tl in textLines) {
            maxTextW = max(maxTextW, customTextPaint.measureText(tl))
        }
        val customBoxW = (maxTextW + (customPadding * 2)).coerceAtMost(imageWidth - 40f * scale)
        val customBoxH = (textLines.size * customLineHeight) + (customPadding * 1.5f)

        val customMarginTop = 48f * scale
        val customLeft = (imageWidth - customBoxW) / 2f
        val customRect = RectF(customLeft, customMarginTop, customLeft + customBoxW, customMarginTop + customBoxH)

        val customBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = 150
            style = Paint.Style.FILL
        }
        val customCorner = 14f * scale
        canvas.drawRoundRect(customRect, customCorner, customCorner, customBgPaint)

        var customY = customRect.top + customPadding + (customTextPaint.textSize * 0.82f)
        for (tl in textLines) {
            val lineX = customRect.left + (customBoxW - customTextPaint.measureText(tl)) / 2f
            canvas.drawText(tl, lineX, customY, customTextPaint)
            customY += customLineHeight
        }
    }

    private fun trimTextToWidth(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length - 1
        while (end > 0 && paint.measureText(text.substring(0, end) + "...") > maxWidth) {
            end--
        }
        return if (end > 0) text.substring(0, end) + "..." else text
    }
}
