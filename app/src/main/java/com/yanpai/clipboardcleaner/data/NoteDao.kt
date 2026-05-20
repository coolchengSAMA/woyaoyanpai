package com.yanpai.clipboardcleaner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM note_entries WHERE category = :category ORDER BY updated_at DESC")
    fun getByCategory(category: String): Flow<List<NoteEntry>>

    @Query("SELECT * FROM note_entries WHERE cleaned = :cleaned AND category = :category LIMIT 1")
    suspend fun findDuplicate(cleaned: String, category: String): NoteEntry?

    @Query("UPDATE note_entries SET updated_at = :updatedAt, original = :original WHERE id = :id")
    suspend fun updateDuplicate(id: Long, updatedAt: Long, original: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: NoteEntry): Long

    @Query("UPDATE note_entries SET is_read = CASE WHEN is_read = 0 THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun toggleRead(id: Long)

    @Delete
    suspend fun delete(entry: NoteEntry)

    @Query("DELETE FROM note_entries WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Query("SELECT COUNT(*) FROM note_entries WHERE category = :category")
    fun getCountByCategory(category: String): Flow<Int>
}
