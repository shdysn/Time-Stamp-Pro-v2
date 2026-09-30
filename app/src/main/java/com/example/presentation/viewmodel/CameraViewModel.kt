package com.example.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.camera.CameraManager
import com.example.camera.FlashMode
import com.example.data.model.LocationData
import com.example.data.model.StampTemplateType
import com.example.data.model.TemplateData
import com.example.data.model.UserSettings
import com.example.data.repository.SettingsRepository
import com.example.database.AppDatabase
import com.example.database.MediaEntity
import com.example.location.CompassManager
import com.example.location.GPSManager
import com.example.storage.FileManager
import com.example.watermark.StampRenderRequest
import com.example.watermark.WatermarkEngine
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CameraUiState(
    val isCapturing: Boolean = false,
    val isFrontCamera: Boolean = false,
    val flashMode: FlashMode = FlashMode.AUTO,
    val currentZoom: Float = 1f,
    val maxZoom: Float = 5f,
    val location: LocationData = LocationData(),
    val compassHeading: Float = 0f,
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val settings: UserSettings = UserSettings(),
    val lastCapturedMedia: MediaEntity? = null,
    val lastCapturedUri: Uri? = null,
    val showQuickNoteDialog: Boolean = false,
    val showCustomTextDialog: Boolean = false,
    val statusMessage: String? = null
)

sealed interface CameraUiEffect {
    data class PhotoSaved(val media: MediaEntity, val uri: Uri?) : CameraUiEffect
    data class ShowToast(val message: String) : CameraUiEffect
}

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    val cameraManager = CameraManager(application)
    val gpsManager = GPSManager(application)
    val compassManager = CompassManager(application)
    val fileManager = FileManager(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CameraUiEffect>()
    val effect: SharedFlow<CameraUiEffect> = _effect.asSharedFlow()

    init {
        // Collect persistent settings from DataStore immediately
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                Log.i("CameraViewModel", "Observed settings update from DataStore: template=${settings.templateType}")
                _uiState.update { it.copy(settings = settings) }
            }
        }

        // Collect camera states
        viewModelScope.launch {
            cameraManager.isFrontCamera.collect { isFront ->
                _uiState.update { it.copy(isFrontCamera = isFront) }
            }
        }
        viewModelScope.launch {
            cameraManager.flashMode.collect { mode ->
                _uiState.update { it.copy(flashMode = mode) }
            }
        }
        viewModelScope.launch {
            cameraManager.zoomRatio.collect { zoom ->
                _uiState.update { it.copy(currentZoom = zoom) }
            }
        }
        viewModelScope.launch {
            cameraManager.maxZoomRatio.collect { maxZ ->
                _uiState.update { it.copy(maxZoom = maxZ) }
            }
        }

        // Collect location updates
        viewModelScope.launch {
            gpsManager.locationData.collect { loc ->
                _uiState.update { it.copy(location = loc) }
            }
        }

        // Collect compass heading
        viewModelScope.launch {
            compassManager.heading.collect { head ->
                _uiState.update { it.copy(compassHeading = head) }
            }
        }

        // Live clock tick (1 second intervals for timestamp)
        viewModelScope.launch {
            while (isActive) {
                _uiState.update { it.copy(currentTimeMillis = System.currentTimeMillis()) }
                delay(1000L)
            }
        }

        // Fetch latest saved photo for thumbnail
        viewModelScope.launch {
            AppDatabase.getDatabase(application).mediaDao().getAllMedia().collect { list ->
                if (list.isNotEmpty()) {
                    _uiState.update { it.copy(lastCapturedMedia = list.first()) }
                }
            }
        }
    }

    fun startSensors() {
        gpsManager.startLocationUpdates()
        compassManager.startListening()
    }

    fun stopSensors() {
        gpsManager.stopLocationUpdates()
        compassManager.stopListening()
    }

    fun bindCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        viewModelScope.launch {
            try {
                cameraManager.getCameraProvider()
                cameraManager.bindCamera(lifecycleOwner, previewView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        cameraManager.toggleCamera(lifecycleOwner, previewView)
    }

    fun cycleFlash() {
        cameraManager.cycleFlashMode()
    }

    fun setZoom(zoom: Float) {
        cameraManager.setZoom(zoom)
    }

    fun setTemplate(templateType: StampTemplateType) {
        Log.i("CameraViewModel", "setTemplate selected: $templateType")
        viewModelScope.launch {
            settingsRepository.setTemplateType(templateType)
        }
    }

    fun setTemplate(templateId: String) {
        val type = StampTemplateType.fromId(templateId)
        setTemplate(type)
    }

    fun updateSettings(newSettings: UserSettings) {
        _uiState.update { it.copy(settings = newSettings) }
    }

    fun updateQuickNotes(project: String, inspector: String, notes: String) {
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(
                    projectName = project,
                    inspectorName = inspector,
                    customNotes = notes
                ),
                showQuickNoteDialog = false
            )
        }
    }

    fun setQuickNoteDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showQuickNoteDialog = visible) }
    }

    fun setCustomTextDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showCustomTextDialog = visible) }
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
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(
                    customText = text,
                    isCustomTextEnabled = isEnabled,
                    customTextSize = size,
                    isCustomTextBold = isBold,
                    isCustomTextItalic = isItalic,
                    isCustomTextUnderline = isUnderline,
                    customTextColorHex = colorHex
                ),
                showCustomTextDialog = false
            )
        }
    }

    fun toggleStampVisibility() {
        _uiState.update { current ->
            current.copy(
                settings = current.settings.copy(
                    isStampVisible = !current.settings.isStampVisible
                )
            )
        }
    }

    fun openDeviceGallery() {
        fileManager.openPhoneGallery(_uiState.value.lastCapturedUri)
    }

    @Volatile
    private var isCapturingInProgress = false

    fun capturePhoto() {
        if (isCapturingInProgress || _uiState.value.isCapturing) return
        isCapturingInProgress = true
        _uiState.update { it.copy(isCapturing = true) }

        viewModelScope.launch {
            try {
                // 1. Capture raw bitmap from camera or fallback simulator
                val rawBitmap = cameraManager.capturePhoto()

                // 2. Prepare mutable copy and apply watermark engine with explicit template
                val timestamp = System.currentTimeMillis()
                val activeSettings = _uiState.value.settings
                Log.i("CameraViewModel", "Capturing and stamping photo with template: ${activeSettings.templateType}")

                val renderRequest = StampRenderRequest(
                    sourceBitmap = rawBitmap,
                    templateType = activeSettings.templateType,
                    settings = activeSettings,
                    location = _uiState.value.location,
                    heading = _uiState.value.compassHeading,
                    timestampMillis = timestamp
                )
                val stampedBitmap = WatermarkEngine.renderStamp(renderRequest)

                // 3. Save directly to device DCIM storage & room database
                val savedMedia = fileManager.saveCapturedPhoto(
                    stampedBitmap = stampedBitmap,
                    originalBitmap = if (_uiState.value.settings.saveOriginalCopy) rawBitmap else null,
                    settings = _uiState.value.settings,
                    location = _uiState.value.location,
                    heading = _uiState.value.compassHeading,
                    timestampMillis = timestamp
                )
                val dcimUri = fileManager.lastSavedDcimUri

                _uiState.update { it.copy(lastCapturedMedia = savedMedia, lastCapturedUri = dcimUri) }
                _effect.emit(CameraUiEffect.PhotoSaved(savedMedia, dcimUri))
            } catch (e: Exception) {
                e.printStackTrace()
                _effect.emit(CameraUiEffect.ShowToast("Capture error: ${e.localizedMessage}"))
            } finally {
                isCapturingInProgress = false
                _uiState.update { it.copy(isCapturing = false) }
            }
        }
    }
}
