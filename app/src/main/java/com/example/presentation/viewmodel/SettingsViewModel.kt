package com.example.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AltitudeUnit
import com.example.data.model.CoordinateFormat
import com.example.data.model.StampFontSize
import com.example.data.model.StampPosition
import com.example.data.model.StampTemplateType
import com.example.data.model.UserSettings
import com.example.data.repository.SettingsRepository
import com.example.location.CompassManager
import com.example.location.GPSManager
import com.example.storage.FileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiagnosticsInfo(
    val hasGpsPermission: Boolean = false,
    val hasCompassSensor: Boolean = false,
    val isMockGpsDetected: Boolean = false,
    val storageUsedMb: Float = 0f,
    val osVersion: String = android.os.Build.VERSION.RELEASE,
    val deviceModel: String = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    private val fileManager = FileManager(application)
    private val gpsManager = GPSManager(application)
    private val compassManager = CompassManager(application)

    val settings: StateFlow<UserSettings> = repository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserSettings()
        )

    private val _diagnostics = MutableStateFlow(DiagnosticsInfo())
    val diagnostics: StateFlow<DiagnosticsInfo> = _diagnostics.asStateFlow()

    init {
        refreshDiagnostics()
    }

    fun refreshDiagnostics() {
        viewModelScope.launch {
            val bytes = fileManager.getStorageUsageBytes()
            _diagnostics.value = DiagnosticsInfo(
                hasGpsPermission = gpsManager.hasLocationPermission(),
                hasCompassSensor = compassManager.isSupported,
                isMockGpsDetected = gpsManager.locationData.value.isMock,
                storageUsedMb = (bytes / (1024f * 1024f))
            )
        }
    }

    fun updateTemplateType(type: StampTemplateType) {
        Log.i("SettingsViewModel", "updateTemplateType called: $type")
        viewModelScope.launch {
            repository.setTemplateType(type)
        }
    }

    fun updateStampDesignStyle(style: com.example.data.model.StampDesignStyle) {
        val type = when (style) {
            com.example.data.model.StampDesignStyle.CLASSIC_CARD -> StampTemplateType.CLASSIC_CARD
            com.example.data.model.StampDesignStyle.MODERN_MINIMAL -> StampTemplateType.MODERN_MINIMAL
            com.example.data.model.StampDesignStyle.TECH_HUD -> StampTemplateType.CYBER_TECH_HUD
            com.example.data.model.StampDesignStyle.OUTLINE_FRAME -> StampTemplateType.FRAMED_OUTLINE
            com.example.data.model.StampDesignStyle.COMPACT_PILL -> StampTemplateType.COMPACT_PILL
            com.example.data.model.StampDesignStyle.BOTTOM_BANNER -> StampTemplateType.FULL_BANNER
        }
        updateTemplateType(type)
    }

    fun updateStampPosition(pos: StampPosition) {
        viewModelScope.launch {
            repository.setStampPosition(pos)
        }
    }

    fun updateTextColor(colorHex: Long) {
        viewModelScope.launch {
            repository.setTextColor(colorHex)
        }
    }

    fun updateBackgroundOpacity(opacity: Float) {
        viewModelScope.launch {
            repository.setBackgroundOpacity(opacity)
        }
    }

    fun updateFontSize(size: StampFontSize) {
        viewModelScope.launch {
            repository.setFontSize(size)
        }
    }

    fun updateDateFormat(format: String) {
        viewModelScope.launch {
            repository.setDateFormat(format)
        }
    }

    fun updateCoordinateFormat(format: CoordinateFormat) {
        viewModelScope.launch {
            repository.setCoordinateFormat(format)
        }
    }

    fun updateAltitudeUnit(unit: AltitudeUnit) {
        viewModelScope.launch {
            repository.setAltitudeUnit(unit)
        }
    }

    fun toggleShowTimestamp(show: Boolean) {
        viewModelScope.launch {
            repository.setShowTimestamp(show)
        }
    }

    fun toggleShowCoordinates(show: Boolean) {
        viewModelScope.launch {
            repository.setShowCoordinates(show)
        }
    }

    fun toggleShowAddress(show: Boolean) {
        viewModelScope.launch {
            repository.setShowAddress(show)
        }
    }

    fun toggleShowAltitude(show: Boolean) {
        viewModelScope.launch {
            repository.setShowAltitude(show)
        }
    }

    fun toggleShowCompass(show: Boolean) {
        viewModelScope.launch {
            repository.setShowCompass(show)
        }
    }

    fun toggleStampVisibility(show: Boolean) {
        viewModelScope.launch {
            repository.setStampVisible(show)
        }
    }

    fun updateCustomText(
        text: String,
        isEnabled: Boolean,
        size: Float,
        isBold: Boolean,
        isItalic: Boolean,
        isUnderline: Boolean,
        colorHex: Long
    ) {
        viewModelScope.launch {
            repository.setCustomText(text, isEnabled, size, isBold, isItalic, isUnderline, colorHex)
        }
    }

    fun toggleCustomTextEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setCustomTextEnabled(enabled)
        }
    }

    fun toggleSaveOriginal(save: Boolean) {
        viewModelScope.launch {
            repository.setSaveOriginalCopy(save)
        }
    }
}
