package com.example.presentation.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationData
import com.example.data.model.StampTemplateType
import com.example.data.model.UserSettings
import com.example.presentation.components.stamps.ClassicCardStamp
import com.example.presentation.components.stamps.CompactPillStamp
import com.example.presentation.components.stamps.CyberTechHudStamp
import com.example.presentation.components.stamps.FramedOutlineStamp
import com.example.presentation.components.stamps.FullBannerStamp
import com.example.presentation.components.stamps.ModernMinimalStamp
import com.example.presentation.designsystem.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(
    currentTemplate: StampTemplateType,
    onTemplateSelected: (StampTemplateType) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    userSettings: UserSettings = UserSettings()
) {
    BackHandler {
        onNavigateBack()
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Timestamp Templates",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(StampTemplateType.entries) { template ->
                val isSelected = template == currentTemplate
                val previewSettings = userSettings.copy(templateType = template)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AppColors.DarkCard else AppColors.DarkSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) AppColors.AccentGold else AppColors.BorderSubtle,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            onTemplateSelected(template)
                            onNavigateBack()
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = template.displayName,
                                color = if (isSelected) AppColors.AccentGold else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

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
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "ACTIVE",
                                            color = Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = template.description,
                            color = AppColors.TextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Visual Live Thumbnail
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onTemplateSelected(template)
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AppColors.AccentGold else AppColors.DarkSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isSelected) "Current Active Template" else "Use This Template",
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
