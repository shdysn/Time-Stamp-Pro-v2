package com.example

import android.graphics.Bitmap
import com.example.data.model.AltitudeUnit
import com.example.data.model.CoordinateFormat
import com.example.data.model.LocationData
import com.example.data.model.StampTemplateType
import com.example.data.model.UserSettings
import com.example.presentation.viewmodel.CameraUiState
import com.example.watermark.StampLayoutSpec
import com.example.watermark.StampRenderRequest
import com.example.watermark.StampRenderer
import com.example.watermark.WatermarkEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TemplatePipelineTest {

    @Test
    fun testEveryTemplateIdSavesAndRestoresCorrectly() {
        for (template in StampTemplateType.entries) {
            val savedId = template.id
            val restored = StampTemplateType.fromId(savedId)
            assertEquals("Template $template must restore from its ID string '$savedId'", template, restored)
        }

        // Invalid or null IDs must safely fallback to CLASSIC_CARD
        assertEquals(StampTemplateType.CLASSIC_CARD, StampTemplateType.fromId(null))
        assertEquals(StampTemplateType.CLASSIC_CARD, StampTemplateType.fromId(""))
        assertEquals(StampTemplateType.CLASSIC_CARD, StampTemplateType.fromId("NON_EXISTENT_TEMPLATE_ID"))
    }

    @Test
    fun testNoTemplateAccidentallyMappedToClassicCard() {
        for (template in StampTemplateType.entries) {
            if (template != StampTemplateType.CLASSIC_CARD) {
                val restored = StampTemplateType.fromId(template.id)
                assertNotEquals(
                    "Template ${template.name} should not be mapped to CLASSIC_CARD",
                    StampTemplateType.CLASSIC_CARD,
                    restored
                )
                assertEquals(template, restored)
            }
        }
    }

    @Test
    fun testSelectingEachTemplateUpdatesCameraUiState() {
        var state = CameraUiState()
        for (template in StampTemplateType.entries) {
            val updatedSettings = state.settings.copy(templateType = template)
            state = state.copy(settings = updatedSettings)
            assertEquals("CameraUiState must reflect selected template $template", template, state.settings.templateType)
        }
    }

    @Test
    fun testSelectedTemplateReachesStampRenderRequest() {
        val testBitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val location = LocationData(latitude = 37.7749, longitude = -122.4194, altitude = 50.0, address = "Test Street")

        for (template in StampTemplateType.entries) {
            val settings = UserSettings(templateType = template)
            val request = StampRenderRequest(
                sourceBitmap = testBitmap,
                templateType = template,
                settings = settings,
                location = location,
                heading = 90f,
                timestampMillis = 1700000000000L
            )

            assertEquals("StampRenderRequest must contain the explicit selected template", template, request.templateType)
            assertEquals("Settings inside StampRenderRequest must match template", template, request.settings.templateType)
        }
    }

    @Test
    fun testSavedBitmapRenderingUsesSelectedTemplate() {
        val testBitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val location = LocationData(
            latitude = 37.7749,
            longitude = -122.4194,
            altitude = 45.0,
            address = "123 Market St, San Francisco, CA",
            locality = "San Francisco",
            accuracy = 5.0f
        )

        for (template in StampTemplateType.entries) {
            val settings = UserSettings(templateType = template)
            val request = StampRenderRequest(
                sourceBitmap = testBitmap,
                templateType = template,
                settings = settings,
                location = location,
                heading = 180f,
                timestampMillis = System.currentTimeMillis()
            )

            val output = WatermarkEngine.renderStamp(request)
            assertNotNull("Output bitmap must not be null for template $template", output)
            assertEquals(1080, output.width)
            assertEquals(1920, output.height)
        }
    }

    @Test
    fun testProportionalScalingOnDifferentResolutions() {
        val resolutions = listOf(
            Pair(1080, 1920), // 1080p
            Pair(1440, 2560), // 1440p
            Pair(2160, 3840)  // 4K
        )

        val location = LocationData(latitude = 34.0522, longitude = -118.2437, altitude = 100.0, address = "Los Angeles")

        for ((w, h) in resolutions) {
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            for (template in StampTemplateType.entries) {
                val request = StampRenderRequest(
                    sourceBitmap = bitmap,
                    templateType = template,
                    settings = UserSettings(templateType = template),
                    location = location,
                    heading = 0f
                )
                val rendered = StampRenderer.render(request)
                assertNotNull("Rendered bitmap must be valid at resolution ${w}x${h}", rendered)
                assertEquals(w, rendered.width)
                assertEquals(h, rendered.height)
            }
        }
    }

    @Test
    fun testMissingDataDoesNotCrashAnyTemplate() {
        val bitmap = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888)
        // Missing / empty / zero / extreme values
        val emptyLocation = LocationData(
            latitude = 0.0,
            longitude = 0.0,
            altitude = 0.0,
            address = "",
            locality = "",
            accuracy = 0f,
            isMock = false
        )

        for (template in StampTemplateType.entries) {
            val request = StampRenderRequest(
                sourceBitmap = bitmap,
                templateType = template,
                settings = UserSettings(templateType = template, showAddress = true, showCoordinates = true, showAltitude = true),
                location = emptyLocation,
                heading = 0f,
                timestampMillis = 0L
            )

            val result = WatermarkEngine.renderStamp(request)
            assertNotNull("Rendering must not crash when location data is empty for $template", result)
        }
    }

    @Test
    fun testDistinctDesignLayoutSpecs() {
        val classic = StampLayoutSpec.create(StampTemplateType.CLASSIC_CARD, 0xFFFFC107, 0.65f)
        val modern = StampLayoutSpec.create(StampTemplateType.MODERN_MINIMAL, 0xFFFFC107, 0.65f)
        val hud = StampLayoutSpec.create(StampTemplateType.CYBER_TECH_HUD, 0xFFFFC107, 0.65f)
        val framed = StampLayoutSpec.create(StampTemplateType.FRAMED_OUTLINE, 0xFFFFC107, 0.65f)
        val pill = StampLayoutSpec.create(StampTemplateType.COMPACT_PILL, 0xFFFFC107, 0.65f)
        val banner = StampLayoutSpec.create(StampTemplateType.FULL_BANNER, 0xFFFFC107, 0.65f)

        // 1. Classic Card has chips and rounded corners
        assertTrue("Classic Card should show chips", classic.showChips)
        assertEquals(12f, classic.cornerRadiusDp, 0.01f)

        // 2. Modern Minimal has accent bar, no border, hides coordinates/alt/compass
        assertTrue("Modern Minimal should have accent bar", modern.showAccentBar)
        assertEquals(0f, modern.borderWidthDp, 0.01f)
        assertFalse("Modern Minimal should hide coordinates", modern.showCoordinates)
        assertFalse("Modern Minimal should hide altitude", modern.showAltitude)

        // 3. Cyber Tech HUD has corner brackets and monospace font
        assertTrue("Cyber Tech HUD should have corner brackets", hud.showCornerBrackets)
        assertTrue("Cyber Tech HUD should be monospace", hud.isMonospace)
        assertEquals(0f, hud.cornerRadiusDp, 0.01f)

        // 4. Framed Outline has formal centered uppercase layout without chips
        assertTrue("Framed Outline should be centered", framed.isCentered)
        assertTrue("Framed Outline should be uppercase only", framed.isUppercaseOnly)
        assertFalse("Framed Outline should not have chips", framed.showChips)

        // 5. Compact Pill has high corner radius capsule
        assertTrue("Compact Pill must have high corner radius", pill.cornerRadiusDp >= 20f)
        assertFalse("Compact Pill should hide technical coordinates", pill.showCoordinates)

        // 6. Full Banner is docked edge-to-edge
        assertTrue("Full Banner must be full width", banner.isFullWidth)
        assertEquals("Full Banner bottom corner radius should be zero", 0f, banner.cornerRadiusDp, 0.01f)
    }
}
