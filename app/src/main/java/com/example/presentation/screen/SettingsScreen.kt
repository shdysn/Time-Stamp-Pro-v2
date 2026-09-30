package com.example.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.collectAsState
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
import com.example.data.model.AltitudeUnit
import com.example.data.model.CoordinateFormat
import com.example.data.model.StampColor
import com.example.data.model.StampDesignStyle
import com.example.data.model.StampFontSize
import com.example.data.model.StampPosition
import com.example.presentation.components.CustomTextDialog
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
    val settings by viewModel.settings.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCustomTextDialog by remember { mutableStateOf(false) }

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
                SettingsNavigationTab.values().forEachIndexed { index, tab ->
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
                            SectionCard(title = "STAMP DESIGN & LAYOUT STYLE") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    StampDesignStyle.values().forEach { design ->
                                        val isSelected = settings.stampDesignStyle == design
                                        Surface(
                                            color = if (isSelected) AppColors.DarkSurfaceVariant else AppColors.DarkSurface,
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) AppColors.AccentGold else AppColors.BorderSubtle
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.updateStampDesignStyle(design) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = design.displayName,
                                                        color = if (isSelected) AppColors.AccentGold else Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = design.description,
                                                        color = AppColors.TextSecondary,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = AppColors.AccentGold,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Watermark Position
                            SectionCard(title = "WATERMARK POSITION") {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StampPosition.values().forEach { pos ->
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

                            // Color & Opacity
                            SectionCard(title = "COLOR, OPACITY & SIZE") {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("Stamp Text Color", color = AppColors.TextSecondary, fontSize = 13.sp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        StampColor.values().forEach { stampColor ->
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
                                        StampFontSize.values().forEach { size ->
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

                            // Custom Text on Picture
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
                                        CoordinateFormat.values().forEach { format ->
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
                                        AltitudeUnit.values().forEach { unit ->
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
