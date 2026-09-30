package com.example.watermark

import android.graphics.Bitmap
import com.example.data.model.LocationData
import com.example.data.model.StampTemplateType
import com.example.data.model.UserSettings

data class StampRenderRequest(
    val sourceBitmap: Bitmap,
    val templateType: StampTemplateType,
    val settings: UserSettings,
    val location: LocationData,
    val heading: Float,
    val timestampMillis: Long = System.currentTimeMillis()
)
