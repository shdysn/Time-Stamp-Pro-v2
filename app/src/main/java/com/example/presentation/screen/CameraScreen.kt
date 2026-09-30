package com.example.presentation.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.presentation.components.GalleryShortcutButton
import com.example.presentation.components.ShutterButton
import com.example.presentation.components.TimestampOverlayView
import com.example.presentation.designsystem.AppColors
import com.example.presentation.viewmodel.CameraUiEffect
import com.example.presentation.viewmodel.CameraViewModel
import java.io.File

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var flashWhiteScreen by remember { mutableStateOf(false) }

    // Permission launcher for Camera and Location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        if (cameraGranted && previewViewRef != null) {
            viewModel.bindCamera(lifecycleOwner, previewViewRef!!)
        }
        viewModel.startSensors()
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Lifecycle effect for sensors
    DisposableEffect(lifecycleOwner) {
        viewModel.startSensors()
        onDispose {
            viewModel.stopSensors()
        }
    }

    // Collect effects
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CameraUiEffect.PhotoSaved -> {
                    // Shutter flash effect only - no toast popup as requested
                    flashWhiteScreen = true
                }
                is CameraUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(flashWhiteScreen) {
        if (flashWhiteScreen) {
            kotlinx.coroutines.delay(80L)
            flashWhiteScreen = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Camera Viewfinder
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewViewRef = this
                    viewModel.bindCamera(lifecycleOwner, this)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        previewViewRef?.let { pv ->
                            viewModel.cameraManager.focusOnPoint(offset.x, offset.y, pv)
                        }
                    }
                }
        )

        // 2. Real-time Timestamp & Geotag Overlay HUD
        TimestampOverlayView(
            settings = uiState.settings,
            location = uiState.location,
            heading = uiState.compassHeading,
            currentTimeMillis = uiState.currentTimeMillis,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Shutter flash effect
        AnimatedVisibility(
            visible = flashWhiteScreen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White))
        }

        // 4. Top Action Bar: Clean & Minimal - Only the Settings Icon as requested
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 32.dp)
                .align(Alignment.TopEnd)
        ) {
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // 5. Bottom Controls: Fast Front/Back switch, Shutter, and Gallery Thumbnail
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Device Gallery Shortcut / Last Photo Thumbnail
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                    .clickable { viewModel.openDeviceGallery() },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.lastCapturedMedia != null) {
                    AsyncImage(
                        model = File(uiState.lastCapturedMedia!!.filePath),
                        contentDescription = "Open Phone DCIM Gallery",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    GalleryShortcutButton(onClick = { viewModel.openDeviceGallery() })
                }
            }

            // Center: Shutter Button
            ShutterButton(
                onClick = { viewModel.capturePhoto() },
                isCapturing = uiState.isCapturing
            )

            // Right: Instant Front / Back Camera Switch Button
            IconButton(
                onClick = {
                    previewViewRef?.let { pv ->
                        viewModel.toggleCamera(lifecycleOwner, pv)
                    }
                },
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = if (uiState.isFrontCamera) "Switch to Back Camera" else "Switch to Front Camera",
                    tint = if (uiState.isFrontCamera) AppColors.AccentGold else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
