package com.yanpai.clipboardcleaner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // ── 正常查询（排除已删除，置顶优先）──

    @Query("SELECT * FROM note_entries WHERE category = :category AND deleted_at IS NULL ORDER BY is_pinned DESC, updated_at DESC")
    fun getByCategory(category: String): Flow<List<NoteEntry>>

    @Query("SELECT COUNT(*) FROM note_entries WHERE category = :category AND deleted_at IS NULL")
    fun getCountByCategory(category: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM note_entries WHERE deleted_at IS NULL")
    fun getTotalCount(): Flow<Int>

    // ── 搜索 ──

    @Query("SELECT * FROM note_entries WHERE category = :category AND deleted_at IS NULL AND (cleaned LIKE '%' || :query || '%' OR original LIKE '%' || :query || '%') ORDER BY is_pinned DESC, updated_at DESC")
    fun searchByCategory(category: String, query: String): Flow<List<NoteEntry>>

    // ── 回收站 ──

    @Query("SELECT * FROM note_entries WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC")
    fun getTrash(): Flow<List<NoteEntry>>

    @Query("UPDATE note_entries SET deleted_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE note_entries SET deleted_at = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM note_entries WHERE deleted_at IS NOT NULL")
    suspend fun clearTrash()

    @Query("DELETE FROM note_entries WHERE deleted_at IS NOT NULL AND category = :category")
    suspend fun clearTrashByCategory(category: String)

    @Query("DELETE FROM note_entries WHERE deleted_at IS NOT NULL AND deleted_at < :before")
    suspend fun autoCleanTrash(before: Long)

    // ── 置顶 ──

    @Query("UPDATE note_entries SET is_pinned = CASE WHEN is_pinned = 0 THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun togglePin(id: Long)

    // ── 基本操作 ──

    @Query("SELECT * FROM note_entries WHERE cleaned = :cleaned AND category = :category AND deleted_at IS NULL LIMIT 1")
    suspend fun findDuplicate(cleaned: String, category: String): NoteEntry?

    @Query("SELECT * FROM note_entries WHERE cleaned = :cleaned AND category = :category AND deleted_at IS NOT NULL LIMIT 1")
    suspend fun findDeletedDuplicate(cleaned: String, category: String): NoteEntry?

    @Query("UPDATE note_entries SET updated_at = :updatedAt, original = :original, deleted_at = NULL WHERE id = :id")
    suspend fun updateDuplicate(id: Long, updatedAt: Long, original: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: NoteEntry): Long

    @Query("UPDATE note_entries SET is_read = CASE WHEN is_read = 0 THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun toggleRead(id: Long)

    @Query("DELETE FROM note_entries WHERE id = :id")
    suspend fun permanentDelete(id: Long)

    // ── 分类管理（软删除） ──

    @Query("UPDATE note_entries SET deleted_at = :timestamp WHERE category = :category AND deleted_at IS NULL")
    suspend fun softDeleteByCategory(category: String, timestamp: Long = System.currentTimeMillis())

    // ── 批量操作 ──

    @Query("UPDATE note_entries SET is_read = 1 WHERE id IN (:ids)")
    suspend fun batchMarkRead(ids: List<Long>)

    @Query("UPDATE note_entries SET deleted_at = :timestamp WHERE id IN (:ids)")
    suspend fun batchSoftDelete(ids: List<Long>, timestamp: Long = System.currentTimeMillis())

    // ── 导出（获取所有数据，含已删除） ──

    @Query("SELECT * FROM note_entries ORDER BY created_at ASC")
    suspend fun getAllForExport(): List<NoteEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun importEntries(entries: List<NoteEntry>)

    @Query("DELETE FROM note_entries")
    suspend fun nukeAll()
}
