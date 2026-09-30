package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AltitudeUnit
import com.example.data.model.CoordinateFormat
import com.example.data.model.StampDesignStyle
import com.example.data.model.StampFontSize
import com.example.data.model.StampPosition
import com.example.data.model.StampTemplateType
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings_pref")

class SettingsRepository(private val context: Context) {

    private val dataStore = context.settingsDataStore

    companion object {
        private const val TAG = "SettingsRepository"
        val KEY_TEMPLATE_TYPE = stringPreferencesKey("template_type")
        val KEY_STAMP_POSITION = stringPreferencesKey("stamp_position")
        val KEY_TEXT_COLOR_HEX = longPreferencesKey("text_color_hex")
        val KEY_BACKGROUND_OPACITY = floatPreferencesKey("background_opacity")
        val KEY_FONT_SIZE = stringPreferencesKey("font_size")
        val KEY_DATE_FORMAT = stringPreferencesKey("date_format")
        val KEY_COORDINATE_FORMAT = stringPreferencesKey("coordinate_format")
        val KEY_ALTITUDE_UNIT = stringPreferencesKey("altitude_unit")
        val KEY_SHOW_TIMESTAMP = booleanPreferencesKey("show_timestamp")
        val KEY_SHOW_COORDINATES = booleanPreferencesKey("show_coordinates")
        val KEY_SHOW_ADDRESS = booleanPreferencesKey("show_address")
        val KEY_SHOW_ALTITUDE = booleanPreferencesKey("show_altitude")
        val KEY_SHOW_COMPASS = booleanPreferencesKey("show_compass")
        val KEY_IS_STAMP_VISIBLE = booleanPreferencesKey("is_stamp_visible")
        val KEY_CUSTOM_TEXT = stringPreferencesKey("custom_text")
        val KEY_IS_CUSTOM_TEXT_ENABLED = booleanPreferencesKey("is_custom_text_enabled")
        val KEY_CUSTOM_TEXT_SIZE = floatPreferencesKey("custom_text_size")
        val KEY_CUSTOM_TEXT_BOLD = booleanPreferencesKey("custom_text_bold")
        val KEY_CUSTOM_TEXT_ITALIC = booleanPreferencesKey("custom_text_italic")
        val KEY_CUSTOM_TEXT_UNDERLINE = booleanPreferencesKey("custom_text_underline")
        val KEY_CUSTOM_TEXT_COLOR_HEX = longPreferencesKey("custom_text_color_hex")
        val KEY_SAVE_ORIGINAL_COPY = booleanPreferencesKey("save_original_copy")
        val KEY_AUTO_SAVE_TO_GALLERY = booleanPreferencesKey("auto_save_to_gallery")
    }

    val settingsFlow: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading settings preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val templateId = preferences[KEY_TEMPLATE_TYPE]
            val templateType = StampTemplateType.fromId(templateId)
            Log.d(TAG, "Restored template from preferences: id='$templateId' -> type=$templateType")

            val posString = preferences[KEY_STAMP_POSITION]
            val position = StampPosition.entries.firstOrNull { it.name == posString } ?: StampPosition.BOTTOM_LEFT

            val fontSizeString = preferences[KEY_FONT_SIZE]
            val fontSize = StampFontSize.entries.firstOrNull { it.name == fontSizeString } ?: StampFontSize.MEDIUM

            val coordFormatString = preferences[KEY_COORDINATE_FORMAT]
            val coordFormat = CoordinateFormat.entries.firstOrNull { it.name == coordFormatString } ?: CoordinateFormat.DECIMAL

            val altUnitString = preferences[KEY_ALTITUDE_UNIT]
            val altUnit = AltitudeUnit.entries.firstOrNull { it.name == altUnitString } ?: AltitudeUnit.METERS

            val style = when (templateType) {
                StampTemplateType.CLASSIC_CARD -> StampDesignStyle.CLASSIC_CARD
                StampTemplateType.MODERN_MINIMAL -> StampDesignStyle.MODERN_MINIMAL
                StampTemplateType.CYBER_TECH_HUD -> StampDesignStyle.TECH_HUD
                StampTemplateType.FRAMED_OUTLINE -> StampDesignStyle.OUTLINE_FRAME
                StampTemplateType.COMPACT_PILL -> StampDesignStyle.COMPACT_PILL
                StampTemplateType.FULL_BANNER -> StampDesignStyle.BOTTOM_BANNER
            }

            UserSettings(
                templateType = templateType,
                stampPosition = position,
                stampDesignStyle = style,
                textColorHex = preferences[KEY_TEXT_COLOR_HEX] ?: 0xFFFFC107,
                backgroundOpacity = preferences[KEY_BACKGROUND_OPACITY] ?: 0.65f,
                fontSize = fontSize,
                dateFormat = preferences[KEY_DATE_FORMAT] ?: "yyyy-MM-dd HH:mm:ss",
                coordinateFormat = coordFormat,
                altitudeUnit = altUnit,
                showTimestamp = preferences[KEY_SHOW_TIMESTAMP] ?: true,
                showCoordinates = preferences[KEY_SHOW_COORDINATES] ?: true,
                showAddress = preferences[KEY_SHOW_ADDRESS] ?: true,
                showAltitude = preferences[KEY_SHOW_ALTITUDE] ?: true,
                showCompass = preferences[KEY_SHOW_COMPASS] ?: true,
                isStampVisible = preferences[KEY_IS_STAMP_VISIBLE] ?: true,
                customText = preferences[KEY_CUSTOM_TEXT] ?: "",
                isCustomTextEnabled = preferences[KEY_IS_CUSTOM_TEXT_ENABLED] ?: false,
                customTextSize = preferences[KEY_CUSTOM_TEXT_SIZE] ?: 24f,
                isCustomTextBold = preferences[KEY_CUSTOM_TEXT_BOLD] ?: true,
                isCustomTextItalic = preferences[KEY_CUSTOM_TEXT_ITALIC] ?: false,
                isCustomTextUnderline = preferences[KEY_CUSTOM_TEXT_UNDERLINE] ?: false,
                customTextColorHex = preferences[KEY_CUSTOM_TEXT_COLOR_HEX] ?: 0xFFFFFFFF,
                saveOriginalCopy = preferences[KEY_SAVE_ORIGINAL_COPY] ?: false,
                autoSaveToGallery = preferences[KEY_AUTO_SAVE_TO_GALLERY] ?: true
            )
        }

    suspend fun setTemplateType(type: StampTemplateType) {
        Log.i(TAG, "Persisting template selection: $type (id=${type.id})")
        dataStore.edit { preferences ->
            preferences[KEY_TEMPLATE_TYPE] = type.id
        }
    }

    suspend fun setStampPosition(position: StampPosition) {
        dataStore.edit { preferences ->
            preferences[KEY_STAMP_POSITION] = position.name
        }
    }

    suspend fun setTextColor(colorHex: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_TEXT_COLOR_HEX] = colorHex
        }
    }

    suspend fun setBackgroundOpacity(opacity: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_BACKGROUND_OPACITY] = opacity
        }
    }

    suspend fun setFontSize(fontSize: StampFontSize) {
        dataStore.edit { preferences ->
            preferences[KEY_FONT_SIZE] = fontSize.name
        }
    }

    suspend fun setDateFormat(format: String) {
        dataStore.edit { preferences ->
            preferences[KEY_DATE_FORMAT] = format
        }
    }

    suspend fun setCoordinateFormat(format: CoordinateFormat) {
        dataStore.edit { preferences ->
            preferences[KEY_COORDINATE_FORMAT] = format.name
        }
    }

    suspend fun setAltitudeUnit(unit: AltitudeUnit) {
        dataStore.edit { preferences ->
            preferences[KEY_ALTITUDE_UNIT] = unit.name
        }
    }

    suspend fun setShowTimestamp(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOW_TIMESTAMP] = show
        }
    }

    suspend fun setShowCoordinates(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOW_COORDINATES] = show
        }
    }

    suspend fun setShowAddress(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOW_ADDRESS] = show
        }
    }

    suspend fun setShowAltitude(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOW_ALTITUDE] = show
        }
    }

    suspend fun setShowCompass(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOW_COMPASS] = show
        }
    }

    suspend fun setStampVisible(visible: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_STAMP_VISIBLE] = visible
        }
    }

    suspend fun setCustomText(
        text: String,
        enabled: Boolean,
        size: Float,
        bold: Boolean,
        italic: Boolean,
        underline: Boolean,
        colorHex: Long
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_CUSTOM_TEXT] = text
            preferences[KEY_IS_CUSTOM_TEXT_ENABLED] = enabled
            preferences[KEY_CUSTOM_TEXT_SIZE] = size
            preferences[KEY_CUSTOM_TEXT_BOLD] = bold
            preferences[KEY_CUSTOM_TEXT_ITALIC] = italic
            preferences[KEY_CUSTOM_TEXT_UNDERLINE] = underline
            preferences[KEY_CUSTOM_TEXT_COLOR_HEX] = colorHex
        }
    }

    suspend fun setCustomTextEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_CUSTOM_TEXT_ENABLED] = enabled
        }
    }

    suspend fun setSaveOriginalCopy(save: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SAVE_ORIGINAL_COPY] = save
        }
    }
}
