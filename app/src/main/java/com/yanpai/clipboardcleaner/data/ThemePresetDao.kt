package com.yanpai.clipboardcleaner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemePresetDao {

    @Query("SELECT * FROM theme_presets ORDER BY is_built_in DESC, created_at ASC")
    fun getAll(): Flow<List<ThemePresetEntity>>

    @Query("SELECT * FROM theme_presets WHERE id = :id")
    suspend fun getById(id: Long): ThemePresetEntity?

    @Insert
    suspend fun insert(preset: ThemePresetEntity): Long

    @Update
    suspend fun update(preset: ThemePresetEntity)

    @Query("DELETE FROM theme_presets WHERE id = :id AND is_built_in = 0")
    suspend fun delete(id: Long)
}
