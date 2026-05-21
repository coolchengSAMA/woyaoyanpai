package com.yanpai.clipboardcleaner.viewmodel

import android.app.Application
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yanpai.clipboardcleaner.data.AppDatabase
import com.yanpai.clipboardcleaner.data.ThemePresetEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val dao = database.themePresetDao()
    private val prefs = application.getSharedPreferences("theme", android.content.Context.MODE_PRIVATE)

    private val _allPresets = MutableStateFlow<List<ThemePresetEntity>>(emptyList())
    val allPresets: StateFlow<List<ThemePresetEntity>> = _allPresets

    private val _activePresetId = MutableStateFlow(prefs.getLong("active_theme_id", 1L))
    val activePresetId: StateFlow<Long> = _activePresetId

    private val _activePreset = MutableStateFlow<ThemePresetEntity?>(null)
    val activePreset: StateFlow<ThemePresetEntity?> = _activePreset

    private fun refreshActivePreset() {
        val presets = _allPresets.value
        _activePreset.value = presets.find { it.id == _activePresetId.value }
            ?: presets.firstOrNull()
    }

    init {
        viewModelScope.launch {
            dao.getAll().collect { presets ->
                _allPresets.value = presets
                if (presets.isEmpty()) {
                    insertBuiltInPresets()
                } else {
                    refreshActivePreset()
                }
            }
        }
    }

    fun colorSchemeFor(isDark: Boolean): ColorScheme {
        val preset = _activePreset.value ?: return defaultFallback(isDark)
        return preset.toColorScheme(isDark)
    }

    data class BackgroundConfig(
        val path: String?,
        val scaleMode: Int,  // 0=fit 1=crop 2=fill 3=free
        val offsetX: Float,
        val offsetY: Float,
        val scale: Float
    )

    fun backgroundConfig(isDark: Boolean): BackgroundConfig {
        val preset = _activePreset.value ?: return BackgroundConfig(null, 0, 0f, 0f, 1f)
        val path = if (isDark) preset.darkBackgroundImage else preset.lightBackgroundImage
        return BackgroundConfig(path, preset.backgroundScaleMode, preset.bgOffsetX, preset.bgOffsetY, preset.bgScale)
    }

    fun backgroundImagePath(isDark: Boolean): String? {
        val preset = _activePreset.value ?: return null
        return if (isDark) preset.darkBackgroundImage else preset.lightBackgroundImage
    }

    fun setBackgroundScaleMode(presetId: Long, mode: Int) {
        viewModelScope.launch {
            val preset = dao.getById(presetId) ?: return@launch
            val updated = preset.copy(backgroundScaleMode = mode)
            dao.update(updated)
            _allPresets.value = _allPresets.value.map { if (it.id == presetId) updated else it }
            if (_activePresetId.value == presetId) _activePreset.value = updated
        }
    }

    fun setBackgroundFreeTransform(presetId: Long, offsetX: Float, offsetY: Float, scale: Float) {
        viewModelScope.launch {
            val preset = dao.getById(presetId) ?: return@launch
            val updated = preset.copy(bgOffsetX = offsetX, bgOffsetY = offsetY, bgScale = scale)
            dao.update(updated)
            _allPresets.value = _allPresets.value.map { if (it.id == presetId) updated else it }
            if (_activePresetId.value == presetId) _activePreset.value = updated
        }
    }

    fun setBackgroundImage(presetId: Long, isDark: Boolean, path: String?) {
        viewModelScope.launch {
            val preset = dao.getById(presetId) ?: return@launch
            val updated = if (isDark)
                preset.copy(darkBackgroundImage = path, backgroundScaleMode = 0, bgOffsetX = 0f, bgOffsetY = 0f, bgScale = 1f)
            else
                preset.copy(lightBackgroundImage = path, backgroundScaleMode = 0, bgOffsetX = 0f, bgOffsetY = 0f, bgScale = 1f)
            dao.update(updated)
            _allPresets.value = _allPresets.value.map { if (it.id == presetId) updated else it }
            if (_activePresetId.value == presetId) {
                _activePreset.value = updated
            }
        }
    }

    fun setActivePreset(id: Long) {
        _activePresetId.value = id
        prefs.edit().putLong("active_theme_id", id).apply()
        refreshActivePreset()
    }

    fun createPreset(name: String, lightColors: List<Color>, darkColors: List<Color>,
                     lightBgImage: String? = null, darkBgImage: String? = null) {
        viewModelScope.launch {
            val entity = ThemePresetEntity(
                name = name,
                isBuiltIn = false,
                lightPrimary = lightColors[0].toArgb(),
                lightSecondary = lightColors[1].toArgb(),
                lightTertiary = lightColors[2].toArgb(),
                lightBackground = lightColors[3].toArgb(),
                lightSurface = lightColors[4].toArgb(),
                lightError = lightColors[5].toArgb(),
                lightBackgroundImage = lightBgImage,
                darkPrimary = darkColors[0].toArgb(),
                darkSecondary = darkColors[1].toArgb(),
                darkTertiary = darkColors[2].toArgb(),
                darkBackground = darkColors[3].toArgb(),
                darkSurface = darkColors[4].toArgb(),
                darkError = darkColors[5].toArgb(),
                darkBackgroundImage = darkBgImage
            )
            val id = dao.insert(entity)
            setActivePreset(id)
        }
    }

    fun updatePreset(preset: ThemePresetEntity) {
        viewModelScope.launch {
            dao.update(preset)
            // 更新列表中对应项，触发重组
            _allPresets.value = _allPresets.value.map { if (it.id == preset.id) preset else it }
            if (_activePresetId.value == preset.id) {
                _activePreset.value = preset
            }
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            dao.delete(id)
            _allPresets.value = _allPresets.value.filter { it.id != id }
            if (_activePresetId.value == id) {
                val fallback = _allPresets.value.firstOrNull()
                if (fallback != null) setActivePreset(fallback.id)
            }
        }
    }

    private suspend fun insertBuiltInPresets() {
        val now = System.currentTimeMillis()
        // 预设1：默认蓝色
        dao.insert(ThemePresetEntity(
            name = "默认蓝色", isBuiltIn = true,
            lightPrimary = 0xFF2196F3.toInt(), lightSecondary = 0xFFE3F2FD.toInt(),
            lightTertiary = 0xFF2196F3.toInt(), lightBackground = 0xFFF5F5F5.toInt(),
            lightSurface = 0xFFFFFFFF.toInt(), lightError = 0xFFF44336.toInt(),
            darkPrimary = 0xFFB8860B.toInt(), darkSecondary = 0xFF2A2A2A.toInt(),
            darkTertiary = 0xFFB8860B.toInt(), darkBackground = 0xFF000000.toInt(),
            darkSurface = 0xFF1A1A1A.toInt(), darkError = 0xFFF44336.toInt(),
            createdAt = now
        ))
        // 预设2：暗金色
        dao.insert(ThemePresetEntity(
            name = "暗金色", isBuiltIn = true,
            lightPrimary = 0xFFB8860B.toInt(), lightSecondary = 0xFFFFF3E0.toInt(),
            lightTertiary = 0xFFB8860B.toInt(), lightBackground = 0xFFF5F5F5.toInt(),
            lightSurface = 0xFFFFFFFF.toInt(), lightError = 0xFFF44336.toInt(),
            darkPrimary = 0xFFB8860B.toInt(), darkSecondary = 0xFF2A2A2A.toInt(),
            darkTertiary = 0xFFB8860B.toInt(), darkBackground = 0xFF000000.toInt(),
            darkSurface = 0xFF1A1A1A.toInt(), darkError = 0xFFF44336.toInt(),
            createdAt = now + 1
        ))
    }

    private fun defaultFallback(isDark: Boolean): ColorScheme {
        return if (isDark) darkColorScheme(
            primary = Color(0xFFB8860B), secondary = Color(0xFF2A2A2A),
            tertiary = Color(0xFFB8860B), background = Color(0xFF000000),
            surface = Color(0xFF1A1A1A), error = Color(0xFFF44336)
        ) else lightColorScheme(
            primary = Color(0xFF2196F3), secondary = Color(0xFFE3F2FD),
            tertiary = Color(0xFF2196F3), background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF), error = Color(0xFFF44336)
        )
    }
}

fun ThemePresetEntity.toColorScheme(darkTheme: Boolean): ColorScheme {
    val primary: Color
    val secondary: Color
    val tertiary: Color
    val background: Color
    val surface: Color
    val error: Color
    if (darkTheme) {
        primary = Color(darkPrimary); secondary = Color(darkSecondary)
        tertiary = Color(darkTertiary); background = Color(darkBackground)
        surface = Color(darkSurface); error = Color(darkError)
    } else {
        primary = Color(lightPrimary); secondary = Color(lightSecondary)
        tertiary = Color(lightTertiary); background = Color(lightBackground)
        surface = Color(lightSurface); error = Color(lightError)
    }
    val onPrimary = contentColorFor(primary)
    val onSecondary = contentColorFor(secondary)
    val onBackground = contentColorFor(background)
    val onSurface = contentColorFor(surface)
    return if (darkTheme) darkColorScheme(
        primary = primary, secondary = secondary, tertiary = tertiary,
        background = background, surface = surface, error = error,
        onPrimary = onPrimary, onSecondary = onSecondary,
        onBackground = onBackground, onSurface = onSurface
    ) else lightColorScheme(
        primary = primary, secondary = secondary, tertiary = tertiary,
        background = background, surface = surface, error = error,
        onPrimary = onPrimary, onSecondary = onSecondary,
        onBackground = onBackground, onSurface = onSurface
    )
}

private fun contentColorFor(color: Color): Color {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return if (luminance > 0.5f) Color(0xFF000000) else Color(0xFFFFFFFF)
}
