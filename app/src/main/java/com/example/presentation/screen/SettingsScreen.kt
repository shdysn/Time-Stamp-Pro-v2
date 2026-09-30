package com.example.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AltitudeUnit
import com.example.data.model.CoordinateFormat
import com.example.data.model.LocationData
import com.example.data.model.StampColor
import com.example.data.model.StampFontSize
import com.example.data.model.StampPosition
import com.example.data.model.StampTemplateType
import com.example.presentation.components.CustomTextDialog
import com.example.presentation.components.stamps.ClassicCardStamp
import com.example.presentation.components.stamps.CompactPillStamp
import com.example.presentation.components.stamps.CyberTechHudStamp
import com.example.presentation.components.stamps.FramedOutlineStamp
import com.example.presentation.components.stamps.FullBannerStamp
import com.example.presentation.components.stamps.ModernMinimalStamp
import com.example.presentation.designsystem.AppColors
import com.example.presentation.viewmodel.SettingsViewModel
import com.example.timestamp.DateFormatter
import java.util.Locale

enum class SettingsNavigationTab(val label: String, val icon: ImageVector) {
    DESIGN("Style & Designs", Icons.Default.Palette),
    GPS("GPS & Time", Icons.Default.LocationOn),
    CAMERA("Camera & Info", Icons.Default.CameraAlt)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val diagnostics by viewModel.diagnostics.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCustomTextDialog by remember { mutableStateOf(false) }

    // Sample data for realistic previews
    val sampleLocation = remember {
        LocationData(
            latitude = 37.774929,
            longitude = -122.419416,
            altitude = 42.5,
            address = "100 Market St, Financial District, San Francisco, CA",
            locality = "San Francisco",
            accuracy = 3.5f,
            isMock = false
        )
    }
    val sampleHeading = 45f
    val sampleTime = remember { System.currentTimeMillis() }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Camera Settings",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.DarkBackground
                )
            )
        },
        containerColor = AppColors.DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Smart Navigation Tabs Header
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AppColors.DarkCard,
                contentColor = AppColors.AccentGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AppColors.AccentGold,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                SettingsNavigationTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AppColors.AccentGold else AppColors.TextSecondary
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(20.dp),
                                tint = if (isSelected) AppColors.AccentGold else AppColors.TextTertiary
                            )
                        }
                    )
                }
            }

            // Tab Content with Animated Transition
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "settings_tab_content",
                modifier = Modifier.fillMaxSize()
            ) { targetTab ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (targetTab) {
                        0 -> {
                            // TAB 1: STAMP STYLE & DESIGNS

                            // 1. Prominent Large Live Preview at Top
                            SectionCard(title = "LIVE TEMPLATE PREVIEW") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black)
                                        .border(1.dp, AppColors.BorderSubtle, RoundedCornerShape(10.dp))
                                        .padding(if (settings.templateType == StampTemplateType.FULL_BANNER) 0.dp else 12.dp),
                                    contentAlignment = when (settings.stampPosition) {
                                        StampPosition.TOP_LEFT -> Alignment.TopStart
                                        StampPosition.TOP_RIGHT -> Alignment.TopEnd
                                        StampPosition.BOTTOM_LEFT -> Alignment.BottomStart
                                        StampPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
                                        StampPosition.BOTTOM_BANNER -> Alignment.BottomCenter
                                    }.let { if (settings.templateType == StampTemplateType.FULL_BANNER) Alignment.BottomCenter else it }
                                ) {
                                    when (settings.templateType) {
                                        StampTemplateType.CLASSIC_CARD -> ClassicCardStamp(
                                            settings = settings,
                                            location = sampleLocation,
                                            heading = sampleHeading,
                                            currentTimeMillis = sampleTime
                                        )
                                        StampTemplateType.MODERN_MINIMAL -> ModernMinimalStamp(
                                            settings = settings,
                                            location = sampleLocation,
                                            currentTimeMillis = sampleTime
                                        )
                                        StampTemplateType.CYBER_TECH_HUD -> CyberTechHudStamp(
                                            settings = settings,
                                            location = sampleLocation,
                                            heading = sampleHeading,
                                            currentTimeMillis = sampleTime
                                        )
                                        StampTemplateType.FRAMED_OUTLINE -> FramedOutlineStamp(
                                            settings = settings,
                                            location = sampleLocation,
                                            currentTimeMillis = sampleTime
                                        )
                                        StampTemplateType.COMPACT_PILL -> CompactPillStamp(
                                            settings = settings,
                                            location = sampleLocation,
                                            currentTimeMillis = sampleTime
                                        )
                                        StampTemplateType.FULL_BANNER -> FullBannerStamp(
                                            settings = settings,
                                            location = sampleLocation,
                                            heading = sampleHeading,
                                            currentTimeMillis = sampleTime
                                        )
                                    }
                                }
                            }

                            // 2. Six Distinct Templates with Live Visual Thumbnails
                            SectionCard(title = "SELECT STAMP TEMPLATE") {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    StampTemplateType.entries.forEach { template ->
                                        val isSelected = settings.templateType == template
                                        val previewSettings = settings.copy(templateType = template)

                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) AppColors.DarkSurfaceVariant else AppColors.DarkSurface
                                            ),
                                            border = BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) AppColors.AccentGold else AppColors.BorderSubtle
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.updateTemplateType(template) }
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                // Header Row: Title, Description, Active Checkmark
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = template.displayName,
                                                            color = if (isSelected) AppColors.AccentGold else Color.White,
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = template.description,
                                                            color = AppColors.TextSecondary,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                    if (isSelected) {
                                                        Surface(
                                                            color = AppColors.AccentGold,
                                                            shape = RoundedCornerShape(12.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Check,
                                                                    contentDescription = "Selected",
                                                                    tint = Color.Black,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                                Text(
                                                                    text = "ACTIVE",
                                                                    color = Color.Black,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Live Visual Thumbnail Preview Container
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color.Black.copy(alpha = 0.85f))
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isSelected) AppColors.AccentGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(8.dp),
                                                    contentAlignment = if (template == StampTemplateType.FULL_BANNER) Alignment.BottomCenter else Alignment.CenterStart
                                                ) {
                                                    when (template) {
                                                        StampTemplateType.CLASSIC_CARD -> ClassicCardStamp(
                                                            settings = previewSettings,
                                                            location = sampleLocation,
                                                            heading = sampleHeading,
                                                            currentTimeMillis = sampleTime,
                                                            isThumbnailPreview = true
                                                        )
                                                        StampTemplateType.MODERN_MINIMAL -> ModernMinimalStamp(
                                                            settings = previewSettings,
                                                            location = sampleLocation,
                                                            currentTimeMillis = sampleTime,
                                                            isThumbnailPreview = true
                                                        )
                                                        StampTemplateType.CYBER_TECH_HUD -> CyberTechHudStamp(
                                                            settings = previewSettings,
                                                            location = sampleLocation,
                                                            heading = sampleHeading,
                                                            currentTimeMillis = sampleTime,
                                                            isThumbnailPreview = true
                                                        )
                                                        StampTemplateType.FRAMED_OUTLINE -> FramedOutlineStamp(
                                                            settings = previewSettings,
                                                            location = sampleLocation,
                                                            currentTimeMillis = sampleTime,
                                                            isThumbnailPreview = true
                                                        )
                                                        StampTemplateType.COMPACT_PILL -> CompactPillStamp(
                                                            settings = previewSettings,
                                                            location = sampleLocation,
                                                            currentTimeMillis = sampleTime,
                                                            isThumbnailPreview = true
                                                        )
                                                        StampTemplateType.FULL_BANNER -> FullBannerStamp(
                                                            settings = previewSettings,
                                                            location = sampleLocation,
                                                            heading = sampleHeading,
                                                            currentTimeMillis = sampleTime,
                                                            isThumbnailPreview = true
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Watermark Position
                            SectionCard(title = "WATERMARK POSITION") {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StampPosition.entries.forEach { pos ->
                                        val isSelected = settings.stampPosition == pos
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) AppColors.DarkSurfaceVariant else Color.Transparent)
                                                .clickable { viewModel.updateStampPosition(pos) }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = pos.displayName,
                                                color = if (isSelected) AppColors.AccentGold else Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.AccentGold, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // 4. Color, Opacity & Font Size
                            SectionCard(title = "COLOR, OPACITY & SIZE") {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("Stamp Text Color", color = AppColors.TextSecondary, fontSize = 13.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        StampColor.entries.forEach { stampColor ->
                                            val isSelected = settings.textColorHex == stampColor.colorHex
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(stampColor.colorHex))
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.4f),
                                                        shape = CircleShape
                                                    )
                                                    .clickable { viewModel.updateTextColor(stampColor.colorHex) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }

                                    // Background Opacity
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Background Opacity", color = AppColors.TextSecondary, fontSize = 13.sp)
                                        Text("${(settings.backgroundOpacity * 100).toInt()}%", color = AppColors.AccentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = settings.backgroundOpacity,
                                        onValueChange = { viewModel.updateBackgroundOpacity(it) },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = AppColors.AccentGold,
                                            activeTrackColor = AppColors.AccentGold
                                        )
                                    )

                                    // Font Size
                                    Text("Font Size", color = AppColors.TextSecondary, fontSize = 13.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StampFontSize.entries.forEach { size ->
                                            val isSelected = settings.fontSize == size
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { viewModel.updateFontSize(size) },
                                                label = { Text(size.displayName) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AppColors.AccentGold,
                                                    selectedLabelColor = Color.Black
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            // 5. Custom Text on Picture
                            SectionCard(title = "CUSTOM TEXT OVERLAY") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SettingToggleRow("Enable Custom Text", settings.isCustomTextEnabled) {
                                        viewModel.toggleCustomTextEnabled(it)
                                    }

                                    Surface(
                                        onClick = { showCustomTextDialog = true },
                                        color = AppColors.DarkSurfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = if (settings.customText.isNotBlank()) "\"${settings.customText}\"" else "Set Custom Text",
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Configure text, size, font weight & styling",
                                                    color = AppColors.AccentGold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(
                                                text = "Edit ✎",
                                                color = AppColors.AccentGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // TAB 2: GPS & LOCATION
                            SectionCard(title = "VISIBLE ELEMENTS ON PHOTO") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SettingToggleRow("Display Timestamp HUD Stamp", settings.isStampVisible) { viewModel.toggleStampVisibility(it) }
                                    SettingToggleRow("Show Date & Time", settings.showTimestamp) { viewModel.toggleShowTimestamp(it) }
                                    SettingToggleRow("Show GPS Coordinates", settings.showCoordinates) { viewModel.toggleShowCoordinates(it) }
                                    SettingToggleRow("Show Reverse Address", settings.showAddress) { viewModel.toggleShowAddress(it) }
                                    SettingToggleRow("Show Altitude", settings.showAltitude) { viewModel.toggleShowAltitude(it) }
                                    SettingToggleRow("Show Compass Bearing", settings.showCompass) { viewModel.toggleShowCompass(it) }
                                }
                            }

                            SectionCard(title = "DATE & TIME FORMAT") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DateFormatter.COMMON_DATE_FORMATS.forEach { format ->
                                        val isSelected = settings.dateFormat == format
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) AppColors.DarkSurfaceVariant else Color.Transparent)
                                                .clickable { viewModel.updateDateFormat(format) }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = format,
                                                color = if (isSelected) AppColors.AccentGold else Color.White,
                                                fontSize = 13.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.AccentGold, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            SectionCard(title = "GPS COORDINATES & ALTITUDE UNITS") {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Coordinate Notation", color = AppColors.TextSecondary, fontSize = 13.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        CoordinateFormat.entries.forEach { format ->
                                            FilterChip(
                                                selected = settings.coordinateFormat == format,
                                                onClick = { viewModel.updateCoordinateFormat(format) },
                                                label = { Text(format.name) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AppColors.AccentGold,
                                                    selectedLabelColor = Color.Black
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text("Altitude Unit", color = AppColors.TextSecondary, fontSize = 13.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AltitudeUnit.entries.forEach { unit ->
                                            FilterChip(
                                                selected = settings.altitudeUnit == unit,
                                                onClick = { viewModel.updateAltitudeUnit(unit) },
                                                label = { Text(unit.displayName) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AppColors.AccentGold,
                                                    selectedLabelColor = Color.Black
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // TAB 3: CAMERA & SYSTEM
                            SectionCard(title = "CAMERA PREFERENCES") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SettingToggleRow("Save Original Unstamped Copy (Internal Backup)", settings.saveOriginalCopy) {
                                        viewModel.toggleSaveOriginal(it)
                                    }
                                }
                            }

                            SectionCard(title = "SYSTEM & SENSOR DIAGNOSTICS") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DiagnosticsRow("Location Services", if (diagnostics.hasGpsPermission) "Active (High Accuracy)" else "Permission Required", diagnostics.hasGpsPermission)
                                    DiagnosticsRow("Compass Magnetometer", if (diagnostics.hasCompassSensor) "Hardware Available" else "Synthetic Heading", diagnostics.hasCompassSensor)
                                    DiagnosticsRow("Mock Location Check", if (diagnostics.isMockGpsDetected) "MOCK GPS DETECTED" else "Genuine GPS", !diagnostics.isMockGpsDetected)
                                    DiagnosticsRow("Internal Storage Cache", String.format(Locale.US, "%.2f MB", diagnostics.storageUsedMb), true)
                                    DiagnosticsRow("Device Model", diagnostics.deviceModel, true)
                                    DiagnosticsRow("Android OS Version", "Android ${diagnostics.osVersion}", true)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCustomTextDialog) {
            CustomTextDialog(
                initialText = settings.customText,
                initialEnabled = settings.isCustomTextEnabled,
                initialSize = settings.customTextSize,
                initialBold = settings.isCustomTextBold,
                initialItalic = settings.isCustomTextItalic,
                initialUnderline = settings.isCustomTextUnderline,
                initialColorHex = settings.customTextColorHex,
                onApply = { text, isEnabled, size, isBold, isItalic, isUnderline, colorHex ->
                    viewModel.updateCustomText(text, isEnabled, size, isBold, isItalic, isUnderline, colorHex)
                    showCustomTextDialog = false
                },
                onDismiss = { showCustomTextDialog = false }
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.DarkCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = AppColors.AccentGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.AccentGold,
                checkedTrackColor = AppColors.AccentGold.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = AppColors.DarkSurface
            )
        )
    }
}

@Composable
private fun DiagnosticsRow(label: String, status: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = AppColors.TextSecondary, fontSize = 13.sp)
        Text(
            text = status,
            color = if (isOk) AppColors.AccentGreen else AppColors.AccentRed,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
