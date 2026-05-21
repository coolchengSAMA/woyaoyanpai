package com.yanpai.clipboardcleaner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "theme_presets")
data class ThemePresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "is_built_in") val isBuiltIn: Boolean = false,
    // 亮色模式
    @ColumnInfo(name = "light_primary") val lightPrimary: Int,
    @ColumnInfo(name = "light_secondary") val lightSecondary: Int,
    @ColumnInfo(name = "light_tertiary") val lightTertiary: Int,
    @ColumnInfo(name = "light_background") val lightBackground: Int,
    @ColumnInfo(name = "light_surface") val lightSurface: Int,
    @ColumnInfo(name = "light_error") val lightError: Int,
    @ColumnInfo(name = "light_bg_image") val lightBackgroundImage: String? = null,
    // 深色模式
    @ColumnInfo(name = "dark_primary") val darkPrimary: Int,
    @ColumnInfo(name = "dark_secondary") val darkSecondary: Int,
    @ColumnInfo(name = "dark_tertiary") val darkTertiary: Int,
    @ColumnInfo(name = "dark_background") val darkBackground: Int,
    @ColumnInfo(name = "dark_surface") val darkSurface: Int,
    @ColumnInfo(name = "dark_error") val darkError: Int,
    @ColumnInfo(name = "dark_bg_image") val darkBackgroundImage: String? = null,
    @ColumnInfo(name = "bg_scale_mode") val backgroundScaleMode: Int = 0, // 0=fit 1=crop 2=fill 3=free
    @ColumnInfo(name = "bg_offset_x") val bgOffsetX: Float = 0f,
    @ColumnInfo(name = "bg_offset_y") val bgOffsetY: Float = 0f,
    @ColumnInfo(name = "bg_scale") val bgScale: Float = 1f,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
